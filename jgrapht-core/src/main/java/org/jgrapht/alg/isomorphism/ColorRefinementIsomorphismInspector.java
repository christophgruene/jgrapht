package org.jgrapht.alg.isomorphism;

import org.jgrapht.Graph;
import org.jgrapht.GraphMapping;
import org.jgrapht.GraphTests;
import org.jgrapht.GraphType;
import org.jgrapht.alg.color.ColorRefinementAlgorithm;
import org.jgrapht.alg.interfaces.VertexColoringAlgorithm;
import org.jgrapht.alg.interfaces.VertexColoringAlgorithm.Coloring;
import org.jgrapht.graph.SimpleGraph;

import java.util.*;
import java.util.function.Supplier;

/**
 * @param <V> the type of the vertices
 * @param <E> the type of the edges
 */
public class ColorRefinementIsomorphismInspector<V, E> implements IsomorphismInspector<V, E> {

    /**
     * The input graphs
     */
    private Graph<V, E> graph1, graph2;

    /**
     * The isomorphism that is calculated by this color refinement isomorphism inspector
     */
    private GraphMapping<V, E> isomorphicGraphMapping;

    /**
     * contains whether the graphs are isomorphic or not.
     * If we cannot decide whether they are isomorphic the value will be not present.
     */
    private Boolean isIsomorphic;
    /**
     * contains whether the two graphs produce a discrete coloring.
     * Then, we can decide whether the graphs are isomorphic.
     */
    private boolean isColoringDiscrete;
    /**
     * contains whether the two graphs are forests. Forests can be identified to be isomorphic or not.
     */
    private boolean isForest;

    /**
     * contains whether the isomorphism test is executed to ensure that every operation is defined all the time
     */
    private boolean isomorphismTestExecuted;

    /**
     * Constructor for a isomorphism inspector based on color refinement. It checks whether <code>graph1</code> and
     * <code>graph2</code> are isomorphic.
     *
     * @param graph1 the first graph
     * @param graph2 the second graph
     */
    public ColorRefinementIsomorphismInspector(Graph<V, E> graph1, Graph<V, E> graph2) {

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
        this.isColoringDiscrete = false;
        this.isomorphismTestExecuted = false;
        this.isForest = false;
    }

    /**
     *
     *
     * @throws IsomorphismUndecidableException if the isomorphism test was not executed and the inspector cannot decide whether the graphs are isomorphic
     */
    @Override
    public Iterator<GraphMapping<V, E>> getMappings() {
        if(!isomorphismTestExecuted) {
            isomorphismExists();
        }
        ArrayList<GraphMapping<V, E>> iteratorList = new ArrayList<>(1);
        if(isIsomorphic != null && isIsomorphic) {
            iteratorList.add(isomorphicGraphMapping);
        }
        return iteratorList.iterator();
    }

    /**
     * {@inheritDoc}
     *
     * @throws IsomorphismUndecidableException if the inspector cannot decide whether the graphs are isomorphic
     */
    @Override
    public boolean isomorphismExists() {
        if(isomorphismTestExecuted) {
            if(isIsomorphic != null) {
                return isIsomorphic;
            } else {
                throw new IsomorphismUndecidableException();
            }
        }

        if(graph1.vertexSet().size() != graph2.vertexSet().size()) {
            return false;
        }

        Graph<Tuple<V, Integer>, Tuple<E, Integer>> graph = new UnionGraph<V, E>(graph1, graph2);

        ColorRefinementAlgorithm<Tuple<V, Integer>, Tuple<E, Integer>> colorRefinementAlgorithm1 = new ColorRefinementAlgorithm<Tuple<V, Integer>, Tuple<E, Integer>>(graph);

        // execute color refinement for graph1
        Coloring<Tuple<V, Integer>> coloring = colorRefinementAlgorithm1.getColoring();

        isomorphismTestExecuted = true;

        isIsomorphic = coarseColoringAreEqual(coloring);
        return isIsomorphic;
    }

    private void AddGraph(SimpleGraph<Tuple<V, Integer>, E> graph, Graph<V, E> sourceGraph, int index) {
        for (V v : sourceGraph.vertexSet()) {
            graph.addVertex(new Tuple<>(v, index));
        }

        for(E e : sourceGraph.edgeSet()) {
            V source = sourceGraph.getEdgeSource(e);
            V target = sourceGraph.getEdgeTarget(e);
            graph.addEdge(new Tuple<>(source, index), new Tuple<>(target, index));
        }
    }

    /**
     * Returns whether the coarse colorings of the two given graphs are discrete if the colorings are the same.
     * Otherwise, we do not computed if the coloring is discrete, hence, this method is undefined.
     *
     * @return if the both colorings are discrete if they are the same on both graphs.
     *
     * @throws IsomorphismUndecidableException if the isomorphism test was not executed and the inspector cannot decide whether the graphs are isomorphic
     */
    public boolean isColoringDiscrete() {
        if(!isomorphismTestExecuted) {
            isomorphismExists();
        }
        return isColoringDiscrete;
    }

    /**
     * Returns whether the two given graphs are forests if an isomorphism exists and the coloring is not discrete,
     * because if the coloring is discrete, it does not have to be calculated if the graphs are forests.
     * Otherwise, we do not compute if the graphs are forests, hence, this method is undefined.
     *
     * @return if the both colorings are discrete if they are the same on both graphs.
     *
     * @throws IsomorphismUndecidableException if the isomorphism test was not executed and the inspector cannot decide whether the graphs are isomorphic
     */
    public boolean isForest() {
        if(!isomorphismTestExecuted) {
            isomorphismExists();
        }
        return isForest;
    }

    /**
     * Checks whether two coarse colorings are equal. Furthermore, it sets <code>isColoringDiscrete</code> to true iff the colorings are discrete.
     *
     * @param coloring the coarse coloring
     * @return if the given coarse colorings are equal
     */
    private boolean coarseColoringAreEqual(Coloring<Tuple<V, Integer>> coloring) throws IsomorphismUndecidableException {
        Tuple<Coloring<V>, Coloring<V>> splitColoring = splitColoring(coloring);
        Coloring<V> coloring1 = splitColoring.x;
        Coloring<V> coloring2 = splitColoring.y;
        if (coloring1.getNumberColors() != coloring2.getNumberColors()) {
            return false;
        }

        List<Set<V>> colorClasses1 = coloring1.getColorClasses();
        List<Set<V>> colorClasses2 = coloring2.getColorClasses();

        if (colorClasses1.size() != colorClasses2.size()) {
            return false;
        }

        sortColorClasses(colorClasses1, coloring1);
        sortColorClasses(colorClasses2, coloring2);

        Iterator<Set<V>> it1 = colorClasses1.iterator();
        Iterator<Set<V>> it2 = colorClasses2.iterator();

        // check the color classes
        while (it1.hasNext() && it2.hasNext()) {
            Set<V> cur1 = it1.next();
            Set<V> cur2 = it2.next();

            // check if the size for the current color class are the same for both graphs.
            if (cur1.size() != cur2.size()) {
                return false;
            }
            // safety check whether the color class is not empty.
            if (cur1.iterator().hasNext()) {
                // check if the color are not the same (works as colors are integers).
                if (!coloring1.getColors().get(cur1.iterator().next()).equals(coloring2.getColors().get(cur2.iterator().next()))) {
                    // colors are not the same -> graphs are not isomorphic.
                    return false;
                }
            }
        }

        // no more color classes for both colorings, that is, the graphs have the same coloring.
        if (!it1.hasNext() && !it2.hasNext()) {

            /*
             * Check if the colorings are discrete, that is, the color mapping is injective.
             * Check if the graphs are forests.
             * In both cases color refinement can decide if there is an isomorphism.
             */
            if (coloring1.getColorClasses().size() == graph1.vertexSet().size() && coloring2.getColorClasses().size() == graph2.vertexSet().size()) {
                isColoringDiscrete = true;
                calculateGraphMapping(coloring1, coloring2);
                return true;
            } else if (GraphTests.isForest(graph1) && GraphTests.isForest(graph2)) {
                isForest = true;
                calculateGraphMapping(coloring1, coloring2);
                return true;
            }
            isIsomorphic = null;
            throw new IsomorphismUndecidableException("Color refinement cannot decide whether the two graphs are isomorphic or not.");
        } else {
            return false;
        }
    }

    private Tuple<Coloring<V>, Coloring<V>> splitColoring(Coloring<Tuple<V, Integer>> coloring) {
        Map<V, Integer> col1 = new HashMap<>();
        Map<V, Integer> col2 = new HashMap<>();

        int index = 0;

        for(Set<Tuple<V, Integer>> set1 : coloring.getColorClasses()) {
            for (Tuple<V, Integer> entry : set1) {
                if (entry.y == 1) {
                    col1.put(entry.x, index);
                } else {
                    col2.put(entry.x, index);
                }
            }
            index++;
        }

        Coloring<V> coloring1 =  new VertexColoringAlgorithm.ColoringImpl<>(col1, col1.size());
        Coloring<V> coloring2 =  new VertexColoringAlgorithm.ColoringImpl<>(col2, col2.size());
        return new Tuple<>(coloring1, coloring2);
    }

    /**
     * Sorts a list of color classes by the size and the color (integer representation of the color) and
     *
     * @param colorClasses the list of the color classes
     * @param coloring the coloring
     */
    private void sortColorClasses(List<Set<V>> colorClasses, Coloring<V> coloring) {
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
    private void calculateGraphMapping(Coloring<V> coloring1, Coloring<V> coloring2) {
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

     class Tuple<X, Y>  {
        public final X x;
        public final Y y;
        public Tuple(X x, Y y) {
            this.x = x;
            this.y = y;
        }

         @Override
         public boolean equals(Object o) {
             if (this == o) return true;
             if (o == null || getClass() != o.getClass()) return false;
             Tuple<?, ?> tuple = (Tuple<?, ?>) o;
             return Objects.equals(x, tuple.x) &&
                     Objects.equals(y, tuple.y);
         }

         @Override
         public int hashCode() {
             return Objects.hash(x, y);
         }

         @Override
         public String toString() {
            return "(" + x + ", " + y + ")";
         }
     }

     class UnionGraph<V, E> implements Graph<Tuple<V, Integer>, Tuple<E, Integer>> {
        Graph<V, E> graph1, graph2;
        Set<Tuple<V, Integer>> vertexSet;

        public UnionGraph(Graph<V, E> graph1, Graph<V, E> graph2) {
            this.graph1 = graph1;
            this.graph2 = graph2;
        }

         @Override
         public Set<Tuple<E, Integer>> getAllEdges(Tuple<V, Integer> sourceVertex, Tuple<V, Integer> targetVertex) {
            return null;
         }

         @Override
         public Tuple<E, Integer> getEdge(Tuple<V, Integer> sourceVertex, Tuple<V, Integer> targetVertex) {
             return null;
         }

         @Override
         public Supplier<Tuple<V, Integer>> getVertexSupplier() {
             return null;
         }

         @Override
         public Supplier<Tuple<E, Integer>> getEdgeSupplier() {
             return null;
         }

         @Override
         public Tuple<E, Integer> addEdge(Tuple<V, Integer> sourceVertex, Tuple<V, Integer> targetVertex) {
             return null;
         }

         @Override
         public boolean addEdge(Tuple<V, Integer> sourceVertex, Tuple<V, Integer> targetVertex, Tuple<E, Integer> e) {
             return false;
         }

         @Override
         public Tuple<V, Integer> addVertex() {
             return null;
         }

         @Override
         public boolean addVertex(Tuple<V, Integer> vIntegerTuple) {
             return false;
         }

         @Override
         public boolean containsEdge(Tuple<V, Integer> sourceVertex, Tuple<V, Integer> targetVertex) {
             return false;
         }

         @Override
         public boolean containsEdge(Tuple<E, Integer> e) {
             return false;
         }

         @Override
         public boolean containsVertex(Tuple<V, Integer> vIntegerTuple) {
             return false;
         }

         @Override
         public Set<Tuple<E, Integer>> edgeSet() {
             return null;
         }

         @Override
         public int degreeOf(Tuple<V, Integer> vertex) {
             return 0;
         }

         @Override
         public Set<Tuple<E, Integer>> edgesOf(Tuple<V, Integer> vertex) {
             return null;
         }

         @Override
         public int inDegreeOf(Tuple<V, Integer> vertex) {
             return 0;
         }

         @Override
         public Set<Tuple<E, Integer>> incomingEdgesOf(Tuple<V, Integer> vertex) {
            Graph<V, E> graph = vertex.y == 1 ? graph1 : graph2;

            return new TupleSet<>(graph.incomingEdgesOf(vertex.x), vertex.y);
         }

         @Override
         public int outDegreeOf(Tuple<V, Integer> vertex) {
             return 0;
         }

         @Override
         public Set<Tuple<E, Integer>> outgoingEdgesOf(Tuple<V, Integer> vertex) {
             return null;
         }

         @Override
         public boolean removeAllEdges(Collection<? extends Tuple<E, Integer>> edges) {
             return false;
         }

         @Override
         public Set<Tuple<E, Integer>> removeAllEdges(Tuple<V, Integer> sourceVertex, Tuple<V, Integer> targetVertex) {
             return null;
         }

         @Override
         public boolean removeAllVertices(Collection<? extends Tuple<V, Integer>> vertices) {
             return false;
         }

         @Override
         public Tuple<E, Integer> removeEdge(Tuple<V, Integer> sourceVertex, Tuple<V, Integer> targetVertex) {
             return null;
         }

         @Override
         public boolean removeEdge(Tuple<E, Integer> e) {
             return false;
         }

         @Override
         public boolean removeVertex(Tuple<V, Integer> vIntegerTuple) {
             return false;
         }

         @Override
         public Set<Tuple<V, Integer>> vertexSet() {
            if (vertexSet == null) {
                vertexSet = new HashSet<>();

                for (V v : graph1.vertexSet()) {
                    vertexSet.add(new Tuple<>(v, 1));
                }

                for (V v : graph2.vertexSet()) {
                    vertexSet.add(new Tuple<>(v, 2));
                }
            }

            return vertexSet;
         }

         @Override
         public Tuple<V, Integer> getEdgeSource(Tuple<E, Integer> e) {
            if (e.y == 1){
                return new Tuple<>(graph1.getEdgeSource(e.x), 1);
            } else {
                return new Tuple<>(graph2.getEdgeSource(e.x), 2);
            }
         }

         @Override
         public Tuple<V, Integer> getEdgeTarget(Tuple<E, Integer> e) {
             if (e.y == 1){
                 return new Tuple<>(graph1.getEdgeTarget(e.x), 1);
             } else {
                 return new Tuple<>(graph2.getEdgeTarget(e.x), 2);
             }
         }

         @Override
         public GraphType getType() {
             return null;
         }

         @Override
         public double getEdgeWeight(Tuple<E, Integer> e) {
             return 0;
         }

         @Override
         public void setEdgeWeight(Tuple<E, Integer> e, double weight) {

         }
     }

     class TupleSet<V> implements Set<Tuple<V, Integer>> {
        Set<V> innerSet;
        Integer index;

        public TupleSet(Set<V> innerSet, Integer index) {
            this.innerSet = innerSet;
            this.index = index;
        }

         @Override
         public int size() {
             return innerSet.size();
         }

         @Override
         public boolean isEmpty() {
             return innerSet.isEmpty();
         }

         @Override
         public boolean contains(Object o) {
            Tuple<V, Integer> val = (Tuple<V, Integer>)o;
             return val.y == index && innerSet.contains(val.y);
         }

         @Override
         public Iterator<Tuple<V, Integer>> iterator() {
            Iterator<V> innerIterator = innerSet.iterator();
            return new Iterator<Tuple<V, Integer>>() {
                @Override
                public boolean hasNext() {
                    return innerIterator.hasNext();
                }

                @Override
                public Tuple<V, Integer> next() {
                    return new Tuple<>(innerIterator.next(), index);
                }
            };
         }

         @Override
         public Object[] toArray() {
             return new Object[0];
         }

         @Override
         public <T> T[] toArray(T[] a) {
             return null;
         }

         @Override
         public boolean add(Tuple<V, Integer> vIntegerTuple) {
             if (index != vIntegerTuple.y) {
                 return false;
             }

             return innerSet.add(vIntegerTuple.x);
         }

         @Override
         public boolean remove(Object o) {
             return false;
         }

         @Override
         public boolean containsAll(Collection<?> c) {
             return false;
         }

         @Override
         public boolean addAll(Collection<? extends Tuple<V, Integer>> c) {
             return false;
         }

         @Override
         public boolean retainAll(Collection<?> c) {
             return false;
         }

         @Override
         public boolean removeAll(Collection<?> c) {
             return false;
         }

         @Override
         public void clear() {

         }
     }
}
