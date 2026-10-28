package instructor;

import AbstractMath.Rings.RingElement;
import AbstractMath.Rings.Structures.Polynomial.Polynomial;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

final class PolynomialCases {
    private PolynomialCases() { }

    record IntegerCoefficient(int value) implements RingElement<IntegerCoefficient> {
        public IntegerCoefficient zero() { return new IntegerCoefficient(0); }
        public IntegerCoefficient one() { return new IntegerCoefficient(1); }
        public IntegerCoefficient add(IntegerCoefficient other) { return new IntegerCoefficient(value + other.value); }
        public IntegerCoefficient negate() { return new IntegerCoefficient(-value); }
        public IntegerCoefficient multiply(IntegerCoefficient other) { return new IntegerCoefficient(value * other.value); }
    }

    static Polynomial<IntegerCoefficient> polynomial(int... values) {
        return new Polynomial<>(Arrays.stream(values).mapToObj(IntegerCoefficient::new).toList());
    }

    static Polynomial<IntegerCoefficient> polynomial(String values) {
        return polynomial(Arrays.stream(values.split(" ")).mapToInt(Integer::parseInt).toArray());
    }

    static List<?> storage(Polynomial<?> polynomial) {
        var fields = Arrays.stream(Polynomial.class.getDeclaredFields())
                .filter(field -> !Modifier.isStatic(field.getModifiers()) && field.getType() == ArrayList.class)
                .toList();
        assertEquals(1, fields.size(), "Store the coefficients in one ArrayList field");
        var field = fields.get(0);
        assertTrue(Modifier.isPrivate(field.getModifiers()), "The coefficient list must be private");
        assertTrue(Modifier.isFinal(field.getModifiers()), "The coefficient list reference must be final");
        try {
            field.setAccessible(true);
            return (ArrayList<?>) field.get(polynomial);
        } catch (IllegalAccessException exception) {
            throw new AssertionError(exception);
        }
    }

    static void assertCoefficients(Polynomial<IntegerCoefficient> polynomial, int... expected) {
        assertEquals(expected.length - 1, polynomial.degree(), "degree");
        assertEquals(expected.length, storage(polynomial).size(), "stored coefficient count");
        for (int degree = 0; degree < expected.length; degree++) {
            assertEquals(new IntegerCoefficient(expected[degree]), polynomial.coefficient(degree));
        }
        assertEquals(new IntegerCoefficient(0), polynomial.coefficient(expected.length));
    }
}
