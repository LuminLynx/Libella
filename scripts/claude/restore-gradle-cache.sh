#!/usr/bin/env bash
# Restores a pre-baked Gradle dependency cache so AGP plugin resolution does
# not need to reach dl.google.com / maven.google.com (denied by the Claude
# Code cloud sandbox proxy with x-deny-reason: host_not_allowed).
#
# Wired to SessionStart in .claude/settings.json. Idempotent: a marker file
# under ~/.gradle skips re-extraction once a given cache version is in place.
#
# One-time setup (see scripts/claude/capture-gradle-cache.sh):
#   1. On a machine with full network access, run capture-gradle-cache.sh to
#      produce gradle-cache-<version>.tar.zst.
#   2. Upload it to a host the sandbox can reach (GitHub Release asset,
#      Maven Central-fronted bucket, internal mirror, etc.).
#   3. Set GRADLE_CACHE_URL in the cloud session environment to that URL.
#   4. Bump GRADLE_CACHE_VERSION whenever AGP / Kotlin / Compose deps change
#      so a stale cache is replaced.

set -euo pipefail

CACHE_URL="${GRADLE_CACHE_URL:-}"
CACHE_VERSION="${GRADLE_CACHE_VERSION:-agp-8.5.2-v1}"
GRADLE_HOME="${GRADLE_USER_HOME:-$HOME/.gradle}"
MARKER="$GRADLE_HOME/.claude-cache-${CACHE_VERSION}"

log() { printf '[gradle-cache] %s\n' "$*"; }

if [ -f "$MARKER" ]; then
  log "already restored ($CACHE_VERSION); skipping."
  exit 0
fi

if [ -z "$CACHE_URL" ]; then
  log "GRADLE_CACHE_URL not set; skipping restore." >&2
  log "Android builds requiring AGP will fail until the cache is provisioned." >&2
  exit 0
fi

mkdir -p "$GRADLE_HOME"
TMP="$(mktemp)"
trap 'rm -f "$TMP"' EXIT

log "downloading $CACHE_URL ..."
if ! curl -fsSL --max-time 600 -o "$TMP" "$CACHE_URL"; then
  log "download failed; continuing without cache." >&2
  exit 0
fi

log "extracting into $GRADLE_HOME ..."
# Tarball is rooted at $GRADLE_HOME and is expected to contain
# caches/modules-2 and caches/jars-9.
if [[ "$CACHE_URL" == *.zst ]]; then
  if ! command -v zstd >/dev/null 2>&1; then
    log "zstd not installed; attempting apt-get install ..."
    if command -v sudo >/dev/null 2>&1; then SUDO="sudo"; else SUDO=""; fi
    $SUDO apt-get install -y zstd >/dev/null 2>&1 || {
      log "could not install zstd; skipping restore." >&2
      exit 0
    }
  fi
  tar --zstd -xf "$TMP" -C "$GRADLE_HOME"
else
  tar -xf "$TMP" -C "$GRADLE_HOME"
fi

touch "$MARKER"
log "done ($CACHE_VERSION)."
