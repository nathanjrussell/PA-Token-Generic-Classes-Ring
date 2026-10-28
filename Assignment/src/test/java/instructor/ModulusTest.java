package instructor;

import AbstractMath.Rings.RingElement;
import AbstractMath.Rings.ZMod.*;
import AbstractMath.Rings.Structures.Matrix.Matrix;
import AbstractMath.Rings.Structures.Polynomial.Polynomial;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ModulusTest {
    @Test
    void providedTypesHaveFixedModuli() {
        assertEquals(3, new Mod3().value());
        assertEquals(5, new Mod5().value());
    }

    @Test
    void elementsRejectMissingModuli() {
        assertThrows(NullPointerException.class, () -> new ZModElement<Mod3>(1, null));
    }

    @Test
    void separateModulusInstancesRepresentTheSameRing() {
        ZModElement<Mod3> first = new ZModElement<Mod3>(2, new Mod3());
        ZModElement<Mod3> second = new ZModElement<Mod3>(2, new Mod3());
        assertEquals(1, first.add(second).getValue());
        assertEquals(1, first.multiply(second).getValue());
    }
}
