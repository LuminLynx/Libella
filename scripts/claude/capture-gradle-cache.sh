#!/usr/bin/env bash
# Captures a Gradle dependency cache tarball for AI-101's current AGP version,
# so a Claude Code cloud session (which cannot reach dl.google.com) can
# restore it via scripts/claude/restore-gradle-cache.sh.
#
# Run this on a machine with full network access (your laptop, CI, etc.).
# The output tarball must be uploaded to a host the cloud sandbox can reach,
# and GRADLE_CACHE_URL set in the session env to point at it.
#
# Bump CACHE_VERSION whenever AGP / Kotlin / Compose dep versions change.

set -euo pipefail

CACHE_VERSION="${CACHE_VERSION:-agp-8.5.2-v1}"
GRADLE_HOME="${GRADLE_USER_HOME:-$HOME/.gradle}"
OUT="gradle-cache-${CACHE_VERSION}.tar.zst"

cd "$(dirname "$0")/../.."

if [ ! -x ./gradlew ]; then
  echo "[capture] ./gradlew not found or not executable" >&2
  exit 1
fi

echo "[capture] populating cache via ./gradlew --refresh-dependencies tasks ..."
./gradlew --refresh-dependencies tasks > /dev/null

echo "[capture] verifying offline build works ..."
./gradlew --offline assembleDebug

echo "[capture] packaging $OUT ..."
tar --zstd -cf "$OUT" \
  -C "$GRADLE_HOME" \
  caches/modules-2 \
  caches/jars-9

SIZE="$(du -h "$OUT" | cut -f1)"
echo "[capture] done: $OUT ($SIZE)"
echo "[capture] upload this file to a sandbox-reachable URL and set:"
echo "[capture]   GRADLE_CACHE_URL=<that-url>"
echo "[capture]   GRADLE_CACHE_VERSION=${CACHE_VERSION}"
