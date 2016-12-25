package ar.com.docdigital.vector.util;

/**
 * Convenience class to enable single level of Stream.iterate() to traverse a matrix.
 *
 * @author juanmf@gmail.com
 */
public class LinearToSquareIndexHelper {
    public static Integer getAbsoluteRow(Integer linear, Integer width, Integer rowOffset) {
        return getRow(linear, width) + rowOffset;
    }

    public static Integer getAbsoluteCol(Integer linear, Integer width, Integer colOffset) {
        return getCol(linear, width) + colOffset;
    }

    /**
     * Zero based calculations, to determine in which row a linear index falls.
     *
     * @param linear Lienar index that references an element in a matrix
     * @param width  The column number in the matrix
     * @return The Row number of the linear index.
     */
    public static Integer getRow(Integer linear, Integer width) {
        return ((linear) / width);
    }

    /**
     * Zero based calculations, to determine in which col a linear index falls.
     *
     * @param linear Lienar index that references an element in a matrix
     * @param width  The column number in the matrix
     * @return The Column number of the linear index.
     */
    public static Integer getCol(Integer linear, Integer width) {
        return ((linear) % width);
    }

    public static class ParamHolder {
        private final int height;
        private final int width;
        private final int subSquareWith;
        private final int firstRow;
        private final int firstCol;

        public ParamHolder(int height, int width, int subSquareWith) {
            this(height, width, subSquareWith, 0, 0);
        }

        public ParamHolder(int height, int width, int subSquareWith, int firstRow, int firstCol) {
            this.height = height;
            this.width = width;
            this.subSquareWith = subSquareWith;
            this.firstRow = firstRow;
            this.firstCol = firstCol;
        }

        public int getHeight() {
            return height;
        }

        public int getWidth() {
            return width;
        }

        public int getSubSquareWith() {
            return subSquareWith;
        }

        public int getFirstRow() {
            return firstRow;
        }

        public int getFirstCol() {
            return firstCol;
        }

        public int getCol(int linearIdx) {
            return getAbsoluteCol(linearIdx, getSubSquareWith(), getFirstCol());
        }

        public int getRow(int linearIdx) {
            return getAbsoluteRow(linearIdx, getSubSquareWith(), getFirstRow());
        }
    }
}
