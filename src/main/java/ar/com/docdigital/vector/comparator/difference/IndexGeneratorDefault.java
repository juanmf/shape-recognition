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
 * The default strategy uses the formula: [distance * (1 + (Lost)⁴)]. 
 * 
 * Where <b>Lost</b> is the percentage of lost points divided by PROP_LOST_THRESHOLD.
 * So any % below PROP_LOST_THRESHOLD would render (1>K)⁴ adjusting distance by 
 * less than 2  
 * 
 * @author juan.fernandez
 */
public class IndexGeneratorDefault implements IndexGenerator {

    private static final float PROP_LOST_THRESHOLD = 25f;  
    private static final float DIVERGENCE_EXPONENT = 4;  
    
    @Override
    public float getDifferenceIndex(IntersectionsDifference diff) {
        float propLost = 100 * diff.lostPoints / (float) diff.totalPoints;
        double divergenceIdx = Math.pow(propLost / PROP_LOST_THRESHOLD, DIVERGENCE_EXPONENT);
        return (float) (diff.distance * (1 + divergenceIdx));
    }
}
