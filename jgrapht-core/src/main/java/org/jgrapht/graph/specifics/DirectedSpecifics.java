package org.jgrapht.graph.specifics;

import org.jgrapht.DirectedGraph;
import org.jgrapht.Graph;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.AbstractBaseGraph;
import org.jgrapht.graph.EdgeSetFactory;
import org.jgrapht.util.ArrayUnenforcedSet;

import java.io.Serializable;
import java.util.*;

/**
 * .
 *
 * @author Barak Naveh
 */
public class DirectedSpecifics<V,E>
    extends Specifics<V,E>
    implements Serializable
{
    private static final long serialVersionUID = 8971725103718958232L;
    private static final String NOT_IN_DIRECTED_GRAPH =
        "no such operation in a directed graph";

    private AbstractBaseGraph<V,E> abstractBaseGraph;
    protected Map<V, DirectedEdgeContainer<V, E>> vertexMapDirected;
    protected EdgeSetFactory<V, E> edgeSetFactory;

    public DirectedSpecifics(AbstractBaseGraph<V,E> abstractBaseGraph)
    {
        this(abstractBaseGraph, new LinkedHashMap<>());
    }

    public DirectedSpecifics(AbstractBaseGraph<V,E> abstractBaseGraph, Map<V, DirectedEdgeContainer<V, E>> vertexMap)
    {
        this.abstractBaseGraph = abstractBaseGraph;
        this.vertexMapDirected = vertexMap;
        this.edgeSetFactory=abstractBaseGraph.getEdgeSetFactory();
    }

    @Override public void addVertex(V v)
    {
        // add with a lazy edge container entry
        vertexMapDirected.put(v, null);
    }

    @Override public Set<V> getVertexSet()
    {
        return vertexMapDirected.keySet();
    }

    /**
     * @see Graph#getAllEdges(Object, Object)
     */
    @Override public Set<E> getAllEdges(V sourceVertex, V targetVertex)
    {
        Set<E> edges = null;

        if (abstractBaseGraph.containsVertex(sourceVertex)
            && abstractBaseGraph.containsVertex(targetVertex))
        {
            edges = new ArrayUnenforcedSet<>();

            DirectedEdgeContainer<V, E> ec = getEdgeContainer(sourceVertex);

            for (E e : ec.outgoing) {
                if (abstractBaseGraph.getEdgeTarget(e).equals(targetVertex)) {
                    edges.add(e);
                }
            }
        }

        return edges;
    }

    /**
     * @see Graph#getEdge(Object, Object)
     */
    @Override public E getEdge(V sourceVertex, V targetVertex)
    {
        if (abstractBaseGraph.containsVertex(sourceVertex)
            && abstractBaseGraph.containsVertex(targetVertex))
        {
            DirectedEdgeContainer<V, E> ec = getEdgeContainer(sourceVertex);

            for (E e : ec.outgoing) {
                if (abstractBaseGraph.getEdgeTarget(e).equals(targetVertex)) {
                    return e;
                }
            }
        }

        return null;
    }

    @Override public void addEdgeToTouchingVertices(E e)
    {
        V source = abstractBaseGraph.getEdgeSource(e);
        V target = abstractBaseGraph.getEdgeTarget(e);

        getEdgeContainer(source).addOutgoingEdge(e);
        getEdgeContainer(target).addIncomingEdge(e);
    }

    /**
     * @see UndirectedGraph#degreeOf(Object)
     */
    @Override public int degreeOf(V vertex)
    {
        throw new UnsupportedOperationException(NOT_IN_DIRECTED_GRAPH);
    }

    /**
     * @see Graph#edgesOf(Object)
     */
    @Override public Set<E> edgesOf(V vertex)
    {
        ArrayUnenforcedSet<E> inAndOut =
                new ArrayUnenforcedSet<>(getEdgeContainer(vertex).incoming);
        inAndOut.addAll(getEdgeContainer(vertex).outgoing);

        // we have two copies for each self-loop - remove one of them.
        if (abstractBaseGraph.isAllowingLoops()) {
            Set<E> loops = getAllEdges(vertex, vertex);

            for (int i = 0; i < inAndOut.size();) {
                E e = inAndOut.get(i);

                if (loops.contains(e)) {
                    inAndOut.remove(i);
                    loops.remove(e); // so we remove it only once
                } else {
                    i++;
                }
            }
        }

        return Collections.unmodifiableSet(inAndOut);
    }

    /**
     * @see DirectedGraph#inDegreeOf(Object)
     */
    @Override public int inDegreeOf(V vertex)
    {
        return getEdgeContainer(vertex).incoming.size();
    }

    /**
     * @see DirectedGraph#incomingEdgesOf(Object)
     */
    @Override public Set<E> incomingEdgesOf(V vertex)
    {
        return getEdgeContainer(vertex).getUnmodifiableIncomingEdges();
    }

    /**
     * @see DirectedGraph#outDegreeOf(Object)
     */
    @Override public int outDegreeOf(V vertex)
    {
        return getEdgeContainer(vertex).outgoing.size();
    }

    /**
     * @see DirectedGraph#outgoingEdgesOf(Object)
     */
    @Override public Set<E> outgoingEdgesOf(V vertex)
    {
        return getEdgeContainer(vertex).getUnmodifiableOutgoingEdges();
    }

    @Override public void removeEdgeFromTouchingVertices(E e)
    {
        V source = abstractBaseGraph.getEdgeSource(e);
        V target = abstractBaseGraph.getEdgeTarget(e);

        getEdgeContainer(source).removeOutgoingEdge(e);
        getEdgeContainer(target).removeIncomingEdge(e);
    }

    /**
     * A lazy build of edge container for specified vertex.
     *
     * @param vertex a vertex in this graph.
     *
     * @return EdgeContainer
     */
    private DirectedEdgeContainer<V, E> getEdgeContainer(V vertex)
    {
        //abstractBaseGraph.assertVertexExist(vertex); //JK: I don't think we need this here. This should have been verified upstream

        DirectedEdgeContainer<V, E> ec = vertexMapDirected.get(vertex);

        if (ec == null) {
            ec = new DirectedEdgeContainer<>(edgeSetFactory, vertex);
            vertexMapDirected.put(vertex, ec);
        }

        return ec;
    }
}
