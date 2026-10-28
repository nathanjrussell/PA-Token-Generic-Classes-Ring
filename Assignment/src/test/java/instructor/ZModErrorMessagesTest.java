package instructor;

import AbstractMath.Rings.ZMod.ErrorStrings;
import AbstractMath.Rings.ZMod.Mod3;
import AbstractMath.Rings.ZMod.Mod5;
import AbstractMath.Rings.ZMod.ZModElement;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ZModErrorMessagesTest {
    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
            NULL_MODULUS | modulus must not be null
            INVALID_MODULUS | modulus must be greater than zero and less than 1,000
            INVALID_VALUE | value must be less than 1,000
            NULL_OPERAND | Other ZModElement must not be null
            MODULUS_MISMATCH | The modulus value of the two operands must be equal
            INCOMPATIBLE_TYPE | Cannot compare ZModElements with different types
            MODULUS_TYPE_MISMATCH | ZModElements can only be compared to ZModElements with the same modulus type
            """)
    void enumContainsTheRequiredStrings(ErrorStrings error, String expected) {
        assertEquals(expected, error.getMessage());
    }

    @Test
    void constructorUsesTheMatchingMessages() {
        expect(NullPointerException.class, ErrorStrings.NULL_MODULUS,
                () -> new ZModElement<>(1, null));
        for (int modulus : new int[]{Integer.MIN_VALUE, -1, 0, 1_000, Integer.MAX_VALUE}) {
            expect(IllegalArgumentException.class, ErrorStrings.INVALID_MODULUS,
                    () -> new ZModElement<>(1, new FixedModulus(modulus)));
        }
        for (int value : new int[]{1_000, 1_001, Integer.MAX_VALUE}) {
            expect(IllegalArgumentException.class, ErrorStrings.INVALID_VALUE,
                    () -> new ZModElement<>(value, new Mod3()));
        }
    }

    @Test
    void arithmeticUsesTheMatchingMessages() {
        var left = new ZModElement<>(1, new FixedModulus(3));
        var right = new ZModElement<>(1, new FixedModulus(5));
        expect(NullPointerException.class, ErrorStrings.NULL_OPERAND, () -> left.add(null));
        expect(NullPointerException.class, ErrorStrings.NULL_OPERAND, () -> left.multiply(null));
        expect(IllegalArgumentException.class, ErrorStrings.MODULUS_MISMATCH, () -> left.add(right));
        expect(IllegalArgumentException.class, ErrorStrings.MODULUS_MISMATCH, () -> left.multiply(right));
    }

    @Test
    void equalityUsesTheMatchingMessagesAndChecksNumericModuliFirst() {
        var element = new ZModElement<>(1, new Mod3());
        expect(ClassCastException.class, ErrorStrings.INCOMPATIBLE_TYPE, () -> element.equals(null));
        expect(ClassCastException.class, ErrorStrings.INCOMPATIBLE_TYPE, () -> element.equals("1"));
        expect(IllegalArgumentException.class, ErrorStrings.MODULUS_MISMATCH,
                () -> element.equals(new ZModElement<>(1, new Mod5())));
        expect(ClassCastException.class, ErrorStrings.MODULUS_TYPE_MISMATCH,
                () -> element.equals(new ZModElement<>(1, new FixedModulus(3))));
        expect(ClassCastException.class, ErrorStrings.MODULUS_TYPE_MISMATCH,
                () -> element.equals(new ZModElement<>(1, new Mod3()) { }));
    }

    private static <E extends Throwable> void expect(Class<E> type, ErrorStrings error, Executable operation) {
        assertEquals(error.getMessage(), assertThrows(type, operation).getMessage());
    }
}
