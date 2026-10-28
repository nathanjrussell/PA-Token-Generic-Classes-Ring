package instructor;

import AbstractMath.Rings.Structures.Polynomial.Polynomial;
import instructor.PolynomialCases.IntegerCoefficient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static instructor.PolynomialCases.*;
import static org.junit.jupiter.api.Assertions.*;

class PolynomialRepresentationTest {
    @Test
    void constructorRemovesAllTrailingZeros() {
        assertCoefficients(polynomial(8, 3, 0, 0, 0), 8, 3);
    }

    @Test
    void interiorAndConstantZerosKeepTheirPositions() {
        assertCoefficients(polynomial(0, 0, 7, 0, 2, 0), 0, 0, 7, 0, 2);
    }

    @Test
    void zeroHasOneStoredCoefficientAndDegreeZero() {
        assertCoefficients(polynomial(0), 0);
        assertCoefficients(polynomial(0, 0, 0, 0), 0);
    }

    @Test
    void constantHasDegreeZero() {
        assertCoefficients(polynomial(-8), -8);
    }

    @Test
    void constructorCopiesTheListWithoutChangingItsContents() {
        ArrayList<IntegerCoefficient> input = new ArrayList<>(List.of(
                new IntegerCoefficient(8), new IntegerCoefficient(3), new IntegerCoefficient(0)));
        Polynomial<IntegerCoefficient> polynomial = new Polynomial<>(input);

        assertEquals(3, input.size(), "Trimming must not change the caller's list");
        input.set(0, new IntegerCoefficient(99));
        input.clear();
        assertCoefficients(polynomial, 8, 3);
    }

    @Test
    void constructorAcceptsAnUnmodifiableList() {
        Polynomial<IntegerCoefficient> polynomial = new Polynomial<>(List.of(
                new IntegerCoefficient(1), new IntegerCoefficient(0)));
        assertCoefficients(polynomial, 1);
    }

    @ParameterizedTest
    @ValueSource(ints = {3, 4, 100, Integer.MAX_VALUE})
    void coefficientAboveDegreeReturnsTheRingZero(int degree) {
        Polynomial<IntegerCoefficient> polynomial = polynomial(4, 1, 2);
        assertEquals(new IntegerCoefficient(0), polynomial.coefficient(degree));
        assertCoefficients(polynomial, 4, 1, 2);
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, -2, Integer.MIN_VALUE})
    void negativeCoefficientDegreeIsRejected(int degree) {
        assertThrows(IllegalArgumentException.class, () -> polynomial(1).coefficient(degree));
    }

    @Test
    void emptyInputIsRejectedBecauseThereIsNoCoefficientRingToUse() {
        assertThrows(IllegalArgumentException.class, () -> new Polynomial<IntegerCoefficient>(List.of()));
    }

    @Test
    void nullInputIsRejected() {
        assertThrows(NullPointerException.class, () -> new Polynomial<IntegerCoefficient>(null));
    }

    @Test
    void nullCoefficientsAreRejectedEvenWhenOtherTrailingEntriesAreZero() {
        for (int index = 0; index < 3; index++) {
            List<IntegerCoefficient> values = new ArrayList<>(Arrays.asList(
                    new IntegerCoefficient(0), new IntegerCoefficient(0), new IntegerCoefficient(0)));
            values.set(index, null);
            assertThrows(NullPointerException.class, () -> new Polynomial<>(values));
        }
    }
}
