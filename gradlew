#!/usr/bin/env sh
set -eu

SCRIPT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
CACHED_GRADLE="$HOME/.gradle/wrapper/dists/gradle-8.14.3-all/10utluxaxniiv4wxiphsi49nj/gradle-8.14.3/bin/gradle"

if [ -x "$SCRIPT_DIR/gradle/wrapper/gradle-wrapper.jar" ]; then
  exec "$SCRIPT_DIR/gradle/wrapper/gradle-wrapper.jar" "$@"
fi

if [ -x "$CACHED_GRADLE" ]; then
  exec "$CACHED_GRADLE" "$@"
fi

echo "Gradle is not installed. Open this project in Android Studio or install Gradle 8.14.3." >&2
exit 1
