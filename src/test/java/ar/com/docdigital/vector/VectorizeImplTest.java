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

//Let's import Mockito statically so that the code looks clearer
import java.io.File;
import marvin.image.MarvinImage;
import marvin.io.MarvinImageIO;
 import static org.mockito.Mockito.*;
import org.testng.Assert;

/**
 *
 * @author juan.fernandez
 */
public class VectorizeImplTest {
    //mock creation
//    List mockedList = mock(List.class);
//
//    //using mock object
//    mockedList.add("one");
//    mockedList.clear();
//
//    //verification
//    verify(mockedList).add("one");
//    verify(mockedList).clear();
    
    public VectorizeImplTest() {
    }

    /**
     * Test of processImage method, of class VectorizeImpl.
     */
    @org.junit.Test
    public void testProcessImage() {
        VectorizeStrategy vectorizer;
        MarvinImage img;
        VectorImageGridIntersections processImage;
        
//        MarvinImage img = MarvinImageIO.loadImage("src/main/resources/batman.png");
//        MarvinImage img = MarvinImageIO.loadImage("src/main/resources/bulon.jpg");
        img = MarvinImageIO.loadImage("src/main/resources/shape1.png");
        System.out.println(img.getHeight());
        vectorizer = VectorizeStrategy.ConcreteStrategy.DEFAULT;
        processImage = vectorizer.processImage(img);
        System.out.println(processImage);
        
        img = MarvinImageIO.loadImage("src/main/resources/shape2.png");
        System.out.println(img.getHeight());
        vectorizer = VectorizeStrategy.ConcreteStrategy.DEFAULT;
        processImage = vectorizer.processImage(img);
        System.out.println(processImage);
        
        img = MarvinImageIO.loadImage("src/main/resources/shape3.png");
        System.out.println(img.getHeight());
        vectorizer = VectorizeStrategy.ConcreteStrategy.DEFAULT;
        processImage = vectorizer.processImage(img);
        System.out.println(processImage);
        
        File file = new File("/tmp/MarvinImage.png");
        Assert.assertTrue(file.exists());
    }    
}
