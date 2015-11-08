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

import ar.com.docdigital.testing.test.util.Stopwatch;
import ar.com.docdigital.vector.VectorImageGridIntersections;
import ar.com.docdigital.vector.VectorizeStrategy;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import marvin.image.MarvinImage;
import marvin.io.MarvinImageIO;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author juan.fernandez
 */
public class VectorImageComparatorTest {
    
    public VectorImageComparatorTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of compare method, of class VectorImageComparator.
     */
    @Test
    public void testCompare() {
        VectorizeStrategy vectorizer;
        MarvinImage img;
        VectorImageGridIntersections grid, gridAgainst;
        
        Stopwatch.start("loadingAgainstPattern");
        String shapeAgainst = "MercedesB.png";
        img = MarvinImageIO.loadImage("src/main/resources/" + shapeAgainst);
        System.out.println("Comparing against " + shapeAgainst);

        vectorizer = VectorizeStrategy.ConcreteStrategy.DEFAULT;
        gridAgainst = vectorizer.processImage(img);
        Stopwatch.end("loadingAgainstPattern");
        
        Stopwatch.start("InitComparator");
        VectorImageComparator comparator = new VectorImageComparator(
                gridAgainst, ShapeComparisonStrategy.ConcreteStrategy.DEFAULT
            );
        Stopwatch.end("InitComparator");
        
        System.out.println("Pattern To Sort Against: \n" + gridAgainst);
        
        Stopwatch.start("loadingResources");
        List<VectorImageGridIntersections> patternsDB = new ArrayList<>();
        File f = new File("src/main/resources/");
        ArrayList<String> shapes = new ArrayList<>(Arrays.asList(f.list()));
        for (String shape : shapes) {
            try {
                img = MarvinImageIO.loadImage("src/main/resources/" + shape);
            } catch (Exception e) {
                System.out.println("No pude cargar la imagen: " + shape);
                continue;
            }
            System.out.println("Loading: " + shape);
            vectorizer = VectorizeStrategy.ConcreteStrategy.DEFAULT;
            grid = vectorizer.processImage(img);
            patternsDB.add(grid);
            System.out.println(grid);
        }
        Stopwatch.end("loadingResources");
        
        Stopwatch.start("sorting");
        System.out.println("Sorting...");
        Collections.sort(patternsDB, comparator);
        Stopwatch.end("sorting");
        
        patternsDB.forEach((e) -> System.out.println(e));
        patternsDB.forEach((e) -> System.out.println(comparator.getDifference(e, gridAgainst)));
        
        Stopwatch.getRunning().entrySet().stream().forEach(e -> {
            System.out.println(e.getKey() + " => nanos: \t" + e.getValue().getElapsedTime());
        });
        System.out.println("Done, the resource database has " + patternsDB.size() + " loadable images");
    }
}
