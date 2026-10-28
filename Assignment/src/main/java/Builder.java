import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Assigns matrix entries before building an immutable value.
 * This class has setters and build, but no entry getters or ring operations.
 * @param <T> the matrix entry type
 */
public final class Builder<T extends RingElement<T>> {
    private final int rows;
    private final int columns;
    private final List<List<T>> values;

    /**
     * Creates a square matrix builder with every entry initially null.
     * @throws IllegalArgumentException if dimensions are nonpositive or unequal
     */
    public Builder(int rows, int columns) {
        Matrix.validateDimensions(rows, columns);
        // stuff missing here
    }

    /**
     * Assigns one entry without changing the dimensions.
     * @throws IndexOutOfBoundsException if either coordinate is outside the matrix
     * @throws NullPointerException if value is null
     */
    public void setValue(int row, int column, T value) {
        Matrix.validateCoordinates(row, column, rows, columns);
        values.get(row).set(column, Objects.requireNonNull(value, ErrorStrings.NULL_ENTRY.getMessage()));
    }

    /**
     * Copies the completed entries into an immutable matrix.
     * Later changes to this builder do not change previously built matrices.
     * @return a complete matrix
     * @throws IllegalStateException if any entry is still null
     * @throws IllegalArgumentException if entries have different nested types
     */
    public Matrix<T> build() {
        for (List<T> row : values) {
            for (T value : row) {
                if (value == null) {
                    throw new IllegalStateException(ErrorStrings.INCOMPLETE.getMessage());
                }
            }
        }
        return new Matrix<>(rows, columns, values);
    }
}
