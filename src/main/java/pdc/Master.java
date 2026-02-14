package pdc;

/**
 * Master class for distributed matrix computation.
 * Coordinates with Worker instances to perform parallel computations.
 */
public class Master {

    /**
     * Computes the sum of all elements in a matrix.
     * 
     * @param matrix the input matrix
     * @return the sum of all elements
     */
    public int sumMatrix(int[][] matrix) {
        // TODO: Implement matrix sum computation
        return 0;
    }

    /**
     * Computes the product of two matrices.
     * 
     * @param matrixA first matrix
     * @param matrixB second matrix
     * @return the product matrix A * B
     */
    public int[][] multiplyMatrices(int[][] matrixA, int[][] matrixB) {
        // TODO: Implement matrix multiplication
        return null;
    }

    /**
     * Distributes work to workers and aggregates results.
     * 
     * @param matrix     the matrix to process
     * @param numWorkers number of workers to use
     * @return the processed result
     */
    public Object processDistributed(int[][] matrix, int numWorkers) {
        // TODO: Implement distributed processing with workers
        return null;
    }
}
