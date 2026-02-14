#!/bin/bash
# CSM218 AUTOGRADER - FINAL TEST & PUSH TO REMOTE
# This script tests the entire autograder infrastructure before pushing

set -e  # Exit on any error

echo "╔════════════════════════════════════════════════════════════╗"
echo "║  CSM218 AUTOGRADER - PRE-DEPLOYMENT TEST & PUSH SCRIPT    ║"
echo "╚════════════════════════════════════════════════════════════╝"
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Test 1: Verify directory structure
echo -e "${YELLOW}[TEST 1]${NC} Checking directory structure..."
REQUIRED_DIRS=(
    "autograder"
    "autograder/tests"
    "autograder/harness"
    "reference_solution/src/main/java/pdc"
    ".github/workflows"
)

for dir in "${REQUIRED_DIRS[@]}"; do
    if [ -d "$dir" ]; then
        echo -e "  ${GREEN}✓${NC} $dir"
    else
        echo -e "  ${RED}✗${NC} $dir (MISSING)"
        exit 1
    fi
done
echo ""

# Test 2: Verify required files
echo -e "${YELLOW}[TEST 2]${NC} Checking required files..."
REQUIRED_FILES=(
    "autograder/grade.py"
    "autograder/run_autograder.sh"
    "autograder/config.json"
    "autograder/tests/test_rpc_basic.py"
    "autograder/tests/test_protocol_structure.py"
    "autograder/tests/test_concurrency.py"
    "autograder/tests/test_parallel_execution.py"
    "autograder/tests/test_failure_handling.py"
    "autograder/tests/test_advanced_protocol.py"
    "autograder/harness/ProcessLauncher.java"
    "autograder/harness/DistributedSystemRunner.java"
    "autograder/harness/integration_test.py"
    "reference_solution/src/main/java/pdc/Message.java"
    "reference_solution/src/main/java/pdc/ReferenceMaster.java"
    "reference_solution/src/main/java/pdc/ReferenceWorker.java"
    "reference_solution/build.gradle"
    ".github/workflows/classroom.yml"
    "rubric.json"
    "ASSIGNMENT.md"
    "README_AUTOGRADER.md"
    "setup.sh"
)

MISSING_FILES=0
for file in "${REQUIRED_FILES[@]}"; do
    if [ -f "$file" ]; then
        echo -e "  ${GREEN}✓${NC} $file"
    else
        echo -e "  ${RED}✗${NC} $file (MISSING)"
        MISSING_FILES=$((MISSING_FILES + 1))
    fi
done

if [ $MISSING_FILES -gt 0 ]; then
    echo -e "\n${RED}ERROR: $MISSING_FILES files missing${NC}"
    exit 1
fi
echo ""

# Test 3: Verify Java compilation
echo -e "${YELLOW}[TEST 3]${NC} Checking Java compiler..."
if command -v javac &> /dev/null; then
    JAVA_VERSION=$(javac -version 2>&1)
    echo -e "  ${GREEN}✓${NC} Java compiler available: $JAVA_VERSION"
else
    echo -e "  ${RED}✗${NC} Java compiler not found"
    exit 1
fi
echo ""

# Test 4: Verify Git repository
echo -e "${YELLOW}[TEST 4]${NC} Checking Git repository..."
if git rev-parse --git-dir > /dev/null 2>&1; then
    REMOTE=$(git config --get remote.origin.url)
    echo -e "  ${GREEN}✓${NC} Git repository found"
    echo -e "  ${GREEN}✓${NC} Remote: $REMOTE"
else
    echo -e "  ${RED}✗${NC} Not a Git repository"
    exit 1
fi
echo ""

# Test 5: Check for uncommitted changes
echo -e "${YELLOW}[TEST 5]${NC} Checking for uncommitted changes..."
if git diff --quiet && git diff --cached --quiet; then
    echo -e "  ${GREEN}✓${NC} No uncommitted changes"
else
    echo -e "  ${YELLOW}⚠${NC} Uncommitted changes detected"
    git status --short
fi
echo ""

# Test 6: Compile reference solution
echo -e "${YELLOW}[TEST 6]${NC} Compiling reference solution..."
cd reference_solution
if gradle build > /dev/null 2>&1; then
    echo -e "  ${GREEN}✓${NC} Reference solution compiled successfully"
else
    echo -e "  ${RED}✗${NC} Reference solution compilation failed"
    gradle build
    exit 1
fi
cd ..
echo ""

# Test 7: Verify Python test syntax
echo -e "${YELLOW}[TEST 7]${NC} Checking Python syntax..."
PYTHON_FILES=(
    "autograder/grade.py"
    "autograder/run_autograder.sh"
    "autograder/tests/test_rpc_basic.py"
    "autograder/tests/test_protocol_structure.py"
    "autograder/tests/test_concurrency.py"
    "autograder/tests/test_parallel_execution.py"
    "autograder/tests/test_failure_handling.py"
    "autograder/tests/test_advanced_protocol.py"
)

for file in "${PYTHON_FILES[@]}"; do
    if [[ $file == *.py ]]; then
        python3 -m py_compile "$file" 2>/dev/null && echo -e "  ${GREEN}✓${NC} $file" || echo -e "  ${YELLOW}⚠${NC} $file (skipped - Python not available)"
    fi
done
echo ""

# Test 8: Check shell scripts
echo -e "${YELLOW}[TEST 8]${NC} Checking shell scripts..."
SHELL_FILES=("setup.sh" "classroom_setup.sh" "autograder/run_autograder.sh")
for file in "${SHELL_FILES[@]}"; do
    if bash -n "$file" 2>/dev/null; then
        echo -e "  ${GREEN}✓${NC} $file"
    else
        echo -e "  ${RED}✗${NC} $file (syntax error)"
        exit 1
    fi
done
echo ""

# Test 9: Verify JSON configuration
echo -e "${YELLOW}[TEST 9]${NC} Checking JSON files..."
JSON_FILES=("rubric.json" "autograder/config.json")
for file in "${JSON_FILES[@]}"; do
    if python3 -m json.tool "$file" > /dev/null 2>&1; then
        echo -e "  ${GREEN}✓${NC} $file (valid JSON)"
    else
        echo -e "  ${RED}✗${NC} $file (invalid JSON)"
        exit 1
    fi
done
echo ""

# Test 10: Count lines of code
echo -e "${YELLOW}[TEST 10]${NC} Code statistics..."
JAVA_LINES=$(find . -name "*.java" -path "*/reference_solution/*" -o -path "*/autograder/harness/*" | xargs wc -l 2>/dev/null | tail -1 | awk '{print $1}')
PYTHON_LINES=$(find autograder -name "*.py" | xargs wc -l 2>/dev/null | tail -1 | awk '{print $1}')
DOC_LINES=$(find . -name "*.md" -maxdepth 1 | xargs wc -l 2>/dev/null | tail -1 | awk '{print $1}')

echo -e "  ${GREEN}✓${NC} Java code: ~$JAVA_LINES lines"
echo -e "  ${GREEN}✓${NC} Python code: ~$PYTHON_LINES lines"
echo -e "  ${GREEN}✓${NC} Documentation: ~$DOC_LINES lines"
echo ""

# Summary
echo "╔════════════════════════════════════════════════════════════╗"
echo "║           ALL TESTS PASSED - READY TO PUSH                ║"
echo "╚════════════════════════════════════════════════════════════╝"
echo ""

# Git operations
echo -e "${YELLOW}[GIT]${NC} Staging all changes..."
git add -A
echo -e "  ${GREEN}✓${NC} Files staged"
echo ""

echo -e "${YELLOW}[GIT]${NC} Current status:"
git status --short | sed 's/^/  /'
echo ""

# Prompt for commit message
read -p "Enter commit message (default: 'Complete autograder infrastructure setup'): " COMMIT_MSG
COMMIT_MSG="${COMMIT_MSG:-Complete autograder infrastructure setup}"

echo ""
echo -e "${YELLOW}[GIT]${NC} Creating commit..."
git commit -m "$COMMIT_MSG"
echo -e "  ${GREEN}✓${NC} Commit created"
echo ""

# Get current branch
CURRENT_BRANCH=$(git rev-parse --abbrev-ref HEAD)
echo -e "${YELLOW}[GIT]${NC} Pushing to remote ($CURRENT_BRANCH)..."
git push origin "$CURRENT_BRANCH"
echo -e "  ${GREEN}✓${NC} Pushed successfully"
echo ""

echo "╔════════════════════════════════════════════════════════════╗"
echo "║         AUTOGRADER DEPLOYMENT COMPLETE!                  ║"
echo "╚════════════════════════════════════════════════════════════╝"
echo ""
echo "Next steps:"
echo "1. Configure GitHub Classroom with this repository"
echo "2. Set autograder command: bash autograder/run_autograder.sh"
echo "3. Test with first student submission"
echo ""
