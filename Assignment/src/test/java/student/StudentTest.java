package student;

import AbstractMath.Rings.Structures.Polynomial.Polynomial;
import AbstractMath.Rings.ZMod.Mod5;
import AbstractMath.Rings.ZMod.ZModElement;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StudentTest {
    @Test
    void addingTheNegationProducesTheZeroPolynomial() {
        Mod5 modulus = new Mod5();
        Polynomial<ZModElement<Mod5>> polynomial = new Polynomial<>(List.of(
                new ZModElement<>(2, modulus), new ZModElement<>(3, modulus)));

        Polynomial<ZModElement<Mod5>> result = polynomial.add(polynomial.negate());

        assertEquals(0, result.degree());
        assertEquals(0, result.coefficient(0).getValue());
        assertEquals(0, result.coefficient(10).getValue());
    }
}
