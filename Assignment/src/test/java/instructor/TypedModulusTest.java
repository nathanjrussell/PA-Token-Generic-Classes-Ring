package instructor;

import AbstractMath.Rings.RingElement;
import AbstractMath.Rings.ZMod.*;
import AbstractMath.Rings.Structures.Matrix.Matrix;
import AbstractMath.Rings.Structures.Polynomial.Polynomial;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TypedModulusTest {
    @Test
    void modulusFiveArithmeticPreservesItsRing() {
        ZModElement<Mod5> a = new ZModElement<>(3, new Mod5());
        ZModElement<Mod5> b = new ZModElement<>(4, new Mod5());

        ZModElement<Mod5> sum = a.add(b);
        ZModElement<Mod5> product = a.multiply(b);
        assertEquals(2, sum.getValue());
        assertEquals(2, product.getValue());
        assertEquals(5, sum.getModulus());
        assertEquals(5, product.getModulus());
        assertEquals(a.zero(), a.add(a.negate()));
        assertEquals(a, a.multiply(a.one()));
    }

    @Test
    void typedMatrixUsesEntryOperationsAndIdentities() {
        ZModElement<Mod3> entry = new ZModElement<>(2, new Mod3());
        Matrix<ZModElement<Mod3>> matrix = new Matrix<>(1, 1, List.of(List.of(entry)));

        assertEquals(1, matrix.add(matrix).getValue(0, 0).getValue());
        assertEquals(1, matrix.multiply(matrix).getValue(0, 0).getValue());
        assertEquals(entry, matrix.multiply(matrix.one()).getValue(0, 0));
        assertEquals(entry.zero(), matrix.add(matrix.negate()).getValue(0, 0));
    }

    @Test
    void equalsStillChecksDifferentRingsAtRuntime() {
        ZModElement<Mod3> three = new ZModElement<>(1, new Mod3());
        ZModElement<Mod5> five = new ZModElement<>(1, new Mod5());

        assertThrows(IllegalArgumentException.class, () -> three.equals(five));
    }
}
