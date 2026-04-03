#!/bin/bash
set -e
cd "$(dirname "$0")"
echo "Running backend tests..."
mvn test -q
echo "Backend tests passed"
echo "Running frontend tests..."
cd frontend && npx vitest run
echo "All tests passed"
