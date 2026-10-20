/**
 * هستهٔ رمزگذاری پشتیبان نوروتور — قالب «NTBK1»
 *
 * این ماژول فقط با Web Crypto کار می‌کند و هیچ کلید ثابتی در خود ندارد.
 * کلید از گذرواژه‌ای که کاربر در همان لحظه وارد می‌کند ساخته می‌شود و
 * هیچ‌جا (نه در localStorage، نه در مخزن، نه در APK) ذخیره نمی‌شود.
 *
 * ساختار فایل خروجی (Big-endian):
 *   0..4    "NTBK1"          ۵ بایت جادو
 *   5       version = 1
 *   6       kdf     = 1      (PBKDF2-SHA256)
 *   7       flags            بیت ۰ = بدنه gzip شده
 *   8..11   iterations       uint32
 *   12..27  salt             ۱۶ بایت
 *   28..39  iv               ۱۲ بایت
 *   40..    ciphertext       AES-256-GCM (شامل تگ ۱۶ بایتی)
 *
 * سرب (۴۰ بایت اول) به‌عنوان AAD به AES-GCM داده می‌شود؛ پس دست‌کاری
 * iterations/salt/iv/flags باعث شکست احراز اصالت می‌شود.
 */

export const MAGIC_TEXT = "NTBK1";
export const FORMAT_VERSION = 1;
export const KDF_PBKDF2_SHA256 = 1;
export const HEADER_BYTES = 40;
export const SALT_BYTES = 16;
export const IV_BYTES = 12;
export const DEFAULT_ITERATIONS = 310000;
export const MIN_PASSPHRASE_LENGTH = 12;
export const MAX_UPLOAD_BYTES = 10 * 1024 * 1024;

const textEncoder = new TextEncoder();
const textDecoder = new TextDecoder();

const MAGIC = new Uint8Array([0x4e, 0x54, 0x42, 0x4b, 0x31]); // NTBK1

function writeUint32(view, offset, value) {
  view.setUint32(offset, value >>> 0, false);
}

function readUint32(view, offset) {
  return view.getUint32(offset, false);
}

export function concatBytes(...chunks) {
  const total = chunks.reduce((sum, chunk) => sum + chunk.length, 0);
  const out = new Uint8Array(total);
  let offset = 0;
  for (const chunk of chunks) {
    out.set(chunk, offset);
    offset += chunk.length;
  }
  return out;
}

export function bytesToBase64(bytes) {
  const CHUNK = 0x8000;
  let binary = "";
  for (let i = 0; i < bytes.length; i += CHUNK) {
    binary += String.fromCharCode.apply(null, bytes.subarray(i, i + CHUNK));
  }
  return btoa(binary);
}

export function base64ToBytes(value) {
  const binary = atob(value);
  const out = new Uint8Array(binary.length);
  for (let i = 0; i < binary.length; i += 1) out[i] = binary.charCodeAt(i);
  return out;
}

export function randomBytes(length) {
  const out = new Uint8Array(length);
  crypto.getRandomValues(out);
  return out;
}

export async function sha256Hex(bytes) {
  const digest = new Uint8Array(await crypto.subtle.digest("SHA-256", bytes));
  return [...digest].map(byte => byte.toString(16).padStart(2, "0")).join("");
}

/** گزیپ در مرورگر؛ اگر مرورگر پشتیبانی نکند null برمی‌گردد و بدنه خام می‌ماند. */
export async function gzipBytes(bytes) {
  if (typeof CompressionStream === "undefined") return null;
  try {
    const stream = new Blob([bytes]).stream().pipeThrough(new CompressionStream("gzip"));
    return new Uint8Array(await new Response(stream).arrayBuffer());
  } catch (error) {
    return null;
  }
}

export async function gunzipBytes(bytes) {
  if (typeof DecompressionStream === "undefined") {
    throw new Error("این مرورگر از بازکردن gzip پشتیبانی نمی‌کند؛ از Chrome/Firefox/Safari جدید استفاده کنید.");
  }
  const stream = new Blob([bytes]).stream().pipeThrough(new DecompressionStream("gzip"));
  return new Uint8Array(await new Response(stream).arrayBuffer());
}

async function deriveKey(passphrase, salt, iterations) {
  const baseKey = await crypto.subtle.importKey(
    "raw",
    textEncoder.encode(passphrase),
    "PBKDF2",
    false,
    ["deriveKey"]
  );

  return crypto.subtle.deriveKey(
    {
      name: "PBKDF2",
      salt,
      iterations,
      hash: "SHA-256"
    },
    baseKey,
    { name: "AES-GCM", length: 256 },
    false,
    ["encrypt", "decrypt"]
  );
}

/**
 * ساخت فایل رمزگذاری‌شده.
 * @param {{ plainBytes: Uint8Array, passphrase: string, iterations?: number, compress?: boolean }} options
 */
export async function encryptBackup({ plainBytes, passphrase, iterations = DEFAULT_ITERATIONS, compress = true }) {
  if (typeof passphrase !== "string" || passphrase.length < MIN_PASSPHRASE_LENGTH) {
    throw new Error(`گذرواژه باید حداقل ${MIN_PASSPHRASE_LENGTH} نویسه باشد.`);
  }

  const source = plainBytes instanceof Uint8Array ? plainBytes : new Uint8Array(plainBytes);

  let body = source;
  let compressed = false;

  if (compress) {
    const gz = await gzipBytes(source);
    if (gz && gz.length < source.length) {
      body = gz;
      compressed = true;
    }
  }

  const salt = randomBytes(SALT_BYTES);
  const iv = randomBytes(IV_BYTES);

  const header = new Uint8Array(HEADER_BYTES);
  const view = new DataView(header.buffer);
  header.set(MAGIC, 0);
  header[5] = FORMAT_VERSION;
  header[6] = KDF_PBKDF2_SHA256;
  header[7] = compressed ? 1 : 0;
  writeUint32(view, 8, iterations);
  header.set(salt, 12);
  header.set(iv, 28);

  const key = await deriveKey(passphrase, salt, iterations);

  const ciphertext = new Uint8Array(
    await crypto.subtle.encrypt(
      { name: "AES-GCM", iv, additionalData: header, tagLength: 128 },
      key,
      body
    )
  );

  const bytes = concatBytes(header, ciphertext);

  return {
    bytes,
    meta: {
      format: MAGIC_TEXT,
      version: FORMAT_VERSION,
      iterations,
      compressed,
      sourceBytes: source.length,
      bodyBytes: body.length,
      ciphertextBytes: ciphertext.length,
      totalBytes: bytes.length,
      overheadBytes: bytes.length - body.length,
      sha256: await sha256Hex(bytes)
    }
  };
}

/**
 * بازکردن فایل NTBK1. هر دست‌کاری در فایل یا گذرواژهٔ نادرست باعث خطا می‌شود.
 */
export async function decryptBackup({ fileBytes, passphrase }) {
  const bytes = fileBytes instanceof Uint8Array ? fileBytes : new Uint8Array(fileBytes);

  if (bytes.length < HEADER_BYTES + 16) {
    throw new Error("فایل پشتیبان کوتاه‌تر از حد انتظار است.");
  }

  for (let i = 0; i < MAGIC.length; i += 1) {
    if (bytes[i] !== MAGIC[i]) {
      throw new Error("این فایل پشتیبان نوروتور (NTBK1) نیست.");
    }
  }

  const view = new DataView(bytes.buffer, bytes.byteOffset, bytes.byteLength);
  const version = bytes[5];
  const kdf = bytes[6];
  const flags = bytes[7];
  const iterations = readUint32(view, 8);
  const salt = bytes.slice(12, 28);
  const iv = bytes.slice(28, 40);
  const header = bytes.slice(0, HEADER_BYTES);
  const ciphertext = bytes.slice(HEADER_BYTES);

  if (version !== FORMAT_VERSION) throw new Error(`نسخهٔ قالب پشتیبانی‌نشده است: ${version}`);
  if (kdf !== KDF_PBKDF2_SHA256) throw new Error(`نوع KDF پشتیبانی‌نشده است: ${kdf}`);
  if (iterations < 100000 || iterations > 5000000) throw new Error("تعداد تکرار KDF خارج از محدودهٔ مجاز است.");

  const key = await deriveKey(passphrase, salt, iterations);

  let body;
  try {
    body = new Uint8Array(
      await crypto.subtle.decrypt(
        { name: "AES-GCM", iv, additionalData: header, tagLength: 128 },
        key,
        ciphertext
      )
    );
  } catch (error) {
    throw new Error("گذرواژه نادرست است یا فایل پشتیبان آسیب دیده است.");
  }

  const compressed = (flags & 1) === 1;
  const plainBytes = compressed ? await gunzipBytes(body) : body;

  return {
    plainBytes,
    meta: {
      version,
      iterations,
      compressed,
      totalBytes: bytes.length,
      plainBytes: plainBytes.length
    }
  };
}

/** بدنهٔ JSON پشتیبان را از داده‌های سایت می‌سازد. */
export async function buildBackupPayload({
  data,
  leads = [],
  favorites = [],
  monoZip = null,
  monoFileName = null,
  origin = "",
  now = new Date()
}) {
  const payload = {
    schema: "nourotour.mono.backup/v1",
    createdAt: now.toISOString(),
    origin: origin || null,
    app: {
      name: "NOUROTOUR",
      dataKey: "nourotour_data_v1"
    },
    data,
    leads,
    favorites,
    mono: monoZip
      ? {
          fileName: monoFileName,
          bytes: monoZip.length,
          sha256: await sha256Hex(monoZip),
          base64: bytesToBase64(monoZip)
        }
      : null
  };

  const plainBytes = textEncoder.encode(JSON.stringify(payload));

  const parts = {
    data: textEncoder.encode(JSON.stringify(data)).length,
    leads: textEncoder.encode(JSON.stringify(leads)).length,
    favorites: textEncoder.encode(JSON.stringify(favorites)).length,
    mono: monoZip ? monoZip.length : 0,
    monoBase64: monoZip ? Math.ceil(monoZip.length / 3) * 4 : 0
  };

  return { payload, plainBytes, parts };
}

export function decodePayload(plainBytes) {
  return JSON.parse(textDecoder.decode(plainBytes));
}

export function formatBytes(bytes) {
  const value = Number(bytes) || 0;
  if (value < 1024) return `${value} بایت`;
  const units = ["کیلوبایت", "مگابایت", "گیگابایت"];
  let size = value / 1024;
  let unit = 0;
  while (size >= 1024 && unit < units.length - 1) {
    size /= 1024;
    unit += 1;
  }
  return `${size.toFixed(size >= 100 ? 0 : 1)} ${units[unit]}`;
}

export function faDigits(value) {
  return String(value).replace(/\d/g, digit => "۰۱۲۳۴۵۶۷۸۹"[Number(digit)]);
}

/** ساخت امضای HMAC-SHA256 برای درخواست آپلود: HMAC(token, timestamp + "\n" + sha256(file)) */
export async function signUpload({ token, timestamp, fileHashHex }) {
  const key = await crypto.subtle.importKey(
    "raw",
    textEncoder.encode(token),
    { name: "HMAC", hash: "SHA-256" },
    false,
    ["sign"]
  );

  const signature = await crypto.subtle.sign(
    "HMAC",
    key,
    textEncoder.encode(`${timestamp}\n${fileHashHex}`)
  );

  return [...new Uint8Array(signature)]
    .map(byte => byte.toString(16).padStart(2, "0"))
    .join("");
}
