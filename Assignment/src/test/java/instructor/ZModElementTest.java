package instructor;

import AbstractMath.Rings.RingElement;
import AbstractMath.Rings.ZMod.*;
import AbstractMath.Rings.Structures.Matrix.Matrix;
import AbstractMath.Rings.Structures.Polynomial.Polynomial;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ZModElementTest {

    @ParameterizedTest
    @CsvSource({
            "3, 5, 3",
            "8, 5, 3",
            "-6, 5, 4",
            "-10, 5, 0",
            "0, 5, 0",
            "999, 999, 0",
            "998, 999, 998",
            "999, 998, 1",
            "7, 1, 0",
            "-1000, 7, 1",
            "-2147483648, 5, 2"
    })
    void constructorNormalizesValueAndPreservesModulus(int value, int modulus, int expected) {
        ZModElement<Modulus> element = new ZModElement<>(value, new FixedModulus(modulus));

        assertElement(expected, modulus, element);
    }

    @ParameterizedTest
    @ValueSource(ints = {Integer.MIN_VALUE, -1, 0, 1_000, Integer.MAX_VALUE})
    void constructorRejectsModulusOutsideAllowedRange(int modulus) {
        assertThrows(IllegalArgumentException.class, () -> new ZModElement<>(1, new FixedModulus(modulus)));
    }

    @ParameterizedTest
    @ValueSource(ints = {1_000, 1_001, Integer.MAX_VALUE})
    void constructorRejectsInputValuesAtOrAboveOneThousand(int value) {
        assertThrows(IllegalArgumentException.class, () -> new ZModElement<>(value, new FixedModulus(5)));
    }

    @ParameterizedTest
    @CsvSource({
            "2, 3, 7, 5",
            "4, 3, 5, 2",
            "-6, 3, 5, 2",
            "998, 998, 999, 997",
            "4, 0, 5, 4",
            "1, 1, 1, 0"
    })
    void addReturnsSumModuloModulus(int leftValue, int rightValue, int modulus, int expected) {
        ZModElement<Modulus> left = new ZModElement<>(leftValue, new FixedModulus(modulus));
        ZModElement<Modulus> right = new ZModElement<>(rightValue, new FixedModulus(modulus));

        ZModElement<Modulus> result = left.add(right);

        assertElement(expected, modulus, result);
    }

    @ParameterizedTest
    @CsvSource({
            "2, 3, 7, 6",
            "4, 3, 5, 2",
            "-6, 3, 5, 2",
            "998, 998, 999, 1",
            "4, 0, 5, 0",
            "4, 1, 5, 4",
            "1, 1, 1, 0"
    })
    void multiplyReturnsProductModuloModulus(int leftValue, int rightValue, int modulus, int expected) {
        ZModElement<Modulus> left = new ZModElement<>(leftValue, new FixedModulus(modulus));
        ZModElement<Modulus> right = new ZModElement<>(rightValue, new FixedModulus(modulus));

        ZModElement<Modulus> result = left.multiply(right);

        assertElement(expected, modulus, result);
    }

    @ParameterizedTest
    @CsvSource({
            "2, 5, 3",
            "0, 5, 0",
            "-6, 5, 1",
            "998, 999, 1",
            "1, 1, 0"
    })
    void negateReturnsAdditiveInverse(int value, int modulus, int expected) {
        ZModElement<Modulus> element = new ZModElement<>(value, new FixedModulus(modulus));

        ZModElement<Modulus> result = element.negate();

        assertElement(expected, modulus, result);
    }

    @Test
    void addRejectsDifferentModuli() {
        ZModElement<Modulus> left = new ZModElement<>(2, new FixedModulus(5));
        ZModElement<Modulus> right = new ZModElement<>(3, new FixedModulus(7));

        assertThrows(IllegalArgumentException.class, () -> left.add(right));
    }

    @Test
    void multiplyRejectsDifferentModuli() {
        ZModElement<Modulus> left = new ZModElement<>(2, new FixedModulus(5));
        ZModElement<Modulus> right = new ZModElement<>(3, new FixedModulus(7));

        assertThrows(IllegalArgumentException.class, () -> left.multiply(right));
    }

    @Test
    void addingNegationProducesZero() {
        ZModElement<Modulus> element = new ZModElement<>(998, new FixedModulus(999));

        ZModElement<Modulus> result = element.add(element.negate());

        assertElement(0, 999, result);
    }

    @Test
    void arithmeticDoesNotChangeOperands() {
        ZModElement<Modulus> left = new ZModElement<>(-6, new FixedModulus(5));
        ZModElement<Modulus> right = new ZModElement<>(3, new FixedModulus(5));

        left.add(right);
        left.multiply(right);
        left.negate();

        assertElement(4, 5, left);
        assertElement(3, 5, right);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 5, 999})
    void zeroIsAdditiveIdentityInSameRing(int modulus) {
        ZModElement<Modulus> element = new ZModElement<>(-1, new FixedModulus(modulus));

        ZModElement<Modulus> zero = element.zero();

        assertElement(0, modulus, zero);
        assertEquals(element, element.add(zero));
        assertEquals(element, zero.add(element));
    }

    @ParameterizedTest
    @CsvSource({"1, 0", "2, 1", "5, 1", "999, 1"})
    void oneIsMultiplicativeIdentityInSameRing(int modulus, int expectedValue) {
        ZModElement<Modulus> element = new ZModElement<>(-1, new FixedModulus(modulus));

        ZModElement<Modulus> one = element.one();

        assertElement(expectedValue, modulus, one);
        assertEquals(element, element.multiply(one));
        assertEquals(element, one.multiply(element));
    }

    @ParameterizedTest
    @CsvSource({
            "3, 3, 7, true",
            "3, 10, 7, true",
            "3, 4, 7, false",
            "-6, 4, 5, true",
            "0, 999, 999, true",
            "0, 1, 1, true"
    })
    void equalsComparesNormalizedValuesInSameRing(int leftValue, int rightValue, int modulus, boolean expected) {
        Object left = new ZModElement<>(leftValue, new FixedModulus(modulus));
        Object right = new ZModElement<>(rightValue, new FixedModulus(modulus));

        assertEquals(expected, left.equals(right));
        assertEquals(expected, right.equals(left));
    }

    @Test
    void equalsRecognizesSameObject() {
        ZModElement<Modulus> element = new ZModElement<>(3, new FixedModulus(7));

        assertTrue(element.equals(element));
    }

    @Test
    void equalsRejectsNull() {
        ZModElement<Modulus> element = new ZModElement<>(3, new FixedModulus(7));

        assertThrows(ClassCastException.class, () -> element.equals(null));
    }

    @Test
    void equalsRejectsOtherTypes() {
        ZModElement<Modulus> element = new ZModElement<>(3, new FixedModulus(7));

        assertThrows(ClassCastException.class, () -> element.equals(Integer.valueOf(3)));
        assertThrows(ClassCastException.class, () -> element.equals("3"));
        assertThrows(ClassCastException.class, () -> element.equals(new Object()));
    }

    @ParameterizedTest
    @CsvSource({"3, 3, 7, 5", "3, 4, 7, 5", "0, 0, 1, 5"})
    void equalsRejectsDifferentRings(int leftValue, int rightValue, int leftModulus, int rightModulus) {
        Object left = new ZModElement<>(leftValue, new FixedModulus(leftModulus));
        Object right = new ZModElement<>(rightValue, new FixedModulus(rightModulus));

        assertThrows(IllegalArgumentException.class, () -> left.equals(right));
        assertThrows(IllegalArgumentException.class, () -> right.equals(left));
    }

    @Test
    void genericInterfaceCanNegateTheElement() {
        ZModElement<Modulus> receiver = new ZModElement<>(2, new FixedModulus(3));
        RingElement<ZModElement<Modulus>> element = receiver;

        ZModElement<Modulus> result = element.negate();

        assertElement(1, 3, result);
        assertElement(2, 3, receiver);
    }

    private static void assertElement(int expectedValue, int expectedModulus, ZModElement<Modulus> actual) {
        assertEquals(expectedValue, actual.getValue(), "value");
        assertEquals(expectedModulus, actual.getModulus(), "modulus");
    }
}
