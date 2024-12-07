package wdmsim;

public class MyStatistics {
	
	private static MyStatistics singletonObject;

    private int minNumberArrivals;
    private int numberArrivals;
    private int arrivals;
    private int departures;
    private int accepted;
    private int blocked;
    private int requiredBandwidth;
    private int blockedBandwidth;
    private double acumulatedCost;
    private int numNodes;
    private int[][] arrivalsPairs;
    private int[][] blockedPairs;
    private int[][] requiredBandwidthPairs;
    private int[][] blockedBandwidthPairs;
    private int acumulatedK;
    private int acumulatedHops;
    private int acumulatedLPs;
    private int numfails;
    private int flowfails;
    private int lpsfails;
    private float trafficfails;
    private long execTime;

    private int numClasses;
    private int[] arrivalsDiff;
    private int[] blockedDiff;
    private int[] requiredBandwidthDiff;
    private int[] blockedBandwidthDiff;
    private int[][][] arrivalsPairsDiff;
    private int[][][] blockedPairsDiff;
    private int[][][] requiredBandwidthPairsDiff;
    private int[][][] blockedBandwidthPairsDiff;
    
    private MyStatistics() {
    	
        numberArrivals = 0;

        arrivals = 0;
        departures = 0;
        accepted = 0;
        blocked = 0;

        requiredBandwidth = 0;
        blockedBandwidth = 0;

        numfails = 0;
        flowfails = 0;
        lpsfails = 0;
        trafficfails = 0;

        execTime = 0;
    }
    
    public static synchronized MyStatistics getMyStatisticsObject() {
        if (singletonObject == null) {
            singletonObject = new MyStatistics();
        }
        return singletonObject;
    }
    
    @Override
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }
    
    public void statisticsSetup(int numNodes, int numClasses, int minNumberArrivals) {
        this.numNodes = numNodes;
        this.arrivalsPairs = new int[numNodes][numNodes];
        this.blockedPairs = new int[numNodes][numNodes];
        this.requiredBandwidthPairs = new int[numNodes][numNodes];
        this.blockedBandwidthPairs = new int[numNodes][numNodes];

        this.minNumberArrivals = minNumberArrivals;

        this.numClasses = numClasses;
        this.arrivalsDiff = new int[numClasses];
        this.blockedDiff = new int[numClasses];
        this.requiredBandwidthDiff = new int[numClasses];
        this.blockedBandwidthDiff = new int[numClasses];
        for (int i = 0; i < numClasses; i++) {
            this.arrivalsDiff[i] = 0;
            this.blockedDiff[i] = 0;
            this.requiredBandwidthDiff[i] = 0;
            this.blockedBandwidthDiff[i] = 0;
        }
        this.arrivalsPairsDiff = new int[numNodes][numNodes][numClasses];
        this.blockedPairsDiff = new int[numNodes][numNodes][numClasses];
        this.requiredBandwidthPairsDiff = new int[numNodes][numNodes][numClasses];
        this.blockedBandwidthPairsDiff = new int[numNodes][numNodes][numClasses];
    }
    
    public void acceptFlow(Flow flow, LightPath[] lightpaths)
    {
        if (this.numberArrivals > this.minNumberArrivals)
        {
            this.accepted++;
        }
    }
    
    public void blockFlow(Flow flow) {
        if (this.numberArrivals > this.minNumberArrivals) {
            int cos = flow.getCOS();
            this.blocked++;
            this.blockedDiff[cos]++;
            this.blockedBandwidth += flow.getRate();
            this.blockedBandwidthDiff[cos] += flow.getRate();
            this.blockedPairs[flow.getSource()][flow.getDestination()]++;
            this.blockedPairsDiff[flow.getSource()][flow.getDestination()][cos]++;
            this.blockedBandwidthPairs[flow.getSource()][flow.getDestination()] += flow.getRate();
            this.blockedBandwidthPairsDiff[flow.getSource()][flow.getDestination()][cos] += flow.getRate();
        }
    }
    
    public void addEvent(Event event) {
        try {
            if (event instanceof FlowArrivalEvent)
            {
                this.numberArrivals++;
                if (this.numberArrivals > this.minNumberArrivals)
                {
                    int cos = ((FlowArrivalEvent) event).getFlow().getCOS();
                    this.arrivals++;
                    this.arrivalsDiff[cos]++;
                    this.requiredBandwidth += ((FlowArrivalEvent) event).getFlow().getRate();
                    this.requiredBandwidthDiff[cos] += ((FlowArrivalEvent) event).getFlow().getRate();
                    this.arrivalsPairs[((FlowArrivalEvent) event).getFlow().getSource()][((FlowArrivalEvent) event).getFlow().getDestination()]++;
                    this.arrivalsPairsDiff[((FlowArrivalEvent) event).getFlow().getSource()][((FlowArrivalEvent) event).getFlow().getDestination()][cos]++;
                    this.requiredBandwidthPairs[((FlowArrivalEvent) event).getFlow().getSource()][((FlowArrivalEvent) event).getFlow().getDestination()] += ((FlowArrivalEvent) event).getFlow().getRate();
                    this.requiredBandwidthPairsDiff[((FlowArrivalEvent) event).getFlow().getSource()][((FlowArrivalEvent) event).getFlow().getDestination()][cos] += ((FlowArrivalEvent) event).getFlow().getRate();
                }
                if (Simulator.verbose && Math.IEEEremainder((double) arrivals, (double) 10000) == 0)
                {
                    System.out.println(Integer.toString(arrivals));
                }
            }
            else if (event instanceof FlowDepartureEvent)
            {
                if (this.numberArrivals > this.minNumberArrivals)
                {
                    this.departures++;
                }
            }
        }
        
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
    
    public String fancyStatistics() {
        float acceptProb, blockProb, bbr, meanK;
        if (accepted == 0) {
            acceptProb = 0;
            meanK = 0;
        } else {
            acceptProb = ((float) accepted) / ((float) arrivals) * 100;
        }
        if (blocked == 0) {
            blockProb = 0;
            bbr = 0;
        } else {
            blockProb = ((float) blocked) / ((float) arrivals) * 100;
            bbr = ((float) blockedBandwidth) / ((float) requiredBandwidth) * 100;
        }

        String stats = "Arrivals \t: " + Integer.toString(arrivals) + "\n";
        stats += "Required BW \t: " + Integer.toString(requiredBandwidth) + "\n";
        stats += "Accepted \t: " + Integer.toString(accepted) + "\t(" + Float.toString(acceptProb) + "%)\n";
        stats += "Blocked \t: " + Integer.toString(blocked) + "\t(" + Float.toString(blockProb) + "%)\n";
        stats += "BBR     \t: " + Float.toString(bbr) + "%\n";
        stats += "\n";
        stats += "Blocking probability per s-d pair:\n";
        for (int i = 0; i < numNodes; i++) {
            for (int j = i + 1; j < numNodes; j++) {
                stats += "Pair (" + Integer.toString(i) + "->" + Integer.toString(j) + ") ";
                stats += "Calls (" + Integer.toString(arrivalsPairs[i][j]) + ")";
                if (blockedPairs[i][j] == 0) {
                    blockProb = 0;
                    bbr = 0;
                } else {
                    blockProb = ((float) blockedPairs[i][j]) / ((float) arrivalsPairs[i][j]) * 100;
                    bbr = ((float) blockedBandwidthPairs[i][j]) / ((float) requiredBandwidthPairs[i][j]) * 100;
                }
                stats += "\tBP (" + Float.toString(blockProb) + "%)";
                stats += "\tBBR (" + Float.toString(bbr) + "%)\n";
            }
        }

        return stats;
    }
    
    public void printStatistics() {
        float bp, bbr, jfi, sum1 = 0, sum2 = 0;
        float bpDiff[], bbrDiff[];
        int count = 0;
        
        // Overall statistics calculation
        bp = (blocked == 0) ? 0 : ((float) blocked / arrivals) * 100;
        bbr = (blockedBandwidth == 0) ? 0 : ((float) blockedBandwidth / requiredBandwidth) * 100;
        
        bpDiff = new float[numClasses];
        bbrDiff = new float[numClasses];
        
        // Per-class blocking probability and bandwidth blocking ratio
        for (int i = 0; i < numClasses; i++) {
            if (blockedDiff[i] == 0) {
                bpDiff[i] = 0;
                bbrDiff[i] = 0;
            } else {
                bpDiff[i] = ((float) blockedDiff[i] / arrivalsDiff[i]) * 100;
                bbrDiff[i] = ((float) blockedBandwidthDiff[i] / requiredBandwidthDiff[i]) * 100;
            }
        }
    
        // Print summary statistics
        System.out.println("Summary Statistics:");
        System.out.println("===================");
        System.out.printf("Total Arrivals: %d%n", arrivals);
        System.out.printf("Total Accepted: %d (%.2f%%)%n", accepted, ((float) accepted / arrivals) * 100);
        System.out.printf("Total Blocked: %d (%.2f%%)%n", blocked, bp);
        System.out.printf("Required Bandwidth: %d%n", requiredBandwidth);
        System.out.printf("Blocked Bandwidth: %d (%.2f%%)%n", blockedBandwidth, bbr);
        System.out.println();
    
        // Print per-class statistics
        System.out.println("Class-specific Blocking and Bandwidth Blocking Ratio:");
        System.out.println("====================================================");
        for (int i = 0; i < numClasses; i++) {
            System.out.printf("Class %d: Blocking Probability = %.2f%%, Bandwidth Blocking Ratio = %.2f%%%n", i, bpDiff[i], bbrDiff[i]);
        }
        System.out.println();
    
        // Detailed pairwise statistics
        System.out.println("Pairwise Blocking and Bandwidth Blocking Ratio:");
        System.out.println("===============================================");
        for (int i = 0; i < numNodes; i++) {
            for (int j = i + 1; j < numNodes; j++) {
                if (arrivalsPairs[i][j] > 0) {
                    float pairBP = (blockedPairs[i][j] == 0) ? 0 : ((float) blockedPairs[i][j] / arrivalsPairs[i][j]) * 100;
                    float pairBBR = (blockedBandwidthPairs[i][j] == 0) ? 0 : ((float) blockedBandwidthPairs[i][j] / requiredBandwidthPairs[i][j]) * 100;
                    System.out.printf("Pair (%d -> %d): Arrivals = %d, Blocking Probability = %.2f%%, Bandwidth Blocking Ratio = %.2f%%%n",
                            i, j, arrivalsPairs[i][j], pairBP, pairBBR);
                    
                    // Accumulate values for JFI calculation
                    count++;
                    sum1 += pairBBR;
                    sum2 += pairBBR * pairBBR;
                }
            }
        }
        
        // Calculate and print Jain's Fairness Index
        jfi = (sum1 * sum1) / ((float) count * sum2);
        System.out.printf("Jain's Fairness Index (JFI): %.4f%n", jfi);
        System.out.println();
    
        // Per-class Jain's Fairness Index calculation
        System.out.println("Class-specific Jain's Fairness Index (JFI):");
        System.out.println("===========================================");
        for (int c = 0; c < numClasses; c++) {
            count = 0;
            sum1 = 0;
            sum2 = 0;
            for (int i = 0; i < numNodes; i++) {
                for (int j = i + 1; j < numNodes; j++) {
                    if (arrivalsPairsDiff[i][j][c] > 0) {
                        float classPairBBR = (blockedBandwidthPairsDiff[i][j][c] == 0) ? 0 :
                            ((float) blockedBandwidthPairsDiff[i][j][c] / requiredBandwidthPairsDiff[i][j][c]) * 100;
                        count++;
                        sum1 += classPairBBR;
                        sum2 += classPairBBR * classPairBBR;
                    }
                }
            }
            jfi = (count == 0) ? 0 : (sum1 * sum1) / (count * sum2);
            System.out.printf("Class %d JFI: %.4f%n", c, jfi);
        }
        System.out.println();
    }
    
	
    public void finish()
    {
        singletonObject = null;
    }
}