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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
    private static final int RUNLEVEL_DEBUG = 1;
    private static final int RUNLEVEL_INFO = 2;
    private static final int RUNLEVEL_ERROR = 3;
    
    int debugRunnignLevel = RUNLEVEL_INFO;
    
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
        VectorizeStrategy vectorizer = VectorizeStrategy.ConcreteStrategy.DEFAULT;
        VectorImageGridIntersections grid, gridAgainst;

        Stopwatch.start("loadingAgainstPattern");
        gridAgainst = getCmpAgainst("MercedesB.png");
        Stopwatch.end("loadingAgainstPattern");
        
        Stopwatch.start("InitComparator");
        VectorImageComparator comparator = new VectorImageComparator(
                gridAgainst, ShapeComparisonStrategy.ConcreteStrategy.DEFAULT
            );
        Stopwatch.end("InitComparator");
        
        out("Pattern To Sort Against: \n" + gridAgainst, RUNLEVEL_INFO);
        
        Stopwatch.start("loadingResources");
        List<VectorImageGridIntersections> patternsDB = new ArrayList<>();
        loadResources(patternsDB);
        Stopwatch.end("loadingResources");
        
        Stopwatch.start("sorting");
        out("Sorting...", RUNLEVEL_INFO);
        Collections.sort(patternsDB, comparator);
        Stopwatch.end("sorting");
        
        printAll(patternsDB, comparator, gridAgainst);
    }

    private VectorImageGridIntersections getCmpAgainst(String imageName) {
        String shapeAgainst = imageName;
        out("Comparing against " + shapeAgainst, RUNLEVEL_INFO);
        MarvinImage img = MarvinImageIO.loadImage("src/main/resources/" + shapeAgainst);
        return VectorizeStrategy.ConcreteStrategy.DEFAULT.processImage(img);
    }

    private void loadResources(List<VectorImageGridIntersections> patternsDB) {
        String cacheFilename = "/tmp/patternsCache.ser";
        File cache = new File(cacheFilename);
        if (cache.exists()) {
            patternsDB.addAll(unserializeCache(cacheFilename));
            out("loaded from cache", RUNLEVEL_INFO);
            return;
        }
        File f = new File("src/main/resources/");
        ArrayList<String> shapes = new ArrayList<>(Arrays.asList(f.list()));
        MarvinImage img;
        VectorImageGridIntersections grid;
        for (String shape : shapes) {
            try {
                img = MarvinImageIO.loadImage("src/main/resources/" + shape);
            } catch (Exception e) {
                out("No pude cargar la imagen: " + shape, RUNLEVEL_ERROR);
                continue;
            }
            out("Loading: " + shape, RUNLEVEL_INFO);
            grid = VectorizeStrategy.ConcreteStrategy.DEFAULT.processImage(img);
            patternsDB.add(grid);
            out(grid, RUNLEVEL_DEBUG);
        }
        serializeChache(cacheFilename, patternsDB);
    }

    private void printAll(
            List<VectorImageGridIntersections> patternsDB, 
            VectorImageComparator comparator,
            VectorImageGridIntersections gridAgainst
    ) {
        
        for (int i = patternsDB.size()-1; i > patternsDB.size() - 10; i--) {
            out(patternsDB.get(i), RUNLEVEL_INFO);
            out(comparator.getDifference(patternsDB.get(i), gridAgainst), RUNLEVEL_INFO);
        }
        
        Stopwatch.getRunning().entrySet().stream().forEach(e -> {
            out(e.getKey() + " => nanos: \t" + e.getValue().getElapsedTime(), RUNLEVEL_INFO);
        });
        out("Done, the resource database has " + patternsDB.size() + " loadable images", RUNLEVEL_INFO);
    }

    private List<VectorImageGridIntersections> unserializeCache(String cache) {
        List<VectorImageGridIntersections> patternsDB;
        try {
            FileInputStream fileIn = new FileInputStream(cache);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            patternsDB = (List<VectorImageGridIntersections>) in.readObject();
            in.close();
            fileIn.close();
        } catch (IOException i) {
            i.printStackTrace();
            return null;
        } catch (ClassNotFoundException c) {
            out("Employee class not found", RUNLEVEL_ERROR);
            c.printStackTrace();
            return null;
        }
        return patternsDB;
    }

    private void serializeChache(
            String cacheFilename, 
            List<VectorImageGridIntersections> patternsDB
    ) {
        try {
            FileOutputStream fileOut
                    = new FileOutputStream(cacheFilename);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(patternsDB);
            out.close();
            fileOut.close();
            out("Serialized data is saved in " + cacheFilename, RUNLEVEL_INFO);

        } catch (IOException i) {
            i.printStackTrace();
        }
    }

    private void out(Object string, int level) {
        if (level >= debugRunnignLevel) {
            System.out.println(string);
        }
    }
}
