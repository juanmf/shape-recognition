/*
 * The MIT License
 *
 * Copyright 2015 Bernardo Martínez Garrido.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package ar.com.docdigital.vector;

import ar.com.docdigital.vector.config.Config;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author juan.fernandez
 */
public final class VectorImageGridIntersections implements Serializable {
    private static final long serialVersionUID = 30359651L;

    private static final transient AtomicLong ID_GENERATOR = new AtomicLong(0);
    private final Long id;
    private final transient Map<Vectorize.Grid, Map<Float, List<Float>>> constXintersect;
    private final transient Map<Vectorize.Grid, Map<Float, List<Float>>> constYintersect;
    private static final Vectorize.Grid DEFAULT_GRID = Config.getDefaultGrid();

    /**
     * As the vectorized image representation is designed to fit in a square 
     * delimited by [[-2, -2], [2, 2]] it can be stringified as a 40*40 String
     * representation of a boolean (0,1) matrix like the following.
     * 
     * 0000000000000111111000000000000000000000
     * 0000000000100000000010000000000000000000
     * 0000000100000000000000100000000000000000
     * 0000010000000000000000001000000000000000
     * 0000100000000000000000000010000000000000
     * :: more lines until line 40
     * 0000000000000000000000000000000000000000
     */
    private static final int GRID_FRAME_SIZE = 39;
    
    /**
     * The constant multiplier to adjust vectorized image float boundaries -2 to 
     * the integer -20
     */
    private static final int GRID_FRAME_RATIO = 10;
    
    /**
     * Matrix of long is used to represent the intersections as a string output.
     * the cut point discards the difference as log has 64 bits, and we need only 40 
     */
    private static final int FRAME_LONG_STRING_CUT_POINT = 64 - GRID_FRAME_SIZE;
    
    public VectorImageGridIntersections(
            Map<Vectorize.Grid, Map<Float, List<Float>>> constXintersect,
            Map<Vectorize.Grid, Map<Float, List<Float>>> constYintersect
            
    ) {
        // Autogenerated Ids are negative.
        this(constXintersect, constYintersect, ID_GENERATOR.decrementAndGet());
    }
    
    public VectorImageGridIntersections(
            Map<Vectorize.Grid, Map<Float, List<Float>>> constXintersect,
            Map<Vectorize.Grid, Map<Float, List<Float>>> constYintersect,
            Long id
    ) {
        this.constXintersect = Collections.unmodifiableMap(constXintersect);
        this.constYintersect = Collections.unmodifiableMap(constYintersect);
        this.id = id;
    }

    public Map<Vectorize.Grid, Map<Float, List<Float>>> getConstXintersect() {
        return constXintersect;
    }

    public Map<Vectorize.Grid, Map<Float, List<Float>>> getConstYintersect() {
        return constYintersect;
    }

    /**
     * creates a String representation of a Matrix of integers in binary format
     * representing the intersections of shape lines with each grid
     * 
     * @return 
     */
    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        Vectorize.Grid grid = Config.getDefaultGrid();
//        for (Vectorize.Grid grid : Vectorize.Grid.values()) {
            long[] frame = new long[grid.getLines().length];
            Arrays.fill(frame, 0);
            markIntersectionsInframe(grid, frame);
            appendFrameToOutput(frame, out);
//        }
        return out.toString();
    }
    
    private int getLineNumForFloat(float l) {
        int line = Math.round(l * GRID_FRAME_RATIO + ((GRID_FRAME_SIZE + 1) / 2));
        if (line < 0 || line > GRID_FRAME_SIZE) {
            throw new IllegalArgumentException("The line falls out of the frame");
        }
        return line;
    }

    /**
     * 
     * @param grid
     * @param frame 
     */
    private void markIntersectionsInframe(Vectorize.Grid grid, long[] frame) {
        Map<Float, List<Float>> xGridIntersects = constXintersect.get(grid);
        Map<Float, List<Float>> yGridIntersects = constYintersect.get(grid);
        float[] lines = grid.getLines();
        for (float l : lines) {
            int row;
            int col = getLineNumForFloat(l);
            for (Float intersect : xGridIntersects.get(l)) {
                row = getLineNumForFloat(intersect);
                frame[row] = frame[row] | (1 << col);
            }

            row = getLineNumForFloat(l);
            for (Float intersect : yGridIntersects.get(l)) {
                col = getLineNumForFloat(intersect);
                frame[row] = frame[row] | (1 << col);
            }
        }
    }

    private void appendFrameToOutput(long[] frame, StringBuilder out) {
        for (long i : frame) {
            out.append(String.format("%40s", Long.toBinaryString(i)).replace(' ', '0'));
            out.append(System.lineSeparator());
        }
        out.append(System.lineSeparator());
    }

    public Long getId() {
        return id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.id);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final VectorImageGridIntersections other = (VectorImageGridIntersections) obj;
        return Objects.equals(this.id, other.id);
    }
    
    private Object writeReplace() {
        return new SerializationProxy(this);
    }

    private void readObject(ObjectInputStream stream)
            throws InvalidObjectException {
        // writeReplace prevents the existance of this class serialized form.
        throw new InvalidObjectException("Proxy required");
    }

    private static class SerializationProxy implements Serializable {
        // TODO: las implementaciones de Map no garantizan compatibilidad entre 
        // distintas JVM, buscar una representacion agnostica.
        private static final long serialVersionUID = 234098243823485285L;
        private final Long id;
        private final Map<Vectorize.Grid, Map<Float, List<Float>>> constXintersect;
        private final Map<Vectorize.Grid, Map<Float, List<Float>>> constYintersect;

        SerializationProxy(VectorImageGridIntersections v) {
            this.id = v.id;
            this.constXintersect = v.constXintersect;
            this.constYintersect = v.constYintersect;
        }

        private Object readResolve() {
            return new VectorImageGridIntersections(constXintersect, constYintersect, id);
        }
    }
    
    public static class Builder {
        private final Map<Vectorize.Grid, Map<Float, List<Float>>> constXintersect;
        private final Map<Vectorize.Grid, Map<Float, List<Float>>> constYintersect;
        
        public Builder() {
            constXintersect = new HashMap<>();
            constYintersect = new HashMap<>();

            constXintersect.put(DEFAULT_GRID, new HashMap<Float, List<Float>>());
            constYintersect.put(DEFAULT_GRID, new HashMap<Float, List<Float>>());
            
            for (Float f : DEFAULT_GRID.getLines()) {
                constXintersect.get(DEFAULT_GRID).put(f, new ArrayList<Float>());
                constYintersect.get(DEFAULT_GRID).put(f, new ArrayList<Float>());
            }
        }
        
        public Builder addXintersect(Float xLine, Float intersection) {
            constXintersect.get(DEFAULT_GRID).get(xLine).add(intersection);
            return this;
        }
        
        public Builder addYintersect(Float yLine, Float intersection) {
            constYintersect.get(DEFAULT_GRID).get(yLine).add(intersection);
            return this;
        }
        
        public VectorImageGridIntersections build() {
            // The List<Float> for each grid line is still modifiable.
            constXintersect.put(DEFAULT_GRID, Collections.unmodifiableMap(constXintersect.get(DEFAULT_GRID)));
            constYintersect.put(DEFAULT_GRID, Collections.unmodifiableMap(constYintersect.get(DEFAULT_GRID)));
            return new VectorImageGridIntersections(
                    Collections.unmodifiableMap(constXintersect), 
                    Collections.unmodifiableMap(constYintersect)
            );
        }
    }
}
