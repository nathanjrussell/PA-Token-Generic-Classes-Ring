/**
 * Common operations for a ring element whose concrete type is T.
 * Implementations must obey the ring laws and return results without changing operands.
 * The interface specifies operations, but cannot enforce the mathematical laws.
 * Implementations used as coefficients must compare values with equals so that
 * structures can recognize their zero element.
 * Implementations with nested elements or ring configuration must override hasSameType
 * to check those details recursively. Generic declarations cannot be recovered after erasure.
 */
public interface RingElement<T> {
    /**
     * Checks whether the other object has the same concrete class.
     * Classes with nested elements or ring configuration must extend this check
     * without comparing element values, matrix dimensions, or polynomial degrees.
     *
     * @param other the object to check
     * @return true for the same concrete class, false for null or another class
     */
    default boolean hasSameType(Object other) {
        return other != null && getClass() == other.getClass();
    }

}
