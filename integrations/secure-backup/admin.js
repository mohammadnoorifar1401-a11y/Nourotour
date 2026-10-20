/**
 * رابط کاربری «بکاپ امن MONO» در پنل مدیریت نوروتور.
 *
 * قواعد امنیتی این ماژول:
 *  - گذرواژهٔ رمزگذاری فقط در حافظهٔ همین صفحه است و بعد از ساخت بکاپ پاک می‌شود.
 *  - توکن آپلود در هیچ Storage ذخیره نمی‌شود (نه localStorage، نه sessionStorage، نه کوکی).
 *  - هیچ کلیدی در کد نیست؛ همه‌چیز در زمان اجرا از کاربر گرفته می‌شود.
 *  - اندازهٔ واقعی بکاپ در همین صفحه اندازه‌گیری و نمایش داده می‌شود.
 */

import {
  DEFAULT_ITERATIONS,
  MAX_UPLOAD_BYTES,
  MIN_PASSPHRASE_LENGTH,
  buildBackupPayload,
  decryptBackup,
  decodePayload,
  encryptBackup,
  faDigits,
  formatBytes,
  sha256Hex,
  signUpload
} from "./core.js";

const MONO_ZIP_URL = "MONO-v1.7.0-source.zip";
const MONO_ZIP_NAME = "MONO-v1.7.0-source.zip";

const state = {
  built: null,
  passphrase: "",
  monoBytes: null,
  timer: null
};

const $ = selector => document.querySelector(selector);

function toast(title, message) {
  if (window.NourotourBackup?.toast) {
    window.NourotourBackup.toast(title, message);
    return;
  }
  console.log(title, message);
}

function api() {
  return window.NourotourBackup || null;
}

function setStatus(element, text, kind = "") {
  if (!element) return;
  element.textContent = text;
  element.dataset.kind = kind;
}

function faTitle(value) {
  return faDigits(value);
}

function row(label, bytes, extra = "") {
  return `
    <div style="display:flex;justify-content:space-between;gap:10px;padding:4px 0;border-bottom:1px dashed rgba(255,255,255,.08)">
      <span>${label}</span>
      <span dir="ltr" style="font-variant-numeric:tabular-nums">${faTitle(formatBytes(bytes))} — ${faTitle(bytes.toLocaleString("en-US"))} B${extra ? ` · ${extra}` : ""}</span>
    </div>`;
}

async function fetchMonoZip() {
  if (state.monoBytes) return state.monoBytes;

  const response = await fetch(new URL(MONO_ZIP_URL, document.baseURI).href, { cache: "no-store" });

  if (!response.ok) {
    throw new Error(`دریافت فایل سورس MONO ناموفق بود (${response.status}).`);
  }

  state.monoBytes = new Uint8Array(await response.arrayBuffer());
  return state.monoBytes;
}

function readPassphrase() {
  const first = $("#backupPassphrase")?.value || "";
  const second = $("#backupPassphrase2")?.value || "";

  if (first.length < MIN_PASSPHRASE_LENGTH) {
    throw new Error(`گذرواژه باید حداقل ${MIN_PASSPHRASE_LENGTH} نویسه باشد.`);
  }
  if (first !== second) {
    throw new Error("گذرواژه و تکرار آن یکسان نیست.");
  }
  return first;
}

async function buildBackup() {
  const button = $("#backupBuild");
  const status = $("#backupBuildStatus");
  const measure = $("#backupMeasure");

  const data = api();
  if (!data) {
    toast("پنل آماده نیست", "داده‌های سایت در دسترس نیست؛ صفحه را یک بار تازه کنید.");
    return;
  }

  button.disabled = true;
  setStatus(status, "در حال ساخت و رمزگذاری…", "busy");
  const started = performance.now();

  try {
    const passphrase = readPassphrase();
    const includeMono = Boolean($("#backupIncludeMono")?.checked);

    const monoZip = includeMono ? await fetchMonoZip() : null;

    const { payload, plainBytes, parts } = await buildBackupPayload({
      data: data.getData(),
      leads: data.getLeads(),
      favorites: data.getFavorites(),
      monoZip,
      monoFileName: monoZip ? MONO_ZIP_NAME : null,
      origin: location.origin
    });

    const { bytes, meta } = await encryptBackup({
      plainBytes,
      passphrase,
      iterations: DEFAULT_ITERATIONS,
      compress: true
    });

    state.built = { bytes, meta, payload, plainBytes };
    state.passphrase = passphrase;

    const elapsed = Math.round(performance.now() - started);
    const overLimit = bytes.length > MAX_UPLOAD_BYTES;

    measure.innerHTML = `
      <div style="margin-top:10px;padding:10px;border:1px solid rgba(255,255,255,.12);border-radius:10px">
        <div style="display:flex;justify-content:space-between;gap:10px;font-weight:700;margin-bottom:6px">
          <span>حجم واقعی فایل بکاپ (رمزشده)</span>
          <span dir="ltr">${faTitle(formatBytes(bytes.length))} — ${faTitle(bytes.length.toLocaleString("en-US"))} B</span>
        </div>
        ${row("داده‌های سایت (JSON)", parts.data)}
        ${row("درخواست‌ها", parts.leads)}
        ${row("علاقه‌مندی‌ها", parts.favorites)}
        ${row("سورس MONO (zip)", parts.mono, monoZip ? faTitle(monoZip.length.toLocaleString("en-US")) + " B" : "شامل نشده")}
        ${row("JSON خام (پیش از فشرده‌سازی)", meta.sourceBytes)}
        ${row("پس از gzip", meta.compressed ? meta.bodyBytes : meta.sourceBytes, meta.compressed ? "فشرده شد" : "فشرده‌سازی پشتیبانی نشد")}
        ${row("افزودهٔ رمزگذاری (سربرگ ۴۰ + تگ ۱۶)", meta.overheadBytes, "ثابت")}
        <div style="margin-top:8px;font-size:.62rem;line-height:1.7">
          <div>SHA-256 فایل: <code dir="ltr" style="word-break:break-all">${meta.sha256}</code></div>
          <div>PBKDF2-SHA256 با ${faTitle(meta.iterations.toLocaleString("en-US"))} تکرار · AES-256-GCM · ساخت در ${faTitle(elapsed.toLocaleString("fa-IR"))} میلی‌ثانیه</div>
          ${overLimit
            ? `<div style="color:#ff8a8a;margin-top:6px">حجم فایل از سقف آپلود (${faTitle(formatBytes(MAX_UPLOAD_BYTES))}) بیشتر است؛ سورس MONO را کنار بگذارید یا نسخه‌های قدیمی را حذف کنید.</div>`
            : ""}
        </div>
      </div>`;

    $("#backupDownload").disabled = false;
    $("#backupUpload").disabled = overLimit;

    setStatus(status, `آماده است — ${formatBytes(bytes.length)} رمزشده`, "ok");
    toast("بکاپ رمزگذاری شد", `حجم واقعی: ${formatBytes(bytes.length)}`);
  } catch (error) {
    state.built = null;
    $("#backupDownload").disabled = true;
    $("#backupUpload").disabled = true;
    setStatus(status, error.message || "ساخت بکاپ ناموفق بود.", "error");
    toast("خطا در بکاپ", error.message || "ساخت بکاپ ناموفق بود.");
  } finally {
    button.disabled = false;
  }
}

function downloadBackup() {
  if (!state.built) return;

  const stamp = new Date().toISOString().slice(0, 10);
  const blob = new Blob([state.built.bytes], { type: "application/octet-stream" });
  const url = URL.createObjectURL(blob);
  const link = document.createElement("a");

  link.href = url;
  link.download = `nourotour-mono-${stamp}.ntbk`;
  document.body.appendChild(link);
  link.click();
  link.remove();
  URL.revokeObjectURL(url);

  toast("دانلود شد", "فایل رمزشده فقط با همان گذرواژه باز می‌شود.");
}

function normalizeEndpoint(value) {
  const raw = String(value || "").trim();
  if (!raw) throw new Error("نشانی سرویس بکاپ را وارد کنید.");
  const url = new URL(raw, location.href);
  if (url.protocol !== "https:") throw new Error("آپلود فقط روی HTTPS مجاز است.");
  return url.href;
}

async function uploadBackup() {
  const status = $("#backupUploadStatus");
  const button = $("#backupUpload");

  if (!state.built) {
    setStatus(status, "اول بکاپ را بسازید.", "error");
    return;
  }

  button.disabled = true;
  setStatus(status, "در حال ارسال…", "busy");

  try {
    const endpoint = normalizeEndpoint($("#backupEndpoint")?.value);
    const token = $("#backupToken")?.value || "";

    if (token.length < 24) throw new Error("توکن آپلود نامعتبر یا خیلی کوتاه است.");

    const timestamp = String(Math.floor(Date.now() / 1000));
    const signature = await signUpload({
      token,
      timestamp,
      fileHashHex: await sha256Hex(state.built.bytes)
    });

    const response = await fetch(endpoint, {
      method: "POST",
      headers: {
        "content-type": "application/octet-stream",
        "authorization": `Bearer ${token}`,
        "x-backup-timestamp": timestamp,
        "x-backup-signature": signature,
        "x-backup-name": "mono"
      },
      body: state.built.bytes
    });

    const text = await response.text();
    let payload = null;
    try {
      payload = JSON.parse(text);
    } catch (error) {
      payload = null;
    }

    if (!response.ok || !payload?.ok) {
      throw new Error(payload?.detail || payload?.error || `پاسخ سرور: ${response.status}`);
    }

    setStatus(
      status,
      `ذخیره شد · ${formatBytes(payload.size)} · ${payload.key}`,
      "ok"
    );
    toast("بکاپ روی سرور ذخیره شد", `${formatBytes(payload.size)} در ${payload.key}`);
  } catch (error) {
    setStatus(status, error.message || "ارسال ناموفق بود.", "error");
    toast("ارسال ناموفق", error.message || "ارسال ناموفق بود.");
  } finally {
    button.disabled = false;
    // توکن پس از هر تلاش از حافظهٔ صفحه پاک می‌شود
    const tokenField = $("#backupToken");
    if (tokenField) tokenField.value = "";
  }
}

async function verifyBackup() {
  const status = $("#backupVerifyStatus");
  const output = $("#backupVerifyResult");
  const file = $("#backupVerifyFile")?.files?.[0];
  const passphrase = $("#backupVerifyPassphrase")?.value || "";

  output.innerHTML = "";

  if (!file) {
    setStatus(status, "فایل .ntbk را انتخاب کنید.", "error");
    return;
  }
  if (!passphrase) {
    setStatus(status, "گذرواژه را وارد کنید.", "error");
    return;
  }

  setStatus(status, "در حال بازکردن و بررسی…", "busy");

  try {
    const fileBytes = new Uint8Array(await file.arrayBuffer());
    const { plainBytes, meta } = await decryptBackup({ fileBytes, passphrase });
    const payload = decodePayload(plainBytes);

    state.decrypted = { plainBytes, payload };

    output.innerHTML = `
      <div style="margin-top:10px;padding:10px;border:1px solid rgba(120,220,150,.35);border-radius:10px;font-size:.65rem;line-height:1.9">
        <div><strong>فایل سالم است</strong> — گذرواژه درست و محتوا دست‌نخورده.</div>
        <div>تاریخ ساخت: <span dir="ltr">${payload.createdAt || "?"}</span></div>
        <div>حجم فایل: ${faTitle(formatBytes(meta.totalBytes))} · حجم داده: ${faTitle(formatBytes(meta.plainBytes))}</div>
        <div>تعداد تور: ${faTitle((payload.data?.tours || []).length)} · درخواست‌ها: ${faTitle((payload.leads || []).length)}</div>
        ${payload.mono ? `<div>سورس MONO: ${faTitle(payload.mono.fileName || "")} — ${faTitle(formatBytes(payload.mono.bytes))}</div>` : "<div>سورس MONO در این بکاپ نیست.</div>"}
        <div style="margin-top:8px;display:flex;gap:8px;flex-wrap:wrap">
          <button class="btn btn-ghost btn-small" type="button" id="backupDownloadDecrypted">دانلود JSON بازشده</button>
          <button class="btn btn-danger btn-small" type="button" id="backupRestoreData">بازیابی داده‌های سایت</button>
        </div>
      </div>`;

    $("#backupDownloadDecrypted")?.addEventListener("click", () => {
      const blob = new Blob([JSON.stringify(state.decrypted.payload, null, 2)], { type: "application/json;charset=utf-8" });
      const url = URL.createObjectURL(blob);
      const link = document.createElement("a");
      link.href = url;
      link.download = `nourotour-mono-decrypted-${new Date().toISOString().slice(0, 10)}.json`;
      document.body.appendChild(link);
      link.click();
      link.remove();
      URL.revokeObjectURL(url);
    });

    $("#backupRestoreData")?.addEventListener("click", () => {
      const target = api();
      if (!target?.importData) {
        setStatus(status, "بازیابی از این صفحه پشتیبانی نمی‌شود؛ از تب JSON استفاده کنید.", "error");
        return;
      }
      if (!confirm("داده‌های فعلی سایت با محتوای این بکاپ جایگزین شود؟")) return;
      target.importData(state.decrypted.payload.data);
      setStatus(status, "داده‌های سایت از بکاپ بازیابی شد.", "ok");
      toast("بازیابی شد", "محتوای سایت از بکاپ بازگردانده شد.");
    });

    setStatus(status, "باز شد و بررسی شد.", "ok");
  } catch (error) {
    state.decrypted = null;
    setStatus(status, error.message || "بازکردن ناموفق بود.", "error");
  }
}

function clearSecrets() {
  state.passphrase = "";
  ["#backupPassphrase", "#backupPassphrase2", "#backupToken", "#backupVerifyPassphrase"].forEach(selector => {
    const field = $(selector);
    if (field) field.value = "";
  });
}

function wire() {
  if (!api()) {
    state.timer = setTimeout(wire, 200);
    return;
  }
  clearTimeout(state.timer);

  $("#backupBuild")?.addEventListener("click", buildBackup);
  $("#backupDownload")?.addEventListener("click", downloadBackup);
  $("#backupUpload")?.addEventListener("click", uploadBackup);
  $("#backupVerify")?.addEventListener("click", verifyBackup);

  $("#backupEndpoint")?.addEventListener("input", event => {
    const value = String(event.target.value || "");
    if (value && !value.startsWith("https://")) {
      event.target.setCustomValidity("نشانی باید با https:// شروع شود.");
    } else {
      event.target.setCustomValidity("");
    }
  });

  // هنگام بسته‌شدن پنل، رازها از حافظه پاک می‌شوند
  document.querySelector("#closeAdmin")?.addEventListener("click", () => {
    clearSecrets();
  });

  // اگر تب بکاپ باز شد و گذرواژه‌ها پاک شده‌اند، اندازه‌گیری قبلی باطل می‌شود
  document.querySelectorAll(".admin-tabs [data-admin-tab]").forEach(button => {
    button.addEventListener("click", () => {
      if (button.dataset.adminTab !== "backup") clearSecrets();
    });
  });
}

if (document.readyState === "loading") {
  document.addEventListener("DOMContentLoaded", wire, { once: true });
} else {
  wire();
}
