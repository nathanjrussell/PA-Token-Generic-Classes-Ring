package instructor;

import AbstractMath.Rings.Structures.Polynomial.Builder;
import AbstractMath.Rings.Structures.Polynomial.Polynomial;
import AbstractMath.Rings.Structures.Polynomial.ErrorStrings;
import AbstractMath.Rings.Structures.Matrix.Matrix;
import AbstractMath.Rings.ZMod.Mod3;
import AbstractMath.Rings.ZMod.Mod5;
import AbstractMath.Rings.ZMod.ZModElement;
import instructor.PolynomialCases.IntegerCoefficient;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static instructor.PolynomialCases.*;
import static org.junit.jupiter.api.Assertions.*;

class PolynomialBuilderTest {
    @Test
    void everyCoefficientMustBeSetBeforeBuildIncludingInteriorZeros() {
        Builder<IntegerCoefficient> builder = new Builder<>(3);
        assertIncomplete(builder);
        builder.setValue(3, new IntegerCoefficient(2));
        assertIncomplete(builder);
        builder.setValue(0, new IntegerCoefficient(8));
        assertIncomplete(builder);
        builder.setValue(2, new IntegerCoefficient(0));
        assertIncomplete(builder);
        builder.setValue(1, new IntegerCoefficient(0));
        assertCoefficients(builder.build(), 8, 0, 0, 2);
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, Integer.MIN_VALUE, Integer.MAX_VALUE})
    void invalidDegreesAreRejected(int degree) {
        assertEquals(ErrorStrings.INVALID_DEGREE.getMessage(),
                assertThrows(IllegalArgumentException.class, () -> new Builder<IntegerCoefficient>(degree)).getMessage());
        assertThrows(IllegalArgumentException.class, () -> Polynomial.<IntegerCoefficient>builder(degree));
    }

    @Test
    void settersRejectInvalidDegreesAndNullWithoutChangingExistingValues() {
        var builder = Polynomial.<IntegerCoefficient>builder(1);
        builder.setValue(0, new IntegerCoefficient(8));
        builder.setValue(1, new IntegerCoefficient(3));
        assertEquals(ErrorStrings.NEGATIVE_TERM_DEGREE.getMessage(),
                assertThrows(IllegalArgumentException.class, () -> builder.setValue(-1, new IntegerCoefficient(1))).getMessage());
        for (int degree : new int[]{2, 100, Integer.MAX_VALUE}) {
            assertEquals(ErrorStrings.TERM_OUT_OF_BOUNDS.getMessage(),
                    assertThrows(IndexOutOfBoundsException.class, () -> builder.setValue(degree, new IntegerCoefficient(0))).getMessage());
        }
        assertEquals(ErrorStrings.NULL_COEFFICIENT.getMessage(),
                assertThrows(NullPointerException.class, () -> builder.setValue(1, null)).getMessage());
        assertCoefficients(builder.build(), 8, 3);
    }

    @Test
    void leadingZeroIsRejectedBeforeAndAfterInitialization() {
        var builder = Polynomial.<IntegerCoefficient>builder(3);
        assertEquals(ErrorStrings.LEADING_ZERO.getMessage(),
                assertThrows(IllegalArgumentException.class, () -> builder.setValue(3, new IntegerCoefficient(0))).getMessage());
        assertIncomplete(builder);
        for (int degree = 0; degree <= 3; degree++) {
            builder.setValue(degree, new IntegerCoefficient(degree + 1));
        }
        assertThrows(IllegalArgumentException.class, () -> builder.setValue(3, new IntegerCoefficient(0)));
        assertCoefficients(builder.build(), 1, 2, 3, 4);
    }

    @Test
    void zeroConstantIsAllowedAndStillMustBeExplicitlySet() {
        var builder = Polynomial.<IntegerCoefficient>builder(0);
        assertIncomplete(builder);
        builder.setValue(0, new IntegerCoefficient(0));
        var zero = builder.build();
        assertCoefficients(zero, 0);
        assertEquals(zero, zero.zero());
        assertCoefficients(zero.one(), 1);
    }

    @Test
    void builderReuseDoesNotChangeBuiltValuesOrArithmeticResults() {
        var builder = Polynomial.<IntegerCoefficient>builder(1);
        builder.setValue(0, new IntegerCoefficient(1));
        builder.setValue(1, new IntegerCoefficient(2));
        var first = builder.build();
        var sum = first.add(first);
        var product = first.multiply(first);
        builder.setValue(0, new IntegerCoefficient(8));
        builder.setValue(1, new IntegerCoefficient(3));
        assertCoefficients(first, 1, 2);
        assertCoefficients(sum, 2, 4);
        assertCoefficients(product, 1, 4, 4);
        assertCoefficients(builder.build(), 8, 3);
    }

    @Test
    void getterReadsTermsAndReturnsZeroAboveDegreeWithoutGrowingStorage() {
        var polynomial = polynomial(8, 0, 3);
        for (int degree : new int[]{0, 1, 2, 3, 100, Integer.MAX_VALUE}) {
            assertEquals(polynomial.coefficient(degree), polynomial.getValue(degree));
        }
        assertThrows(IllegalArgumentException.class, () -> polynomial.getValue(-1));
        assertCoefficients(polynomial, 8, 0, 3);
    }

    @Test
    void suppliedCoefficientListsAreCopiedAndAutomaticallyReduced() {
        var input = new ArrayList<>(List.of(new IntegerCoefficient(8), new IntegerCoefficient(3),
                new IntegerCoefficient(0), new IntegerCoefficient(0)));
        var polynomial = new Polynomial<>(3, input);
        assertCoefficients(polynomial, 8, 3);
        assertEquals(polynomial, new Polynomial<>(input));
        input.clear();
        assertCoefficients(polynomial, 8, 3);
        assertThrows(IllegalArgumentException.class, () -> new Polynomial<>(2, List.of(new IntegerCoefficient(1))));
        assertThrows(IllegalArgumentException.class, () -> new Polynomial<IntegerCoefficient>(0, List.of()));
        assertThrows(NullPointerException.class, () -> new Polynomial<IntegerCoefficient>(0, null));
    }

    @Test
    void arithmeticMayReduceTheDegreeEvenThoughTheBuilderPreservesIt() {
        var builder = Polynomial.<IntegerCoefficient>builder(3);
        builder.setValue(0, new IntegerCoefficient(1));
        builder.setValue(1, new IntegerCoefficient(1));
        builder.setValue(2, new IntegerCoefficient(-3));
        builder.setValue(3, new IntegerCoefficient(-2));
        assertCoefficients(builder.build().add(polynomial(7, 2, 3, 2)), 8, 3);
        assertCoefficients(builder.build().add(builder.build().negate()), 0);
    }

    @Test
    void leadingZeroCheckUsesNormalizedModularValueEquality() {
        var modulus = new Mod5();
        var builder = Polynomial.<ZModElement<Mod5>>builder(1);
        builder.setValue(0, new ZModElement<>(1, modulus));
        assertThrows(IllegalArgumentException.class, () -> builder.setValue(1, new ZModElement<>(5, modulus)));
        assertIncomplete(builder);
        builder.setValue(1, new ZModElement<>(2, modulus));
        assertEquals(2, builder.build().getValue(1).getValue());
    }

    @Test
    void buildersWorkWithMatrixAndPolynomialCoefficients() {
        var entry = new ZModElement<>(1, new Mod3());
        var matrix = new Matrix<>(1, 1, List.of(List.of(entry)));
        var matrixCoefficients = Polynomial.<Matrix<ZModElement<Mod3>>>builder(1);
        matrixCoefficients.setValue(0, matrix.zero());
        assertThrows(IllegalArgumentException.class, () -> matrixCoefficients.setValue(1, matrix.zero()));
        matrixCoefficients.setValue(1, matrix);
        assertEquals(matrix, matrixCoefficients.build().getValue(1));

        var polynomialCoefficients = Polynomial.<Polynomial<IntegerCoefficient>>builder(1);
        polynomialCoefficients.setValue(0, polynomial(0));
        assertThrows(IllegalArgumentException.class, () -> polynomialCoefficients.setValue(1, polynomial(0)));
        polynomialCoefficients.setValue(1, polynomial(1, 2));
        assertEquals(polynomial(1, 2), polynomialCoefficients.build().getValue(1));
    }

    private static void assertIncomplete(Builder<?> builder) {
        assertEquals(ErrorStrings.INCOMPLETE.getMessage(),
                assertThrows(IllegalStateException.class, builder::build).getMessage());
    }
}
