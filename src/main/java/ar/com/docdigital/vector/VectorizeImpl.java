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
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import javax.imageio.ImageIO;
import marvin.image.MarvinColorModelConverter;
import marvin.image.MarvinImage;
import marvin.io.MarvinImageIO;
import marvin.plugin.MarvinImagePlugin;
import marvin.util.MarvinPluginLoader;

/**
 * Default implementation for vectorization. 
 * 
 * It uses Marvin FW to detect object boundary, and then follows the lines generating
 * vectors of length {@link #SQUARE_RADIUS}. Then normalize it so as to make it 
 * comparable with other shapes.
 * 
 * @author juan.fernandez
 */
public class VectorizeImpl implements Vectorize {

    private static final boolean debug = false;
    
    private static final String MARV_EDGE_PLUGIN = "org.marvinproject.image.morphological.boundary.jar";
    private static final MarvinImagePlugin MARVIN_PLUGIN = MarvinPluginLoader.loadImagePlugin(MARV_EDGE_PLUGIN);
    private static final int RGB_BLUE_COLOR_MASK = 0xff;
    private static final int RGB_OPAQUE_WHITE = 0xffffffff;
    private static final int SQUARE_RADIUS = 5;
    private static final List<int[]> SQUARE_DELTAS;
    
    private final VectorExtractorHelper vectorExtractor;
    private final VectorNormalizerHelper vectorNormalizer;
    private final GridIntersectionsFinderHelper gridIntersectionsFinder;
            
    /**
     * Initializing Square relative coordinates.
     * 
     * @link #VectorExtractorHelperImpl#followLine(BufferedImage, int, int, List<Vector>) followLine 
     */
    static {
        List<int[]> list = new ArrayList<>();
        int row = SQUARE_RADIUS;
        int col = SQUARE_RADIUS;
        for (; row >= -SQUARE_RADIUS; row--) {
            list.add(new int[] {row, col});
        }
        row++;
        // Now we are in B
        
        for (; col >= -SQUARE_RADIUS; col--) {
            list.add(new int[] {row, col});
        }
        col++;
        // Now we are in C
        
        for (; row <= SQUARE_RADIUS; row++) {
            list.add(new int[] {row, col});
        }
        row--;
        // Now we are in D
        
        for (; col < SQUARE_RADIUS; col++) {
            list.add(new int[] {row, col});
        }
        SQUARE_DELTAS = Collections.unmodifiableList(list);
    }

    public VectorizeImpl() {
        vectorExtractor = this.new VectorExtractorHelperImpl();
        vectorNormalizer = this.new VectorNormalizerHelperImpl();
        gridIntersectionsFinder = this.new GridIntersectionsFinderImpl();
    }
    
    @Override
    public VectorImageGridIntersections processImage(MarvinImage img) {
        MarvinImage binImg = MarvinColorModelConverter.rgbToBinary(img, 200);
        MarvinImageIO.saveImage(binImg, "/tmp/MarvinImage.png");
        MARVIN_PLUGIN.process(binImg.clone(), binImg);
        MarvinImageIO.saveImage(binImg, "/tmp/MarvinImage1.png");
        List<Vector> vectors = vectorExtractor.extractVectors(binImg);
        System.out.println(vectors);
        vectorNormalizer.normalizeVectors(vectors);
        return gridIntersectionsFinder.findGridIntersections(vectors);
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
        List<Vector> extractVectors(MarvinImage img);
    }
    
    private interface VectorNormalizerHelper {
        void normalizeVectors(List<Vector> vectors);
    }

    private interface GridIntersectionsFinderHelper {
        VectorImageGridIntersections findGridIntersections(List<Vector> vectors);
    }

    private class VectorExtractorHelperImpl implements VectorExtractorHelper {

        /**
         * Finds every significant border in edgeDetected img, and replace every 
         * straight line by a vector whose endpoints are integer pairs, corresponding 
         * to the right pixels in the image.
         * 
         * @param img The EdgeDetected Image.
         * @return 
         */
        @Override
        public List<Vector> extractVectors(MarvinImage img) {
            BufferedImage bImg = img.getBufferedImage();
            List<Vector> vectors = new ArrayList<>();
            int height = bImg.getHeight();
            int width = bImg.getWidth();
            for (int row = 0; row < height; row++) {
                for (int col = 0; col < width; col++) {
                        if (isPixelBlack(bImg.getRGB(col, row))) {
                            followLine(bImg, col, row, vectors);
                            saveImage(bImg, row, col);
                        }
                }
            }
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
            System.out.println("centralCol:" + centralCol + " centralRow: " + centralRow);
            int height = bImg.getHeight();
            int width = bImg.getWidth();
            // This leaves us in (A)

            for (int[] coords : SQUARE_DELTAS) {
                int row = centralRow + coords[0];
                int col = centralCol + coords[1];
                if (row > 0 &&  col > 0 && row < height &&  col < width 
                        && isPixelBlack(bImg.getRGB(col, row))
                ) {
                    vectors.add(new Vector(centralCol, centralRow, col, row));
                    eraseSquare(bImg, centralRow, centralCol);
                    followLine(bImg, col, row, vectors);
                    return;
                }
            }
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
            int height = bImg.getHeight();
            int width = bImg.getWidth();
            for (int row = centralRow - SQUARE_RADIUS + 1; row < centralRow + SQUARE_RADIUS; row++) {
                for (int col = centralCol - SQUARE_RADIUS + 1; col < centralCol + SQUARE_RADIUS; col++) {
                    if (row > 0 &&  col > 0 && row < height &&  col < width 
                        && isPixelBlack(bImg.getRGB(col, row))
                    ) {
                        bImg.setRGB(col, row, RGB_OPAQUE_WHITE);
                    }
                }
            }
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
        
        @Override
        public VectorImageGridIntersections findGridIntersections(List<Vector> vectors) {

            VectorImageGridIntersections.Builder b = new VectorImageGridIntersections.Builder();

            for (Vector vector : vectors) {
                findCrossedGridLines(vector, b);
            }
            return b.build();
        }

        private void findCrossedGridLines(Vector vector, VectorImageGridIntersections.Builder builder) {
            Vectorize.Grid grid = Config.getDefaultGrid();
                
            for (float f : grid.getLines()) {
                if ((vector.endAx <= f && vector.endBx >= f)
                        || (vector.endAx >= f && vector.endBx <= f)
                ) {
                    builder.addXintersect(f, getIntersection(f, vector.endAx, vector.endBx));
                }
                if ((vector.endAy <= f && vector.endBy >= f)
                        || (vector.endAy >= f && vector.endBy <= f)
                ) {
                    builder.addXintersect(f, getIntersection(f, vector.endAy, vector.endBy));
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
