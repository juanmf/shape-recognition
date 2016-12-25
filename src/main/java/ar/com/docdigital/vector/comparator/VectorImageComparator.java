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

package ar.com.docdigital.vector.comparator;

import ar.com.docdigital.vector.VectorImageGridIntersections;
import ar.com.docdigital.vector.util.IntersectionsDifference;

import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is a somewhat strange comparator, in that the comparison is made against
 * a particular VectorImageGridIntersections. 
 * 
 * o1 will be greater than o2 if it is more similar to compareAgainst than o2.
 * 
 * @author juan.fernandez
 * @ThreadSafe
 */
public class VectorImageComparator implements Comparator<VectorImageGridIntersections> {

    private final VectorImageGridIntersections compareAgainst;
    private final Map<Long, Float> comparisonCache = new ConcurrentHashMap<>(); 
    private final ShapeComparisonStrategy scs;        
    
    public VectorImageComparator(
            VectorImageGridIntersections compareAgainst,
            ShapeComparisonStrategy scs
    ) {
        if (null == compareAgainst) {
            throw new IllegalArgumentException("compareAgainst must not be null");
        }
            
        this.compareAgainst = compareAgainst;
        this.scs = scs;
    }

    @Override
    public int compare(VectorImageGridIntersections o1, VectorImageGridIntersections o2) {
        Float o1CashedResult = comparisonCache.get(o1.getId());
        Float o2CashedResult = comparisonCache.get(o2.getId());
        
        o1CashedResult = (o1CashedResult == null) 
                ? getDifference(o1) : o1CashedResult;
        o2CashedResult = (o2CashedResult == null) 
                ? getDifference(o2) : o2CashedResult;
        
        comparisonCache.putIfAbsent(o1.getId(), o1CashedResult);
        comparisonCache.putIfAbsent(o2.getId(), o2CashedResult);
        return o2CashedResult.compareTo(o1CashedResult);
    }

    private Float getDifference(VectorImageGridIntersections o1) {
        return getDifference(o1, compareAgainst);
    }

    public Float getDifference(
            VectorImageGridIntersections o1,
            VectorImageGridIntersections o2
    ) {
        return scs.getDifference(o1, o2);
    }

    IntersectionsDifference getIntersectionsDifference(
            VectorImageGridIntersections o1,
            VectorImageGridIntersections o2
    ) {
        return scs.getIntersectionsDifference(o1, o2);
    }
}
