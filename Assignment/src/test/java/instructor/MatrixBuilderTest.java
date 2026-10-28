package instructor;

import AbstractMath.Rings.Structures.Matrix.Builder;
import AbstractMath.Rings.Structures.Matrix.Matrix;
import AbstractMath.Rings.Structures.Matrix.ErrorStrings;
import AbstractMath.Rings.Structures.Polynomial.Polynomial;
import instructor.PolynomialCases.IntegerCoefficient;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static instructor.PolynomialCases.polynomial;
import static org.junit.jupiter.api.Assertions.*;

class MatrixBuilderTest {
    @Test
    void everyEntryMustBeSetBeforeBuildIncludingZeros() {
        Builder<IntegerCoefficient> builder = new Builder<>(2, 2);
        assertIncomplete(builder);
        builder.setValue(1, 1, new IntegerCoefficient(1));
        assertIncomplete(builder);
        builder.setValue(0, 0, new IntegerCoefficient(1));
        assertIncomplete(builder);
        builder.setValue(1, 0, new IntegerCoefficient(0));
        assertIncomplete(builder);
        builder.setValue(0, 1, new IntegerCoefficient(0));
        var matrix = builder.build();
        assertEquals(2, matrix.getRows());
        assertEquals(2, matrix.getColumns());
        assertEquals(matrix, matrix.one());
        assertEquals(matrix.zero(), matrix.add(matrix.negate()));
    }

    @ParameterizedTest
    @CsvSource({"0, 0", "-1, 1", "1, 0", "1, 2"})
    void builderValidatesDimensions(int rows, int columns) {
        var error = rows <= 0 || columns <= 0 ? ErrorStrings.NONPOSITIVE_DIMENSIONS : ErrorStrings.NOT_SQUARE;
        assertEquals(error.getMessage(), assertThrows(IllegalArgumentException.class,
                () -> new Builder<IntegerCoefficient>(rows, columns)).getMessage());
    }

    @Test
    void nullAndInvalidCoordinatesDoNotInitializeAnEntry() {
        var builder = Matrix.<IntegerCoefficient>builder(1, 1);
        assertEquals(ErrorStrings.NULL_ENTRY.getMessage(),
                assertThrows(NullPointerException.class, () -> builder.setValue(0, 0, null)).getMessage());
        assertEquals(ErrorStrings.INDEX_OUT_OF_BOUNDS.getMessage(),
                assertThrows(IndexOutOfBoundsException.class, () -> builder.setValue(1, 0, new IntegerCoefficient(1))).getMessage());
        assertIncomplete(builder);
    }

    @Test
    void builderReusePreservesNestedImmutableValues() {
        var polynomialBuilder = Polynomial.<IntegerCoefficient>builder(1);
        polynomialBuilder.setValue(0, new IntegerCoefficient(1));
        polynomialBuilder.setValue(1, new IntegerCoefficient(2));
        var matrixBuilder = Matrix.<Polynomial<IntegerCoefficient>>builder(1, 1);
        matrixBuilder.setValue(0, 0, polynomialBuilder.build());
        var original = matrixBuilder.build();
        polynomialBuilder.setValue(1, new IntegerCoefficient(3));
        matrixBuilder.setValue(0, 0, polynomialBuilder.build());
        assertEquals(polynomial(1, 2), original.getValue(0, 0));
        assertEquals(polynomial(1, 3), matrixBuilder.build().getValue(0, 0));
    }

    private static void assertIncomplete(Builder<?> builder) {
        assertEquals(ErrorStrings.INCOMPLETE.getMessage(),
                assertThrows(IllegalStateException.class, builder::build).getMessage());
    }
}
