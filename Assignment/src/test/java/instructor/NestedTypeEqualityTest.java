package instructor;

import AbstractMath.Rings.RingElement;
import AbstractMath.Rings.Structures.Matrix.Matrix;
import AbstractMath.Rings.Structures.Polynomial.Polynomial;
import AbstractMath.Rings.ZMod.Mod3;
import AbstractMath.Rings.ZMod.Mod5;
import AbstractMath.Rings.ZMod.Modulus;
import AbstractMath.Rings.ZMod.ZModElement;
import instructor.PolynomialCases.IntegerCoefficient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import static instructor.PolynomialCases.polynomial;
import static org.junit.jupiter.api.Assertions.*;

class NestedTypeEqualityTest {
    private static final String MATRIX_TYPE_ERROR =
            "Matrices can only be compared to matrices with the same nested entry type";
    private static final String POLYNOMIAL_TYPE_ERROR =
            "Polynomials can only be compared to polynomials with the same nested coefficient type";

    @Test
    void defaultHelperChecksLeafClassesWithoutComparingValues() {
        RingElement<IntegerCoefficient> first = new IntegerCoefficient(1);
        assertTrue(first.hasSameType(new IntegerCoefficient(9)));
        assertFalse(first.hasSameType(new ZModElement<>(1, new Mod3())));
        assertFalse(first.hasSameType(null));
        assertFalse(first.hasSameType("integer"));
    }

    @Test
    void helperReturnsFalseWithoutThrowingForUnrelatedObjects() {
        RingElement<?> value = matrix(modular(new Mod3(), 1), 1);
        assertFalse(value.hasSameType(null));
        assertFalse(value.hasSameType("matrix"));
        assertFalse(value.hasSameType(modular(new Mod3(), 1)));
        assertFalse(value.hasSameType(Matrix.builder(1, 1)));
        assertTrue(value.hasSameType(matrix(modular(new Mod3(), 2), 1)));
    }

    @Test
    void deeplyNestedModulusMismatchThrowsTheOuterMatrixMessageInBothDirections() {
        Object threes = matrix(matrix(modular(new Mod3(), 1, 2), 1), 1);
        Object fives = matrix(matrix(modular(new Mod5(), 1, 2), 1), 1);
        assertTypeError(threes, fives, MATRIX_TYPE_ERROR);
        assertTypeError(fives, threes, MATRIX_TYPE_ERROR);
    }

    @Test
    void typeCheckPrecedesOuterAndInnerDimensionChecks() {
        var threes = matrix(matrix(modular(new Mod3(), 1), 1), 1);
        var largerOutside = matrix(matrix(modular(new Mod5(), 1), 1), 2);
        var largerInside = matrix(matrix(modular(new Mod5(), 1), 2), 1);
        assertTypeError(threes, largerOutside, MATRIX_TYPE_ERROR);
        assertTypeError(threes, largerInside, MATRIX_TYPE_ERROR);
        assertTypeError(largerOutside, threes, MATRIX_TYPE_ERROR);
        assertTypeError(largerInside, threes, MATRIX_TYPE_ERROR);
    }

    @Test
    void typeCheckPrecedesPolynomialDegreeAndValueChecks() {
        var threes = modular(new Mod3(), 1);
        var fives = modular(new Mod5(), 2, 1, 1);
        assertTypeError(threes, fives, POLYNOMIAL_TYPE_ERROR);
        assertTypeError(fives, threes, POLYNOMIAL_TYPE_ERROR);
        assertTypeError(matrix(matrix(threes, 1), 1), matrix(matrix(fives, 1), 1), MATRIX_TYPE_ERROR);
    }

    @Test
    void sameNestedTypesReturnBooleansForDifferentDimensionsDegreesAndValues() {
        var first = matrix(matrix(modular(new Mod3(), 1, 2), 1), 1);
        var equal = matrix(matrix(modular(new Mod3(), 1, 2), 1), 1);
        var otherValues = matrix(matrix(modular(new Mod3(), 2, 2), 1), 1);
        var otherDegree = matrix(matrix(modular(new Mod3(), 1), 1), 1);
        var otherInnerSize = matrix(matrix(modular(new Mod3(), 1, 2), 2), 1);
        var otherOuterSize = matrix(matrix(modular(new Mod3(), 1, 2), 1), 2);
        assertEquals(first, equal);
        for (var other : List.of(otherValues, otherDegree, otherInnerSize, otherOuterSize)) {
            assertTrue(first.hasSameType(other));
            assertFalse(first.equals(other));
            assertFalse(other.equals(first));
        }
    }

    @Test
    void wrongOuterTypeAndNullUseTheReceiversSpecificMessage() {
        var polynomial = modular(new Mod3(), 1);
        var matrix = matrix(polynomial, 1);
        assertTypeError(matrix, polynomial, MATRIX_TYPE_ERROR);
        assertTypeError(polynomial, matrix, POLYNOMIAL_TYPE_ERROR);
        assertTypeError(matrix, null, MATRIX_TYPE_ERROR);
        assertTypeError(polynomial, null, POLYNOMIAL_TYPE_ERROR);
        assertTypeError(matrix, "matrix", MATRIX_TYPE_ERROR);
        assertTypeError(polynomial, "polynomial", POLYNOMIAL_TYPE_ERROR);
    }

    @Test
    void differentNestingOrdersAndDepthsAreRejected() {
        var entry = new ZModElement<>(1, new Mod3());
        var matrixOfPolynomials = matrix(new Polynomial<>(List.of(entry)), 1);
        var polynomialOfMatrices = new Polynomial<>(List.of(matrix(entry, 1)));
        assertTypeError(matrixOfPolynomials, polynomialOfMatrices, MATRIX_TYPE_ERROR);
        assertTypeError(matrixOfPolynomials, matrix(matrixOfPolynomials, 1), MATRIX_TYPE_ERROR);
        assertTypeError(new Polynomial<>(List.of(matrixOfPolynomials)), polynomialOfMatrices, POLYNOMIAL_TYPE_ERROR);
    }

    private static final class OtherMod3 implements Modulus {
        public int value() { return 3; }
    }

    @Test
    void equalNumericModuliWithDifferentTokenClassesAreDifferentTypes() {
        var first = new ZModElement<>(1, new Mod3());
        var other = new ZModElement<>(1, new OtherMod3());
        assertFalse(first.hasSameType(other));
        assertThrows(ClassCastException.class, () -> first.equals(other));
        assertTypeError(new Polynomial<>(List.of(first)), new Polynomial<>(List.of(other)), POLYNOMIAL_TYPE_ERROR);
    }

    @Test
    void numericModulusStillMattersWhenTheTokenClassIsTheSame() {
        var threes = new ZModElement<>(1, new FixedModulus(3));
        var fives = new ZModElement<>(1, new FixedModulus(5));
        assertFalse(threes.hasSameType(fives));
        assertThrows(IllegalArgumentException.class, () -> threes.equals(fives));
        assertTypeError(matrix(threes, 1), matrix(fives, 1), MATRIX_TYPE_ERROR);
    }

    @Test
    void wideningADeclaredGenericTypeDoesNotChangeTheActualRuntimeType() {
        ZModElement<Modulus> broad = new ZModElement<>(1, new Mod3());
        ZModElement<Mod3> narrow = new ZModElement<>(1, new Mod3());
        assertTrue(broad.hasSameType(narrow));
        assertEquals(broad, narrow);
        assertTrue(new Polynomial<>(List.of(broad)).hasSameType(new Polynomial<>(List.of(narrow))));
    }

    @Test
    void allArithmeticResultsPreserveTheNestedType() {
        var first = matrix(matrix(modular(new Mod3(), 1, 2), 1), 1);
        for (var result : List.of(first.add(first), first.multiply(first), first.negate(), first.zero(), first.one())) {
            assertTrue(first.hasSameType(result));
        }
        assertEquals(first.zero(), first.add(first.negate()));
        assertEquals(first, first.multiply(first.one()));
    }

    @Test
    void matrixConstructorChecksLaterEntriesIncludingDeeperNestedTypes() {
        Modulus threes = new Mod3();
        Modulus fives = new Mod5();
        var first = matrix(modular(threes, 1), 1);
        var other = matrix(modular(fives, 1), 1);
        assertEquals("All matrix entries must have the same nested type",
                assertThrows(IllegalArgumentException.class, () -> new Matrix<>(2, 2,
                        List.of(List.of(first, first), List.of(first, other)))).getMessage());
    }

    @Test
    void polynomialConstructorChecksTrailingZerosBeforeDiscardingThem() {
        ZModElement<Modulus> first = new ZModElement<>(1, new Mod3());
        ZModElement<Modulus> otherZero = new ZModElement<>(0, new Mod5());
        assertEquals("All polynomial coefficients must have the same nested type",
                assertThrows(IllegalArgumentException.class, () -> new Polynomial<>(List.of(first, otherZero))).getMessage());
        assertEquals("All polynomial coefficients must have the same nested type",
                assertThrows(IllegalArgumentException.class, () -> new Polynomial<>(List.of(matrix(first, 1), matrix(otherZero, 1)))).getMessage());
    }

    @Test
    void buildersRejectMixedTypesAndCanBeCorrectedWithoutChangingPriorValues() {
        ZModElement<Modulus> three = new ZModElement<>(1, new Mod3());
        ZModElement<Modulus> five = new ZModElement<>(1, new Mod5());
        var polynomialBuilder = Polynomial.<ZModElement<Modulus>>builder(1);
        polynomialBuilder.setValue(0, three);
        polynomialBuilder.setValue(1, three);
        var originalPolynomial = polynomialBuilder.build();
        polynomialBuilder.setValue(1, five);
        assertEquals("All polynomial coefficients must have the same nested type",
                assertThrows(IllegalArgumentException.class, polynomialBuilder::build).getMessage());
        polynomialBuilder.setValue(0, five);
        var changedPolynomial = polynomialBuilder.build();
        assertFalse(originalPolynomial.hasSameType(changedPolynomial));
        assertEquals(3, originalPolynomial.coefficient(1).getModulus());

        var matrixBuilder = Matrix.<Polynomial<ZModElement<Modulus>>>builder(2, 2);
        for (int row = 0; row < 2; row++) {
            for (int column = 0; column < 2; column++) {
                matrixBuilder.setValue(row, column, originalPolynomial);
            }
        }
        var originalMatrix = matrixBuilder.build();
        matrixBuilder.setValue(1, 1, changedPolynomial);
        assertEquals("All matrix entries must have the same nested type",
                assertThrows(IllegalArgumentException.class, matrixBuilder::build).getMessage());
        matrixBuilder.setValue(1, 1, originalPolynomial);
        assertEquals(originalMatrix, matrixBuilder.build());
    }

    @Test
    void recursiveTypeChecksAlsoWorkForCustomCoefficientClasses() {
        var integers = polynomial(1, 2);
        var modular = modular(new Mod3(), 1, 2);
        assertTrue(integers.hasSameType(polynomial(9)));
        assertTypeError(integers, modular, POLYNOMIAL_TYPE_ERROR);
    }

    private static final class Box<T extends RingElement<T>> implements RingElement<Box<T>> {
        private final T value;

        Box(T value) { this.value = value; }

        public boolean hasSameType(Object other) {
            return other instanceof Box<?> box && value.hasSameType(box.value);
        }

        public Box<T> zero() { return new Box<>(value.zero()); }
        public Box<T> one() { return new Box<>(value.one()); }
        public Box<T> add(Box<T> other) { return new Box<>(value.add(other.value)); }
        public Box<T> negate() { return new Box<>(value.negate()); }
        public Box<T> multiply(Box<T> other) { return new Box<>(value.multiply(other.value)); }

        public boolean equals(Object other) {
            if (!hasSameType(other)) {
                throw new ClassCastException("Boxes must have matching nested types");
            }
            return value.equals(((Box<?>) other).value);
        }
    }

    @Test
    void recursiveChecksDelegateToCustomGenericEntryTypes() {
        var threes = new Box<>(new ZModElement<>(1, new Mod3()));
        var equalThrees = new Box<>(new ZModElement<>(1, new Mod3()));
        var fives = new Box<>(new ZModElement<>(1, new Mod5()));
        var first = matrix(new Polynomial<>(List.of(threes)), 1);
        var matching = matrix(new Polynomial<>(List.of(equalThrees)), 1);
        var incompatible = matrix(new Polynomial<>(List.of(fives)), 1);
        assertTrue(first.hasSameType(matching));
        assertEquals(first, matching);
        assertTypeError(first, incompatible, MATRIX_TYPE_ERROR);
        assertTypeError(incompatible, first, MATRIX_TYPE_ERROR);
        assertEquals(first.zero(), first.add(first.negate()));
    }

    private static void assertTypeError(Object left, Object right, String message) {
        assertFalse(((RingElement<?>) left).hasSameType(right));
        assertEquals(message, assertThrows(ClassCastException.class, () -> left.equals(right)).getMessage());
    }

    private static <M extends Modulus> Polynomial<ZModElement<M>> modular(M modulus, int... values) {
        return new Polynomial<>(Arrays.stream(values).mapToObj(value -> new ZModElement<>(value, modulus)).toList());
    }

    private static <T extends RingElement<T>> Matrix<T> matrix(T entry, int size) {
        List<List<T>> rows = new ArrayList<>();
        for (int row = 0; row < size; row++) {
            List<T> entries = new ArrayList<>();
            for (int column = 0; column < size; column++) {
                entries.add(entry);
            }
            rows.add(entries);
        }
        return new Matrix<>(size, size, rows);
    }
}
