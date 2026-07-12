#!/bin/sh

#
# Copyright © 2015-2021 the original authors.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

set -e

APP_NAME="Gradle"
APP_BASE_NAME=$(basename "$0")
DIR=$( cd "${APP_HOME:-$(dirname "$0")}" && pwd -P ) || exit

APP_HOME="$DIR"
export APP_HOME

# Use the maximum available, or set MAX_FD != maximum.
MAX_FD=maximum

# Increase the maximum file descriptors if we can.
if ! is_cygwin && ! is_darwin && ! is_sunos ; then
    case $OSTYPE in
        cygwin*|msys*|mingw*)
            MAX_FD=maximum ;;
        *)
            MAX_FD=$(ulimit -H -n) 2>/dev/null
    esac
fi

if [ -n "$MAX_FD" ] && [ "$MAX_FD" != unlimited ] ; then
    ulimit -n $MAX_FD || echo "Could not set maximum file descriptor limit: $MAX_FD"
fi

DO_NOT_EXIT_ON_CLOSE=1
warn() { printf '%s\n' "$*" >&2 ; }
exec_cmd() {
    if [ "${GRADLE_WRAPPER_DEBUG-}" ]
    then
        printf 'Gradle Wrapper script will execute the command: %s\n' "$(printf ' %q' "$@")"
    fi
    exec "$@"
}

case $? in
    126) exec_cmd "${CMD[@]}" ;;
    127) echo "gradle: command not found: ${CMD[0]}" >&2; exit 127 ;;
esac

CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar

exec "$JAVACMD" "${JVM_ARGS[@]}" -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
