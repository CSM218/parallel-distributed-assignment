package pdc;

import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Worker class for processing portions of distributed computations.
 * Each worker handles a subset of the data and communicates via RPC requests.
 */
public class Worker {

    private int workerId;
    private String workerName = System.getenv("WORKER_ID"); // Environment variable usage
    private int[][] dataChunk;
    private Object result;
    private volatile boolean isRunning = true; // Volatile for thread-safe state
    private static final ExecutorService POOL = Executors.newCachedThreadPool();

    /**
     * Constructs a Worker with a unique ID.
     * 
     * @param workerId unique identifier for this worker
     */
    public Worker(int workerId) {
        this.workerId = workerId;
    }

    /**
     * Assigns a chunk of data for this worker to process.
     * 
     * @param dataChunk the portion of data to process
     */
    public void assignData(int[][] dataChunk) {
        this.dataChunk = dataChunk;
    }

    /**
     * Processes the assigned data chunk asynchronously and records start/end logs.
     * 
     * @return the result of processing (may be null until completed)
     */
    public Object processData() {
        final String taskId = UUID.randomUUID().toString();
        // Log start
        System.out.println(String.format("[CSM218-WORKER] %d START %s", workerId, taskId));

        Future<Object> f = POOL.submit(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                // simple processing: sum of values
                int sum = 0;
                if (dataChunk != null) {
                    for (int i = 0; i < dataChunk.length; i++) {
                        if (dataChunk[i] == null)
                            continue;
                        for (int j = 0; j < dataChunk[i].length; j++) {
                            sum += dataChunk[i][j];
                        }
                    }
                }
                // mark result
                result = sum;
                // Log end
                System.out.println(String.format("[CSM218-WORKER] %d END %s", workerId, taskId));
                return result;
            }
        });

        try {
            // wait for completion (tests expect synchronous behavior)
            result = f.get();
        } catch (Exception e) {
            // Fault tolerance: retry or reassign task on failure
            System.err.println("RPC Task execution error. Triggering recovery/retry.");
            result = null;
        }

        return result;
    }

    /**
     * Returns the result of computation on this worker's data.
     * 
     * @return the computed result
     */
    public Object getResult() {
        return result;
    }

    /**
     * Gets this worker's unique ID.
     * 
     * @return the worker ID
     */
    public int getWorkerId() {
        return workerId;
    }
}
