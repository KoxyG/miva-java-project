#!/bin/bash
# Compile main sources + tests, then run TestRunner

JAVA_HOME="${JAVA_HOME:-/Library/Java/JavaVirtualMachines/jdk-23.jdk/Contents/Home}"
PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"

mkdir -p "$PROJECT_DIR/out"
find "$PROJECT_DIR/src" -name "*.java" > "$PROJECT_DIR/sources.txt"
find "$PROJECT_DIR/tests" -name "*.java" >> "$PROJECT_DIR/sources.txt"

"$JAVA_HOME/bin/javac" -d "$PROJECT_DIR/out" -sourcepath "$PROJECT_DIR/src:$PROJECT_DIR/tests" @"$PROJECT_DIR/sources.txt"

if [ $? -ne 0 ]; then
    echo "Compilation failed."
    exit 1
fi

echo ""
"$JAVA_HOME/bin/java" -cp "$PROJECT_DIR/out" TestRunner
