import java.nio.file.LinkOption;
import java.util.HashSet;
import java.util.Set;

import jbotsim.Color;
import jbotsim.Link;
import jbotsim.Message;
import jbotsim.Node;
import jbotsim.Topology;
import jbotsim.event.MessageListener;
import jbotsim.event.SelectionListener;
import jbotsim.event.TopologyListener;
import jbotsimx.topology.TopologyGenerator;
import jbotsimx.ui.JViewer;

public class Main{
	public final static Set<Integer> listNodeLog = new HashSet<>();
    public static void main(String[] args){
    	
        Topology tp = new Topology();
        tp.setWirelessStatus(false);
        tp.disableWireless();
        tp.setDefaultNodeModel(BasicNode.class);
        final Integer[] nbMessTotal = new Integer[1];
        final Integer[] nbStopedNode = new Integer[1];
        nbMessTotal[0] = 0;
        nbStopedNode[0] = 0;
   
        
        JViewer viewer = new JViewer(tp);
   
        ///TEST: for Log purpose

       
        
        ///END Test
        

        
        
        //viewer.onCommand(command);

        tp.addTopologyListener(new TopologyListener()
		{
			
			@Override
			public void onNodeRemoved(Node arg0)
			{
				tp.resetTime();
				tp.setClockSpeed(20);
				for(Link l : tp.getLinks())
				{
					
					l.setColor(Link.DEFAULT_COLOR);
					l.setWidth(1);
				}
				nbStopedNode[0] = 0;
				nbMessTotal[0] = 0;
				tp.restart();
				tp.start();
				
			}
			
			@Override
			public void onNodeAdded(Node n)
			{
				
				//tp.resetTime();
				tp.pause();
					
			}
		});
        
        tp.addMessageListener(new MessageListener()
		{
			
			@Override
			public void onMessage(Message mess)
			{
				nbMessTotal[0]++;
				System.out.println(nbMessTotal[0]);
				
				if(mess.getFlag() == "STOP")
				{
					

					nbStopedNode[0]++;
					if(nbStopedNode[0] == (tp.getNodes().size()-1))
					{
						for(Link l : tp.getLinks())
						{
							if(l.getColor() == Color.BLUE || l.getColor() == Color.RED)
							{
								l.setColor(Color.MAGENTA);
							}
						}
						System.out.println("With " + nbMessTotal[0] + " messages sent.");
						tp.pause();
					}
					
				}
				
			}
		});

        /*TopologyGenerator.generateRing(tp, 13);
        tp.setClockSpeed(50);
        tp.start();*/
      
    }
}    