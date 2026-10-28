package instructor;

import AbstractMath.Rings.Structures.Matrix.Matrix;
import AbstractMath.Rings.Structures.Matrix.ErrorStrings;
import AbstractMath.Rings.Structures.Polynomial.Polynomial;
import instructor.PolynomialCases.IntegerCoefficient;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class ErrorMessagesTest {
    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
            NONPOSITIVE_DIMENSIONS | Matrix dimensions must be greater than zero
            NOT_SQUARE | A ring matrix must be square
            NULL_VALUES | Default values must not be null
            NULL_ROW | Default row must not be null
            ROW_COUNT | Default values must match the number of rows
            COLUMN_COUNT | Each default row must match the number of columns
            NULL_ENTRY | Matrix entries must not be null
            INDEX_OUT_OF_BOUNDS | Matrix coordinates are outside the fixed dimensions
            NULL_OPERAND | Other matrix must not be null
            SIZE_MISMATCH | The matrices must have the same size
            INCOMPLETE | All matrix entries must be set before building
            MIXED_ENTRY_TYPES | All matrix entries must have the same nested type
            INCOMPATIBLE_TYPE | Matrices can only be compared to matrices with the same nested entry type
            """)
    void matrixEnumContainsTheRequiredStrings(ErrorStrings error, String expected) {
        assertEquals(expected, error.getMessage());
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
            INVALID_DEGREE | Degree must be nonnegative and degree + 1 must fit in an int
            NULL_COEFFICIENTS | Coefficients must not be null
            EMPTY_COEFFICIENTS | Supply at least the constant coefficient
            COEFFICIENT_COUNT | Supply exactly degree + 1 coefficients
            NULL_COEFFICIENT | Coefficients must not contain null
            NEGATIVE_TERM_DEGREE | Degree must not be negative
            TERM_OUT_OF_BOUNDS | Term degree is above the builder's fixed degree
            LEADING_ZERO | The leading coefficient cannot be zero because the degree would be reduced
            NULL_OPERAND | Other polynomial must not be null
            INCOMPLETE | All polynomial coefficients must be set before building
            MIXED_COEFFICIENT_TYPES | All polynomial coefficients must have the same nested type
            INCOMPATIBLE_TYPE | Polynomials can only be compared to polynomials with the same nested coefficient type
            """)
    void polynomialEnumContainsTheRequiredStrings(AbstractMath.Rings.Structures.Polynomial.ErrorStrings error, String expected) {
        assertEquals(expected, error.getMessage());
    }

    @Test
    void matrixConstructorsAndOperationsUseTheMatchingMessages() {
        var entry = new IntegerCoefficient(1);
        List<List<IntegerCoefficient>> values = List.of(List.of(entry));
        expect(IllegalArgumentException.class, ErrorStrings.NONPOSITIVE_DIMENSIONS.getMessage(),
                () -> new Matrix<>(0, 0, values));
        expect(IllegalArgumentException.class, ErrorStrings.NOT_SQUARE.getMessage(),
                () -> new Matrix<>(1, 2, values));
        expect(NullPointerException.class, ErrorStrings.NULL_VALUES.getMessage(),
                () -> new Matrix<IntegerCoefficient>(1, 1, null));
        expect(NullPointerException.class, ErrorStrings.NULL_ROW.getMessage(),
                () -> new Matrix<>(1, 1, Arrays.asList((List<IntegerCoefficient>) null)));
        expect(IllegalArgumentException.class, ErrorStrings.ROW_COUNT.getMessage(),
                () -> new Matrix<IntegerCoefficient>(1, 1, List.of()));
        expect(IllegalArgumentException.class, ErrorStrings.COLUMN_COUNT.getMessage(),
                () -> new Matrix<>(1, 1, List.of(List.of(entry, entry))));
        expect(NullPointerException.class, ErrorStrings.NULL_ENTRY.getMessage(),
                () -> new Matrix<>(1, 1, List.of(Arrays.asList((IntegerCoefficient) null))));
        var matrix = new Matrix<>(1, 1, values);
        expect(IndexOutOfBoundsException.class, ErrorStrings.INDEX_OUT_OF_BOUNDS.getMessage(),
                () -> matrix.getValue(0, 1));
        expect(NullPointerException.class, ErrorStrings.NULL_OPERAND.getMessage(), () -> matrix.add(null));
        expect(NullPointerException.class, ErrorStrings.NULL_OPERAND.getMessage(), () -> matrix.multiply(null));
        var larger = new Matrix<>(2, 2, List.of(List.of(entry, entry), List.of(entry, entry)));
        expect(IllegalArgumentException.class, ErrorStrings.SIZE_MISMATCH.getMessage(), () -> matrix.add(larger));
        expect(IllegalArgumentException.class, ErrorStrings.SIZE_MISMATCH.getMessage(), () -> matrix.multiply(larger));
    }

    @Test
    void polynomialConstructorsAndOperationsUseTheMatchingMessages() {
        var entry = new IntegerCoefficient(1);
        expect(IllegalArgumentException.class, AbstractMath.Rings.Structures.Polynomial.ErrorStrings.INVALID_DEGREE.getMessage(),
                () -> new Polynomial<>(-1, List.of(entry)));
        expect(NullPointerException.class, AbstractMath.Rings.Structures.Polynomial.ErrorStrings.NULL_COEFFICIENTS.getMessage(),
                () -> new Polynomial<IntegerCoefficient>(null));
        expect(IllegalArgumentException.class, AbstractMath.Rings.Structures.Polynomial.ErrorStrings.EMPTY_COEFFICIENTS.getMessage(),
                () -> new Polynomial<IntegerCoefficient>(List.of()));
        expect(IllegalArgumentException.class, AbstractMath.Rings.Structures.Polynomial.ErrorStrings.COEFFICIENT_COUNT.getMessage(),
                () -> new Polynomial<>(2, List.of(entry)));
        expect(NullPointerException.class, AbstractMath.Rings.Structures.Polynomial.ErrorStrings.NULL_COEFFICIENT.getMessage(),
                () -> new Polynomial<>(Arrays.asList(entry, null)));
        var polynomial = new Polynomial<>(List.of(entry));
        expect(IllegalArgumentException.class, AbstractMath.Rings.Structures.Polynomial.ErrorStrings.NEGATIVE_TERM_DEGREE.getMessage(),
                () -> polynomial.coefficient(-1));
        expect(IllegalArgumentException.class, AbstractMath.Rings.Structures.Polynomial.ErrorStrings.NEGATIVE_TERM_DEGREE.getMessage(),
                () -> polynomial.getValue(-1));
        expect(NullPointerException.class, AbstractMath.Rings.Structures.Polynomial.ErrorStrings.NULL_OPERAND.getMessage(), () -> polynomial.add(null));
        expect(NullPointerException.class, AbstractMath.Rings.Structures.Polynomial.ErrorStrings.NULL_OPERAND.getMessage(), () -> polynomial.multiply(null));
    }

    private static <E extends Throwable> void expect(Class<E> type, String message, Executable operation) {
        assertEquals(message, assertThrows(type, operation).getMessage());
    }
}
