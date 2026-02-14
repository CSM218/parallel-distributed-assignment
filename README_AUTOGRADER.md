# CSM218 Autograder Documentation

## Overview

This autograder evaluates distributed systems implementations for CSM218 (Parallel & Distributed Systems). It validates:

- Socket-based Inter-Process Communication (IPC)
- Custom RPC protocol implementation
- Parallel task execution
- Failure detection and recovery
- Thread safety and concurrency
- Protocol compliance with CSM218 schema

## How Grading Works

### 1. Compilation Phase

- Compiles student code using Gradle
- Fails if code doesn't compile
- Compilation check: 5% of score

### 2. Static Analysis Tests

- Scans source code for required components
- Validates protocol message structure
- Checks for forbidden frameworks (gRPC, RMI, Akka, etc.)
- Verifies environment variable usage

### 3. Protocol Validation

- Checks Message class contains CSM218 fields
- Validates magic constant "CSM218"
- Verifies version field equals 1
- Ensures studentId from environment variable

### 4. Concurrency Tests

- Verifies thread support (Thread, ExecutorService)
- Checks thread-safe collections (ConcurrentHashMap, CopyOnWriteArrayList)
- Validates multi-connection handling
- Tests atomic operations and synchronization

### 5. Integration Tests (Runtime)

- Launches master and worker processes
- Sends RPC requests
- Measures execution timing
- Detects parallel execution
- Simulates worker failure

## Test Categories

### Visible Tests (Run on every push)

- `test_rpc_basic.py` - RPC protocol structure
- `test_protocol_structure.py` - Message format validation
- `test_concurrency.py` - Thread support checks
- Compilation verification

### Hidden Tests (Run on GitHub Classroom evaluation)

- `test_parallel_execution.py` - Timing measurements
- `test_failure_handling.py` - Failure recovery scenarios
- `test_protocol_structure.py` - Message mutation validation
- Serialization consistency checks
- Performance signature validation

## Scoring Rubric

| Category                 | Weight | Criteria                                      |
| ------------------------ | ------ | --------------------------------------------- |
| IPC Communication        | 20%    | Socket messaging, bidirectional communication |
| RPC Abstraction          | 20%    | Request/response semantics, serialization     |
| Parallel Execution       | 20%    | Concurrent task execution, speedup detection  |
| Distributed Coordination | 15%    | Task distribution, result aggregation         |
| Failure Handling         | 15%    | Heartbeat timeout, task reassignment          |
| Concurrency              | 10%    | Multi-threaded request handling               |

**Passing Score:** 40%
**Excellent Score:** 85%

## Course-Specific Requirements

### Message Protocol (CSM218)

Every message must include:

```json
{
  "magic": "CSM218",
  "version": 1,
  "messageType": "...",
  "studentId": "...",
  "timestamp": ...,
  "payload": "..."
}
```

### Runtime Token

- Server generates random token on startup
- Client receives token in greeting
- Client must include token in all RPC requests
- Server validates token

### RPC Call Sequence

1. CONNECT
2. REGISTER_CAPABILITIES
3. RPC_REQUEST
4. HEARTBEAT (ongoing)
5. RPC_RESPONSE

### Parallelism Detection

- Autograder sends 4 simultaneous heavy tasks
- Each task includes delay seed for timing
- Workers measured for overlapping execution
- Sequential implementations fail test

### Failure Recovery

- During computation, worker process killed
- System must detect via heartbeat timeout
- Task reassigned to available worker
- Job completes successfully

### Log Signature

Workers must output:

```
[CSM218-WORKER] <id> START <taskId>
[CSM218-WORKER] <id> END <taskId>
```

### Environment Variables

- `STUDENT_ID` - Student identifier (injected by autograder)
- `MASTER_PORT` - Master process port
- `WORKER_ID` - Worker unique identifier
- `MASTER_HOST` - Master hostname
- `CSM218_PORT_BASE` - Base port for allocation

## Running Locally

### Prerequisites

```bash
sudo apt-get install openjdk-11-jdk python3 gradle
```

### Run autograder

```bash
cd autograder
python3 grade.py
```

### Run reference solution

```bash
# Terminal 1 - Master
export MASTER_PORT=9999
export STUDENT_ID=test
java -cp reference_solution/target/classes pdc.ReferenceMaster

# Terminal 2 - Worker 1
export WORKER_ID=worker-1
export MASTER_HOST=localhost
export MASTER_PORT=9999
export STUDENT_ID=test
java -cp reference_solution/target/classes pdc.ReferenceWorker

# Terminal 3 - Worker 2
export WORKER_ID=worker-2
java -cp reference_solution/target/classes pdc.ReferenceWorker

# Terminal 4 - Worker 3
export WORKER_ID=worker-3
java -cp reference_solution/target/classes pdc.ReferenceWorker
```

## Anti-Template Protections

The autograder includes mechanisms that prevent generic solutions from passing:

1. **CSM218 Protocol Schema** - Custom message format unknown to external tools
2. **Runtime Token Exchange** - Dynamic handshake not in specification
3. **Lecture-Specific RPC Sequence** - Non-standard call ordering
4. **Parallelism Trap** - Hidden delay seeds for execution timing
5. **Failure Recovery** - Task reassignment logic required
6. **Protocol Mutation Tests** - Strict schema validation
7. **Explicit Serialization** - No string concatenation allowed
8. **Timing Signature** - Reference performance profile validation
9. **Concurrency Validation** - Overlapping RPC requests
10. **Environment-Bound Identity** - STUDENT_ID in protocol
11. **Hidden Task Variants** - BLOCK_TRANSPOSE type routing
12. **Log Signature Verification** - CSM218 prefix in logs
13. **Framework Prohibition** - Rejects RMI/gRPC/Akka
14. **Dynamic Port Allocation** - CSM218_PORT_BASE usage
15. **Deterministic Result Validation** - Exact expected values

## Troubleshooting

### Compilation Fails

- Check Java version (requires Java 11+)
- Verify build.gradle configuration
- Ensure all dependencies in classpath

### Tests Report "Protocol validation failed"

- Verify Message class contains all 6 required fields
- Check CSM218 magic constant matches exactly
- Ensure version field equals 1
- Validate studentId from System.getenv("STUDENT_ID")

### No workers detected

- Check Master accepts connections properly
- Verify Worker registration sends REGISTER_WORKER message
- Ensure socket connection established before timeout
- Check firewall isn't blocking localhost connections

### Parallelism test fails

- Verify workers execute tasks concurrently
- Check ExecutorService or Thread pool usage
- Ensure no Thread.sleep() in master RPC handler
- Validate worker processes run independently

### Failure handling test fails

- Implement heartbeat mechanism with timeout
- Detect dead workers via missing heartbeat ACK
- Reassign incomplete tasks to available workers
- Ensure task completion despite worker failure

## Reference Solution Architecture

The reference solution demonstrates:

- **Message.java** - CSM218 protocol schema and validation
- **ReferenceMaster.java** - Socket server with worker registry
- **ReferenceWorker.java** - Client connecting to master
- **ProcessLauncher.java** - Test harness for spawning processes
- **DistributedSystemRunner.java** - Integration test framework

## Contact

For autograder issues, contact the course instructor.
