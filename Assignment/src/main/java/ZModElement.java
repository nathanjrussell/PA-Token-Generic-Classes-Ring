
    /**
     * Compares values within the same ring.
     *
     * @throws ClassCastException if the other object is null, has another element class,
     *         or uses another concrete modulus token class
     * @throws IllegalArgumentException if the other AbstractMath.ZModElement has a different modulus
     */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof ZModElement<?> otherElement)) {
            throw new ClassCastException(ErrorStrings.INCOMPATIBLE_TYPE.getMessage());
        }
        if (otherElement.getModulus() != this.getModulus()) {
            throw new IllegalArgumentException(ErrorStrings.MODULUS_MISMATCH.getMessage());
        }
        if (!hasSameType(other)) {
            throw new ClassCastException(ErrorStrings.MODULUS_TYPE_MISMATCH.getMessage());
        }
        return this.value == otherElement.value;
    }


}
