// تست Worker با R2 تقلبی (بدون Cloudflare، بدون شبکه)
// اجرا: node integrations/cloudflare-r2/test-worker.mjs
import assert from "node:assert/strict";
import worker from "./worker.js";

const TOKEN = "test-token-0123456789abcdef0123456789";

function makeBucket() {
  const store = new Map();
  return {
    store,
    async put(key, value, options) {
      store.set(key, { value: new Uint8Array(value), options, uploaded: new Date().toISOString() });
    },
    async list({ prefix = "", limit = 100 } = {}) {
      const objects = [...store.entries()]
        .filter(([key]) => key.startsWith(prefix))
        .slice(0, limit)
        .map(([key, item]) => ({ key, size: item.value.length, uploaded: item.uploaded }));
      return { objects, truncated: false };
    },
    async get(key) {
      const item = store.get(key);
      if (!item) return null;
      return {
        body: item.value,
        customMetadata: item.options?.customMetadata || {}
      };
    },
    async delete(key) {
      store.delete(key);
    }
  };
}

const env = { BACKUPS: makeBucket(), UPLOAD_TOKEN: TOKEN, ALLOWED_ORIGIN: "https://nourotour.ir" };

const hex = buffer => [...new Uint8Array(buffer)].map(b => b.toString(16).padStart(2, "0")).join("");

async function sign(body, timestamp) {
  const hash = hex(await crypto.subtle.digest("SHA-256", body));
  const key = await crypto.subtle.importKey("raw", new TextEncoder().encode(TOKEN), { name: "HMAC", hash: "SHA-256" }, false, ["sign"]);
  return hex(await crypto.subtle.sign("HMAC", key, new TextEncoder().encode(`${timestamp}\n${hash}`)));
}

const body = new Uint8Array(512);
body.set([0x4e, 0x54, 0x42, 0x4b, 0x31]);
body.fill(7, 5);

// 1) health
let res = await worker.fetch(new Request("https://api.nourotour.ir/health"), env);
assert.equal(res.status, 200);
assert.equal((await res.json()).ok, true);

// 2) آپلود بدون توکن
res = await worker.fetch(new Request("https://api.nourotour.ir/v1/backup", { method: "POST", body }), env);
assert.equal(res.status, 401);

// 3) آپلود با امضای درست
const ts = String(Math.floor(Date.now() / 1000));
res = await worker.fetch(
  new Request("https://api.nourotour.ir/v1/backup", {
    method: "POST",
    body,
    headers: {
      authorization: `Bearer ${TOKEN}`,
      "x-backup-timestamp": ts,
      "x-backup-signature": await sign(body, ts),
      "x-backup-name": "mono",
      origin: "https://nourotour.ir"
    }
  }),
  env
);
assert.equal(res.status, 201);
const created = await res.json();
assert.ok(created.key.startsWith("mono/"));
assert.equal(created.size, body.length);
assert.equal(res.headers.get("access-control-allow-origin"), "https://nourotour.ir");

// 4) امضای جعلی
res = await worker.fetch(
  new Request("https://api.nourotour.ir/v1/backup", {
    method: "POST",
    body,
    headers: { authorization: `Bearer ${TOKEN}`, "x-backup-timestamp": ts, "x-backup-signature": "00".repeat(32) }
  }),
  env
);
assert.equal(res.status, 401);

// 5) timestamp کهنه (replay)
const old = String(Math.floor(Date.now() / 1000) - 1000);
res = await worker.fetch(
  new Request("https://api.nourotour.ir/v1/backup", {
    method: "POST",
    body,
    headers: { authorization: `Bearer ${TOKEN}`, "x-backup-timestamp": old, "x-backup-signature": await sign(body, old) }
  }),
  env
);
assert.equal(res.status, 401);

// 6) فایل غیر NTBK1
const foreign = new Uint8Array(64).fill(1);
res = await worker.fetch(
  new Request("https://api.nourotour.ir/v1/backup", {
    method: "POST",
    body: foreign,
    headers: { authorization: `Bearer ${TOKEN}`, "x-backup-timestamp": ts, "x-backup-signature": await sign(foreign, ts) }
  }),
  env
);
assert.equal(res.status, 415);

// 7) فهرست + دانلود + حذف
const listTs = String(Math.floor(Date.now() / 1000));
res = await worker.fetch(
  new Request("https://api.nourotour.ir/v1/backups", {
    headers: { authorization: `Bearer ${TOKEN}`, "x-backup-timestamp": listTs, "x-backup-signature": await sign(new Uint8Array(), listTs) }
  }),
  env
);
assert.equal(res.status, 200);
assert.equal((await res.json()).count, 1);

res = await worker.fetch(
  new Request(`https://api.nourotour.ir/v1/backup/${encodeURIComponent(created.key)}`, {
    headers: { authorization: `Bearer ${TOKEN}`, "x-backup-timestamp": listTs, "x-backup-signature": await sign(new Uint8Array(), listTs) }
  }),
  env
);
assert.equal(res.status, 200);
assert.equal(new Uint8Array(await res.arrayBuffer()).length, body.length);

res = await worker.fetch(
  new Request(`https://api.nourotour.ir/v1/backup/${encodeURIComponent(created.key)}`, {
    method: "DELETE",
    headers: { authorization: `Bearer ${TOKEN}`, "x-backup-timestamp": listTs, "x-backup-signature": await sign(new Uint8Array(), listTs) }
  }),
  env
);
assert.equal(res.status, 200);
assert.equal(env.BACKUPS.store.size, 0);

// 8) مسیر ناشناس و preflight
res = await worker.fetch(new Request("https://api.nourotour.ir/nope"), env);
assert.equal(res.status, 404);

res = await worker.fetch(
  new Request("https://api.nourotour.ir/v1/backup", {
    method: "OPTIONS",
    headers: { origin: "https://nourotour.ir", "access-control-request-method": "POST" }
  }),
  env
);
assert.equal(res.status, 204);
assert.equal(res.headers.get("access-control-allow-origin"), "https://nourotour.ir");

// 9) امنیت: توکن تنظیم‌نشده باید همه را رد کند
res = await worker.fetch(
  new Request("https://api.nourotour.ir/v1/backup", {
    method: "POST",
    body,
    headers: { authorization: "Bearer ", "x-backup-timestamp": ts, "x-backup-signature": "aa" }
  }),
  { BACKUPS: makeBucket(), ALLOWED_ORIGIN: "" }
);
assert.equal(res.status, 401);

// 10) پاک‌سازی خودکار نسخه‌های قدیمی (cron)
const bucket = env.BACKUPS;
for (let i = 0; i < 5; i += 1) {
  await bucket.put(`mono/2026/10/0${i}/old-${i}.ntbk`, body, {
    customMetadata: { sha256: "x" }
  });
  bucket.store.get(`mono/2026/10/0${i}/old-${i}.ntbk`).uploaded = new Date(Date.now() - (10 - i) * 1000).toISOString();
}
const pruneEnv = { ...env, KEEP_OBJECTS: "2" };
let pruned;
await worker.scheduled({}, pruneEnv, { waitUntil: promise => (pruned = promise) });
await pruned;
assert.equal(bucket.store.size, 2);
const keptKeys = [...bucket.store.keys()];
assert.ok(keptKeys.every(key => key.includes("old-3") || key.includes("old-4")), `کلیدهای نگه‌داشته‌شده: ${keptKeys}`);

console.log("تست‌های Worker موفق بود ✔");
