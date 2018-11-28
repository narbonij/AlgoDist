import jbotsim.Topology;
import jbotsimx.topology.TopologyGenerator;
import jbotsimx.ui.JViewer;

public class Main{
    public static void main(String[] args){
        Topology tp = new Topology();
        tp.setDefaultNodeModel(BasicNode.class);
        new JViewer(tp);
        TopologyGenerator.generateRing(tp, 10);
        tp.start();
    }
}    