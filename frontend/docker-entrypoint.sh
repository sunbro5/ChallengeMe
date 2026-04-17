#!/bin/sh
# ─────────────────────────────────────────────────────────────────────────────
# nginx entrypoint — vybere HTTP nebo HTTPS konfiguraci podle toho,
# zda existuje Let's Encrypt certifikát pro nastavenou doménu.
#
#   HTTP pouze:  při prvním nasazení, certifikát ještě nebyl získán
#   HTTPS:       certifikát nalezen → automaticky přepne na HTTPS + redirect
# ─────────────────────────────────────────────────────────────────────────────
set -eu

DOMAIN="${DOMAIN:-localhost}"
CERT="/etc/letsencrypt/live/${DOMAIN}/fullchain.pem"
TPL="/etc/nginx/templates"
CFG="/etc/nginx/conf.d/default.conf"

if [ -f "$CERT" ]; then
    echo "[nginx] Certifikát nalezen pro '${DOMAIN}' — spouštím HTTPS"
    envsubst '${DOMAIN}' < "${TPL}/nginx-ssl.conf.template" > "$CFG"
else
    echo "[nginx] Certifikát nenalezen — spouštím HTTP (port 80)"
    envsubst '${DOMAIN}' < "${TPL}/nginx.conf.template" > "$CFG"
fi

# Otestuj konfiguraci před spuštěním
nginx -t

exec "$@"
