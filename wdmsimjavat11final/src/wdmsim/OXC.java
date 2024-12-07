package wdmsim;
public class OXC {

    private int id;
    private int groomingInputPorts;
    private int groomingOutputPorts;
    private int freeGroomingInputPorts;
    private int freeGroomingOutputPorts;
    private int wvlConverters;
    private int freeWvlConverters;
    private int wvlConversionRange;
    public OXC(int id, int groomingInputPorts, int groomingOutputPorts, int wvlConverters, int wvlConversionRange) {
        this.id = id;
        this.groomingInputPorts = this.freeGroomingInputPorts = groomingInputPorts;
        this.groomingOutputPorts = this.freeGroomingOutputPorts = groomingOutputPorts;
        this.wvlConverters = this.freeWvlConverters = wvlConverters;
        this.wvlConversionRange = wvlConversionRange;
    }
    public int getID() {
        return id;
    }
    public boolean hasFreeGroomingInputPort() {
        if (freeGroomingInputPorts > 0) {
            return true;
        } else {
            return false;
        }
    }
    public boolean reserveGroomingInputPort() {
        if (freeGroomingInputPorts > 0) {
            freeGroomingInputPorts--;
            return true;
        } else {
            return false;
        }
    }
    public boolean releaseGroomingInputPort() {
        if (freeGroomingInputPorts < groomingInputPorts) {
            freeGroomingInputPorts++;
            return true;
        } else {
            return false;
        }
    }
    public boolean hasFreeGroomingOutputPort() {
        if (freeGroomingOutputPorts > 0) {
            return true;
        } else {
            return false;
        }
    }
    public boolean reserveGroomingOutputPort() {
        if (freeGroomingOutputPorts > 0) {
            freeGroomingOutputPorts--;
            return true;
        } else {
            return false;
        }
    }
    public boolean releaseGroomingOutputPort() {
        if (freeGroomingOutputPorts < groomingOutputPorts) {
            freeGroomingOutputPorts++;
            return true;
        } else {
            return false;
        }
    }
    public boolean hasFreeWvlConverters() {
        if (freeWvlConverters > 0) {
            return true;
        } else {
            return false;
        }
    }
    public boolean reserveWvlConverter() {
        if (freeWvlConverters > 0) {
            freeWvlConverters--;
            return true;
        } else {
            return false;
        }
    }
    public boolean releaseWvlConverter() {
        if (freeWvlConverters < wvlConverters) {
            freeWvlConverters++;
            return true;
        } else {
            return false;
        }
    }
    public int getWvlConversionRange() {
        return wvlConversionRange;
    }
}
