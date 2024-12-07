package wdmsim.rwa;

import wdmsim.Flow;
import wdmsim.LightPath;
import wdmsim.PhysicalTopology;
import wdmsim.VirtualTopology;
import wdmsim.util.Dijkstra;
import wdmsim.util.WeightedGraph;

import java.util.ArrayList;
import java.util.Random;

public class GeneticRWA implements RWA {

    private PhysicalTopology pt;
    private VirtualTopology vt;
    private ControlPlaneForRWA cp;
    private WeightedGraph graph;
    private static final int POPULATION_SIZE = 80;
    private static final int GENERATIONS = 70;
    private static final double MUTATION_RATE = 0.1;

    public void simulationInterface(PhysicalTopology pt, VirtualTopology vt, ControlPlaneForRWA cp) {
        this.pt = pt;
        this.vt = vt;
        this.cp = cp;
        this.graph = pt.getWeightedGraph();
    }

    public void flowArrival(Flow flow) {
        int[] nodes = Dijkstra.getShortestPath(graph, flow.getSource(), flow.getDestination());

        if (nodes.length == 0) {
            cp.blockFlow(flow.getID());
            return;
        }

        int[] links = new int[nodes.length - 1];
        for (int i = 0; i < nodes.length - 1; i++) {
            links[i] = pt.getLink(nodes[i], nodes[i + 1]).getID();
        }

        // Run genetic algorithm to find the best wavelength assignment
        int[] bestWavelengthAssignment = geneticAlgorithmWavelengthAssignment(links, flow.getRate());

        if (bestWavelengthAssignment != null) {
            long id = vt.createLightpath(links, bestWavelengthAssignment);
            if (id >= 0) {
                LightPath[] lps = { vt.getLightpath(id) };
                cp.acceptFlow(flow.getID(), lps);
                return;
            }
        }

        // Block flow if no suitable assignment found
        cp.blockFlow(flow.getID());
    }

    private int[] geneticAlgorithmWavelengthAssignment(int[] links, int flowRate) {
        int numWavelengths = pt.getNumWavelengths();
        ArrayList<int[]> population = initializePopulation(links.length, numWavelengths);

        for (int gen = 0; gen < GENERATIONS; gen++) {
            population = evolvePopulation(population, links, flowRate);

            int[] bestCandidate = population.get(0);
            if (evaluateFitness(bestCandidate, links, flowRate) == 0) {
                return bestCandidate; // Return the best candidate if it meets the requirements
            }
        }

        return population.get(0); // Return the best solution found within the generations
    }

    private ArrayList<int[]> initializePopulation(int linkCount, int numWavelengths) {
        ArrayList<int[]> population = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < POPULATION_SIZE; i++) {
            int[] individual = new int[linkCount];
            for (int j = 0; j < linkCount; j++) {
                individual[j] = random.nextInt(numWavelengths);
            }
            population.add(individual);
        }

        return population;
    }

    private ArrayList<int[]> evolvePopulation(ArrayList<int[]> population, int[] links, int flowRate) {
        ArrayList<int[]> newPopulation = new ArrayList<>();

        while (newPopulation.size() < POPULATION_SIZE) {
            int[] parent1 = selectParent(population, links, flowRate);
            int[] parent2 = selectParent(population, links, flowRate);
            int[] offspring = crossover(parent1, parent2);
            mutate(offspring);
            newPopulation.add(offspring);
        }

        newPopulation.sort((a, b) -> Double.compare(evaluateFitness(a, links, flowRate), evaluateFitness(b, links, flowRate)));
        return newPopulation;
    }

    private int[] selectParent(ArrayList<int[]> population, int[] links, int flowRate) {
        Random random = new Random();
        int idx = random.nextInt(POPULATION_SIZE);
        int[] selected = population.get(idx);

        for (int i = 0; i < POPULATION_SIZE; i++) {
            int candidateIdx = random.nextInt(POPULATION_SIZE);
            if (evaluateFitness(population.get(candidateIdx), links, flowRate) < evaluateFitness(selected, links, flowRate)) {
                selected = population.get(candidateIdx);
            }
        }

        return selected;
    }

    private int[] crossover(int[] parent1, int[] parent2) {
        Random random = new Random();
        int[] offspring = new int[parent1.length];

        for (int i = 0; i < parent1.length; i++) {
            offspring[i] = (random.nextBoolean()) ? parent1[i] : parent2[i];
        }

        return offspring;
    }

    private void mutate(int[] individual) {
        Random random = new Random();
        for (int i = 0; i < individual.length; i++) {
            if (random.nextDouble() < MUTATION_RATE) {
                individual[i] = random.nextInt(pt.getNumWavelengths());
            }
        }
    }

    private double evaluateFitness(int[] assignment, int[] links, int flowRate) {
        double fitness = 0;

        for (int i = 0; i < links.length; i++) {
            int availableWavelengths = pt.getNumWavelengths();
            fitness += (availableWavelengths - flowRate) * 0.1;

            // Use the custom isWLAvailable() method here to check if wavelength is available
            if (!pt.getLink(links[i]).isWLAvailable(assignment[i])) {
                fitness += 100; // Penalty for using unavailable wavelength
            }
        }

        return fitness;
    }

    public void flowDeparture(long id) {
    }
}
