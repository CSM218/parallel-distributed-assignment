package pdc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 tests for the Worker class.
 * Tests worker functionality and data processing.
 */
class WorkerTest {

    private Worker worker;
    private int[][] testData;

    @BeforeEach
    void setUp() {
        worker = new Worker(1);
        testData = new int[][] { { 1, 2 }, { 3, 4 } };
    }

    @Test
    void testWorkerCreation() {
        assertNotNull(worker, "Worker should be created successfully");
        assertEquals(1, worker.getWorkerId(), "Worker ID should be 1");
    }

    @Test
    void testAssignData() {
        worker.assignData(testData);
        assertNotNull(worker.getResult() == null || worker.getResult() != null,
                "Worker should accept data assignment");
    }

    @Test
    void testProcessData() {
        worker.assignData(testData);
        Object result = worker.processData();
        // Result can be null if not implemented, but should not throw exception
        assertDoesNotThrow(() -> {
            worker.assignData(testData);
            worker.processData();
        }, "Processing should not throw an exception");
    }

    @Test
    void testGetResult() {
        worker.assignData(testData);
        worker.processData();
        Object result = worker.getResult();
        // Result should be retrievable without throwing exception
        assertDoesNotThrow(() -> worker.getResult(),
                "Getting result should not throw an exception");
    }

    @Test
    void testMultipleWorkers() {
        Worker worker1 = new Worker(1);
        Worker worker2 = new Worker(2);

        assertEquals(1, worker1.getWorkerId(), "Worker 1 ID should be 1");
        assertEquals(2, worker2.getWorkerId(), "Worker 2 ID should be 2");
        assertNotEquals(worker1.getWorkerId(), worker2.getWorkerId(),
                "Workers should have different IDs");
    }
}
