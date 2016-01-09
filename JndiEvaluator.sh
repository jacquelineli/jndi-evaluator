#!/bin/bash

CALL_PATH="'dirname $0'"
CLASSPATHVAL='cat "${CALL_PATH}/classpath.txt" | sed "s|REPLACEMENT_TOKEN|${CALL_PATH}|g"'
java -classpath "${CLASSPATHVAL}" com.pitsu.tools.JndiEvaluator "$@"