#!/bin/bash
# ─────────────────────────────────────────────
#  Personal Task Manager — Build & Run Script
# ─────────────────────────────────────────────
# Usage:
#   ./compile.sh          → compile only
#   ./compile.sh run      → compile + run
#   ./compile.sh clean    → remove build output
# ─────────────────────────────────────────────

set -e  # Exit on any error

PROJECT_ROOT="$(cd "$(dirname "$0")" && pwd)"
SRC_DIR="$PROJECT_ROOT/src"
OUT_DIR="$PROJECT_ROOT/out"
MAIN_CLASS="taskmanager.Main"

# ── Clean ────────────────────────────────────
if [ "$1" = "clean" ]; then
    echo "Cleaning build output..."
    rm -rf "$OUT_DIR"
    echo "Done."
    exit 0
fi

# ── Compile ──────────────────────────────────
echo "Compiling sources..."
mkdir -p "$OUT_DIR"

# Collect all .java files recursively
find "$SRC_DIR" -name "*.java" > "$OUT_DIR/sources.txt"

javac -d "$OUT_DIR" @"$OUT_DIR/sources.txt"
echo "Compilation successful."

# ── Run ──────────────────────────────────────
if [ "$1" = "run" ]; then
    echo "Launching application..."
    # Run from PROJECT_ROOT so "data/tasks.dat" resolves correctly
    cd "$PROJECT_ROOT"
    java -cp "$OUT_DIR" "$MAIN_CLASS"
fi
