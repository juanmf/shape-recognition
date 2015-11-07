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

import java.util.List;
import java.util.Map;
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

    /**
     * Test of getConstXintersect method, of class VectorImageGridIntersections.
     */
    @Test
    public void testGetConstXintersect() {
        System.out.println("getConstXintersect");
        VectorImageGridIntersections instance = null;
        Map<VectorizeStrategy.Grid, Map<Float, List<Float>>> expResult = null;
        Map<VectorizeStrategy.Grid, Map<Float, List<Float>>> result = instance.getConstXintersect();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getConstYintersect method, of class VectorImageGridIntersections.
     */
    @Test
    public void testGetConstYintersect() {
        System.out.println("getConstYintersect");
        VectorImageGridIntersections instance = null;
        Map<VectorizeStrategy.Grid, Map<Float, List<Float>>> expResult = null;
        Map<VectorizeStrategy.Grid, Map<Float, List<Float>>> result = instance.getConstYintersect();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of toString method, of class VectorImageGridIntersections.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        VectorImageGridIntersections instance = null;
        String expResult = "";
        String result = instance.toString();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getId method, of class VectorImageGridIntersections.
     */
    @Test
    public void testGetId() {
        System.out.println("getId");
        VectorImageGridIntersections instance = null;
        Long expResult = null;
        Long result = instance.getId();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of hashCode method, of class VectorImageGridIntersections.
     */
    @Test
    public void testHashCode() {
        System.out.println("hashCode");
        VectorImageGridIntersections instance = null;
        int expResult = 0;
        int result = instance.hashCode();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of equals method, of class VectorImageGridIntersections.
     */
    @Test
    public void testEquals() {
        System.out.println("equals");
        Object obj = null;
        VectorImageGridIntersections instance = null;
        boolean expResult = false;
        boolean result = instance.equals(obj);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
