# ── Stage 1: build Vue SPA ────────────────────────────────────────────────────
FROM node:20-alpine AS build
WORKDIR /app

COPY package*.json ./
RUN npm ci

COPY . .
RUN npm run build

# ── Stage 2: nginx (serving + reverse proxy) ──────────────────────────────────
FROM nginx:1.27-alpine

# Odstraň default konfiguraci
RUN rm /etc/nginx/conf.d/default.conf

# Složka pro šablony konfigurace
RUN mkdir -p /etc/nginx/templates

# Nginx konfigurace jako šablony (entrypoint vybere HTTP nebo HTTPS)
COPY nginx.conf.template     /etc/nginx/templates/
COPY nginx-ssl.conf.template /etc/nginx/templates/

# Entrypoint skript — vybírá konfiguraci podle existence SSL certifikátu
COPY docker-entrypoint.sh /docker-entrypoint.sh
# Normalizace line endings (CRLF→LF) — nutné pokud byl soubor editován na Windows
RUN sed -i 's/\r$//' /docker-entrypoint.sh && chmod +x /docker-entrypoint.sh

# Vue build
COPY --from=build /app/dist /usr/share/nginx/html

HEALTHCHECK --interval=15s --timeout=5s --start-period=10s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost/ || exit 1

EXPOSE 80 443

ENTRYPOINT ["/docker-entrypoint.sh"]
CMD ["nginx", "-g", "daemon off;"]
