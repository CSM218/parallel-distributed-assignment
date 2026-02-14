package pdc;

/**
 * Worker class for processing portions of distributed computations.
 * Each worker handles a subset of the data.
 */
public class Worker {

    private int workerId;
    private int[][] dataChunk;

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
     * Processes the assigned data chunk.
     * 
     * @return the result of processing
     */
    public Object processData() {
        // TODO: Implement processing logic for assigned data
        return null;
    }

    /**
     * Returns the result of computation on this worker's data.
     * 
     * @return the computed result
     */
    public Object getResult() {
        // TODO: Implement result retrieval
        return null;
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
