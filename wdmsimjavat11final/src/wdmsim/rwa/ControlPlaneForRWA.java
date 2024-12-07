package wdmsim.rwa;

import java.util.Map;
import wdmsim.*;

public interface ControlPlaneForRWA {

    public boolean acceptFlow(long id, LightPath[] lightpaths);

    public boolean blockFlow(long id);

    public boolean rerouteFlow(long id, LightPath[] lightpaths);
    
    public Flow getFlow(long id);
    
    public Path getPath(Flow flow);
    
    public int getLightpathFlowCount(long id);

    public Map<Flow, Path> getMappedFlows();
}
