#!/bin/bash
set -e
echo "Running Maven build..."
cd "$(dirname "$0")"
mvn clean compile -q
echo "Build checks passed"
