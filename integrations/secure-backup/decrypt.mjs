#!/usr/bin/env node
/**
 * بازکردن فایل بکاپ NTBK1 روی کامپیوتر، بدون مرورگر و بدون هیچ سرویسی.
 *
 * اجرا:
 *   node integrations/secure-backup/decrypt.mjs nourotour-mono-2026-10-20.ntbk
 *   node integrations/secure-backup/decrypt.mjs backup.ntbk --out backup.json --mono MONO.zip
 *
 * گذرواژه از ترمینال پرسیده می‌شود (بدون نمایش) یا از متغیر محیطی
 * NTBK_PASSPHRASE خوانده می‌شود (برای اسکریپت خودکار؛ در shell history نگذارید).
 */
import fs from "node:fs/promises";
import path from "node:path";
import readline from "node:readline";

import { base64ToBytes, decryptBackup, decodePayload, formatBytes, sha256Hex } from "./core.js";

function parseArgs(argv) {
  const args = { file: null, out: null, mono: null };
  for (let i = 0; i < argv.length; i += 1) {
    const item = argv[i];
    if (item === "--out") args.out = argv[++i];
    else if (item === "--mono") args.mono = argv[++i];
    else if (!args.file) args.file = item;
  }
  return args;
}

function askHidden(question) {
  if (!process.stdin.isTTY) {
    return new Promise(resolve => {
      const rl = readline.createInterface({ input: process.stdin, output: process.stdout, terminal: false });
      rl.once("line", line => {
        rl.close();
        resolve(line);
      });
    });
  }

  return new Promise(resolve => {
    process.stdout.write(question);
    process.stdin.setRawMode(true);
    process.stdin.resume();
    process.stdin.setEncoding("utf8");

    let value = "";
    const onData = chunk => {
      for (const char of chunk) {
        if (char === "\r" || char === "\n") {
          process.stdin.setRawMode(false);
          process.stdin.pause();
          process.stdin.removeListener("data", onData);
          process.stdout.write("\n");
          resolve(value);
          return;
        }
        if (char === "\u0003") {
          process.stdout.write("\n");
          process.exit(130);
        }
        if (char === "\u007f" || char === "\b") value = value.slice(0, -1);
        else value += char;
      }
    };

    process.stdin.on("data", onData);
  });
}

const args = parseArgs(process.argv.slice(2));

if (!args.file) {
  console.error("استفاده: node decrypt.mjs <file.ntbk> [--out backup.json] [--mono MONO.zip]");
  process.exit(1);
}

const passphrase = process.env.NTBK_PASSPHRASE || (await askHidden("گذرواژهٔ بکاپ: "));

if (!passphrase) {
  console.error("گذرواژه خالی است.");
  process.exit(1);
}

const fileBytes = new Uint8Array(await fs.readFile(args.file));

console.log(`فایل: ${args.file} — ${formatBytes(fileBytes.length)}`);
console.log(`SHA-256 فایل: ${await sha256Hex(fileBytes)}`);

let plainBytes;
let meta;
let payload;

try {
  ({ plainBytes, meta } = await decryptBackup({ fileBytes, passphrase }));
  payload = decodePayload(plainBytes);
} catch (error) {
  console.error(`✘ ${error.message || error}`);
  process.exit(3);
}

console.log("✔ باز شد؛ گذرواژه درست و محتوا دست‌نخورده است.");
console.log(`  تاریخ ساخت: ${payload.createdAt || "?"}`);
console.log(`  دادهٔ خام: ${formatBytes(meta.plainBytes)} · فشرده: ${meta.compressed ? "بله" : "خیر"} · KDF iterations: ${meta.iterations}`);
console.log(`  تورها: ${(payload.data?.tours || []).length} · درخواست‌ها: ${(payload.leads || []).length}`);

const outPath = args.out || `${path.basename(args.file, path.extname(args.file))}.json`;
await fs.writeFile(outPath, JSON.stringify(payload, null, 2), "utf8");
console.log(`  JSON: ${outPath}`);

if (payload.mono?.base64) {
  const monoPath = args.mono || payload.mono.fileName || "MONO.zip";
  const bytes = base64ToBytes(payload.mono.base64);
  const hash = await sha256Hex(bytes);
  if (payload.mono.sha256 && hash !== payload.mono.sha256) {
    console.error("✘ هش سورس MONO با مقدار ثبت‌شده در بکاپ یکسان نیست!");
    process.exit(2);
  }
  await fs.writeFile(monoPath, bytes);
  console.log(`  سورس MONO: ${monoPath} (${formatBytes(bytes.length)}, sha256 تأیید شد)`);
}
