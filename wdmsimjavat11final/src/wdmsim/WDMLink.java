package wdmsim;

import java.util.ArrayList;

public class WDMLink {

    private int id;
    private int src;
    private int dst;
    private double delay;
    private int wavelengths;
    private boolean[] freeWavelengths;
    private int[] availableBandwidth;
    private int bw;
    private double weight;

    public WDMLink(int id, int src, int dst, double delay, int wavelengths, int bw, double weight) {
        if (id < 0 || src < 0 || dst < 0 || wavelengths < 1 || bw < 1) {
            throw (new IllegalArgumentException());
        } else {
            this.id = id;
            this.src = src;
            this.dst = dst;
            this.delay = delay;
            this.wavelengths = wavelengths;
            this.bw = bw;
            this.weight = weight;
            this.freeWavelengths = new boolean[wavelengths];
            for (int i = 0; i < wavelengths; i++) {
                this.freeWavelengths[i] = true;
            }
            this.availableBandwidth = new int[wavelengths];
            for (int i = 0; i < wavelengths; i++) {
                this.availableBandwidth[i] = bw;
            }
        }
    }
    
    public int getID() {
        return this.id;
    }
    
    public int getSource() {
        return this.src;
    }
    
    public int getDestination() {
        return this.dst;
    }
    
    public int getWavelengths() {
        return this.wavelengths;
    }
    
    public double getWeight() {
        return this.weight;
    }
    
    public double getDelay() {
        return this.delay;
    }
    
    public int getBandwidth() {
        return this.bw;
    }
    
    public Boolean isWLAvailable(int wavelength) {
        if (wavelength < 0 || wavelength >= this.wavelengths) {
            throw (new IllegalArgumentException());
        } else {
            return freeWavelengths[wavelength];
        }
    }
    
    public int[] hasWLAvailable() {
        ArrayList<Integer> wls = new ArrayList<Integer>();
        for (int i = 0; i < this.wavelengths; i++) {
            if (this.isWLAvailable(i)) {
                wls.add(i);
            }
        }
        int[] a = new int[wls.size()];
        for (int i = 0; i < wls.size(); i++) {
            a[i] = wls.get(i);
        }
        return a;
    }
    
    public int firstWLAvailable() {
        for (int i = 0; i < this.wavelengths; i++) {
            if (this.isWLAvailable(i)) {
                return i;
            }
        }
        return -1;
    }
    
    public int amountBWAvailable(int wavelength) {
        if (wavelength < 0 || wavelength >= this.wavelengths) {
            throw (new IllegalArgumentException());
        } else {
            return availableBandwidth[wavelength];
        }
    }
    
    public int[] hasBWAvailable(int bw) {
        ArrayList<Integer> bws = new ArrayList<Integer>();
        for (int i = 0; i < this.wavelengths; i++) {
            if (this.amountBWAvailable(i) >= bw) {
                bws.add(i);
            }
        }
        int[] a = new int[bws.size()];
        for (int i = 0; i < bws.size(); i++) {
            a[i] = bws.get(i);
        }
        return a;
    }
    
    public boolean reserveWavelength(int wavelength) {
        if (wavelength < 0 || wavelength >= this.wavelengths) {
            throw (new IllegalArgumentException());
        } else {
            if (freeWavelengths[wavelength]) {
                freeWavelengths[wavelength] = false;
                return true;
            } else {
                return false;
            }
        }
    }
    
    public void releaseWavelength(int wavelength) {
        if (wavelength < 0 || wavelength >= this.wavelengths) {
            throw (new IllegalArgumentException());
        } else {
            freeWavelengths[wavelength] = true;
        }
    }
    
    public int addTraffic(int wavelength, int bw) {
        if (wavelength < 0 || wavelength >= this.wavelengths || bw > availableBandwidth[wavelength]) {
            throw (new IllegalArgumentException());
        } else {
            availableBandwidth[wavelength] -= bw;
            return availableBandwidth[wavelength];
        }

    }
    
    public int removeTraffic(int wavelength, int bw) {
        if (wavelength < 0 || wavelength >= this.wavelengths || bw > this.bw) {
            throw (new IllegalArgumentException());
        } else {
            availableBandwidth[wavelength] += bw;
            return availableBandwidth[wavelength];
        }

    }
    
    @Override
    public String toString() {
        String link = Long.toString(id) + ": " + Integer.toString(src) + "->" + Integer.toString(dst) + " delay: " + Double.toString(delay) + " wvls: " + Integer.toString(wavelengths) + " bw: " + Integer.toString(bw) + " weight:" + Double.toString(weight);
        return link;
    }
}
