package wdmsim.rwa;

import wdmsim.*;

public interface RWA {
    
    public void simulationInterface(PhysicalTopology pt, VirtualTopology vt, ControlPlaneForRWA cp);

    public void flowArrival(Flow flow);
    
    public void flowDeparture(long id);

}
