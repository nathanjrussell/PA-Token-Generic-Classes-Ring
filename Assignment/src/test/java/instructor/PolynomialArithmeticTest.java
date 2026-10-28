package instructor;

import AbstractMath.Rings.RingElement;
import AbstractMath.Rings.Structures.Polynomial.Polynomial;
import instructor.PolynomialCases.IntegerCoefficient;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static instructor.PolynomialCases.*;
import static org.junit.jupiter.api.Assertions.*;

class PolynomialArithmeticTest {
    @Test
    void theAssignmentExampleCancelsBothHighestTerms() {
        var left = polynomial(1, 1, -3, -2);
        var right = polynomial(7, 2, 3, 2);
        assertCoefficients(left.add(right), 8, 3);
        assertCoefficients(right.add(left), 8, 3);
    }

    @ParameterizedTest
    @CsvSource({
            "1 2 3, 4, 5 2 3",
            "4, 1 2 3, 5 2 3",
            "1 2 3, -1 -2 -3, 0",
            "0, 1 0 3, 1 0 3",
            "0 2, 5 -2, 5"
    })
    void additionUsesCorrespondingPowers(String left, String right, String expected) {
        assertEquals(polynomial(expected), polynomial(left).add(polynomial(right)));
        assertEquals(polynomial(expected).degree() + 1, storage(polynomial(left).add(polynomial(right))).size());
    }

    @ParameterizedTest
    @CsvSource({
            "1 2 3, 4 5, 4 13 22 15",
            "-1 1, 1 1, -1 0 1",
            "0 0 2, 0 3, 0 0 0 6",
            "1 2 3, 0, 0",
            "0, 1 2 3, 0",
            "-2, 3, -6",
            "1 2 3, 1, 1 2 3"
    })
    void multiplicationAccumulatesProductsAtTheSumOfTheirDegrees(String left, String right, String expected) {
        var product = polynomial(left).multiply(polynomial(right));
        var answer = polynomial(expected);
        assertEquals(answer, product);
        assertEquals(answer.degree() + 1, storage(product).size());
    }

    @Test
    void negationChangesEveryCoefficient() {
        var original = polynomial(4, 0, -2, 1);
        assertCoefficients(original.negate(), -4, 0, 2, -1);
        assertEquals(original, original.negate().negate());
        assertCoefficients(original.add(original.negate()), 0);
    }

    @Test
    void identitiesAreConstantPolynomialsInTheSameCoefficientRing() {
        var original = polynomial(3, -2, 5);
        assertCoefficients(original.zero(), 0);
        assertCoefficients(original.one(), 1);
        assertEquals(original, original.add(original.zero()));
        assertEquals(original, original.zero().add(original));
        assertEquals(original, original.multiply(original.one()));
        assertEquals(original, original.one().multiply(original));
        assertCoefficients(original.zero().multiply(original), 0);
    }

    @Test
    void operationsCreateNewResultsWithoutChangingOperands() {
        var left = polynomial(1, 1, -3, -2);
        var right = polynomial(7, 2, 3, 2);
        List<Polynomial<IntegerCoefficient>> results = List.of(
                left.add(right), left.multiply(right), left.negate(), left.zero(), left.one());
        for (var result : results) {
            assertNotSame(left, result);
            assertNotSame(right, result);
            assertNotSame(storage(left), storage(result));
            assertNotSame(storage(right), storage(result));
        }
        assertCoefficients(left, 1, 1, -3, -2);
        assertCoefficients(right, 7, 2, 3, 2);
    }

    @Test
    void nullOperandsAreRejected() {
        var polynomial = polynomial(1, 2);
        assertThrows(NullPointerException.class, () -> polynomial.add(null));
        assertThrows(NullPointerException.class, () -> polynomial.multiply(null));
    }

    @Test
    void operationsWorkThroughTheGenericInterface() {
        RingElement<Polynomial<IntegerCoefficient>> element = polynomial(1, 2);
        assertCoefficients(element.add(polynomial(3)), 4, 2);
        assertCoefficients(element.multiply(polynomial(0, 1)), 0, 1, 2);
        assertCoefficients(element.negate(), -1, -2);
        assertCoefficients(element.zero(), 0);
        assertCoefficients(element.one(), 1);
    }

    @Test
    void repeatedCancellationKeepsStorageReduced() {
        var result = polynomial(1, 2, 3, 4);
        for (int degree = 3; degree >= 1; degree--) {
            int[] cancellation = new int[degree + 1];
            cancellation[degree] = -(degree + 1);
            result = result.add(polynomial(cancellation));
            assertEquals(degree - 1, result.degree());
            assertEquals(degree, storage(result).size());
        }
        assertCoefficients(result, 1);
    }

    @Test
    void additionAndMultiplicationObeyTheSharedRingContract() {
        var a = polynomial(1, 0, 2);
        var b = polynomial(-1, 3);
        var c = polynomial(2, 2, -2);
        assertEquals(a.add(b), b.add(a));
        assertEquals(a.add(b).add(c), a.add(b.add(c)));
        assertEquals(a.multiply(b).multiply(c), a.multiply(b.multiply(c)));
        assertEquals(a.multiply(b.add(c)), a.multiply(b).add(a.multiply(c)));
        assertEquals(a.add(b).multiply(c), a.multiply(c).add(b.multiply(c)));
    }

    @Test
    void equalityComparesReducedCoefficientValues() {
        assertEquals(polynomial(1, 2), polynomial(1, 2, 0, 0));
        assertEquals(polynomial(0), polynomial(0, 0));
        assertNotEquals(polynomial(1, 2), polynomial(1, 3));
        assertNotEquals(polynomial(1, 2), polynomial(1, 2, 1));
        assertThrows(ClassCastException.class, () -> polynomial(1).equals(null));
        assertThrows(ClassCastException.class, () -> polynomial(1).equals("1"));
    }
}
