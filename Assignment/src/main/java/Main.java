import AbstractMath.Rings.ZMod.Mod3;
import AbstractMath.Rings.ZMod.Mod5;
import AbstractMath.Rings.ZMod.Modulus;
import AbstractMath.Rings.ZMod.ZModElement;

import java.util.List;
import java.util.StringJoiner;

public class Main {
    public static void main(String[] args) {

        Mod5 mod5 = new Mod5();

        /* This should not compile, generic type and modulus argument type must match
        Mod3 mod3_1 = new Mod3();
        ZModElement<Mod3> example = new ZModElement<>(1, mod5);
        Notice mod5 has type Mod5 but the template parameter utilizes type Mod3 - forces some type safety
        */

        Polynomial<ZModElement<Mod5>> p = new Polynomial<>(3, List.of(
                new ZModElement<>(1, mod5), new ZModElement<>(1, mod5),
                new ZModElement<>(-3, mod5), new ZModElement<>(-2, mod5)));
        Polynomial<ZModElement<Mod5>> q = new Polynomial<>(3, List.of(
                new ZModElement<>(7, mod5), new ZModElement<>(2, mod5),
                new ZModElement<>(3, mod5), new ZModElement<>(2, mod5)));

        System.out.println("Polynomials modulo 5, constant coefficient first");
        printPolynomial("p", p);
        printPolynomial("q", q);
        printPolynomial("sum", p.add(q));
        printPolynomial("p plus its negation", p.add(p.negate()));
        printPolynomial("zero", p.zero());
        printPolynomial("one", p.one());
        System.out.println("coefficient above degree = " + p.coefficient(100).getValue());

        Polynomial<ZModElement<Mod5>> a = new Polynomial<>(1, List.of(
                new ZModElement<>(1, mod5), new ZModElement<>(1, mod5)));
        var polynomialBuilder = Polynomial.<ZModElement<Mod5>>builder(1);
        polynomialBuilder.setValue(0, new ZModElement<>(1, mod5));
        polynomialBuilder.setValue(1, new ZModElement<>(2, mod5));
        Polynomial<ZModElement<Mod5>> b = polynomialBuilder.build();
        System.out.println("updated coefficient at degree 1 = " + b.getValue(1).getValue());
        printPolynomial("product", a.multiply(b));

        var matrixBuilder = Matrix.<Polynomial<ZModElement<Mod5>>>builder(1, 1);
        matrixBuilder.setValue(0, 0, a);
        Matrix<Polynomial<ZModElement<Mod5>>> matrixA = matrixBuilder.build();
        Matrix<Polynomial<ZModElement<Mod5>>> matrixB = new Matrix<>(1, 1, List.of(List.of(b)));
        printPolynomial("matrix product entry", matrixA.multiply(matrixB).getValue(0, 0));

        Polynomial<ZModElement<Mod3>> differentRing = new Polynomial<>(1, List.of(
                new ZModElement<>(1, new Mod3()), new ZModElement<>(1, new Mod3())));
        // a.add(differentRing) does not compile because the coefficient types differ.
        try {
            a.equals(differentRing);
        } catch (ClassCastException exception) {
            System.out.println(exception.getMessage());
        }
    }

    private static <M extends Modulus> void printPolynomial(String label, Polynomial<ZModElement<M>> polynomial) {
        StringJoiner coefficients = new StringJoiner(", ", "[", "]");
        for (int degree = 0; degree <= polynomial.degree(); degree++) {
            coefficients.add(Integer.toString(polynomial.getValue(degree).getValue()));
        }
        System.out.println(label + " degree = " + polynomial.degree());
        System.out.println(label + " coefficients = " + coefficients);
    }
}
