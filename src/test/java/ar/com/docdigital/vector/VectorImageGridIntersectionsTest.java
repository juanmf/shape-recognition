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

import ar.com.docdigital.vector.comparator.ShapeComparisonStrategy;
import ar.com.docdigital.vector.comparator.VectorImageComparator;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import marvin.image.MarvinImage;
import marvin.io.MarvinImageIO;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 *
 * @author juan.fernandez
 */
public class VectorImageGridIntersectionsTest {

    public VectorImageGridIntersectionsTest() {
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

    @org.junit.Test
    public void testSerialization() {

        VectorizeStrategy vectorizer;
        MarvinImage img;
        VectorImageGridIntersections grid, deserializedGrid;

//        MarvinImage img = MarvinImageIO.loadImage("src/main/resources/batman.png");
//        MarvinImage img = MarvinImageIO.loadImage("src/main/resources/bulon.jpg");
        img = MarvinImageIO.loadImage("src/main/resources/shape1.png");
        System.out.println(img.getHeight());
        vectorizer = VectorizeStrategy.ConcreteStrategy.DEFAULT;
        grid = vectorizer.processImage(img);
        System.out.println(grid);

        try {
            FileOutputStream fileOut
                    = new FileOutputStream("/tmp/shape.ser");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(grid);
            out.close();
            fileOut.close();
            System.out.println("Serialized data is saved in /tmp/shape.ser");

        } catch (IOException i) {
            i.printStackTrace();
        }

        try {
            FileInputStream fileIn = new FileInputStream("/tmp/shape.ser");
            ObjectInputStream in = new ObjectInputStream(fileIn);
            deserializedGrid = (VectorImageGridIntersections) in.readObject();
            in.close();
            fileIn.close();
        } catch (IOException i) {
            i.printStackTrace();
            return;
        } catch (ClassNotFoundException c) {
            System.out.println("Employee class not found");
            c.printStackTrace();
            return;
        }
        
        VectorImageComparator comparator = new VectorImageComparator(
                grid, ShapeComparisonStrategy.ConcreteStrategy.DEFAULT
            );
        Assert.assertEquals(grid, deserializedGrid);
        Assert.assertEquals(new Float(0f), comparator.getDifference(deserializedGrid, grid));
        System.out.println(deserializedGrid);
        
    }

}
