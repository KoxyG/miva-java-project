#!/bin/bash
# Compile and run SLCAS

JAVA_HOME="${JAVA_HOME:-/Library/Java/JavaVirtualMachines/jdk-23.jdk/Contents/Home}"
PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"

mkdir -p "$PROJECT_DIR/out"
find "$PROJECT_DIR/src" -name "*.java" > "$PROJECT_DIR/sources.txt"
"$JAVA_HOME/bin/javac" -d "$PROJECT_DIR/out" @"$PROJECT_DIR/sources.txt"

if [ $? -eq 0 ]; then
    echo "Compilation successful. Starting SLCAS..."
    "$JAVA_HOME/bin/java" -cp "$PROJECT_DIR/out" gui.MainWindow
else
    echo "Compilation failed."
    exit 1
fi
