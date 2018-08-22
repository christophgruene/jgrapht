/*
 * (C) Copyright 2018-2018, by Oliver Feith and Contributors.
 *
 * JGraphT : a free Java graph-theory library
 *
 * This program and the accompanying materials are dual-licensed under
 * either
 *
 * (a) the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation, or (at your option) any
 * later version.
 *
 * or (per the licensee's choosing)
 *
 * (b) the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation.
 */
package org.jgrapht.alg.color;


import static org.junit.Assert.assertNotEquals;

import java.util.*;

import org.jgrapht.*;
import org.jgrapht.generate.*;
import org.jgrapht.graph.*;
import org.jgrapht.util.*;
import org.junit.*;

/**
 * Tests for the individualization-refinement algorithm.
 *
 * @author Oliver Feith
 */
public class IndividualizationRefinementAlgorithmTest
{

    @Test
    public void testC5Discreteness() {
        Graph<Integer, DefaultEdge> undirectedCycle5 = new SimpleGraph<>(DefaultEdge.class);
        Graph<Integer, DefaultEdge> directedCycle5 = new DefaultDirectedGraph<>(DefaultEdge.class);

        // Create a cycle graph (directed and undirected) on 5 vertices. IR should need at least 2 rounds of individualization / 3 rounds of refining.

        Graphs.addAllVertices(undirectedCycle5, Arrays.asList(1, 2, 3, 4, 5));
        Graphs.addAllVertices(directedCycle5, Arrays.asList(1, 2, 3, 4, 5));

        undirectedCycle5.addEdge(1, 2);
        undirectedCycle5.addEdge(2, 3);
        undirectedCycle5.addEdge(3, 4);
        undirectedCycle5.addEdge(4, 5);
        undirectedCycle5.addEdge(5, 1);
        
        directedCycle5.addEdge(1, 2);
        directedCycle5.addEdge(2, 3);
        directedCycle5.addEdge(3, 4);
        directedCycle5.addEdge(4, 5);
        directedCycle5.addEdge(5, 1);

        IndividualizationRefinementAlgorithm<Integer, DefaultEdge> undirectedIR = new IndividualizationRefinementAlgorithm<>(undirectedCycle5);
        Map<Integer, Integer> undirectedColors = undirectedIR.getColoring().getColors();
        
        IndividualizationRefinementAlgorithm<Integer, DefaultEdge> directedIR = new IndividualizationRefinementAlgorithm<>(directedCycle5);
        Map<Integer, Integer> directedColors = directedIR.getColoring().getColors();

        // The resulting coloring should be discrete

        for(int i = 1; i < 4; i++) {
            for(int j = i + 1; j < 5; j++) {
                assertNotEquals(undirectedColors.get(i).intValue(), undirectedColors.get(j).intValue());
                assertNotEquals(directedColors.get(i).intValue(), directedColors.get(j).intValue());
            }
        }
    }
    
    @Test
    public void testCompleteGraph() {
        Graph<Integer, DefaultEdge> completeGraph = new SimpleGraph<>(DefaultEdge.class);
        
        Graphs.addAllVertices(completeGraph, Arrays.asList(1, 2, 3, 4, 5, 6, 8, 9, 10));
        
        IndividualizationRefinementAlgorithm<Integer, DefaultEdge> IR = new IndividualizationRefinementAlgorithm<>(completeGraph);
        Map<Integer, Integer> colors = IR.getColoring().getColors();
        
        for(Integer v : completeGraph.vertexSet()) {
            for(Integer w : completeGraph.vertexSet()) {
                if(v != w) {
                    assertNotEquals(colors.get(v).intValue(), colors.get(w).intValue());
                } else {
                    continue;
                }
            }
        }
    }
    
    @Test
    public void testRandomDiscreteness() {
        // Test random graphs for discreteness for n = 100 vertices and p from {0.1 * i | 1 < i < 10}
        for(int i = 1; i < 10; i++) {
            GnpRandomGraphGenerator<Integer, DefaultEdge> generator = new GnpRandomGraphGenerator<>(100, 0.1 * i);
            
            Graph<Integer, DefaultEdge> randomGraph = new SimpleGraph<>(SupplierUtil.createIntegerSupplier(100), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
            generator.generateGraph(randomGraph);
            
            IndividualizationRefinementAlgorithm<Integer, DefaultEdge> IR = new IndividualizationRefinementAlgorithm<>(randomGraph);
            Map<Integer, Integer> colors = IR.getColoring().getColors();
            
            for(Integer v : randomGraph.vertexSet()) {
                for(Integer w : randomGraph.vertexSet()) {
                    if(v != w) {
                        assertNotEquals(colors.get(v).intValue(), colors.get(w).intValue());
                    } else {
                        continue;
                    }
                }
            }
        }
    }

}
