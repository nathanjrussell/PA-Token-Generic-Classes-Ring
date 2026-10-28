import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * An immutable square matrix over one ring.
 * Input lists are copied and entries must themselves be immutable ring elements.
 * Use a Builder to assign entries individually before creating a complete matrix.
 * The constructor validates shape and requires every entry to have the same nested type.
 *
 * @param <T> the entry type, which supplies its own ring operations
 */
public final class Matrix<T extends RingElement<T>> implements RingElement<Matrix<T>> {
    private final int rows;
    private final int columns;
    private final List<List<T>> values;

    /**
     * Copies complete initial rows into a fixed square matrix.
     *
     * @param rows the positive row count
     * @param columns the positive column count, which must equal rows
     * @param defaultValues exactly the requested rows and columns of nonnull entries
     * @throws IllegalArgumentException if dimensions or list sizes do not match,
     *         or entries have different nested types
     * @throws NullPointerException if any list, row, or entry is null
     */
    public Matrix(int rows, int columns, List<List<T>> defaultValues) {
        validateDimensions(rows, columns);
        Objects.requireNonNull(defaultValues, ErrorStrings.NULL_VALUES.getMessage());
        if (defaultValues.size() != rows) {
            throw new IllegalArgumentException(ErrorStrings.ROW_COUNT.getMessage());
        }
        this.rows = rows;
        this.columns = columns;
        List<List<T>> copied = new ArrayList<>();
        for (List<T> row : defaultValues) {
            Objects.requireNonNull(row, ErrorStrings.NULL_ROW.getMessage());
            if (row.size() != columns) {
                throw new IllegalArgumentException(ErrorStrings.COLUMN_COUNT.getMessage());
            }
            for (T entry : row) {
                Objects.requireNonNull(entry, ErrorStrings.NULL_ENTRY.getMessage());
            }
            copied.add(List.copyOf(row));
        }
        this.values = List.copyOf(copied);
        T first = values.get(0).get(0);
        for (List<T> row : values) {
            for (T entry : row) {
                if (!first.hasSameType(entry)) {
                    throw new IllegalArgumentException(ErrorStrings.MIXED_ENTRY_TYPES.getMessage());
                }
            }
        }
    }

    /**
     * Recursively checks entry types without comparing dimensions or values.
     * Construction validates all entries, so the first entry represents the shared type.
     * @return false for null, another structure, or any nested type mismatch
     */
    @Override
    public boolean hasSameType(Object other) {
        return other instanceof Matrix<?> matrix
                && getValue(0, 0).hasSameType(matrix.getValue(0, 0));
    }

    /** Creates a builder with null entry slots and the requested fixed dimensions. */
    public static <T extends RingElement<T>> Builder<T> builder(int rows, int columns) {
        return new Builder<>(rows, columns);
    }









    /**
     * Checks the full nested type before comparing dimensions or entry values.
     * @throws ClassCastException if the other object does not have the same nested matrix type
     */
    @Override
    public boolean equals(Object other) {
        if (!hasSameType(other)) {
            throw new ClassCastException(ErrorStrings.INCOMPATIBLE_TYPE.getMessage());
        }
        Matrix<?> matrix = (Matrix<?>) other;
        if (rows != matrix.rows || columns != matrix.columns) {
            return false;
        }
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                if (!getValue(row, column).equals(matrix.getValue(row, column))) {
                    return false;
                }
            }
        }
        return true;
    }

    static void validateDimensions(int rows, int columns) {
        if (rows <= 0 || columns <= 0) {
            throw new IllegalArgumentException(ErrorStrings.NONPOSITIVE_DIMENSIONS.getMessage());
        }
        if (rows != columns) {
            throw new IllegalArgumentException(ErrorStrings.NOT_SQUARE.getMessage());
        }
    }

    static void validateCoordinates(int row, int column, int rows, int columns) {
        if (row < 0 || row >= rows || column < 0 || column >= columns) {
            throw new IndexOutOfBoundsException(ErrorStrings.INDEX_OUT_OF_BOUNDS.getMessage());
        }
    }

    private void requireSameSize(Matrix<T> other) {
        Objects.requireNonNull(other, ErrorStrings.NULL_OPERAND.getMessage());
        if (rows != other.rows || columns != other.columns) {
            throw new IllegalArgumentException(ErrorStrings.SIZE_MISMATCH.getMessage());
        }
    }
}
