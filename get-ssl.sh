#!/bin/sh
# ─────────────────────────────────────────────────────────────────────────────
#  Získání Let's Encrypt certifikátu přes Certbot (webroot challenge)
#
#  Předpoklady:
#    - aplikace je spuštěna: docker compose up -d
#    - doména směřuje na tento server (DNS záznam A)
#    - v .env je nastaven DOMAIN= a EMAIL=
#
#  Použití:
#    chmod +x get-ssl.sh
#    ./get-ssl.sh
# ─────────────────────────────────────────────────────────────────────────────
set -eu

# Načti .env
if [ -f .env ]; then
    export $(grep -v '^#' .env | grep -v '^$' | xargs)
fi

DOMAIN="${DOMAIN:-}"
EMAIL="${EMAIL:-}"

if [ -z "$DOMAIN" ] || [ "$DOMAIN" = "localhost" ]; then
    echo "Chyba: nastav DOMAIN= v .env (ne localhost)"
    exit 1
fi

if [ -z "$EMAIL" ]; then
    printf "Zadej e-mail pro Let's Encrypt upozornění: "
    read EMAIL
fi

echo "Získávám certifikát pro: $DOMAIN"

docker compose run --rm certbot certonly \
    --webroot \
    --webroot-path=/var/www/certbot \
    --email "$EMAIL" \
    --agree-tos \
    --no-eff-email \
    -d "$DOMAIN"

echo ""
echo "Certifikát získán. Restartuji nginx (přepnutí na HTTPS)..."
docker compose restart nginx

echo "Hotovo! Aplikace běží na https://$DOMAIN"
