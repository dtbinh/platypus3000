package projects.Steiner;

import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Vec2;
import platypus3000.algorithms.Boundary.BoundaryDetection;
import platypus3000.algorithms.flocking.DensityDistribution;
import platypus3000.algorithms.flocking.OlfatiSaberFlocking;
import platypus3000.algorithms.leader.LeaderFollowAlgorithm;
import platypus3000.algorithms.leader.LeaderSet;
import platypus3000.simulation.Configuration;
import platypus3000.simulation.Simulator;
import platypus3000.simulation.control.RobotController;
import platypus3000.simulation.control.RobotInterface;
import platypus3000.utils.ForceTuner;
import platypus3000.utils.LeaderInterface;
import platypus3000.utils.Loopable;
import platypus3000.utils.NeighborState.PublicState;
import platypus3000.utils.NeighborState.StateManager;
import platypus3000.visualisation.VisualisationWindow;

import java.awt.*;
import java.io.IOException;

/**
 * Created by doms on 11/27/14.
 */
public class ExperimentalController extends RobotController implements LeaderInterface {
    public static boolean leadersFollowActivated = true;
    public static boolean leadersInfluenceActivated = true;
    public static boolean densityActivated = true;
    public static boolean thicknessActivated = true;

    OlfatiSaberFlocking flockAlgorithm;
    StateManager stateManager;
    BoundaryDetection boundaryAlgorithm;
    ThicknessDetermination thicknessDetermination;
    LeaderFollowAlgorithm leaderFollowAlgorithm;
    LeaderSet leaderset;
    DensityDistribution densityDistribution;
    ForceTuner forceTuner = new ForceTuner("Basic", this);
    HelpSignal helpSignal;

    public ExperimentalController(LeaderSet l){
        this.leaderset = l;
    }

    public void init(RobotInterface robot){
        flockAlgorithm = new OlfatiSaberFlocking(this);
        stateManager = new StateManager();
        boundaryAlgorithm = new BoundaryDetection(this, stateManager);
        thicknessDetermination = new ThicknessDetermination(this, stateManager, boundaryAlgorithm);
        leaderFollowAlgorithm = new LeaderFollowAlgorithm(this, robot, stateManager, leaderset);
        densityDistribution = new DensityDistribution(this, boundaryAlgorithm, stateManager);
        helpSignal  = new HelpSignal(stateManager, leaderset.isLeader(robot.getID()));
        stateManager.setLocalState("BoundTest", testPublicState);
    }

    //Distributing the boundary force
    class TestPublicState extends PublicState{
        Vec2 bound_mov = new Vec2();

        @Override
        public PublicState clone() throws CloneNotSupportedException {
            TestPublicState cloned = new TestPublicState();
            cloned.bound_mov = bound_mov.clone();
            return cloned;
        }
    }
    TestPublicState testPublicState = new TestPublicState();
    @Override
    public void loop(RobotInterface robot) {
        stateManager.loop(robot);
        flockAlgorithm.loop(robot);
        boundaryAlgorithm.loop(robot);
        thicknessDetermination.loop(robot);
        leaderFollowAlgorithm.loop(robot);
        densityDistribution.loop(robot);
        helpSignal.loop(robot);

        if(densityActivated)
            if(!boundaryAlgorithm.isBoundary()) forceTuner.addForce("Denisty", densityDistribution.getForce(), robot);

        Vec2 bf = boundaryAlgorithm.getBoundaryForce();
       if(!boundaryAlgorithm.isBoundary()){
           int i = 0;

           for(TestPublicState s: stateManager.<TestPublicState>getStates("BoundTest")){
               bf.addLocal(s.bound_mov);
               i++;
           }
           if(i!=0){
               bf.mulLocal(1f/i);
           }
       }
        testPublicState.bound_mov = bf;

        forceTuner.addForce("Boundary", bf, 5f, robot);
        forceTuner.addForce("DynBoundary", boundaryAlgorithm.getDynamicBoundaryForce(), 5f, robot);
        forceTuner.addForce("Flocking", flockAlgorithm.getForce(), robot);
        if(leadersInfluenceActivated) {
            if (!boundaryAlgorithm.isBoundary()) forceTuner.addForce("Leader", leaderFollowAlgorithm.getForce(), robot);
            forceTuner.addForce("Help", helpSignal.getForce(), robot);
        }

        if(thicknessActivated)
            forceTuner.addForce("Thickness", thicknessDetermination.getForce(), robot);

        robot.setMovement(forceTuner.getForce());

        if(leadersInfluenceActivated || leadersFollowActivated)
            if(leaderset.isLeader(robot)) robot.setMovement(leaderFollowAlgorithm.getSteerVector());

        stateManager.broadcast(robot);
    }

    public static void main(String[] args) throws IOException{
        Simulator sim = new Simulator(new Configuration("src/main/java/Steiner/simulation.properties"));
        for(int i = 0; i<400; i++){
            sim.createRobot(i, MathUtils.randomFloat(0, 10), MathUtils.randomFloat(0,10),0).setController(new ExperimentalController(new LeaderSet(0,1,2,3,4)));
        }
        new VisualisationWindow(sim, new Dimension(1900,800));
        ForceTuner.show();
    }

    @Override
    public void setLocalGoal(Vec2 goalPosition) {
        leaderFollowAlgorithm.setSteerVector(goalPosition);
    }
}

class HelpSignal implements Loopable{
    private static final String STATE_KEY = "HelpSignal";
    StateManager stateManager;
    HelpSignalState publicState = new HelpSignalState();
    boolean leader;

    HelpSignal(StateManager stateManager, boolean isLeader){
        this.stateManager = stateManager;
        stateManager.setLocalState(HelpSignal.STATE_KEY, publicState);
        this.leader = isLeader;
    }

    @Override
    public Loopable[] getDependencies() {
        return new Loopable[]{stateManager};
    }

    Vec2 v = new Vec2();

    @Override
    public void loop(RobotInterface robot) {
        v.setZero();
        if(leader){
           publicState.value = (robot.getNeighborhood().size()<3?1:0)*(3-robot.getNeighborhood().size());
        }  else {
            float max = 0;
            for (HelpSignalState hss : stateManager.<HelpSignalState>getStates(HelpSignal.STATE_KEY)) {
                if (hss.value > 0 && robot.getNeighborhood().contains(hss.getRobotID())) {
                    if(max < hss.value){
                        max = hss.value;
                        v.setZero();
                    }
                    if(max == hss.value) v.addLocal(robot.getNeighborhood().getById(hss.getRobotID()).getLocalPosition().mul(hss.value));
                }
            }
            publicState.value = 0.8f*max;
        }
    }

    public Vec2 getForce() {
        return v.clone();
    }

    class HelpSignalState extends PublicState{
        float value = 0;

        @Override
        public PublicState clone() throws CloneNotSupportedException {
            HelpSignalState cloned = new HelpSignalState();
            cloned.value = value;
            return cloned;
        }
    }
}
