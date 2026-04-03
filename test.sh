#!/bin/bash
set -e
echo "Running tests..."
cd "$(dirname "$0")"
mvn test -q
echo "All tests passed"
