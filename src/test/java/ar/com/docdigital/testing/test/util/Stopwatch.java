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
package ar.com.docdigital.testing.test.util;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * Usage:
 * 
 * Stopwatch.start("watchName");
 * ...
 * Stopwatch.end("watchName");
 * 
 * Stopwatch.start("loadingResource");
 * ...
 * Stopwatch.end("loadingResource");
 * ...
 * 
 * Stopwatch.getRunning().entrySet().stream().forEach(e -> {
 *            System.out.println(e.getKey() + " => nanos: " + e.getValue().getElapsedTime());
 *      });
 * 
 * Output would be:
 * 
 * InitComparator => nanos: 1800910
 * loadingAgainstPattern => nanos: 187572841
 * loadingResources => nanos: 1013792631
 * sorting => nanos: 3599532
 * 
 * @author juan.fernandez
 */
public class Stopwatch {
    private static final Map<String, Stopwatch> RUNNING = new TreeMap<>();
    private long startedAt;
    private long endedAt;
    
    public static Map<String, Stopwatch> getRunning() {
        return Collections.unmodifiableMap(RUNNING);
    }
    
    public static void start(String name) {
        RUNNING.put(name, new Stopwatch().start());
    }
    
    public static void end(String name) {
        Stopwatch task = RUNNING.get(name);
        if (task == null) {
            throw new IllegalArgumentException("no task aftre name: " + name);
        }
        task.end();
    }
    
    public static long getElapsedTime(String name) {
        Stopwatch task = RUNNING.get(name);
        if (task == null) {
            throw new IllegalArgumentException("no task aftre name: " + name);
        }
        return task.getElapsedTime();
    }
    
    private Stopwatch start() {
        startedAt = System.nanoTime();
        return this;
    }
    
    private Stopwatch end() {
        endedAt = System.nanoTime();
        return this;
    }
    
    public long getElapsedTime() {
        if (endedAt == 0 || startedAt == 0) {
            throw new IllegalStateException("either startedAt or endedAt are not set.");
        }
        return endedAt - startedAt;
    }
}
