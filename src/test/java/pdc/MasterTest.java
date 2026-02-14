package pdc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 tests for the Master class.
 * Tests matrix computation and worker coordination.
 */
class MasterTest {

    private Master master;

    @BeforeEach
    void setUp() {
        master = new Master();
    }

    @Test
    void testSumMatrix_SimpleCase() {
        int[][] matrix = {
                { 1, 2 },
                { 3, 4 }
        };
        int sum = master.sumMatrix(matrix);
        assertEquals(10, sum, "Matrix sum should be 10 for [[1,2],[3,4]]");
    }

    @Test
    void testSumMatrix_SingleElement() {
        int[][] matrix = { { 5 } };
        int sum = master.sumMatrix(matrix);
        assertEquals(5, sum, "Single element matrix sum should be 5");
    }

    @Test
    void testSumMatrix_ZeroMatrix() {
        int[][] matrix = {
                { 0, 0 },
                { 0, 0 }
        };
        int sum = master.sumMatrix(matrix);
        assertEquals(0, sum, "Zero matrix sum should be 0");
    }

    @Test
    void testMultiplyMatrices_IdentityMultiplication() {
        int[][] identity = {
                { 1, 0 },
                { 0, 1 }
        };
        int[][] matrix = {
                { 2, 3 },
                { 4, 5 }
        };
        int[][] result = master.multiplyMatrices(identity, matrix);
        assertNotNull(result, "Result should not be null");
        assertArrayEquals(matrix[0], result[0], "Identity multiplication should preserve matrix");
        assertArrayEquals(matrix[1], result[1], "Identity multiplication should preserve matrix");
    }

    @Test
    void testMultiplyMatrices_SimpleMultiplication() {
        int[][] matrixA = { { 1, 2 } }; // 1x2
        int[][] matrixB = { { 3 }, { 4 } }; // 2x1
        int[][] result = master.multiplyMatrices(matrixA, matrixB);
        assertNotNull(result, "Result should not be null");
        assertEquals(1, result.length, "Result should have 1 row");
        assertEquals(1, result[0].length, "Result should have 1 column");
        assertEquals(11, result[0][0], "1*3 + 2*4 = 11");
    }

    @Test
    void testProcessDistributed_ValidInput() {
        int[][] matrix = MatrixGenerator.generateRandomMatrix(4, 4, 100);
        Object result = master.processDistributed(matrix, 2);
        assertNotNull(result, "Distributed processing should return a result");
    }
}
