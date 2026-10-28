package instructor;

import AbstractMath.Rings.ZMod.Modulus;

/** Supplies a stable concrete token class for numeric boundary and broad-type tests. */
record FixedModulus(int value) implements Modulus { }
