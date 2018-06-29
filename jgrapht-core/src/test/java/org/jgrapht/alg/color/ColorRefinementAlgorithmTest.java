package org.jgrapht.alg.color;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.*;
import java.util.stream.Collectors;

import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.junit.*;

/**
 * Tests for the Color-refinement algorithm.
 * 
 * @author Oliver Feith
 * @author Abdallah Atouani
 */
public class ColorRefinementAlgorithmTest 
{

    @Test
    public void testTree() {
        Graph<Integer, DefaultEdge> tree = new SimpleGraph<>(DefaultEdge.class);
        
        // Tree has the form ._._|_._.
        
        Graphs.addAllVertices(tree, Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8));
        
        tree.addEdge(1, 2);
        tree.addEdge(2, 3);
        tree.addEdge(3, 4);
        tree.addEdge(4, 5);
        tree.addEdge(3, 6);
        tree.addEdge(6, 7);
        tree.addEdge(6, 8);
        
        ColorRefinementAlgorithm<Integer, DefaultEdge> CR = new ColorRefinementAlgorithm<>(tree);
        Map<Integer, Integer> colors = CR.getColoring().getColors();
        
        // symmetric pairs around 3 should have the same color and different colors otherwise
        
        assertEquals(colors.get(1).intValue(), colors.get(5).intValue());
        assertNotEquals(colors.get(1).intValue(), colors.get(7).intValue());
        assertEquals(colors.get(2).intValue(), colors.get(4).intValue());
        assertEquals(colors.get(7).intValue(), colors.get(8).intValue());
        assertNotEquals(colors.get(1).intValue(), colors.get(2).intValue());
        assertNotEquals(colors.get(2).intValue(), colors.get(3).intValue());
        assertNotEquals(colors.get(3).intValue(), colors.get(6).intValue());
    }
    
    @Test
    public void testRegular() {
        Graph<Integer, DefaultEdge> regularGraph = new SimpleGraph<>(DefaultEdge.class);
        
        // Graph should be the disjoint union of 2 triangles
        
        Graphs.addAllVertices(regularGraph, Arrays.asList(1, 2, 3, 4, 5, 6));
        
        regularGraph.addEdge(1, 2);
        regularGraph.addEdge(2, 3);
        regularGraph.addEdge(3, 1);
        
        regularGraph.addEdge(4, 5);
        regularGraph.addEdge(5, 6);
        regularGraph.addEdge(6, 4);
        
        ColorRefinementAlgorithm<Integer, DefaultEdge> CR = new ColorRefinementAlgorithm<>(regularGraph);
        Map<Integer, Integer> colors = CR.getColoring().getColors();
        
        // all vertices should have the same color
        
        assertEquals(colors.get(1).intValue(), colors.get(2).intValue());
        assertEquals(colors.get(1).intValue(), colors.get(3).intValue());
        assertEquals(colors.get(1).intValue(), colors.get(4).intValue());
        assertEquals(colors.get(1).intValue(), colors.get(5).intValue());
        assertEquals(colors.get(1).intValue(), colors.get(6).intValue());
    }
    
    @Test
    public void testGraph1() {
        Graph<Integer, DefaultEdge> graph1 = new SimpleGraph<>(DefaultEdge.class);
        
        
        Graphs.addAllVertices(graph1, Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11));
        
        graph1.addEdge(1, 2);
        
        graph1.addEdge(2, 3);
        graph1.addEdge(2, 4);
        graph1.addEdge(2, 6);
        graph1.addEdge(2, 11);
        
        graph1.addEdge(3, 4);
        
        graph1.addEdge(4, 6);
        
        graph1.addEdge(5, 6);
        
        graph1.addEdge(6, 7);
        
        graph1.addEdge(7, 8);
        
        graph1.addEdge(8, 9);
        graph1.addEdge(8, 10);
        graph1.addEdge(8, 11);
        
        graph1.addEdge(9, 10);
        graph1.addEdge(9, 11);
        
        graph1.addEdge(10, 11);
        
        ColorRefinementAlgorithm<Integer, DefaultEdge> CR = new ColorRefinementAlgorithm<>(graph1);
        Map<Integer, Integer> colors = CR.getColoring().getColors();
        
        // 9 and 10 should have the same color, all others should have distinct colors

        for(int i = 1; i < 11; i++) {
            for(int j = i + 1; j <= 11; j++) {
                if(i != 9 || j != 10) {
                    assertNotEquals(colors.get(i).intValue(), colors.get(j).intValue());
                }
            }
        }
        assertEquals(colors.get(9).intValue(), colors.get(10).intValue());
    }

    @Test
    public void testTreeMaximalOneChild() {
        Graph<Integer, DefaultEdge> graph = new DefaultUndirectedGraph<>(DefaultEdge.class);

        Graphs.addAllVertices(graph, Arrays.asList(1, 2, 3, 4, 5));
        graph.addEdge(1, 2);
        graph.addEdge(2, 3);
        graph.addEdge(3, 4);
        graph.addEdge(4, 5);

        ColorRefinementAlgorithm<Integer, DefaultEdge> colorRefinementAlgorithm = new ColorRefinementAlgorithm<>(graph);
        int numberOfColors = colorRefinementAlgorithm.getColoring().getNumberColors();
        Map<Integer, Integer> colors = colorRefinementAlgorithm.getColoring().getColors();

        Map<Integer, Integer> firstGroup = colors.entrySet().stream()
                .filter(x -> x.getValue().equals(colors.get(1).intValue()))
                .collect(Collectors.toMap(x -> x.getKey(), x -> x.getValue()));

        Map<Integer, Integer> secondGroup = colors.entrySet().stream()
                .filter(x -> x.getValue().equals(colors.get(2).intValue()))
                .collect(Collectors.toMap(x -> x.getKey(), x -> x.getValue()));

        assertEquals(2, firstGroup.size());
        assertTrue(firstGroup.keySet().containsAll(Arrays.asList(1, 5)));
        assertEquals(2, secondGroup.size());
        assertTrue(secondGroup.keySet().containsAll(Arrays.asList(2, 4)));

        assertEquals(3, numberOfColors);
    }

}
