/*
 * (C) Copyright 2018-2018, by Christoph Grüne and Contributors.
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
package org.jgrapht.alg.isomorphism;

import org.jgrapht.Graph;
import org.jgrapht.GraphMapping;
import org.jgrapht.GraphType;
import org.jgrapht.alg.interfaces.VertexColoringAlgorithm;

import java.util.*;

/**
 * Base implementation of the color refinement algorithms using its feature of detecting
 * <a href="http://mathworld.wolfram.com/GraphIsomorphism.html">isomorphism between two graphs</a>
 * as described in
 * C. Berkholz, P. Bonsma, and M. Grohe.  Tight lower and upper bounds for the complexity of canonical
 * colour refinement. Theory of Computing Systems,doi:10.1007/s00224-016-9686-0, 2016 (color refinement)
 * and
 * reference will follow (individualization refinement).
 *
 * @param <V> the vertex type
 * @param <E> the edge type
 *
 * @author Christoph Grüne
 */
public abstract class RefinementAbstractIsomorphismInspector<V, E> implements IsomorphismInspector<V, E> {

    /**
     * The graphs to tests for isomorphism
     */
    protected Graph<V, E> graph1, graph2;

    /**
     * The isomorphism that is calculated by this color refinement isomorphism inspector
     */
    protected GraphMapping<V, E> isomorphicGraphMapping;

    /**
     * contains whether the graphs are isomorphic or not.
     * If we cannot decide whether they are isomorphic the value will be not present.
     */
    protected Boolean isIsomorphic;

    /**
     * contains whether the isomorphism test is executed to ensure that every operation is defined all the time
     */
    protected boolean isomorphismTestExecuted;

    /**
     * Construct a new base implementation of the Refinement isomorphism inspector.
     *
     * @param graph1 the first graph
     * @param graph2 the second graph
     */
    public RefinementAbstractIsomorphismInspector(Graph<V, E> graph1, Graph<V, E> graph2) {

        GraphType type1 = graph1.getType();
        GraphType type2 = graph2.getType();
        if (type1.isAllowingMultipleEdges() || type2.isAllowingMultipleEdges()) {
            throw new IllegalArgumentException("graphs with multiple (parallel) edges are not supported");
        }

        if (type1.isMixed() || type2.isMixed()) {
            throw new IllegalArgumentException("mixed graphs not supported");
        }

        if (type1.isUndirected() && type2.isDirected() || type1.isDirected() && type2.isUndirected()) {
            throw new IllegalArgumentException("can not match directed with " + "undirected graphs");
        }

        this.graph1 = graph1;
        this.graph2 = graph2;

        this.isomorphicGraphMapping = null;
        this.isIsomorphic = null;
        this.isomorphismTestExecuted = false;
    }

    @Override
    public Iterator<GraphMapping<V, E>> getMappings() {
        if(!isomorphismTestExecuted) {
            isomorphismExists();
        }
        if(isIsomorphic != null && isIsomorphic) {
            ArrayList<GraphMapping<V, E>> iteratorList = new ArrayList<>(1);
            iteratorList.add(isomorphicGraphMapping);
            return iteratorList.iterator();
        } else {
            return null;
        }
    }

    @Override
    public abstract boolean isomorphismExists();

    /**
     * sorts a list of color classes by the size and the color (integer representation of the color) and
     *
     * @param colorClasses the list of the color classes
     * @param coloring the coloring
     */
    protected void sortColorClasses(List<Set<V>> colorClasses, VertexColoringAlgorithm.Coloring<V> coloring) {
        colorClasses.sort((o1, o2) -> {
            if(o1.size() == o2.size()) {
                Iterator it1 = o1.iterator();
                Iterator it2 = o2.iterator();
                if(!it1.hasNext() || !it2.hasNext()) {
                    return Integer.compare(o1.size(), o2.size());
                }
                return coloring.getColors().get(it1.next()).compareTo(coloring.getColors().get(it2.next()));
            }
            return Integer.compare(o1.size(), o2.size());
        });
    }


    /**
     * calculates the graph isomorphism as GraphMapping and assigns it to attribute <code>isomorphicGraphMapping</code>
     *
     * @param coloring1 the discrete vertex coloring of graph1
     * @param coloring2 the discrete vertex coloring of graph2
     */
    protected void calculateGraphMapping(VertexColoringAlgorithm.Coloring<V> coloring1, VertexColoringAlgorithm.Coloring<V> coloring2) {
        GraphOrdering<V, E> graphOrdering1 = new GraphOrdering<>(graph1);
        GraphOrdering<V, E> graphOrdering2 = new GraphOrdering<>(graph2);

        int[] core1 = new int[graph1.vertexSet().size()];
        int[] core2 = new int[graph2.vertexSet().size()];

        Iterator<Set<V>> setIterator1 = coloring1.getColorClasses().iterator();
        Iterator<Set<V>> setIterator2 = coloring2.getColorClasses().iterator();

        // we only have to check one iterator as the color classes have the same size
        while(setIterator1.hasNext()) {
            Iterator<V> vertexIterator1 = setIterator1.next().iterator();
            Iterator<V> vertexIterator2 = setIterator2.next().iterator();

            while(vertexIterator1.hasNext()) {
                V v1 = vertexIterator1.next();
                V v2 = vertexIterator2.next();

                int numberOfV1 = graphOrdering1.getVertexNumber(v1);
                int numberOfV2 = graphOrdering2.getVertexNumber(v2);

                core1[numberOfV1] = numberOfV2;
                core2[numberOfV2] = numberOfV1;
            }
        }

        isomorphicGraphMapping = new IsomorphicGraphMapping<>(graphOrdering1, graphOrdering2, core1, core2);
    }
}
