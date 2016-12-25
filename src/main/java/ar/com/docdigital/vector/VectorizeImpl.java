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
import ar.com.docdigital.vector.util.LinearToSquareIndexHelper;
import ar.com.docdigital.vector.util.Vector;
import marvin.image.MarvinColorModelConverter;
import marvin.image.MarvinImage;
import marvin.io.MarvinImageIO;
import marvin.plugin.MarvinImagePlugin;
import marvin.util.MarvinPluginLoader;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Stream;

/**
 * Default implementation for vectorization. 
 * 
 * It uses Marvin FW to detect object boundary, and then follows the lines generating
 * vectors. Then normalize it so as to make it comparable with other shapes.
 * 
 * @threadSafe 
 * @author juan.fernandez
 */

class VectorizeImpl implements VectorizeStrategy {

    private static final boolean debug = false;
    
    private static final String MARV_EDGE_PLUGIN_NAME = "org.marvinproject.image.morphological.boundary.jar";
    private static final MarvinImagePlugin MARV_EDGE_PLUGIN = MarvinPluginLoader.loadImagePlugin(MARV_EDGE_PLUGIN_NAME);
    private static final int RGB_BLUE_COLOR_MASK = 0xff;
    private static final int RGB_OPAQUE_WHITE = 0xffffffff;

    private final VectorExtractorHelper vectorExtractor;
    private final VectorNormalizerHelper vectorNormalizer;
    private final GridIntersectionsFinderHelper gridIntersectionsFinder;

    VectorizeImpl() {
        System.out.println("Initiating VectorizeImpl");
        vectorExtractor = new VectorExtractorHelperImpl();
        vectorNormalizer = this.new VectorNormalizerHelperImpl();
        gridIntersectionsFinder = this.new GridIntersectionsFinderImpl(Grid.DENSE);
    }
    
    @Override
    public VectorImageGridIntersections processImage(MarvinImage img) {
        MarvinImage binImg = applyPlugins(img);
        List<Vector> vectors = vectorExtractor.extractVectors(binImg.getBufferedImage());
        vectorNormalizer.normalizeVectors(vectors);
        return gridIntersectionsFinder.findGridIntersections(vectors);
    }

    private MarvinImage applyPlugins(MarvinImage img) {
        MarvinImage binImg = MarvinColorModelConverter.rgbToBinary(img, 127);
        MarvinImageIO.saveImage(binImg, "/tmp/MarvinImage.png");
        MARV_EDGE_PLUGIN.process(binImg.clone(), binImg);
        MarvinImageIO.saveImage(binImg, "/tmp/MarvinImage1.png");
        return binImg;
    }

    private static void saveImage(BufferedImage bImg, int row, int col) {
        if (!debug) {
            return;
        }
        try {
            File outputfile = new File("/tmp/MarvinImage-" + row + "-" + col+ ".png");
            ImageIO.write(bImg, "png", outputfile);
        } catch (IOException | java.lang.ArrayIndexOutOfBoundsException e) {
            System.out.println("i:" + row + " j:" + col);
        }
    }
    
    private interface VectorExtractorHelper {
        List<Vector> extractVectors(BufferedImage img);
    }
    
    private interface VectorNormalizerHelper {
        void normalizeVectors(List<Vector> vectors);
    }

    private interface GridIntersectionsFinderHelper {
        VectorImageGridIntersections findGridIntersections(List<Vector> vectors);
    }

    private static class VectorExtractorHelperImpl implements VectorExtractorHelper {
        private static final int SQUARE_RADIUS = 3;
        private static final List<int[]> SQUARE_DELTAS;

        /**
         * Initializing Square relative coordinates.
         *
         * That is, a list of coordinates used to traverse the perimeter of a square around a given point.
         * <pre>
         * A → B
         * ↑ x ↓
         * D ← C
         * </pre>
         *
         * @link #VectorExtractorHelperImpl#followLine(BufferedImage, int, int, List<Vector>) followLine
         */
        static {
            List<int[]> sides = new ArrayList<>();
            // A → [B
            Stream.iterate(-SQUARE_RADIUS, n -> n + 1)
                    .limit(2 * SQUARE_RADIUS)
                    .forEach(col -> sides.add(new int[] {-SQUARE_RADIUS, col}));

            // B → [C
            Stream.iterate(-SQUARE_RADIUS, n -> n + 1)
                    .limit(2 * SQUARE_RADIUS)
                    .forEach(row -> sides.add(new int[] {row, SQUARE_RADIUS}));

            // C → [D
            Stream.iterate(SQUARE_RADIUS, n -> n - 1)
                    .limit(2 * SQUARE_RADIUS)
                    .forEach(col -> sides.add(new int[] {SQUARE_RADIUS, col}));

            // D → [A
            Stream.iterate(SQUARE_RADIUS, n -> n - 1)
                    .limit(2 * SQUARE_RADIUS)
                    .forEach(row -> sides.add(new int[] {row, -SQUARE_RADIUS}));

            SQUARE_DELTAS = Collections.unmodifiableList(sides);
        }

        /**
         * Finds every significant border in edgeDetected img, and replace every 
         * straight line by a vector whose endpoints are integer pairs, corresponding 
         * to the right pixels in the image.
         * 
         * @param bImg The EdgeDetected Image.
         * @return 
         */
        @Override
        public List<Vector> extractVectors(BufferedImage bImg) {
            List<Vector> vectors = new ArrayList<>();

            LinearToSquareIndexHelper.ParamHolder ph = new LinearToSquareIndexHelper.ParamHolder(
                    bImg.getHeight(), bImg.getWidth(), bImg.getWidth(), 0, 0);

            Stream.iterate(0, n -> n + 1)
                    .limit(ph.getHeight() * ph.getWidth())
                    .filter(linearIdx -> isPixelBlack(bImg.getRGB(ph.getCol(linearIdx), ph.getRow(linearIdx))))
                    .peek(linearIdx -> followLine(bImg, ph.getCol(linearIdx), ph.getRow(linearIdx), vectors))
                    .forEach(linearIdx -> saveImage(bImg, ph.getRow(linearIdx), ph.getCol(linearIdx)));

            return vectors;
        }

        /**
         * Oversimplify comparison putting a middle bar, meaning if the blue part
         * (first 8 bits) are greater than 128, I consider it white otherwise, black.
         * 
         * @param rgb The pixel color.
         * 
         * @return false if the blue part is stronger than 128. Otherwise true.
         */
        private boolean isPixelBlack(int rgb) {
            return (rgb & RGB_BLUE_COLOR_MASK) < 128;
        }

        /**
         * The strategy is to iterate through a square that surrounds the given pixel
         * i.e. the X in this ascci art representation testing for black content.
         * 
         * Whenever I find a black, the 0 in must be cleaned to prevent vectors overlapping.
         *             -   +
         *     5 4 3 2 1 j 1 2 3 4 5
         *    ______________________
         * -5: C X X X X X X X X X B 
         * -4: X 0 0 0 0 0 0 0 0 0 X 
         * -3: X 0 0 0 0 0 0 0 0 0 X 
         * -2: X 0 0 0 0 0 0 0 0 0 X 
         * -1: X 0 0 0 0 0 0 0 0 0 X 
         *  i: X 0 0 0 0 X ------> X 
         *  1: X 0 0 0 0 0 0 0 0 0 X 
         *  2: X 0 0 0 0 0 0 0 0 0 X 
         *  3: X 0 0 0 0 0 0 0 0 0 X 
         *  4: X 0 0 0 0 0 0 0 0 0 X 
         *  5: D X X X X X X X X X A <-- starting point.
         * 
         * @param bImg BufferedImage that is being vectorized.
         * @param centralCol col or y coord in BufferedImage
         * @param centralRow row or x coord in BufferedImage
         */
        private void followLine(
                BufferedImage bImg, int centralCol, int centralRow, List<Vector> vectors
        ) {
            if (debug) System.out.println("centralCol:" + centralCol + " centralRow: " + centralRow);
            int height = bImg.getHeight();
            int width = bImg.getWidth();
            SQUARE_DELTAS.stream()
                    .map(point -> new int[] {centralRow + point[0], centralCol + point[1]})
                    .filter(point -> isInside(point[0], point[1], width, height))
                    .filter(point -> isPixelBlack(bImg.getRGB(point[1], point[0])))
                    .peek(point -> vectors.add(new Vector(centralCol, centralRow, point[1], point[0])))
                    .peek(point -> eraseSquare(bImg, centralRow, centralCol))
                    // TODO: This line causes stack overflow
                    //.peek(point -> followLine(bImg, point[1], point[0], vectors))
                    .findFirst();
        }

        /**
         * Traverse the complete inner square (i.e. the pixels up to SQUARE_RADIUS -1) 
         * surrounding pixel [i,j] whitening every pixel.
         *
         * Useful for not falling twice in the same block of pixels.
         * 
         * @param bImg The image that is being vectorized.
         * @paramcentralRowi The pixel row
         * @param centralCol The pixel column
         */
        private void eraseSquare(BufferedImage bImg, int centralRow, int centralCol) {
            final int firstRow = centralRow - SQUARE_RADIUS + 1;
            final int firstCol = centralCol - SQUARE_RADIUS + 1;
            final int subSquareWith = (2 * SQUARE_RADIUS);
            LinearToSquareIndexHelper.ParamHolder ph = new LinearToSquareIndexHelper.ParamHolder(
                    bImg.getHeight(), bImg.getWidth(), subSquareWith, firstRow, firstCol);

            Stream.iterate(0, n -> n + 1)
                    .limit((long) Math.pow(subSquareWith, 2))
                    .filter(linearIdx -> isInside(ph, linearIdx))
                    .filter(linearIdx -> isPixelBlack(bImg.getRGB(ph.getCol(linearIdx), ph.getRow(linearIdx))))
                    .forEach(linearIdx -> bImg.setRGB(ph.getCol(linearIdx), ph.getRow(linearIdx), Color.WHITE.getRGB()));
        }

        private boolean isInside(int row, int col, int width, int height) {
            return row > 0 &&  col > 0 && row < height &&  col < width;
        }

        private boolean isInside(LinearToSquareIndexHelper.ParamHolder ph, int linearIdx) {
            int row = ph.getRow(linearIdx);
            int col = ph.getCol(linearIdx);
            return row > 0 && col > 0 && row < ph.getHeight() && col < ph.getWidth();
        }
    }
    
    private class VectorNormalizerHelperImpl implements VectorNormalizerHelper {

        /**
         * Translates pixels coordinates, present in the List<Vector> vectors into 
         * real Cartesian coordinates. 
         * 
         * Image pixels Layout:
         *   00____3______ 
         *    |    |
         *    |    |
         *  3 |----|_
         *    |    / \ 
         *  8 |   / * \
         * 16 |  /_____\
         * 
         * Cartesian layout:
         *         Y 
         *          |5
         *         /|\ 
         *  X_____/_|0\________
         *       /__|__\      
         *          |-8
         * 
         * @param vectors 
         */
        @Override
        public void normalizeVectors(List<Vector> vectors) {
            adjustOriginToCenterOfMass(vectors); 
            adjustAverageDistanceToOne(vectors);
        }

        private void adjustOriginToCenterOfMass(List<Vector> vectors) {
            float mediaX = 0, mediaY = 0;
            for (Vector vector : vectors) {
                mediaX += vector.endAx + vector.endBx;
                mediaY += vector.endAy + vector.endBy;
            }
            mediaX = mediaX / (2 * vectors.size());
            mediaY = mediaY / (2 * vectors.size());
            ListIterator<Vector> it = vectors.listIterator();
            while (it.hasNext()) {
                Vector vector = it.next();
                it.set(new Vector(
                        vector.endAx - mediaX, mediaY - vector.endAy, 
                        vector.endBx - mediaX, mediaY - vector.endBy 
                ));
            }
        }

        private void adjustAverageDistanceToOne(List<Vector> vectors) {
            float mediaDist = 0;
            for (Vector vector : vectors) {
                mediaDist += Math.sqrt((vector.endAx * vector.endAx) + (vector.endAy * vector.endAy));
                mediaDist += Math.sqrt((vector.endBx * vector.endBx) + (vector.endBy * vector.endBy));
            }
            mediaDist = mediaDist / (2 * vectors.size());
            ListIterator<Vector> it = vectors.listIterator();
            while (it.hasNext()) {
                Vector vector = it.next();
                it.set(new Vector(
                        vector.endAx / mediaDist, vector.endAy / mediaDist, 
                        vector.endBx / mediaDist, vector.endBy / mediaDist
                ));
            }
        }
    }

    private class GridIntersectionsFinderImpl implements GridIntersectionsFinderHelper {
        final VectorizeStrategy.Grid grid;

        private GridIntersectionsFinderImpl(VectorizeStrategy.Grid grid) {
            this.grid = grid;
        }

        @Override
        public VectorImageGridIntersections findGridIntersections(List<Vector> vectors) {

            VectorImageGridIntersections.Builder b = new VectorImageGridIntersections.Builder();

            for (Vector vector : vectors) {
                findCrossedGridLines(vector, b);
            }
            return b.build();
        }

        private void findCrossedGridLines(Vector vector, VectorImageGridIntersections.Builder builder) {
            for (float f : grid.getLines()) {
                if ((vector.endAx <= f && vector.endBx >= f)
                        || (vector.endAx >= f && vector.endBx <= f)
                ) {
                    builder.addXintersect(f, getIntersection(f, vector.endAy, vector.endBy));
                }
                if ((vector.endAy <= f && vector.endBy >= f)
                        || (vector.endAy >= f && vector.endBy <= f)
                ) {
                    builder.addYintersect(f, getIntersection(f, vector.endAx, vector.endBx));
                }
            }
        }

        /**
         * As in the current implementation vectors have 5 pixels length, it's 
         * cheaper and has acceptable accuracy to return the midpoint.
         * 
         * @param gridLine 
         * @param endA
         * @param endB
         * 
         * 
         * @return 
         */
        private Float getIntersection(float gridLine, float endA, float endB) {
            return (endB + endA) / 2;
        }
    }
}
