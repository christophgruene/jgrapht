import org.apache.commons.lang3.time.StopWatch;
import org.jgrapht.Graph;
import org.jgrapht.alg.color.ColorRefinementAlgorithm;
import org.jgrapht.alg.interfaces.VertexColoringAlgorithm;
import org.jgrapht.generate.GnmRandomGraphGenerator;
import org.jgrapht.generate.GnpRandomGraphGenerator;
import org.jgrapht.generate.GraphGenerator;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.util.MathUtil;
import org.jgrapht.util.SupplierUtil;

import java.util.Set;

public class Main {
    static int seed = 0;
    public static void main (String[] args){
//        double e = 200;
//        for (int i = 1; i < 21; i+= 1) {
//            BenchmarkResult result = RunBenchmark(i * 200, 0.1);
//            System.out.println(result.getProblemSize() + "\t" + result.getMilliseconds() + "\t" + result.getNumberOfClasses() + "\t" + result.getNumberOfNonSimplicialClasses());
//        }

        for (int i = 1; i < 100; i+= 1) {
            int numberOfNonSimplicialClasses = 0;
            int nonDisreteCounter = 0;
            int limit = 1000;
            for (int j = 0; j < limit; j++) {
                BenchmarkResult result = RunBenchmark(i, 0.1);
                numberOfNonSimplicialClasses += result.getNumberOfNonSimplicialClasses();
                if (result.getNumberOfNonSimplicialClasses() != 0) {
                    nonDisreteCounter++;
                }
            }
            System.out.println(i + "\t" + (double)numberOfNonSimplicialClasses / limit + "\t" + (double)nonDisreteCounter / limit);
        }
    }

    private static BenchmarkResult RunBenchmark(int size, double edgeProbability) {
        GnpRandomGraphGenerator<Integer, DefaultEdge> generator = null;
        Graph<Integer, DefaultEdge> graph;
        generator = new GnpRandomGraphGenerator<Integer, DefaultEdge>(
                size, edgeProbability, seed++);

        graph = new SimpleGraph<>(
                SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);

        generator.generateGraph(graph);
        ColorRefinementAlgorithm<Integer, DefaultEdge> algorithm = new ColorRefinementAlgorithm<>(graph);
        System.gc();
        StopWatch watch = new StopWatch();
        watch.start();
        VertexColoringAlgorithm.Coloring<Integer> coloring = algorithm.getColoring();
        watch.stop();

        int numberOfNonSimplicialClasses = GetNumberOfNonSimplicialClasses(coloring);

        return new BenchmarkResult((int)watch.getTime(), coloring.getNumberColors(), numberOfNonSimplicialClasses, size);
    }

    private static int GetNumberOfNonSimplicialClasses(VertexColoringAlgorithm.Coloring<Integer> coloring) {
        int numberOfNonSimplicialClasses = 0;
        for (Set<Integer> cl : coloring.getColorClasses()){
            if (cl.size() > 1) {
                numberOfNonSimplicialClasses++;
            }
        }
        return numberOfNonSimplicialClasses;
    }

    private static class BenchmarkResult {
        private int milliseconds;
        private int numberOfClasses;
        private int numberOfNonSimplicialClasses;
        private int problemSize;

        private BenchmarkResult(int milliseconds, int numberOfClasses, int numberOfNonSimplicialClasses, int problemSize) {
            this.milliseconds = milliseconds;
            this.numberOfClasses = numberOfClasses;
            this.numberOfNonSimplicialClasses = numberOfNonSimplicialClasses;
            this.problemSize = problemSize;
        }

        public int getNumberOfClasses() {
            return numberOfClasses;
        }

        public int getMilliseconds() {
            return milliseconds;
        }

        public int getNumberOfNonSimplicialClasses() {
            return numberOfNonSimplicialClasses;
        }

        public int getProblemSize() {
            return problemSize;
        }
    }
}
