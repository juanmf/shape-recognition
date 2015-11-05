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
import ar.com.docdigital.vector.Vectorize;
import ar.com.docdigital.vector.difference.IndexGenerator;
import ar.com.docdigital.vector.difference.IndexGeneratorDefault;
import ar.com.docdigital.vector.util.IntersectionsDifference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 *
 * @author juan.fernandez
 */
public class ShapeComparisonDefault implements ShapeComparisonStrategy {

    private final DistanceCalculatorStrategy dcs;
    private final IndexGenerator diffIdxGen;
    private final Vectorize.Grid gridToCompare;

    public ShapeComparisonDefault() {
        dcs = new SimpleDistanceCalculator();
        diffIdxGen = new IndexGeneratorDefault();
        gridToCompare = Vectorize.Grid.DENSE;
    }
    
    @Override
    public Float getDifference(
            VectorImageGridIntersections o1, 
            VectorImageGridIntersections o2
    ) {
        int totalPoints = 0;
        int lostPoints = 0;
        float distance = 0;
        Map<Float, List<Float>> o1XIntxn = o1.getConstXintersect().get(gridToCompare);
        Map<Float, List<Float>> o2XIntxn = o1.getConstXintersect().get(gridToCompare);
        Map<Float, List<Float>> o1YIntxn = o1.getConstXintersect().get(gridToCompare);
        Map<Float, List<Float>> o2YIntxn = o1.getConstXintersect().get(gridToCompare);
        for (Float f : gridToCompare.getLines()) {
            IntersectionsDifference diffX = getMinLineDiff(o1XIntxn.get(f), o2XIntxn.get(f));
            IntersectionsDifference diffY = getMinLineDiff(o1YIntxn.get(f), o2YIntxn.get(f));

            totalPoints += diffX.totalPoints + diffY.totalPoints;
            lostPoints += diffX.lostPoints + diffY.lostPoints;
            distance += diffX.distance + diffY.distance;
        }
        return diffIdxGen.getDifferenceIndex(
                new IntersectionsDifference(totalPoints, lostPoints, distance)
        );
    }

    private IntersectionsDifference getMinLineDiff(List<Float> o1GridLineIntxn, List<Float> o2GridLineIntxn) {
        int totalPoints = o1GridLineIntxn.size() + o2GridLineIntxn.size();
        byte lostPoints = (byte) Math.abs(o1GridLineIntxn.size() - o2GridLineIntxn.size());
        return (o1GridLineIntxn.size() > 0 && o2GridLineIntxn.size() > 0)
                ? new IntersectionsDifference(
                        totalPoints, lostPoints, 
                        dcs.getIntxnDistance(
                            o1GridLineIntxn, o2GridLineIntxn
                        )
                    ) 
                : new IntersectionsDifference(totalPoints, lostPoints, 0f);
    }

   
    
    private interface DistanceCalculatorStrategy {
        float getIntxnDistance(List<Float> o1GridLineIntxn, List<Float> o2GridLineIntxn);
    }
    
    private static class SimpleDistanceCalculator implements DistanceCalculatorStrategy {
        
        /**
         * I assume params are sorted Lists.
         * 
         * If the lengths are equal, the intersections pairing for distance calculations
         * is direct, walking through the line, from left to right. If the lengths
         * differ, the one with less intersections chooses the matches for each of its 
         * points.
         * 
         * @param o1GridLineIntxn
         * @param o2GridLineIntxn
         * @return 
         */
        @Override
        public float getIntxnDistance(List<Float> o1GridLineIntxn, List<Float> o2GridLineIntxn) {
            Float[] smaller, bigger;
            // TODO: serious performance hit, toArray() makes copies
            if (o1GridLineIntxn.size() < o2GridLineIntxn.size()) {
                smaller = o1GridLineIntxn.toArray(new Float[0]); 
                bigger = o2GridLineIntxn.toArray(new Float[0]);
            } else {
                smaller = o2GridLineIntxn.toArray(new Float[0]); 
                bigger = o1GridLineIntxn.toArray(new Float[0]);
            }
            
            float distance = 0;
            for (int i = 0; i < smaller.length; i++) {
                Float[] pair = new Float[2];
                pair[0] = smaller[i];
                pair[1] = null;
                findPair(pair, bigger, i, smaller);
                distance += Math.abs(pair[0] - pair[1]);
            }
            return distance;
        }

        private void findPair(Float[] pair, Float[] bigger, int smallerIdx, 
                Float[] smaller
        ) {
            float steppingAt = pair[0];
            int lastIxd = -1, biggerIdx = 0;
            float last = Float.MAX_VALUE;
            while (null == pair[1]) {
                while (null == bigger[biggerIdx] && biggerIdx < bigger.length) {
                    biggerIdx++;
                }
                if (biggerIdx >= bigger.length) {
                    // we never passed steppingAt, using closer at left
                    pair[1] = bigger[lastIxd];
                    bigger[lastIxd] = null;
                    
                } else if (bigger[biggerIdx] < steppingAt) {
                    // approaching to steppingAt from left to right, asume sorted array.
                    last =  steppingAt - bigger[biggerIdx];
                    lastIxd = biggerIdx;
                } else if (last == Float.MAX_VALUE) {
                    // there was no Intxn before steppingAt, use 1st greater or eq Intxn 
                    pair[1] = bigger[biggerIdx];
                    bigger[biggerIdx] = null;
                } else if (last > bigger[biggerIdx] - steppingAt) {
                    /**
                     * We found one Intxn on each side of steppingAt
                     * A decision must be made, since the local conclusion aims 
                     * to use the intxn at the right, but it can damage the global 
                     * result
                     */
                    int biggerIdxToUse = decideWhichIntxnToChoose(
                                lastIxd, biggerIdx, bigger, smallerIdx, smaller
                            );
                    pair[1] = bigger[biggerIdxToUse];
                    bigger[biggerIdxToUse] = null;
                } else {
                    // We passed steppingAt, but last Intx before was closer.
                    pair[1] = bigger[lastIxd];
                    bigger[lastIxd] = null;
                }
                biggerIdx++;
            }
        }
        
        /**
         * 
         * @param lastIdx
         * @param biggerCurrentIdx
         * @param bigger
         * @param smallerIdx
         * @param smaller
         * @return 
         */
        private int decideWhichIntxnToChoose(
                int lastIdx, final int biggerCurrentIdx, Float[] bigger, int smallerIdx, 
                Float[] smaller
        ) {
            if (lastIdx < 0) {
                throw new IllegalArgumentException("lastIxd was not initialized");
            } 
            if (smaller.length - 1 == smallerIdx) {
                return biggerCurrentIdx;
            }
            /*
             * There might be competition for biggerCurrentIdx. Figure out if next
             * smallerIdx would also choose biggerCurrentIdx, if so, prioritize it.
             */
            return solveConflict(lastIdx, biggerCurrentIdx, bigger, smallerIdx, smaller);
        }
        
        /**
         * 
         * @param lastIdx
         * @param biggerCurrentIdx
         * @param bigger
         * @param smallerIdx
         * @param smaller
         * @return 
         */
        private int solveConflict(
                int lastIdx, final int biggerCurrentIdx, Float[] bigger, int smallerIdx, 
                Float[] smaller
        ) {
            float nextSmall = smaller[smallerIdx + 1];
            int prevIdx = biggerCurrentIdx;
            int currentIdx = biggerCurrentIdx;
            int incCount = 0;
            while (currentIdx < bigger.length
                    && (bigger[currentIdx] == null || bigger[currentIdx] < nextSmall)
            ) {
                if (null != bigger[currentIdx]) {
                    prevIdx = currentIdx;
                    incCount++;
                }
                currentIdx++;
            }
            if (0 == incCount) {
                /**
                 * biggerCurrentIdx never incremented, so we have following conf.
                 * where b is a dot in bigger and s is a dot in smaller.
                 * --b------s--s--b--
                 *   ^-- returning this one.
                 */
                // TODO: try to improve this logic. its weak.
                return lastIdx;
            } else if (1 < incCount) {
                // there are more "b"s, no contention.
                return biggerCurrentIdx;
            } else {
                /**
                 * biggerCurrentIdx incremented so there must be 1 "b"
                 * before nextSmaller, returning the index that gets a better match
                 * A:
                 * --b------s----B--S--B2--
                 *   ^------^    ^--^
                 *   ^-- return this: lastIdx
                 * B:
                 * --b------s----B--S--B2--
                 *          ^----^  ^--^
                 *               ^-- return this: biggerCurrentIdx
                 */
                float small = smaller[smallerIdx];
                return (currentIdx == bigger.length 
                        || ((small - bigger[lastIdx] + nextSmall - bigger[biggerCurrentIdx])
                        < (bigger[biggerCurrentIdx] - small + bigger[currentIdx] - nextSmall)))
                        ? lastIdx : biggerCurrentIdx;
            }
        }
    }
}
