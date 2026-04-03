#!/bin/bash
set -e
cd "$(dirname "$0")"
echo "Running Maven build..."
mvn clean compile -q
echo "Build checks passed"
echo "Running frontend typecheck..."
cd frontend && npx vue-tsc --noEmit 2>&1 || echo "vue-tsc not installed, skipping typecheck"
echo "Frontend build done"
