#!/bin/sh
# Backup diario do PostgreSQL com retencao de 7 dias (RNF de disponibilidade).
set -eu
DESTINO=/backups
RETENCAO_DIAS="${BACKUP_RETENCAO_DIAS:-7}"
HORARIO="${BACKUP_HORARIO:-02:00}"

executar_backup() {
  arquivo="$DESTINO/siscep_$(date +%Y%m%d_%H%M%S).sql.gz"
  echo "[backup] gerando $arquivo"
  pg_dump -h "$PGHOST" -U "$PGUSER" -d "$PGDATABASE" --no-owner | gzip > "$arquivo.tmp"
  mv "$arquivo.tmp" "$arquivo"
  find "$DESTINO" -name 'siscep_*.sql.gz' -type f -mtime +"$((RETENCAO_DIAS - 1))" -print -delete
}

mkdir -p "$DESTINO"
executar_backup
while true; do
  agora=$(date +%s)
  alvo=$(date -d "$(date +%Y-%m-%d) $HORARIO" +%s 2>/dev/null || date -D '%Y-%m-%d %H:%M' -d "$(date +%Y-%m-%d) $HORARIO" +%s)
  [ "$alvo" -le "$agora" ] && alvo=$((alvo + 86400))
  sleep $((alvo - agora))
  executar_backup || echo "[backup] falha ao gerar backup"
done
