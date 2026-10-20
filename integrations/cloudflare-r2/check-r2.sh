#!/usr/bin/env bash
# بررسی امکان فعال‌سازی / فعال‌بودن R2 روی حساب Cloudflare — بدون هیچ تغییری در حساب.
# اجرا:  bash integrations/cloudflare-r2/check-r2.sh
#
# سه راه بررسی می‌شود:
#   ۱) wrangler whoami            → آیا به حساب دسترسی داریم و حساب کدام است؟
#   ۲) wrangler r2 bucket list    → اگر خطای 10042 داد، R2 هنوز فعال نشده است
#   ۳) Cloudflare API با توکن     → همان بررسی، بدون نصب wrangler (متغیر CLOUDFLARE_API_TOKEN)

set -uo pipefail

GREEN="\033[0;32m"; RED="\033[0;31m"; YELLOW="\033[0;33m"; DIM="\033[2m"; RESET="\033[0m"
ok()   { printf "${GREEN}✔ %s${RESET}\n" "$1"; }
bad()  { printf "${RED}✘ %s${RESET}\n" "$1"; }
warn() { printf "${YELLOW}! %s${RESET}\n" "$1"; }
note() { printf "${DIM}   %s${RESET}\n" "$1"; }

echo "=================================================="
echo " پیش‌بررسی R2 برای بکاپ خصوصی MONO — nourotour.ir"
echo "=================================================="
echo

if command -v node >/dev/null 2>&1; then
  ok "Node.js نصب است: $(node -v)"
else
  bad "Node.js نصب نیست (برای wrangler لازم است: نسخهٔ ۲۰ یا بالاتر)"
fi

if command -v npx >/dev/null 2>&1; then
  ok "npx در دسترس است"
else
  bad "npx در دسترس نیست"
fi

echo
echo "-- ۱) هویت و حساب Cloudflare --"
WHOAMI="$(npx --yes wrangler whoami 2>&1)"
if grep -qiE "You are logged in|associated with the email" <<<"$WHOAMI"; then
  ok "وارد حساب Cloudflare شده‌اید"
  grep -iE "associated with|Account Name|Account ID|accounts" <<<"$WHOAMI" | sed 's/^/   /' | head -12
else
  warn "وارد نشده‌اید. اجرا کنید: npx wrangler login"
  note "یا توکن API را در CLOUDFLARE_API_TOKEN بگذارید و اسکریپت را دوباره اجرا کنید."
fi

echo
echo "-- ۲) وضعیت اشتراک R2 --"
R2OUT="$(npx --yes wrangler r2 bucket list 2>&1)"
R2STATUS=$?

if [ $R2STATUS -eq 0 ]; then
  ok "R2 روی این حساب فعال است (فهرست bucketها دریافت شد)"
  if [ -n "$(tr -d '[:space:]' <<<"$R2OUT")" ]; then
    sed 's/^/   /' <<<"$R2OUT" | head -20
  else
    note "هنوز هیچ bucketی ساخته نشده است."
  fi
elif grep -q "10042" <<<"$R2OUT"; then
  bad "R2 روی این حساب فعال نیست (خطای 10042: Please enable R2 through the Cloudflare Dashboard)"
  note "رفع: داشبورد → Storage & Databases → R2 Object Storage → Add R2 subscription"
  note "این مرحله یک روش پرداخت معتبر می‌خواهد؛ حتی در سقف رایگان."
elif grep -qiE "authentication|not logged in|Unable to authenticate" <<<"$R2OUT"; then
  warn "برای بررسی R2 باید ابتدا وارد حساب شوید (npx wrangler login)."
else
  warn "پاسخ نامشخص؛ خروجی خام:"
  sed 's/^/   /' <<<"$R2OUT" | head -15
fi

echo
echo "-- ۳) بررسی مستقیم با Cloudflare API (اختیاری) --"
if [ -n "${CLOUDFLARE_API_TOKEN:-}" ]; then
  VERIFY="$(curl -s -H "Authorization: Bearer $CLOUDFLARE_API_TOKEN" \
    https://api.cloudflare.com/client/v4/user/tokens/verify)"
  if grep -q '"status":"active"' <<<"$(tr -d '[:space:]' <<<"$VERIFY")"; then
    ok "توکن API معتبر و فعال است"
  else
    bad "توکن API معتبر نیست: $(head -c 200 <<<"$VERIFY")"
  fi

  ACCOUNT_ID="${CLOUDFLARE_ACCOUNT_ID:-}"
  if [ -z "$ACCOUNT_ID" ]; then
    ACCOUNT_ID="$(curl -s -H "Authorization: Bearer $CLOUDFLARE_API_TOKEN" \
      https://api.cloudflare.com/client/v4/accounts \
      | tr -d '\n' | sed -n 's/.*"id":"\([a-f0-9]\{32\}\)".*/\1/p' | head -1)"
  fi

  if [ -n "$ACCOUNT_ID" ]; then
    ok "Account ID: $ACCOUNT_ID"
    BUCKETS="$(curl -s -H "Authorization: Bearer $CLOUDFLARE_API_TOKEN" \
      "https://api.cloudflare.com/client/v4/accounts/$ACCOUNT_ID/r2/buckets")"
    if grep -q '"success":true' <<<"$(tr -d '[:space:]' <<<"$BUCKETS")"; then
      ok "R2 فعال است؛ bucketها:"
      grep -o '"name":"[^"]*"' <<<"$BUCKETS" | sed 's/"name":"/   - /; s/"$//' | head -20
    elif grep -q '"code":10042' <<<"$(tr -d '[:space:]' <<<"$BUCKETS")"; then
      bad "R2 فعال نیست (10042) — باید از داشبورد مشترک R2 را اضافه کنید."
    else
      warn "پاسخ نامشخص: $(head -c 300 <<<"$BUCKETS")"
    fi
  else
    warn "Account ID پیدا نشد؛ CLOUDFLARE_ACCOUNT_ID را دستی تنظیم کنید."
  fi
else
  note "CLOUDFLARE_API_TOKEN تنظیم نشده؛ این بخش رد شد."
fi

echo
echo "-- ۴) مواردی که فقط از داشبورد قابل بررسی است --"
note "Billing → Payment methods : وجود روش پرداخت معتبر (برای فعال‌سازی R2 الزامی است)"
note "Storage & Databases → R2 → Settings : فعال نبودن Public Access و r2.dev"
note "وضعیت تحریم/کشور حساب : کارت و آدرس صورتحساب باید با کشور مجاز Cloudflare سازگار باشد"
echo
echo "پایان بررسی."
