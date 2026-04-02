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

# Test if the student service has valid syntax
if node -c src/services/studentService.js; then
    echo "Student service syntax check passed"
else
    echo "Student service syntax check failed"
    exit 1
fi

echo "Basic tests passed"
