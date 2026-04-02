#!/bin/bash

# Simple build script
echo "Running basic build checks..."

# Check if src directory exists
if [ ! -d "src" ]; then
    echo "Error: src directory not found"
    exit 1
fi

# Check if main file exists
if [ ! -f "src/index.js" ]; then
    echo "Error: src/index.js not found"
    exit 1
fi

echo "Build checks passed"
