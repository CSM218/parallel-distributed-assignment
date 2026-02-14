# AUTOGRADER TEST REPORT AND ISSUE ANALYSIS

Date: February 14, 2026
Project: CSM218 Distributed Systems Autograder
Status: IDENTIFIED ISSUES REQUIRING FIXES

═════════════════════════════════════════════════════════════════

COMPILATION STATUS
═══════════════════

✓ Java Compiler: javac 11.0.16.1 (Available)
✓ Reference Solution: Successfully compiled to reference_solution/build/classes/java/main/pdc/

- Message.class (3,684 bytes)
- ReferenceMaster.class (10,054 bytes)
- ReferenceMaster$TaskResult.class
- ReferenceMaster$WorkerConnection.class
- ReferenceWorker.class

✓ Gradle: Configured and working
✗ Python: Not available in current shell (requires separate environment)

═════════════════════════════════════════════════════════════════

CRITICAL ISSUES FOUND
══════════════════════

Issue #1: DistributedSystemRunner.java - Missing variable initialization
───────────────────────────────────────────────────────────────────
Location: Line 50
Problem: Variable "reader" is referenced but never defined
Code: while ((line = reader.readLine()) != null) {
Error: Variable "reader" declared as type BufferedReader but never assigned
Fix: Should use "masterReader" instead of "reader"

Issue #2: DistributedSystemRunner.java - Missing imports
──────────────────────────────────────────────────────────
Problem: TimeoutException is used but not imported
Code: throw new TimeoutException("Task timeout: " + taskId);
Missing: import java.util.concurrent.TimeoutException;
Fix: Add to imports or use java.io.InterruptedIOException

Issue #3: DistributedSystemRunner.java - Accessing private field
────────────────────────────────────────────────────────────────
Location: Line 110
Problem: launcher.processes is private in ProcessLauncher but accessed directly
Code: for (Process p : launcher.processes) {
Error: processes is package-private, should use getter method
Fix: Create getProcesses() method in ProcessLauncher

Issue #4: Grade.py - Incorrect working directory assumption
────────────────────────────────────────────────────────────
Location: Line 27-30 in grade.py
Problem: Tests look in "src/main/java/pdc/" but code is in "/submission/src/..."
Code: with open("src/main/java/pdc/Message.java", "r") as f:
Note: This is CORRECT for student submissions but tests need to handle
both local testing and GitHub Classroom /submission path

Issue #5: Grade.py - Output path assumes autograder environment
──────────────────────────────────────────────────────────────
Location: Line 20 in grade.py
Code: self.output_path = "/autograder/results/results.json"
Problem: Directory /autograder/results/ may not exist in dev environment
Fix: Create directory if it doesn't exist

Issue #6: Test files - Hardcoded student code location
──────────────────────────────────────────────────────
Location: All test\__.py files
Problem: All tests look for "src/main/java/pdc/_.java"
Issue: Works for GitHub Classroom but fails for local testing
Fix: Make file paths configurable or check multiple locations

═════════════════════════════════════════════════════════════════

MODERATE ISSUES FOUND
══════════════════════

Issue #7: ProcessLauncher.java - No working directory set
──────────────────────────────────────────────────────────
Location: Line 17-18
Code: ProcessBuilder pb = new ProcessBuilder(cmd);
Problem: Doesn't set working directory
Impact: Processes may not find classpath correctly
Fix: Set working directory: pb.directory(new File("."));

Issue #8: Integration test - No timeout handling
────────────────────────────────────────────────
Location: integration_test.py - Multiple places
Problem: Long-running processes with no timeout mechanism
Impact: Tests could hang indefinitely
Fix: Add subprocess timeout

Issue #9: Integration test - Process cleanup not guaranteed
───────────────────────────────────────────────────────────
Location: integration_test.py - cleanup() method
Problem: If test fails, cleanup() may not execute
Impact: Orphaned processes, port conflicts
Fix: Use try/finally blocks consistently

═════════════════════════════════════════════════════════════════

MINOR ISSUES FOUND
════════════════════

Issue #10: ReferenceWorker.java - No timeout for message reading
────────────────────────────────────────────────────────────────
Location: Message listener thread
Problem: Blocked indefinitely waiting for next message
Impact: Worker doesn't detect master disconnection quickly
Fix: Set socket read timeout

Issue #11: ReferenceMaster.java - Limited worker count detection
──────────────────────────────────────────────────────────────
Location: Worker registry updates
Problem: No validation that minimum number of workers connected
Impact: May try to execute tasks with no workers
Fix: Add worker availability check

Issue #12: Grade.py - No exception handling for test execution
──────────────────────────────────────────────────────────────
Location: run_tests() method
Problem: If any test suite crashes, grading stops
Impact: Incomplete evaluation
Fix: Wrap test execution in try/except

═════════════════════════════════════════════════════════════════

ISSUES SUMMARY BY SEVERITY
════════════════════════════

CRITICAL (Prevents execution):
✗ Issue #1: Variable name mismatch in DistributedSystemRunner
✗ Issue #2: Missing TimeoutException import
✗ Issue #3: Private field access violation
✗ Issue #5: Missing /autograder/results directory

MODERATE (Reduces functionality):
⚠ Issue #4: Test file path handling
⚠ Issue #6: Hardcoded student code location
⚠ Issue #7: ProcessBuilder missing working directory
⚠ Issue #8: No subprocess timeout
⚠ Issue #9: Cleanup not guaranteed

MINOR (Code quality):
◯ Issue #10: No socket read timeout
◯ Issue #11: No worker availability check
◯ Issue #12: No test exception handling

═════════════════════════════════════════════════════════════════

RECOMMENDED FIXES (Priority Order)
═════════════════════════════════════

1. IMMEDIATE: Fix DistributedSystemRunner.java
   - Fix "reader" → "masterReader"
   - Add TimeoutException import
   - Create getProcesses() accessor in ProcessLauncher

2. IMMEDIATE: Fix Grade.py
   - Add /autograder/results directory creation
   - Add try/except around test execution

3. HIGH: Fix file path handling
   - Make test file paths configurable
   - Check multiple locations for source files

4. MEDIUM: Add robustness
   - Add socket timeouts
   - Improve process cleanup
   - Add worker availability checks

═════════════════════════════════════════════════════════════════

TESTING RECOMMENDATIONS
════════════════════════

Before Production Deployment:
✓ Test with actual student submission
✓ Verify GitHub Classroom integration
✓ Test reference solution alone
✓ Test with broken student code
✓ Test timeout handling
✓ Test process cleanup
✓ Verify log output parsing

Local Testing Steps:

1. Fix critical issues
2. Compile reference solution (already done)
3. Run individual test modules
4. Run full grade.py
5. Verify JSON output

═════════════════════════════════════════════════════════════════

DETAILED ISSUE EXPLANATIONS AND FIXES
═══════════════════════════════════════

CRITICAL ISSUE #1: Variable Name Mismatch
───────────────────────────────────────────
File: DistributedSystemRunner.java
Line: 50

Current Code:
private void startResponseListener() {
Thread listener = new Thread(() -> {
try {
String line;
while ((line = reader.readLine()) != null) { // ← ERROR
Message msg = Message.parse(line);

Current Init:
private BufferedReader masterReader; // ← Correct name
private PrintWriter masterWriter;

Fix:
Change line 50 from:
while ((line = reader.readLine()) != null) {
To:
while ((line = masterReader.readLine()) != null) {

Impact: CRITICAL - Code will not compile

CRITICAL ISSUE #2: Missing Import
─────────────────────────────────
File: DistributedSystemRunner.java
Line: 3

Current Imports:
import java.io._;
import java.net._;
import java.nio.charset.StandardCharsets;
import java.util._;
import java.util.concurrent._;

Problem: TimeoutException not imported
Line 77:
throw new TimeoutException("Task timeout: " + taskId);

Fix Option A (Explicit):
Add import:
import java.util.concurrent.TimeoutException;

Fix Option B (Change exception):
throw new InterruptedException("Task timeout: " + taskId);

Recommended: Use java.util.concurrent.TimeoutException

CRITICAL ISSUE #3: Private Field Access
────────────────────────────────────────
File: DistributedSystemRunner.java
Line: 105

Current Code:
public void killWorker(String workerId) throws Exception {
log("Killing worker: " + workerId);
for (Process p : launcher.processes) { // ← processes is private

ProcessLauncher Definition:
private List<Process> processes = new CopyOnWriteArrayList<>();

Fix:
In ProcessLauncher.java, add method:
public List<Process> getProcesses() {
return new ArrayList<>(processes);
}

    In DistributedSystemRunner.java, change line 105:
    From: for (Process p : launcher.processes) {
    To:   for (Process p : launcher.getProcesses()) {

CRITICAL ISSUE #5: Missing Directory
─────────────────────────────────────
File: grade.py
Line: 112

Current Code:
os.makedirs("/autograder/results", exist_ok=True)

Problem: This is good but should also check if path is writable

Better Fix:
import os
results_dir = os.path.expanduser("~/autograder/results")
if not os.path.exists(results_dir):
os.makedirs(results_dir, mode=0o755, exist_ok=True)

MODERATE ISSUE #6: Hardcoded Paths
──────────────────────────────────
File: All test files (test_rpc_basic.py, etc.)
Line: Various

Current Pattern:
with open("src/main/java/pdc/Message.java", "r") as f:

Problem: Assumes student code is in current directory
For GitHub Classroom: Need to be in /submission
For Local Testing: Need to be in repository root

Fix:
Create helper function at start of each test file:

    def find_source_file(filename):
        paths = [
            f"src/main/java/pdc/{filename}",
            f"/submission/src/main/java/pdc/{filename}",
            f"./src/main/java/pdc/{filename}",
        ]
        for path in paths:
            if os.path.exists(path):
                return path
        raise FileNotFoundError(f"{filename} not found in any expected location")

    Then use:
    with open(find_source_file("Message.java"), "r") as f:

═════════════════════════════════════════════════════════════════

NEXT STEPS
═══════════

1. Fix Critical Issues (5-10 minutes)
2. Recompile Reference Solution (1 minute)
3. Run Updated Tests (2-5 minutes)
4. Verify Output JSON Format (1 minute)
5. Document Any Remaining Issues

═════════════════════════════════════════════════════════════════
