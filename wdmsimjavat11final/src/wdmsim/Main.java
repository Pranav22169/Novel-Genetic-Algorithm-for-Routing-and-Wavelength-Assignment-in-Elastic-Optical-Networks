package wdmsim;

import wdmsim.util.Distribution;

public class Main {

    public static void main(String[] args) {
        Simulator wdm;
        String usage = "";
        String simConfigFile;
        boolean verbose = false;
        boolean trace = false;
        int seed = 1;
        double minload = 0, maxload = 0, step = 1;

        if (args.length < 2 || args.length > 7) {
            System.out.println(usage);
            System.exit(0);
        } else {
            if (args.length == 3 || args.length == 6) {
                if (args[2].equals("-verbose")) {
                    verbose = true;
                } else {
                    if (args[2].equals("-trace")) {
                        trace = true;
                    } else {
                        System.out.println(usage);
                        System.exit(0);
                    }
                }
            }
            if (args.length == 4 || args.length == 7) {
                if ((args[2].equals("-trace") && args[3].equals("-verbose")) || (args[3].equals("-trace") && args[2].equals("-verbose"))) {
                    trace = true;
                    verbose = true;
                } else {
                    System.out.println(usage);
                    System.exit(0);
                }
            }
            if (args.length == 5 || args.length == 6 || args.length == 7) {
                minload = Double.parseDouble(args[args.length - 3]);
                maxload = Double.parseDouble(args[args.length - 2]);
                step = Double.parseDouble(args[args.length - 1]);
            }
        }

        simConfigFile = args[0];
        seed = Integer.parseInt(args[1]);

        for (double load = minload; load <= maxload; load += step) {
            wdm = new Simulator();
            wdm.Execute(simConfigFile, trace, verbose, load, seed);
        }
    }
}
