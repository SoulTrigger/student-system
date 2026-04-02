#!/bin/bash

# Simple test script
echo "Running basic tests..."

# Test if the application can start (basic syntax check)
if node -c src/index.js; then
    echo "JavaScript syntax check passed"
else
    echo "JavaScript syntax check failed"
    exit 1
fi

echo "Basic tests passed"
