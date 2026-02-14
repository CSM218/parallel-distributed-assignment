#!/bin/bash

set -e

echo "=== CSM218 Autograder Start ==="

# Get the directory where this script is located
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
REPO_ROOT="$(dirname "$SCRIPT_DIR")"

# ALWAYS run from the repository root to ensure all relative paths in tests work
cd "$REPO_ROOT"

# Run Python grading script with forwarded arguments
# We use the absolute path to the script so it can find its own test modules
python3 "$SCRIPT_DIR/grade.py" "$@"

echo "=== Autograder Complete ==="
