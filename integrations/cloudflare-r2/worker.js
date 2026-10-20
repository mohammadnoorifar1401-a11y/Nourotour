/**
 * nourotour-backup Worker — دریافت بکاپ رمزگذاری‌شدهٔ MONO و نگه‌داری در R2 خصوصی.
 *
 * اصول امنیتی:
 *  - هیچ کلید یا توکنی در این فایل نیست؛ توکن از `wrangler secret put UPLOAD_TOKEN` می‌آید.
 *  - بدنه همیشه با AES-256-GCM در مرورگر رمز شده است؛ این Worker هرگز آن را رمزگشایی نمی‌کند
 *    و گذرواژه را نمی‌بیند.
 *  - bucket هیچ دسترسی عمومی ندارد (نه Custom Domain، نه r2.dev)؛ فقط این Worker با binding R2.
 *  - مقایسهٔ توکن و امضا با زمان ثابت انجام می‌شود.
 *  - امضای HMAC + timestamp، پنجرهٔ ۵ دقیقه‌ای، برای جلوگیری از replay.
 */

const MAGIC = new Uint8Array([0x4e, 0x54, 0x42, 0x4b, 0x31]); // "NTBK1"
const MAX_BYTES = 10 * 1024 * 1024;
const CLOCK_SKEW_SECONDS = 300;

const textEncoder = new TextEncoder();

function toHex(buffer) {
  return [...new Uint8Array(buffer)].map(byte => byte.toString(16).padStart(2, "0")).join("");
}

function json(body, status = 200, extraHeaders = {}, origin = "") {
  const headers = new Headers(extraHeaders);
  headers.set("content-type", "application/json; charset=utf-8");
  headers.set("cache-control", "no-store");
  headers.set("x-content-type-options", "nosniff");
  if (origin) {
    headers.set("access-control-allow-origin", origin);
    headers.set("vary", "Origin");
  }
  return new Response(JSON.stringify(body), { status, headers });
}

/** مقایسهٔ زمان‌ثابت برای رشته‌های hex/توکن */
function timingSafeEqual(a, b) {
  const left = textEncoder.encode(String(a));
  const right = textEncoder.encode(String(b));
  if (left.length !== right.length) return false;
  let diff = 0;
  for (let i = 0; i < left.length; i += 1) diff |= left[i] ^ right[i];
  return diff === 0;
}

async function hmacHex(secret, message) {
  const key = await crypto.subtle.importKey(
    "raw",
    textEncoder.encode(secret),
    { name: "HMAC", hash: "SHA-256" },
    false,
    ["sign"]
  );
  return toHex(await crypto.subtle.sign("HMAC", key, textEncoder.encode(message)));
}

function allowedOrigins(env) {
  return String(env.ALLOWED_ORIGIN || "")
    .split(",")
    .map(item => item.trim())
    .filter(Boolean);
}

function corsHeaders(request, env) {
  const origin = request.headers.get("origin") || "";
  const list = allowedOrigins(env);
  if (origin && list.includes(origin)) return { origin, headers: { "access-control-allow-origin": origin } };
  return { origin: "", headers: {} };
}

function preflight(request, env) {
  const { origin, headers } = corsHeaders(request, env);
  const out = new Headers(headers);
  out.set("access-control-allow-methods", "GET,POST,DELETE,OPTIONS");
  out.set(
    "access-control-allow-headers",
    "authorization,content-type,x-backup-timestamp,x-backup-signature,x-backup-name"
  );
  out.set("access-control-max-age", "600");
  if (origin) out.set("vary", "Origin");
  return new Response(null, { status: 204, headers: out });
}

async function authorized(request, env, body) {
  const header = request.headers.get("authorization") || "";
  const token = header.startsWith("Bearer ") ? header.slice(7).trim() : "";
  const expected = String(env.UPLOAD_TOKEN || "");

  if (!expected) return { ok: false, reason: "UPLOAD_TOKEN روی Worker تنظیم نشده است." };
  if (!timingSafeEqual(token, expected)) return { ok: false, reason: "توکن نامعتبر است." };

  const timestamp = request.headers.get("x-backup-timestamp") || "";
  const signature = request.headers.get("x-backup-signature") || "";

  if (!timestamp || !signature) return { ok: false, reason: "امضای درخواست موجود نیست." };

  const ts = Number(timestamp);
  const now = Math.floor(Date.now() / 1000);
  if (!Number.isFinite(ts) || Math.abs(now - ts) > CLOCK_SKEW_SECONDS) {
    return { ok: false, reason: "زمان درخواست منقضی شده است." };
  }

  const bodyHash = toHex(await crypto.subtle.digest("SHA-256", body || new Uint8Array()));
  const expectedSignature = await hmacHex(expected, `${timestamp}\n${bodyHash}`);

  if (!timingSafeEqual(signature.toLowerCase(), expectedSignature)) {
    return { ok: false, reason: "امضای درخواست معتبر نیست." };
  }

  return { ok: true };
}

function objectKey(name) {
  const now = new Date();
  const year = now.getUTCFullYear();
  const month = String(now.getUTCMonth() + 1).padStart(2, "0");
  const day = String(now.getUTCDate()).padStart(2, "0");
  const stamp = now.toISOString().replace(/[-:]/g, "").replace(/\..+$/, "").replace("T", "-");
  const random = toHex(crypto.getRandomValues(new Uint8Array(4)));
  const safeName = String(name || "mono")
    .toLowerCase()
    .replace(/[^a-z0-9-]+/g, "-")
    .replace(/^-+|-+$/g, "")
    .slice(0, 40) || "mono";
  return `mono/${year}/${month}/${day}/${safeName}-${stamp}-${random}.ntbk`;
}

/** حذف نسخه‌های قدیمی؛ تعداد نگه‌داری با متغیر KEEP_OBJECTS قابل تنظیم است (پیش‌فرض ۶۰). */
async function pruneOldBackups(env) {
  const keep = Math.max(1, Number(env.KEEP_OBJECTS || 60));
  const objects = [];
  let cursor;

  do {
    const page = await env.BACKUPS.list({ prefix: "mono/", cursor, limit: 1000 });
    objects.push(...page.objects);
    cursor = page.truncated ? page.cursor : undefined;
  } while (cursor);

  objects.sort((a, b) => new Date(b.uploaded) - new Date(a.uploaded));

  const stale = objects.slice(keep);
  for (const item of stale) {
    await env.BACKUPS.delete(item.key);
  }

  return { scanned: objects.length, deleted: stale.length, kept: Math.min(objects.length, keep) };
}

export default {
  async scheduled(event, env, ctx) {
    ctx.waitUntil(pruneOldBackups(env));
  },

  async fetch(request, env) {
    const url = new URL(request.url);
    const path = url.pathname;

    if (request.method === "OPTIONS") return preflight(request, env);

    const { origin, headers } = corsHeaders(request, env);

    if (path === "/health" && request.method === "GET") {
      return json({ ok: true, service: "nourotour-backup", r2: Boolean(env.BACKUPS) }, 200, headers, origin);
    }

    if (!path.startsWith("/v1/")) {
      return json({ ok: false, error: "not_found" }, 404, headers, origin);
    }

    // ---- آپلود ----
    if (path === "/v1/backup" && request.method === "POST") {
      const length = Number(request.headers.get("content-length") || 0);
      if (length > MAX_BYTES) {
        return json({ ok: false, error: "too_large" }, 413, headers, origin);
      }

      const body = new Uint8Array(await request.arrayBuffer());

      if (!body.length) return json({ ok: false, error: "empty_body" }, 400, headers, origin);
      if (body.length > MAX_BYTES) return json({ ok: false, error: "too_large" }, 413, headers, origin);

      for (let i = 0; i < MAGIC.length; i += 1) {
        if (body[i] !== MAGIC[i]) {
          return json({ ok: false, error: "not_ntbk1" }, 415, headers, origin);
        }
      }

      const auth = await authorized(request, env, body);
      if (!auth.ok) return json({ ok: false, error: "unauthorized", detail: auth.reason }, 401, headers, origin);

      const key = objectKey(request.headers.get("x-backup-name"));
      const sha256 = toHex(await crypto.subtle.digest("SHA-256", body));
      const uploadedAt = new Date().toISOString();

      await env.BACKUPS.put(key, body, {
        httpMetadata: { contentType: "application/octet-stream" },
        customMetadata: {
          format: "NTBK1",
          sha256,
          uploadedAt,
          encrypted: "aes-256-gcm+pbkdf2-sha256"
        }
      });

      return json({ ok: true, key, size: body.length, sha256, uploadedAt }, 201, headers, origin);
    }

    // ---- فهرست ----
    if (path === "/v1/backups" && request.method === "GET") {
      const auth = await authorized(request, env, new Uint8Array());
      if (!auth.ok) return json({ ok: false, error: "unauthorized", detail: auth.reason }, 401, headers, origin);

      const listed = await env.BACKUPS.list({ prefix: "mono/", limit: 100 });
      const items = listed.objects
        .sort((a, b) => new Date(b.uploaded) - new Date(a.uploaded))
        .map(item => ({ key: item.key, size: item.size, uploaded: item.uploaded }));

      return json({ ok: true, count: items.length, truncated: listed.truncated, items }, 200, headers, origin);
    }

    // ---- دانلود/حذف یک شیء ----
    if (path.startsWith("/v1/backup/")) {
      const key = decodeURIComponent(path.slice("/v1/backup/".length));

      if (!key.startsWith("mono/") || key.includes("..")) {
        return json({ ok: false, error: "bad_key" }, 400, headers, origin);
      }

      const auth = await authorized(request, env, new Uint8Array());
      if (!auth.ok) return json({ ok: false, error: "unauthorized", detail: auth.reason }, 401, headers, origin);

      if (request.method === "GET") {
        const object = await env.BACKUPS.get(key);
        if (!object) return json({ ok: false, error: "not_found" }, 404, headers, origin);

        const outHeaders = new Headers(headers);
        outHeaders.set("content-type", "application/octet-stream");
        outHeaders.set("content-disposition", `attachment; filename="${key.split("/").pop()}"`);
        outHeaders.set("x-backup-sha256", object.customMetadata?.sha256 || "");
        outHeaders.set("cache-control", "no-store");

        return new Response(object.body, { status: 200, headers: outHeaders });
      }

      if (request.method === "DELETE") {
        await env.BACKUPS.delete(key);
        return json({ ok: true, deleted: key }, 200, headers, origin);
      }
    }

    return json({ ok: false, error: "method_not_allowed" }, 405, headers, origin);
  }
};
