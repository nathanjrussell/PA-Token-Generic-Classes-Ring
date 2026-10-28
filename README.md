# CSE 220 Generic Classes

The `solutions` branch contains the reference implementation. The `main` branch
contains starter code and a `Main` usage example. The sections below describe the
student assignment.

## What this assignment evaluates

The student repository will include generated JavaDoc at its root. This assignment
tests your ability to use JavaDoc or Doxygen generated documentation to understand
an API and implement it. Examine the supplied documentation to understand the
package structure. This assignment requires the following knowledge and skills

- Reading and applying generated API documentation
- Generic classes and interfaces
- Bounds on type parameters
- Nested generic types
- Using an interface without depending on a concrete implementation
- Encapsulation, equality, and collection invariants
- Compile time checks and runtime validation
- Overloading and overriding methods
- Immutable values and separate builders

## Project manager statement

> As an application developer, I need generic matrices and polynomials that work
> with different ring element types so that I can compose these structures
> without rewriting their arithmetic for each type.

## Acceptance Criteria

Complete `RingElement`, `Modulus`, `Mod3`, `Mod5`, `ZModElement`, `Matrix`,
`Polynomial`, their builders and error enums using the generated documentation.
The starter code is incomplete. Match the documented packages and public signatures.
`Main` is supplied as a usage example and is not tested.
Use Java 24 and Maven. Use course examples and the documentation to complete the assignment.

The completed project should have this layout.

```text
Assignment/
    pom.xml
    src/main/java/
        Main.java
        AbstractMath/
            Rings/
                RingElement.java
                ZMod/
                    Modulus.java
                    Mod3.java
                    Mod5.java
                    ZModElement.java
                    ErrorStrings.java
                Structures/
                    Matrix/
                        Matrix.java
                        Builder.java
                        ErrorStrings.java
                    Polynomial/
                        Polynomial.java
                        Builder.java
                        ErrorStrings.java
    src/test/java/
        instructor/
        student/
            StudentTest.java
```

Key points to notice

- `Main` stays in the default package
- `Matrix`, its `Builder`, and `ErrorStrings` belong to `AbstractMath.Rings.Structures.Matrix`
- `Polynomial`, its `Builder`, and `ErrorStrings` belong to `AbstractMath.Rings.Structures.Polynomial`
- The modular arithmetic classes and their `ErrorStrings` enum belong to `AbstractMath.Rings.ZMod`
- `RingElement` belongs to `AbstractMath.Rings`

## Interfaces and generic classes

- `Modulus` is a non-generic interface declaring `value()`
- `Mod3` and `Mod5` are final, non-generic classes implementing `Modulus`
- `RingElement<T>` is a generic interface declaring the common ring operations
- `ZModElement<M extends Modulus>` implements `RingElement<ZModElement<M>>`
- `Matrix` is a generic class that bounds the type parameter to a `RingElement` and implements the generic `RingElement` interface.
- `Polynomial` is a generic class that bounds the type parameter to a `RingElement` and implements the generic `RingElement` interface.
- The Matrix and Polynomial packages each have a separate generic `Builder<T extends RingElement<T>>` class. Builders do not implement `RingElement`.
- The ZMod, Matrix, and Polynomial packages each have an `ErrorStrings` enum containing their exact validation messages.

The bounds give each generic class access to the operations it needs. When the coefficients of a polynomial or the entries of a matrix are ring elements, then the structures become ring elements themselves.

Understand this allows for nested structures.  Below is an example.

`Matrix<Matrix<Matrix<Polynomial<ZModElement<Mod3>>>>>` - A Matrix whose entries are other matrices, whose entries are other matrices, whose entries are polynomials with coefficients of type `ZModElement<Mod3>`.


## Methods

All constructors and methods listed below are public. Arithmetic and identity
methods return new elements without changing their operands. `negate()` takes no
argument and returns the additive inverse, so adding an element to its negation
produces zero. `Matrix` and `Polynomial` are final immutable classes with getters
and no setters. Each separate `Builder` class has setters and `build()`, but no
value getters or ring operations. Builders begin with null slots. Every slot must
be set before `build()` can return a usable value.

Building copies the builder's storage. Reusing a builder must not change any
previously built value. Entries and coefficients must themselves be immutable
ring elements. The generic bound cannot enforce immutability for a custom type.

### RingElement

Declare the generic interface `RingElement<T>`. Here `T` is the type accepted and
returned by the operations.

| Method | Required behavior |
| --- | --- |
| `T zero()` | Return the additive identity in the same ring |
| `T one()` | Return the multiplicative identity in the same ring |
| `T add(T other)` | Return the sum |
| `T negate()` | Return the additive inverse |
| `T multiply(T other)` | Return the product in operand order |
| `boolean hasSameType(Object other)` | Provide a default method that returns true for the same concrete class and false for null or another class |

Implementing classes must also provide value equality through `equals(Object)`
so that structures can recognize zero elements. Classes with nested elements or
ring configuration override `hasSameType` to check those details. The check
compares types without calling the elements' value equality methods.

The recursion follows the actual objects. A matrix checks its first entry against
the other matrix's first entry. A polynomial checks its constant coefficient
against the other polynomial's constant coefficient. A modular element checks
its concrete element class, modulus token class, and numeric modulus.

One representative entry is sufficient because construction verifies that all
entries have matching nested types. Matrices are nonempty, polynomials always
have a constant coefficient, and completed values are immutable. Custom classes
with nested entries must override `hasSameType` to continue this recursion.

Each structure's `equals` calls `hasSameType` first. A mismatch throws
`ClassCastException` with that structure's `INCOMPATIBLE_TYPE` message. Only
matching nested types proceed to a true or false value comparison. This check
comes before comparing dimensions, degrees, or entries.

* Two polynomials are equal if they have the same coefficients term for term.
* Two matrices are equal if they have the same dimensions and identical entries componentwise.
* Two `ZModElement` objects are equal if their concrete element and modulus token types, numeric moduli, and modded values agree.

### Modulus

Declare the non-generic interface `Modulus`.

| Method | Required behavior |
| --- | --- |
| `int value()` | Return a fixed integer greater than zero and less than 1000 |

Implementations must be immutable. Each instance of a concrete modulus class
must represent the same modulus.

### Mod3

Declare the final class `Mod3` implementing `Modulus`.

| Constructor or method | Required behavior |
| --- | --- |
| `Mod3()` | Create a modulus object with no arguments |
| `int value()` | Return 3 |

### Mod5

Declare the final class `Mod5` implementing `Modulus`.

| Constructor or method | Required behavior |
| --- | --- |
| `Mod5()` | Create a modulus object with no arguments |
| `int value()` | Return 5 |

### ZModElement

Store an immutable normalized integer value and its modulus object of type `M`.

| Constructor or method | Required behavior |
| --- | --- |
| `ZModElement(int value, M modulus)` | Validate the inputs and normalize the value into the range from zero through modulus minus one |
| `int getValue()` | Return the normalized value |
| `int getModulus()` | Return the numeric modulus |
| `boolean hasSameType(Object other)` | Check the concrete element class, modulus token class, and numeric modulus without comparing values |
| `ZModElement<M> add(ZModElement<M> other)` | Add the values and reduce using the same modulus |
| `ZModElement<M> negate()` | Return the additive inverse with the same modulus |
| `ZModElement<M> multiply(ZModElement<M> other)` | Multiply the values and reduce using the same modulus |
| `ZModElement<M> zero()` | Return zero with the same modulus |
| `ZModElement<M> one()` | Return one reduced using the same modulus |
| `boolean equals(Object other)` | Compare normalized values after checking the concrete types and numeric moduli |

The input value must be less than 1000 and may be any negative `int`. The modulus
must be greater than zero and less than 1000. Reject out of range inputs with
`IllegalArgumentException` and a null modulus with `NullPointerException`.
Use integer arithmetic, `%`, and an explicit correction for a negative remainder.
Reduce arithmetic results before passing them to the constructor. Modulo 1,
both identities have value zero.

`add`, `multiply`, and `equals` throw `IllegalArgumentException` for different
numeric moduli. Arithmetic with a null operand throws `NullPointerException`.
For this assignment, `equals` throws `ClassCastException` for null, a non
`ZModElement` object, or mismatched concrete element or modulus token classes.
Separate instances of the same modulus class and numeric value may be compared.
Use named modulus classes consistently. Equal numeric values alone do not make
different token classes compatible.

### ZMod ErrorStrings

Store these exact strings in the enum in `AbstractMath.Rings.ZMod`.
`String getMessage()` returns the stored message. Use the corresponding message
when throwing a validation exception.

| Constant | Message |
| --- | --- |
| `NULL_MODULUS` | modulus must not be null |
| `INVALID_MODULUS` | modulus must be greater than zero and less than 1,000 |
| `INVALID_VALUE` | value must be less than 1,000 |
| `NULL_OPERAND` | Other ZModElement must not be null |
| `MODULUS_MISMATCH` | The modulus value of the two operands must be equal |
| `INCOMPATIBLE_TYPE` | Cannot compare ZModElements with different types |
| `MODULUS_TYPE_MISMATCH` | ZModElements can only be compared to ZModElements with the same modulus type |

`add` and `multiply` use `NULL_OPERAND` for null operands. `equals` uses
`INCOMPATIBLE_TYPE` for null or a nonmodular object. When comparing two modular
elements, check their numeric moduli first using `MODULUS_MISMATCH`. If the
moduli match but their concrete element or modulus token classes differ, use
`MODULUS_TYPE_MISMATCH`.

### Matrix

Store fixed positive square dimensions and a private `List<List<T>>`. Copy the
outer list and each row so that changing a caller's list cannot resize or alter
the matrix. Entry objects themselves are shared.

| Constructor or method | Required behavior |
| --- | --- |
| `Matrix(int rows, int columns, List<List<T>> defaultValues)` | Validate the dimensions and copy the initial rows |
| `static <T extends RingElement<T>> Builder<T> builder(int rows, int columns)` | Create a builder in the matrix package with the requested dimensions |
| `int getRows()` | Return the fixed row count |
| `int getColumns()` | Return the fixed column count |
| `boolean hasSameType(Object other)` | Require another matrix and recursively compare the types of their entries at row zero and column zero |
| `T getValue(int row, int column)` | Return the entry at the zero based coordinates |
| `Matrix<T> add(Matrix<T> other)` | Add corresponding entries |
| `Matrix<T> negate()` | Negate every entry |
| `Matrix<T> multiply(Matrix<T> other)` | Compute each entry as the sum of products from a left row and a right column |
| `Matrix<T> zero()` | Return a matrix of the same size filled with entry zeros |
| `Matrix<T> one()` | Return a matrix of the same size with entry ones on the diagonal and zeros elsewhere |
| `boolean equals(Object other)` | Require matching nested types, then compare dimensions and corresponding entries |

Reject nonpositive or nonsquare dimensions and incorrectly sized lists with
`IllegalArgumentException`. Reject null lists, rows, entries, or arithmetic
operands with `NullPointerException`. Access outside the matrix throws
`IndexOutOfBoundsException`. Addition and multiplication require equal dimensions
and throw `IllegalArgumentException` otherwise.

Validate the type of every entry during construction, including entries in later
rows. Mixed nested types throw `IllegalArgumentException` with `MIXED_ENTRY_TYPES`.
Use `hasSameType` to validate every entry against the first entry.

Use `T` methods for entry arithmetic and identities. Keep multiplication in left
then right order. Arithmetic results must have independent row lists.
For equality, a failed `hasSameType` check throws `ClassCastException` with
`INCOMPATIBLE_TYPE`, including for null or a nonmatrix object. Once types match,
different dimensions return false. Otherwise compare entries with `equals`.
Value equality must recognize a separately constructed zero matrix.

### Matrix Builder

This is the top-level `Builder` class in `AbstractMath.Rings.Structures.Matrix`,
stored in its own file. Its dimensions are fixed and its entries start as null.

| Constructor or method | Required behavior |
| --- | --- |
| `Builder(int rows, int columns)` | Validate positive square dimensions and allocate null entry slots |
| `void setValue(int row, int column, T value)` | Assign a nonnull entry at the zero based coordinates |
| `Matrix<T> build()` | Require every entry to be set and return an independent immutable matrix |

Invalid dimensions throw `IllegalArgumentException`. Invalid coordinates throw
`IndexOutOfBoundsException`. A null entry throws `NullPointerException`.
`build()` throws `IllegalStateException` if even one entry is unset, including an
entry intended to hold zero. Set zero explicitly using an element of the ring.
`build()` also rejects mixed nested entry types with `IllegalArgumentException`.
A failed build leaves the builder available for correction.

### Matrix ErrorStrings

Store these exact strings in the enum. `String getMessage()` returns the stored
message. Use the corresponding message when throwing a validation exception.

| Constant | Message |
| --- | --- |
| `NONPOSITIVE_DIMENSIONS` | Matrix dimensions must be greater than zero |
| `NOT_SQUARE` | A ring matrix must be square |
| `NULL_VALUES` | Default values must not be null |
| `NULL_ROW` | Default row must not be null |
| `ROW_COUNT` | Default values must match the number of rows |
| `COLUMN_COUNT` | Each default row must match the number of columns |
| `NULL_ENTRY` | Matrix entries must not be null |
| `INDEX_OUT_OF_BOUNDS` | Matrix coordinates are outside the fixed dimensions |
| `NULL_OPERAND` | Other matrix must not be null |
| `SIZE_MISMATCH` | The matrices must have the same size |
| `INCOMPLETE` | All matrix entries must be set before building |
| `MIXED_ENTRY_TYPES` | All matrix entries must have the same nested type |
| `INCOMPATIBLE_TYPE` | Matrices can only be compared to matrices with the same nested entry type |

### Polynomial

Store a copy of the coefficients in a private final `ArrayList<T>`. Index zero
holds the constant term and index `i` holds the coefficient of `x` to power `i`.
Remove trailing zeros when constructing from a list and after every arithmetic operation,
using `c.equals(c.zero())` to recognize a zero coefficient. Preserve interior
zeros. The stored list must always contain exactly `degree() + 1` entries.
Represent the zero polynomial as one zero coefficient with degree zero.

| Constructor or method | Required behavior |
| --- | --- |
| `Polynomial(int degree, List<T> coefficients)` | Require exactly degree plus one coefficients, copy the list, and remove trailing zeros |
| `Polynomial(List<T> coefficients)` | Infer the initial degree from the list size and use the same construction rules |
| `static <T extends RingElement<T>> Builder<T> builder(int degree)` | Create a builder in the polynomial package with degree plus one null slots |
| `int degree()` | Return the highest stored power, with degree zero for the zero polynomial |
| `boolean hasSameType(Object other)` | Require another polynomial and recursively compare the types of their constant coefficients |
| `T coefficient(int degree)` | Return that coefficient, or the coefficient ring's zero above the polynomial degree |
| `T getValue(int degree)` | Return the coefficient using the same rules as `coefficient` |
| `Polynomial<T> add(Polynomial<T> other)` | Add matching powers, treating missing coefficients as zero |
| `Polynomial<T> negate()` | Negate every coefficient |
| `Polynomial<T> multiply(Polynomial<T> other)` | Accumulate each product of degrees `i` and `j` at degree `i + j` |
| `Polynomial<T> zero()` | Return the constant polynomial containing the coefficient ring's zero |
| `Polynomial<T> one()` | Return the constant polynomial containing the coefficient ring's one |
| `boolean equals(Object other)` | Require matching nested types, then compare reduced coefficient lists |

The constructor degree describes the supplied list before reduction.
For example, degree 3 with `[8, 3, 0, 0]` produces a degree 1 polynomial
stored as `[8, 3]`. The supplied degree must be nonnegative and less than
`Integer.MAX_VALUE` so that degree plus one fits in an `int`. Reject an invalid
degree, a list whose size does not match, or an empty list with
`IllegalArgumentException`. Reject a null list or any null
coefficient with `NullPointerException`. Do not change or retain the caller's
list. Coefficient objects themselves are shared. Callers must supply
coefficients from one ring and must not mutate them while used in a polynomial.
Check every coefficient's nested type before removing trailing zeros. Mixed
types throw `IllegalArgumentException` with `MIXED_COEFFICIENT_TYPES`. Use
`hasSameType` to validate every coefficient against the constant coefficient.

`coefficient` and `getValue` reject negative degrees with
`IllegalArgumentException`. Reading above the degree returns the coefficient
ring's zero without extending the list. Built polynomials have no setters.
`add` and `multiply` reject null operands with `NullPointerException`.

Multiplication must use the left coefficient's `multiply` method with the right
coefficient as its argument. Order matters for matrix coefficients. Use coefficient
methods to obtain zero and one. Do not assume that a nonzero leading coefficient
product is always nonzero, or that a product's final degree is the sum of the inputs.

For equality, a failed `hasSameType` check throws `ClassCastException` with
`INCOMPATIBLE_TYPE`, including for null or a non-polynomial object. Once types
match, different reduced lengths return false. Otherwise compare each pair of
coefficients with `equals`. Polynomial value
equality allows polynomials themselves to be used as coefficients.

### Polynomial Builder

This is the top-level `Builder` class in `AbstractMath.Rings.Structures.Polynomial`,
stored in its own file. Choose the degree first, then set every coefficient.

| Constructor or method | Required behavior |
| --- | --- |
| `Builder(int degree)` | Allocate exactly degree plus one null coefficient slots |
| `void setValue(int degree, T value)` | Assign a nonnull coefficient without changing the chosen degree |
| `Polynomial<T> build()` | Require every coefficient to be set and return an independent immutable polynomial |

The builder degree must be nonnegative and less than `Integer.MAX_VALUE` so the
list size fits in an `int`. Invalid constructor degrees throw
`IllegalArgumentException`. The setter rejects a negative term degree with
`IllegalArgumentException`, a term above the chosen degree with
`IndexOutOfBoundsException`, and null with `NullPointerException`.

For a positive degree polynomial, setting the leading coefficient to zero throws
`IllegalArgumentException` with the `LEADING_ZERO` message. The builder keeps its
previous state. Test zero with `value.equals(value.zero())`. A zero constant is
allowed when the chosen degree is zero so the additive identity can be built.

`build()` throws `IllegalStateException` until every slot has been set, including
interior zeros. List constructors automatically discard trailing zeros and
compute the reduced degree. Builders preserve the chosen degree. Arithmetic may
still produce a new polynomial of lower degree through cancellation.
`build()` also rejects mixed nested coefficient types with
`IllegalArgumentException`. A failed build leaves the builder available for correction.

### Polynomial ErrorStrings

Store these exact strings in the enum. `String getMessage()` returns the stored
message. Use the corresponding message when throwing a validation exception.

| Constant | Message |
| --- | --- |
| `INVALID_DEGREE` | Degree must be nonnegative and degree + 1 must fit in an int |
| `NULL_COEFFICIENTS` | Coefficients must not be null |
| `EMPTY_COEFFICIENTS` | Supply at least the constant coefficient |
| `COEFFICIENT_COUNT` | Supply exactly degree + 1 coefficients |
| `NULL_COEFFICIENT` | Coefficients must not contain null |
| `NEGATIVE_TERM_DEGREE` | Degree must not be negative |
| `TERM_OUT_OF_BOUNDS` | Term degree is above the builder's fixed degree |
| `LEADING_ZERO` | The leading coefficient cannot be zero because the degree would be reduced |
| `NULL_OPERAND` | Other polynomial must not be null |
| `INCOMPLETE` | All polynomial coefficients must be set before building |
| `MIXED_COEFFICIENT_TYPES` | All polynomial coefficients must have the same nested type |
| `INCOMPATIBLE_TYPE` | Polynomials can only be compared to polynomials with the same nested coefficient type |

### Main

`Main` is provided in the default package as an example. Unit tests do not run it
or check its output. It demonstrates polynomial reduction, identities, builders,
multiplication, a matrix of polynomials, and a mismatched modulus comparison.

The example uses `Polynomial.<ZModElement<Mod5>>builder(1)` and
`Matrix.<Polynomial<ZModElement<Mod5>>>builder(1, 1)` to create builders.
These static generic factory methods avoid importing both classes named `Builder`.
The getters belong to the completed values, and the setters belong to the builders.

## Reduction example

Using ordinary integer coefficients

```text
(-2x^3 - 3x^2 + x + 1) + (2x^3 + 3x^2 + 2x + 7)
= 3x + 8
```

The inputs are stored as `[1, 1, -3, -2]` and `[7, 2, 3, 2]`.
The result must store exactly `[8, 3]` and report degree `1`.
Storing `[8, 3, 0, 0]` does not meet the requirement.
An interior zero remains, so `2x^2 + 1` is stored as `[1, 0, 2]`.

Coefficients follow their own arithmetic rules. The example becomes `[3, 3]`
modulo 5. Modulo 3 it reduces further to `[2]`.

## Why these structures share an interface

Rings provide addition, multiplication, zero, one, and additive inverses.
Polynomials with coefficients from a ring form another ring. Square matrices of
one fixed size over a ring do as well. These common operations let us compose
generic classes without teaching each class about every possible entry type.

## Type Caveat

The compiler keeps `Polynomial<ZModElement<Mod3>>` separate from
`Polynomial<ZModElement<Mod5>>`. Constructing both with the broader coefficient
type `ZModElement<Modulus>` loses that compile time distinction. Constructors and
builders check every entry's nested type recursively and reject mixed types.
The value 3 modulo 7 and the value 3 modulo 5 belong to different rings.
They cannot be compared by this assignment's equality rules.

The recursive checks inspect concrete runtime objects. They do not recover erased
variable declarations. A `ZModElement<Modulus>` constructed with a `Mod3` object
has the same runtime type for this comparison as a `ZModElement<Mod3>` using
another `Mod3` object. Custom ring classes with nested elements or ring
configuration must override `hasSameType` to check those details. The default
interface implementation checks only the concrete class.

`hasSameType` returns a boolean. Each structure's `equals` uses false from that
helper to throw its own error. Equal nested types with different dimensions,
degrees, or values can still compare false. Type mismatches at any nesting depth
throw before those value comparisons.

Generic bounds cannot enforce a positive modulus, matching matrix dimensions,
or the arithmetic laws. `equals(Object)` also accepts any Java object. These
limitations mean some checks remain at runtime and may restrict future extensions.

## Running and grading

From `Assignment`, run

```sh
mvn clean test
```

After implementing the classes, you can also run the example

```sh
java -cp target/classes Main
```

Add your own tests under `Assignment/src/test/java/student`. Do not modify the
instructor tests, Maven configuration, or grading files. Implement the ring
interfaces, classes, builders, and enums under `Assignment/src/main/java`.
`Main` is only an example of how to use them.

Tests check storage, builder completion, immutability, exact error messages,
recursive type compatibility and rejection of mixed entries,
validation, arithmetic, value equality, generic type
restrictions, and composition with several coefficient types.
Both student and instructor tests run. The starter is incomplete, so its tests
will not compile until the required interfaces, classes, and methods exist.
The reference solution can run the full suite as supplied.

| Result | Grade |
| --- | --- |
| Build succeeds and all tests pass | 100 points |
| Build fails, a test fails or errors, or no tests are found | 10 points |

Submit your complete grade token in Blackboard's text field. Submit only the
token. Check it before submitting because only one submission is allowed.
