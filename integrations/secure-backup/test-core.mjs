// تست دورریز قالب NTBK1 با Web Crypto در Node (بدون وابستگی بیرونی)
// اجرا: node integrations/secure-backup/test-core.mjs
import assert from "node:assert/strict";
import {
  MAX_UPLOAD_BYTES,
  buildBackupPayload,
  decryptBackup,
  decodePayload,
  encryptBackup,
  formatBytes,
  sha256Hex,
  signUpload
} from "./core.js";

const passphrase = "correct horse battery staple";
const mono = new Uint8Array(4096).map((_, index) => index % 251);

const data = { tours: [{ id: 1, title: "تور آزمایشی" }], general: { brandName: "نوروتور" } };
const leads = [{ id: "a1", kind: "consult", phone: "09120000000" }];

const { payload, plainBytes, parts } = await buildBackupPayload({
  data,
  leads,
  favorites: [3, 7],
  monoZip: mono,
  monoFileName: "MONO-v1.7.0-source.zip",
  origin: "https://nourotour.ir"
});

assert.equal(payload.schema, "nourotour.mono.backup/v1");
assert.equal(payload.mono.bytes, mono.length);
assert.ok(parts.mono === 4096);

const { bytes, meta } = await encryptBackup({ plainBytes, passphrase });
assert.equal(bytes[0], 0x4e, "magic N");
assert.equal(bytes.length, meta.totalBytes);
assert.ok(meta.totalBytes < MAX_UPLOAD_BYTES);

const decoded = await decryptBackup({ fileBytes: bytes, passphrase });
assert.equal(await sha256Hex(decoded.plainBytes), await sha256Hex(plainBytes));

const restored = decodePayload(decoded.plainBytes);
assert.equal(restored.data.tours[0].title, "تور آزمایشی");
assert.equal(restored.leads.length, 1);
assert.equal(restored.mono.bytes, mono.length);

// گذرواژهٔ نادرست باید رد شود
await assert.rejects(
  () => decryptBackup({ fileBytes: bytes, passphrase: "wrong-passphrase-123" }),
  /گذرواژه نادرست|آسیب/
);

// دست‌کاری یک بایت از سرب باید با AAD شناسایی شود
const tampered = bytes.slice();
tampered[10] ^= 0xff;
await assert.rejects(() => decryptBackup({ fileBytes: tampered, passphrase }), /گذرواژه نادرست|آسیب/);

// فایل بیگانه باید رد شود
await assert.rejects(
  () => decryptBackup({ fileBytes: new Uint8Array(200), passphrase }),
  /NTBK1/
);

// امضا باید قابل بازتولید و حساس به تغییر باشد
const sigA = await signUpload({ token: "t".repeat(32), timestamp: "1700000000", fileHashHex: "ab".repeat(32) });
const sigB = await signUpload({ token: "t".repeat(32), timestamp: "1700000000", fileHashHex: "ab".repeat(32) });
const sigC = await signUpload({ token: "t".repeat(32), timestamp: "1700000001", fileHashHex: "ab".repeat(32) });
assert.equal(sigA, sigB);
assert.notEqual(sigA, sigC);

console.log("plain bytes      :", plainBytes.length);
console.log("encrypted bytes  :", meta.totalBytes, `(${formatBytes(meta.totalBytes)})`);
console.log("compressed       :", meta.compressed, meta.bodyBytes);
console.log("overhead bytes   :", meta.overheadBytes);
console.log("sha256           :", meta.sha256);
console.log("همهٔ تست‌ها موفق بود ✔");
