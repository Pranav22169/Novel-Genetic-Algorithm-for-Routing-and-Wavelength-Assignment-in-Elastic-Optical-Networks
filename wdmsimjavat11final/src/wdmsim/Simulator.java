package wdmsim;
import java.io.File;
import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class Simulator {

    private static final String simName = new String("wdmsim");
    private static final Float simVersion = new Float(0.1);
    public static boolean verbose = false;
    public static boolean trace = false;

    public void Execute(String simConfigFile, boolean trace, boolean verbose, double forcedLoad, int seed) {

        Simulator.verbose = verbose;
        Simulator.trace = trace;

        if (Simulator.verbose) {
            System.out.println("#################################");
            System.out.println("# Simulator " + simName + " version " + simVersion.toString() + "  #");
            System.out.println("#################################\n");
        }

        try {

            long begin = System.currentTimeMillis();

            if (Simulator.verbose) {
                System.out.println("(0) Accessing simulation file " + simConfigFile + "...");
            }
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(new File(simConfigFile));

            doc.getDocumentElement().normalize();

            if (!doc.getDocumentElement().getNodeName().equals(simName)) {
                System.out.println("Root element of the simulation file is " + doc.getDocumentElement().getNodeName() + ", " + simName + " is expected!");
                System.exit(0);
            }
            if (!doc.getDocumentElement().hasAttribute("version")) {
                System.out.println("Cannot find version attribute!");
                System.exit(0);
            }
            if (Float.compare(new Float(doc.getDocumentElement().getAttribute("version")), simVersion) > 0) {
                System.out.println("Simulation config file requires a newer version of the simulator!");
                System.exit(0);
            }
            if (Simulator.verbose) {
                System.out.println("(0) Done. (" + Float.toString((float) ((float) (System.currentTimeMillis() - begin) / (float) 1000)) + " sec)\n");
            }

            begin = System.currentTimeMillis();
            if (Simulator.verbose) {
                System.out.println("(1) Loading physical topology information...");
            }

            PhysicalTopology pt = new PhysicalTopology((Element) doc.getElementsByTagName("physical-topology").item(0));
            if (Simulator.verbose) {
                System.out.println(pt);
            }

            if (Simulator.verbose) {
                System.out.println("(1) Done. (" + Float.toString((float) ((float) (System.currentTimeMillis() - begin) / (float) 1000)) + " sec)\n");
            }

            begin = System.currentTimeMillis();
            if (Simulator.verbose) {
                System.out.println("(2) Loading virtual topology information...");
            }

            VirtualTopology vt = new VirtualTopology((Element) doc.getElementsByTagName("virtual-topology").item(0), pt);
            if (Simulator.verbose) {
                System.out.println(vt);
            }

            if (Simulator.verbose) {
                System.out.println("(2) Done. (" + Float.toString((float) ((float) (System.currentTimeMillis() - begin) / (float) 1000)) + " sec)\n");
            }

            begin = System.currentTimeMillis();
            if (Simulator.verbose) {
                System.out.println("(3) Loading traffic information...");
            }

            EventScheduler events = new EventScheduler();
            TrafficGenerator traffic = new TrafficGenerator((Element) doc.getElementsByTagName("traffic").item(0), forcedLoad);
            traffic.generateTraffic(pt, events, seed);

            if (Simulator.verbose) {
                System.out.println("(3) Done. (" + Float.toString((float) ((float) (System.currentTimeMillis() - begin) / (float) 1000)) + " sec)\n");
            }

            begin = System.currentTimeMillis();
            if (Simulator.verbose) {
                System.out.println("(4) Loading simulation setup information...");
            }

            MyStatistics st = MyStatistics.getMyStatisticsObject();
            st.statisticsSetup(pt.getNumNodes(), 3, 0);
            
            Tracer tr = Tracer.getTracerObject();
            if (Simulator.trace == true)
            {
            	if (forcedLoad == 0) {
                	tr.setTraceFile(simConfigFile.substring(0, simConfigFile.length() - 4) + ".txt");
            	} else {
                	tr.setTraceFile(simConfigFile.substring(0, simConfigFile.length() - 4) + "_Load_" + Double.toString(forcedLoad) + ".txt");
            	}
            }
            tr.toogleTraceWriting(Simulator.trace);
            
            String rwaModule = "wdmsim.rwa." + ((Element) doc.getElementsByTagName("rwa").item(0)).getAttribute("module");
            if (Simulator.verbose) {
                System.out.println("RWA module: " + rwaModule);
            }
            ControlPlane cp = new ControlPlane(rwaModule, pt, vt);

            if (Simulator.verbose) {
                System.out.println("(4) Done. (" + Float.toString((float) ((float) (System.currentTimeMillis() - begin) / (float) 1000)) + " sec)\n");
            }

            begin = System.currentTimeMillis();
            if (Simulator.verbose) {
                System.out.println("(5) Running the simulation...");
            }

            SimulationRunner sim = new SimulationRunner(cp, events);

            if (Simulator.verbose) {
                System.out.println("(5) Done. (" + Float.toString((float) ((float) (System.currentTimeMillis() - begin) / (float) 1000)) + " sec)\n");
            }

            if (Simulator.verbose) {
                if (forcedLoad == 0) {
                    System.out.println("Statistics (" + simConfigFile + "):\n");
                } else {
                    System.out.println("Statistics for " + Double.toString(forcedLoad) + " erlangs (" + simConfigFile + "):\n");
                }
                System.out.println(st.fancyStatistics());
            } else {
                System.out.println("*****");
                if (forcedLoad != 0) {
                    System.out.println("Load:" + Double.toString(forcedLoad));
                }
                st.printStatistics();
            }
            
            st.finish();

            if (Simulator.trace == true)
            	tr.finish();
            
        } catch (SAXParseException err) {
            System.out.println("** Parsing error" + ", line " + err.getLineNumber() + ", uri " + err.getSystemId());
            System.out.println(" " + err.getMessage());

        } catch (SAXException e) {
            Exception x = e.getException();
            ((x == null) ? e : x).printStackTrace();

        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
  
