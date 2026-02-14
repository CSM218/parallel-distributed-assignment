# Parallel and Distributed Computing (PDC) - Matrix Assignment

## Problem Description

In this assignment, you will implement a **distributed matrix computation system** using Java with message-passing between multiple worker threads.

### Objectives

1. **Master-Worker Architecture**: Implement a Master class that coordinates work distribution to Worker instances.
2. **Matrix Operations**: Implement matrix sum and matrix multiplication operations.
3. **Parallel Processing**: Distribute matrix computations across multiple workers.
4. **Message Passing**: Implement thread-safe communication between Master and Workers.
5. **Performance**: Optimize to leverage parallelism on multi-core systems.

### Assignment Tasks

1. **Master.java** - Implement the following methods:
   - `sumMatrix(int[][] matrix)`: Calculate the sum of all elements
   - `multiplyMatrices(int[][] matrixA, int[][] matrixB)`: Multiply two matrices
   - `processDistributed(int[][] matrix, int numWorkers)`: Distribute work to workers

2. **Worker.java** - Implement the following methods:
   - `assignData(int[][] dataChunk)`: Accept a chunk of data to process
   - `processData()`: Perform computation on assigned data
   - `getResult()`: Return the computed result

3. **MatrixGenerator** - Helper class (already provided):
   - Use for generating test matrices
   - Supports random matrices, identity matrices, and filled matrices

### Constraints

- Use Java concurrency utilities (Thread, ExecutorService, etc.) if needed
- Ensure thread-safe communication between Master and Workers
- Maximize parallelism while minimizing synchronization overhead

---

## Project Structure

```
src/
  main/java/pdc/
    Master.java           # Coordinator class (implement here)
    Worker.java           # Worker class (implement here)
    MatrixGenerator.java  # Utility class (provided)
  test/java/pdc/
    MasterTest.java       # JUnit tests for Master
    WorkerTest.java       # JUnit tests for Worker
.github/workflows/
  autograde.yml           # GitHub Actions CI/CD pipeline
build.gradle              # Gradle build configuration
README.md                 # This file
.gitignore                # Git ignore rules
```

---

## Building the Project

### Prerequisites

- **Java 11** or higher
- **Gradle 7.0** or higher (or use the provided Gradle wrapper)

### Build

Build the project and compile all code:

```bash
./gradlew build
```

Or on Windows:

```cmd
gradlew.bat build
```

### Run Tests

Execute all JUnit tests:

```bash
./gradlew test
```

View test results in `build/reports/tests/test/index.html`

### Clean Build

Remove build artifacts:

```bash
./gradlew clean
```

### Run Specific Test

Run a specific test class:

```bash
./gradlew test --tests MasterTest
./gradlew test --tests WorkerTest
```

---

## Running the Code

### From Command Line

Compile and run a main class:

```bash
./gradlew run
```

### From an IDE

1. Open the project in IntelliJ IDEA, Eclipse, or VS Code
2. Right-click on any Java file and select "Run"
3. Run tests individually or as a suite

---

## Test Cases

### MasterTest

- `testSumMatrix_SimpleCase()`: Tests 2x2 matrix summation
- `testSumMatrix_SingleElement()`: Tests single element matrix
- `testSumMatrix_ZeroMatrix()`: Tests zero matrix
- `testMultiplyMatrices_IdentityMultiplication()`: Tests identity matrix multiplication
- `testMultiplyMatrices_SimpleMultiplication()`: Tests basic matrix multiplication
- `testProcessDistributed_ValidInput()`: Tests distributed processing

### WorkerTest

- `testWorkerCreation()`: Tests Worker instantiation
- `testAssignData()`: Tests data assignment
- `testProcessData()`: Tests processing without exceptions
- `testGetResult()`: Tests result retrieval
- `testMultipleWorkers()`: Tests multiple worker instances

---

## GitHub Actions CI/CD

The project includes an automated testing pipeline (`.github/workflows/autograde.yml`) that:

1. **Triggers on**: Push to `main` branch and pull requests
2. **Environment**: Ubuntu latest with JDK 20
3. **Steps**:
   - Checks out code
   - Sets up Java
   - Builds with Gradle
   - Runs all tests
   - Generates test reports

Test results appear in the GitHub Actions tab of your repository.

---

## Grading Criteria

Your implementation will be evaluated on:

1. **Correctness** (60%): Tests pass and produce correct results
2. **Parallelism** (20%): Effective use of multiple workers
3. **Code Quality** (10%): Clean, readable, well-documented code
4. **Thread Safety** (10%): Proper synchronization and message passing

---

## Example Usage

```java
// Create a master coordinator
Master master = new Master();

// Generate a test matrix
int[][] matrix = MatrixGenerator.generateRandomMatrix(100, 100, 1000);

// Compute sum
int sum = master.sumMatrix(matrix);
System.out.println("Matrix sum: " + sum);

// Process distributed with 4 workers
Object result = master.processDistributed(matrix, 4);

// Multiply matrices
int[][] matrixB = MatrixGenerator.generateIdentityMatrix(100);
int[][] product = master.multiplyMatrices(matrix, matrixB);
```

---

## Troubleshooting

| Issue                        | Solution                                                   |
| ---------------------------- | ---------------------------------------------------------- |
| "gradlew: command not found" | Run `chmod +x gradlew` on Linux/Mac                        |
| Tests fail to compile        | Check Java version: `java -version` (need 11+)             |
| Import errors in IDE         | Run `./gradlew build` first to download dependencies       |
| Tests don't run              | Verify JUnit 5 is in classpath via `./gradlew test --info` |

---

## Resources

- [Java Documentation](https://docs.oracle.com/javase/11/)
- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [Gradle Documentation](https://docs.gradle.org/)
- [Java Concurrency](https://docs.oracle.com/javase/tutorial/essential/concurrency/)

---

## License

This project is for educational purposes in the PDC course.
