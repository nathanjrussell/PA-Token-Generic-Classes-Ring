package instructor;

import AbstractMath.Rings.RingElement;
import AbstractMath.Rings.Structures.Matrix.Builder;
import AbstractMath.Rings.Structures.Matrix.Matrix;
import AbstractMath.Rings.Structures.Polynomial.Polynomial;
import AbstractMath.Rings.ZMod.Mod3;
import AbstractMath.Rings.ZMod.Mod5;
import AbstractMath.Rings.ZMod.Modulus;
import AbstractMath.Rings.ZMod.ZModElement;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

import org.junit.jupiter.api.Test;

import static instructor.PolynomialCases.storage;
import static org.junit.jupiter.api.Assertions.*;

class DeeplyNestedRingTest {
    // Three 2 by 2 matrix layers can be viewed as one 8 by 8 matrix.
    // Each fixture row contains a flat row, flat column, then polynomial coefficients.
    // Omitted cells contain the zero polynomial. Expected results are calculated modulo 5.
    private static final int[][] LEFT = {
            {0, 1, 1, 1}, {0, 4, 2, 1}, {3, 2, 0, 1}, {6, 7, 1, 2}, {7, 0, 4}
    };
    private static final int[][] RIGHT = {
            {1, 6, 1, 2}, {4, 6, 3, 3}, {2, 0, 2, 1}, {7, 3, 4, 1}, {0, 7, 1, 1}
    };
    private static final int[][] CANCELLING = {
            {0, 1, 4, 4}, {0, 4, 1, 4}, {3, 2, 2, 4},
            {6, 7, 4, 3}, {7, 0, 3}, {5, 5, 2, 1}
    };

    @Test
    void additionAndNegationHaveExactCoefficientsAndReduceCancelledTerms() {
        var left = nested(new Mod5(), LEFT);
        var right = nested(new Mod5(), CANCELLING);

        assertEntries(left.add(right), new int[][]{
                {0, 4, 3}, {3, 2, 2}, {7, 0, 2}, {5, 5, 2, 1}
        });
        assertEntries(left.negate(), new int[][]{
                {0, 1, 4, 4}, {0, 4, 3, 4}, {3, 2, 0, 4}, {6, 7, 4, 3}, {7, 0, 1}
        });
        assertEntries(left, LEFT);
        assertEntries(right, CANCELLING);
    }

    @Test
    void multiplicationAccumulatesAcrossBlocksAndPreservesOperandOrder() {
        var left = nested(new Mod5(), LEFT);
        var right = nested(new Mod5(), RIGHT);

        // At (0, 6), (1 + x)(1 + 2x) + (2 + x)(3 + 3x) reduces to 2 + 2x.
        assertEntries(left.multiply(right), new int[][]{
                {0, 6, 2, 2}, {3, 0, 0, 2, 1}, {6, 3, 4, 4, 2}, {7, 7, 4, 4}
        });
        assertEntries(right.multiply(left), new int[][]{
                {1, 7, 1, 4, 4}, {4, 7, 3, 4, 1}, {2, 1, 2, 3, 1},
                {2, 4, 4, 4, 1}, {7, 2, 0, 4, 1}, {0, 0, 4, 4}
        });
        assertEntries(left, LEFT);
        assertEntries(right, RIGHT);
    }

    @Test
    void allRingOperationsWorkThroughTheInterfaceWithIdentitiesOnBothSides() {
        var value = nested(new Mod5(), LEFT);
        RingElement<Matrix<Matrix<Matrix<Polynomial<ZModElement<Mod5>>>>>> ring = value;
        var zero = ring.zero();
        var one = ring.one();
        int[][] diagonal = new int[8][];
        for (int index = 0; index < 8; index++) {
            diagonal[index] = new int[]{index, index, 1};
        }
        assertEntries(zero, new int[0][]);
        assertEntries(one, diagonal);
        assertEntries(ring.add(zero), LEFT);
        assertEntries(zero.add(value), LEFT);
        assertEntries(ring.multiply(one), LEFT);
        assertEntries(one.multiply(value), LEFT);
        assertEntries(ring.multiply(zero), new int[0][]);
        assertEntries(zero.multiply(value), new int[0][]);
        assertEntries(ring.add(ring.negate()), new int[0][]);
        assertEntries(ring.negate().add(value), new int[0][]);
        assertEntries(ring.negate().negate(), LEFT);
        for (var result : List.of(zero, one, ring.add(value), ring.multiply(value), ring.negate())) {
            assertTrue(ring.hasSameType(result));
            assertNotSame(value, result);
        }
        assertEntries(value, LEFT);
    }

    @Test
    void additionAndMultiplicationObeyRingLawsAtAllThreeMatrixLayers() {
        var a = nested(new Mod5(), LEFT);
        var b = nested(new Mod5(), RIGHT);
        var c = nested(new Mod5(), CANCELLING);

        assertEquals(a.add(b), b.add(a));
        assertEquals(a.add(b).add(c), a.add(b.add(c)));
        assertEquals(a.multiply(b).multiply(c), a.multiply(b.multiply(c)));
        assertEquals(a.multiply(b.add(c)), a.multiply(b).add(a.multiply(c)));
        assertEquals(a.add(b).multiply(c), a.multiply(c).add(b.multiply(c)));
        assertEntries(a, LEFT);
        assertEntries(b, RIGHT);
        assertEntries(c, CANCELLING);
    }

    @Test
    void reusingBuildersAtEveryLayerDoesNotChangeTheCompletedNestedValue() {
        var polynomialBuilder = Polynomial.<ZModElement<Mod5>>builder(1);
        polynomialBuilder.setValue(0, new ZModElement<>(1, new Mod5()));
        polynomialBuilder.setValue(1, new ZModElement<>(2, new Mod5()));
        var polynomial = polynomialBuilder.build();
        var innerBuilder = filledBuilder(polynomial);
        var inner = innerBuilder.build();
        var middleBuilder = filledBuilder(inner);
        var middle = middleBuilder.build();
        var outerBuilder = filledBuilder(middle);
        var original = outerBuilder.build();

        polynomialBuilder.setValue(1, new ZModElement<>(4, new Mod5()));
        innerBuilder.setValue(1, 1, polynomial.zero());
        middleBuilder.setValue(1, 1, inner.zero());
        outerBuilder.setValue(1, 1, middle.zero());

        assertNotEquals(polynomial, polynomialBuilder.build());
        assertNotEquals(inner, innerBuilder.build());
        assertNotEquals(middle, middleBuilder.build());
        assertNotEquals(original, outerBuilder.build());
        int[][] expected = new int[64][];
        for (int row = 0; row < 8; row++) {
            for (int column = 0; column < 8; column++) {
                expected[8 * row + column] = new int[]{row, column, 1, 2};
            }
        }
        assertEntries(original, expected);
    }

    @Test
    void equalityChecksValuesAndFindsAModulusMismatchThroughAllThreeLayers() {
        var first = nested(new Mod5(), LEFT);
        var equal = nested(new Mod5(), LEFT);
        var differentValues = nested(new Mod5(), RIGHT);
        var differentModulus = nested(new Mod3(), LEFT);

        assertTrue(first.hasSameType(equal));
        assertEquals(first, equal);
        assertEquals(equal, first);
        assertTrue(first.hasSameType(differentValues));
        assertNotEquals(first, differentValues);
        assertNotEquals(differentValues, first);
        assertFalse(first.hasSameType(differentModulus));
        assertFalse(differentModulus.hasSameType(first));
        String message = "Matrices can only be compared to matrices with the same nested entry type";
        assertEquals(message, assertThrows(ClassCastException.class,
                () -> first.equals(differentModulus)).getMessage());
        assertEquals(message, assertThrows(ClassCastException.class,
                () -> differentModulus.equals(first)).getMessage());
    }

    private static <M extends Modulus> Matrix<Matrix<Matrix<Polynomial<ZModElement<M>>>>> nested(
            M modulus, int[][] entries) {
        return matrix((outerRow, outerColumn) -> matrix((middleRow, middleColumn) ->
                matrix((innerRow, innerColumn) -> {
                    int row = 4 * outerRow + 2 * middleRow + innerRow;
                    int column = 4 * outerColumn + 2 * middleColumn + innerColumn;
                    return new Polynomial<>(Arrays.stream(coefficientsAt(entries, row, column))
                            .mapToObj(value -> new ZModElement<>(value, modulus)).toList());
                })));
    }

    private static <T extends RingElement<T>> Matrix<T> matrix(BiFunction<Integer, Integer, T> entry) {
        return new Matrix<>(2, 2, List.of(
                List.of(entry.apply(0, 0), entry.apply(0, 1)),
                List.of(entry.apply(1, 0), entry.apply(1, 1))));
    }

    private static <T extends RingElement<T>> Builder<T> filledBuilder(T entry) {
        Builder<T> builder = Matrix.builder(2, 2);
        for (int row = 0; row < 2; row++) {
            for (int column = 0; column < 2; column++) {
                builder.setValue(row, column, entry);
            }
        }
        return builder;
    }

    private static int[] coefficientsAt(int[][] entries, int row, int column) {
        for (int[] entry : entries) {
            if (entry[0] == row && entry[1] == column) {
                return Arrays.copyOfRange(entry, 2, entry.length);
            }
        }
        return new int[]{0};
    }

    // Inspect every coefficient directly so the arithmetic checks do not depend on equals.
    private static void assertEntries(Matrix<Matrix<Matrix<Polynomial<ZModElement<Mod5>>>>> actual,
                                      int[][] expected) {
        assertEquals(2, actual.getRows());
        assertEquals(2, actual.getColumns());
        for (int row = 0; row < 8; row++) {
            for (int column = 0; column < 8; column++) {
                String cell = "flat cell (" + row + ", " + column + ")";
                var middle = actual.getValue(row / 4, column / 4);
                assertEquals(2, middle.getRows(), cell);
                assertEquals(2, middle.getColumns(), cell);
                var inner = middle.getValue(row / 2 % 2, column / 2 % 2);
                assertEquals(2, inner.getRows(), cell);
                assertEquals(2, inner.getColumns(), cell);
                var polynomial = inner.getValue(row % 2, column % 2);
                int[] coefficients = coefficientsAt(expected, row, column);
                assertEquals(coefficients.length - 1, polynomial.degree(), cell);
                assertEquals(coefficients.length, storage(polynomial).size(), cell);
                for (int degree = 0; degree < coefficients.length; degree++) {
                    assertEquals(coefficients[degree], polynomial.coefficient(degree).getValue(), cell);
                    assertEquals(5, polynomial.coefficient(degree).getModulus(), cell);
                    assertEquals(polynomial.coefficient(degree), polynomial.getValue(degree), cell);
                }
                assertEquals(0, polynomial.coefficient(Integer.MAX_VALUE).getValue(), cell);
                assertEquals(5, polynomial.coefficient(Integer.MAX_VALUE).getModulus(), cell);
            }
        }
    }
}
