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

package ar.com.docdigital.vector.comparator.difference;

import ar.com.docdigital.vector.util.IntersectionsDifference;

/**
 * Comparison between VectorImageGridIntersections grid lines has two outcomes,
 * on one hand there are distances, when intersections on each grid form pairs, 
 * on the other hand we have a percentage of intersections that didn't find pair
 * called lostPoints in IntersectionsDifference, the implementations of this 
 * Interfaces should find a formula to integrate both in a unique index. 
 * 
 * Consider that:
 * Given a pair of grids with 100% of lost points, the distance will be 0, as 
 * there was no matching pair of intersections.
 * 
 * @author juan.fernandez
 */
public interface IndexGenerator {
    float getDifferenceIndex(IntersectionsDifference diff); 
}
