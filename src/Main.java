import java.nio.file.LinkOption;

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
    public static void main(String[] args){
        Topology tp = new Topology();
        tp.setDefaultNodeModel(BasicNode.class);
        final Integer[] nbMessTotal = new Integer[1];
        final Integer[] nbStopedNode = new Integer[1];
        nbMessTotal[0] = 0;
        nbStopedNode[0] = 0;
   
        
        JViewer viewer = new JViewer(tp);
        

        
        TopologyGenerator.generateRing(tp, 5);
        
        //viewer.onCommand(command);

        tp.addTopologyListener(new TopologyListener()
		{
			
			@Override
			public void onNodeRemoved(Node arg0)
			{
				tp.resetTime();
				for(Link l : tp.getLinks())
				{
					
					l.setColor(Link.DEFAULT_COLOR);
					l.setWidth(1);
				}
				tp.restart();
				tp.start();
				
			}
			
			@Override
			public void onNodeAdded(Node n)
			{
				for(Link l : tp.getLinks())
				{
					
					l.setColor(Link.DEFAULT_COLOR);
					l.setWidth(1);
				
			
					
				}
					
			}
		});
        
        tp.addMessageListener(new MessageListener()
		{
			
			@Override
			public void onMessage(Message mess)
			{
				
				if(mess.getFlag() == "STOP")
				{
					nbMessTotal[0] += ((BasicNode)mess.getSender()).getNbMess();
					nbStopedNode[0]++;
					if(nbStopedNode[0] == (tp.getNodes().size()-1))
					{
						for(Link l : tp.getLinks())
						{
							if(l.getColor() == Color.BLUE)
							{
								l.setColor(Color.MAGENTA);
							}
						}
						System.out.println("With " + nbMessTotal[0] + " messages sent.");
					}
					
				}
				
			}
		});
       
        tp.setClockSpeed(50);
        tp.start();
      
    }
}    