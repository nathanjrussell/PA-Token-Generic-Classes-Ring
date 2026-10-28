package instructor;

import AbstractMath.Rings.Structures.Matrix.Matrix;
import AbstractMath.Rings.Structures.Polynomial.Polynomial;
import AbstractMath.Rings.ZMod.Mod3;
import AbstractMath.Rings.ZMod.Mod5;
import AbstractMath.Rings.ZMod.Modulus;
import AbstractMath.Rings.ZMod.ZModElement;
import instructor.PolynomialCases.IntegerCoefficient;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import static instructor.PolynomialCases.*;
import static org.junit.jupiter.api.Assertions.*;

class PolynomialGenericTest {
    private static <M extends Modulus> Polynomial<ZModElement<M>> modular(M modulus, int... values) {
        return new Polynomial<>(Arrays.stream(values)
                .mapToObj(value -> new ZModElement<>(value, modulus)).toList());
    }

    private static <M extends Modulus> void assertModular(Polynomial<ZModElement<M>> polynomial,
                                                         int modulus, int... expected) {
        assertEquals(expected.length - 1, polynomial.degree());
        assertEquals(expected.length, storage(polynomial).size());
        for (int index = 0; index < expected.length; index++) {
            assertEquals(expected[index], polynomial.coefficient(index).getValue());
            assertEquals(modulus, polynomial.coefficient(index).getModulus());
        }
        assertEquals(0, polynomial.coefficient(Integer.MAX_VALUE).getValue());
        assertEquals(modulus, polynomial.coefficient(Integer.MAX_VALUE).getModulus());
    }

    @Test
    void constructorRecognizesZeroByValueRatherThanObjectIdentity() {
        assertModular(modular(new Mod5(), 1, 2, 5, 10, 0), 5, 1, 2);
    }

    @Test
    void modularCoefficientsCanReduceTheExampleFurther() {
        var mod3 = new Mod3();
        var left = modular(mod3, 1, 1, -3, -2);
        var right = modular(mod3, 7, 2, 3, 2);
        assertModular(left.add(right), 3, 2);

        var mod5 = new Mod5();
        assertModular(modular(mod5, 1, 1, -3, -2).add(modular(mod5, 7, 2, 3, 2)), 5, 3, 3);
    }

    @Test
    void modularMultiplicationAndIdentitiesUseCoefficientOperations() {
        var mod5 = new Mod5();
        var left = modular(mod5, 4, 3, 2);
        var right = modular(mod5, 2, 4);
        assertModular(left.multiply(right), 5, 8 % 5, 22 % 5, 16 % 5, 8 % 5);
        assertModular(left.negate(), 5, 1, 2, 3);
        assertModular(left.zero(), 5, 0);
        assertModular(left.one(), 5, 1);
        assertModular(left.multiply(left.zero()), 5, 0);
    }

    private static final class Mod6 implements Modulus {
        public int value() { return 6; }
    }

    @Test
    void multiplicationTrimsZeroLeadingProducts() {
        var mod6 = new Mod6();
        assertModular(modular(mod6, 1, 2).multiply(modular(mod6, 1, 3)), 6, 1, 5);
        assertModular(modular(mod6, 0, 2).multiply(modular(mod6, 0, 3)), 6, 0);
    }

    @Test
    void oneCanEqualZeroInTheCoefficientRing() {
        Modulus mod1 = new FixedModulus(1);
        var polynomial = modular(mod1, 1, 2, 3);
        assertModular(polynomial, 1, 0);
        assertModular(polynomial.zero(), 1, 0);
        assertModular(polynomial.one(), 1, 0);
    }

    @Test
    void broaderTypeArgumentsStillAllowRuntimeModulusErrors() {
        Polynomial<ZModElement<Modulus>> left = modular(new FixedModulus(3), 1, 2);
        Polynomial<ZModElement<Modulus>> right = modular(new FixedModulus(5), 1, 2);
        assertThrows(IllegalArgumentException.class, () -> left.add(right));
        assertThrows(IllegalArgumentException.class, () -> left.multiply(right));
        assertThrows(ClassCastException.class, () -> left.equals(right));
    }

    private static Matrix<ZModElement<Mod3>> matrix(int a, int b, int c, int d) {
        Mod3 modulus = new Mod3();
        return new Matrix<>(2, 2, List.of(
                List.of(new ZModElement<>(a, modulus), new ZModElement<>(b, modulus)),
                List.of(new ZModElement<>(c, modulus), new ZModElement<>(d, modulus))));
    }

    @Test
    void matrixCoefficientsAreReducedUsingMatrixValueEquality() {
        var coefficient = matrix(1, 2, 0, 1);
        var polynomial = new Polynomial<>(List.of(coefficient, coefficient.zero(), coefficient.zero()));
        assertEquals(0, polynomial.degree());
        assertEquals(1, storage(polynomial).size());
        assertEquals(coefficient, polynomial.coefficient(0));
        assertEquals(coefficient.zero(), polynomial.coefficient(100));
        assertEquals(2, polynomial.coefficient(100).getRows());
        assertEquals(coefficient.one(), polynomial.one().coefficient(0));
    }

    @Test
    void multiplicationPreservesTheOrderOfNoncommutingCoefficients() {
        var a = matrix(1, 2, 0, 1);
        var b = matrix(2, 0, 1, 1);
        var left = new Polynomial<>(List.of(a.zero(), a));
        var right = new Polynomial<>(List.of(b.zero(), b));
        var product = left.multiply(right);
        assertEquals(2, product.degree());
        assertEquals(3, storage(product).size());
        assertEquals(a.zero(), product.coefficient(0));
        assertEquals(a.zero(), product.coefficient(1));
        assertEquals(matrix(1, 2, 1, 1), product.coefficient(2));
        assertEquals(matrix(2, 1, 1, 0), right.multiply(left).coefficient(2));
        assertEquals(a, left.coefficient(1));
        assertEquals(b, right.coefficient(1));
    }

    @Test
    void matrixCoefficientCancellationProducesOneZeroEntry() {
        var a = matrix(1, 2, 0, 1);
        var b = matrix(2, 0, 1, 1);
        var polynomial = new Polynomial<>(List.of(a, b));
        var zero = polynomial.add(polynomial.negate());
        assertEquals(0, zero.degree());
        assertEquals(1, storage(zero).size());
        assertEquals(a.zero(), zero.coefficient(0));
    }

    @Test
    void polynomialsCanBeCoefficientsOfOtherPolynomials() {
        var inner = polynomial(1, 2);
        Polynomial<Polynomial<IntegerCoefficient>> outer = new Polynomial<>(List.of(inner, inner.zero()));
        assertEquals(0, outer.degree());
        assertEquals(1, storage(outer).size());
        assertEquals(inner.zero(), outer.coefficient(20));
        assertEquals(inner.one(), outer.one().coefficient(0));
        assertEquals(inner.add(inner), outer.add(outer).coefficient(0));
        assertEquals(polynomial(1, 4, 4), outer.multiply(outer).coefficient(0));
        assertEquals(outer.zero(), outer.add(outer.negate()));
    }

    @Test
    void matricesCanHavePolynomialEntries() {
        var p = polynomial(1, 1);
        var q = polynomial(1, 2);
        Matrix<Polynomial<IntegerCoefficient>> a = new Matrix<>(1, 1, List.of(List.of(p)));
        Matrix<Polynomial<IntegerCoefficient>> b = new Matrix<>(1, 1, List.of(List.of(q)));
        assertCoefficients(a.multiply(b).getValue(0, 0), 1, 3, 2);
        assertCoefficients(a.zero().getValue(0, 0), 0);
        assertCoefficients(a.one().getValue(0, 0), 1);
        assertEquals(a, a.multiply(a.one()));
    }
}
