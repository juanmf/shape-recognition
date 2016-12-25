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

import marvin.image.MarvinImage;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author juan.fernandez
 */
public interface VectorizeStrategy {
    
    /**
     * Keep instance control from here.
     * 
     * Concrete classes constructors should be package private.
     */
    enum ConcreteStrategy implements VectorizeStrategy {
        DEFAULT (new VectorizeImpl());
        
        private final VectorizeStrategy INSTANCE;
        
        ConcreteStrategy(VectorizeStrategy concreteStrategy) {
            INSTANCE = concreteStrategy;
        }

        @Override
        public VectorImageGridIntersections processImage(MarvinImage img) {
            return INSTANCE.processImage(img);
        }
    }
    
    enum Grid {
        DENSE (0.1f),
        MEDIUM (0.3f),
        LIGHT (0.6f);

        private static Map<Float, Grid> grids = new HashMap<>();

        static {
            grids.put(0.1f, DENSE);
            grids.put(0.3f, MEDIUM);
            grids.put(0.6f, LIGHT);
        }

        /**
         * Represents the lines in the grid, in a centered square frame that spans
         * from -2 to 2 in both coordinates (X & Y)
         */
        private final float[] constLines;
        private final float increment;

        Grid(float increment) {
            constLines = new float[(int) (4/increment)];
            int c = 0;
            float i = -2 + increment;
            while (i < 2) {
                constLines[c++] = i;
                i += increment;
            }
            this.increment = increment;
        }

        public static Grid forIncrement(Float increment) {
            if (! grids.containsKey(increment)) {
                throw new IllegalArgumentException("there is no such increment.");
            }
            return grids.get(increment);
        }
        
        public float[] getLines() {
            return constLines;
        }

        public float getIncrement() {
            return increment;
        }
    }
    
    /**
     * Should perform edge Detection in order to have lines, that can be vectorized.
     * 
     * Then, for each of the grids defined in Grid, is should calculate the 
     * intersections and create an immutable VectorImageGridIntersections.
     * 
     * @param img An Image suitable for edge detection.
     * 
     * @return the VectorImageGridIntersections representing img's vectors 
     * intersections with the grids.
     */
    VectorImageGridIntersections processImage(MarvinImage img);
}
