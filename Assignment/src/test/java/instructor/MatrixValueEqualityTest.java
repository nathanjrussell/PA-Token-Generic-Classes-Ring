package instructor;

import AbstractMath.Rings.Structures.Matrix.Matrix;
import AbstractMath.Rings.ZMod.Mod3;
import AbstractMath.Rings.ZMod.ZModElement;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MatrixValueEqualityTest {
    @Test
    void equalityUsesDimensionsAndValues() {
        var element = new ZModElement<>(1, new Mod3());
        var first = new Matrix<>(1, 1, List.of(List.of(element)));
        var second = new Matrix<>(1, 1, List.of(List.of(new ZModElement<>(1, new Mod3()))));
        assertEquals(first, second);
        assertEquals(first.zero(), second.zero());
        assertNotEquals(first, second.zero());
        assertThrows(ClassCastException.class, () -> first.equals(null));
        assertThrows(ClassCastException.class, () -> first.equals(element));
        var larger = new Matrix<>(2, 2, List.of(List.of(element, element), List.of(element, element)));
        assertNotEquals(first, larger);
        assertNotEquals(larger, first);
    }
}
