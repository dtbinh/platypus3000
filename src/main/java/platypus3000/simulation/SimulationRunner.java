package platypus3000.simulation;

import java.net.InterfaceAddress;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by m on 12/4/14.
 */
public class SimulationRunner {
    private final Simulator sim;
    private volatile boolean running = false;
    public volatile boolean paused = false;
    public volatile boolean superspeed = false;
    private Thread workerThread = null;
    public List<SimStepListener> listeners = new LinkedList<SimStepListener>();

    public SimulationRunner(Simulator sim) {
        this.sim = sim;
    }

    public boolean stop() {
        if(running && workerThread != null) {
            running = false;
            try {
                workerThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }
        return false;
    }

    public void run(final float stepsPerSecond) {
        stop();
        workerThread = new Thread() {
            @Override
            public void run() {
                long intervalLengthMs = (long) (1000/stepsPerSecond);
                long lastStep = -1;
                while(running) {
                    if(!superspeed && System.currentTimeMillis() - lastStep < intervalLengthMs) {
                        Thread.yield();
                    }
                    else {
                        if (paused)
                            sim.refresh();
                        else
                            sim.step();
                        for(SimStepListener l : listeners)
                            l.simStep(sim);
                        lastStep = System.currentTimeMillis();
                    }
                }
            }
        };
        running = true;
        workerThread.start();
    }

    public boolean isRunning() {
        return running;
    }

    public Simulator getSim() {
        return sim;
    }

    public interface SimStepListener {
        public void simStep(Simulator sim);
    }
}
