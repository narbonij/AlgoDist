import java.io.FileWriter;
import java.io.IOException;
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
import jbotsimx.format.common.Format;
import jbotsimx.topology.TopologyGenerator;
import jbotsimx.ui.JViewer;

public class Main{
	public final static Set<Integer> listNodeLog = new HashSet<>();
	public final static GHSProperty[] property = new GHSProperty[1];
    public static void main(String[] args){
    	
    	property[0] = new GHSProperty(args);
    	if(property[0]._graphFilename != null)
    	{
    		final Integer[] nbMessTotal = new Integer[1];
            final Integer[] nbStopedNode = new Integer[1];
            nbMessTotal[0] = 0;
            nbStopedNode[0] = 0;
			Topology tp = new Topology();
			tp.setWirelessStatus(false);
			tp.setClockSpeed(property[0]._clockSpeed);
			tp.disableWireless();
			tp.setDefaultNodeModel(BasicNode.class);
	        Format.importFromFile(tp, property[0]._graphFilename);
	        JViewer viewer = new JViewer(tp);
	        
	        
	        
	        tp.addMessageListener(new MessageListener()
			{
				
				@Override
				public void onMessage(Message mess)
				{
					nbMessTotal[0]++;
				if(property[0]._verboseMode && nbMessTotal[0]%property[0]._verboseStep == 0)
						System.out.println(nbMessTotal[0]);
					
					if(mess.getFlag() == "STOP")
					{
						nbStopedNode[0]++;
						if(nbStopedNode[0] == (tp.getNodes().size()-1))
						{
							System.out.println("With " + nbMessTotal[0] + " messages sent.");
							tp.pause();
							
							for(Link l : tp.getLinks())
							{
								if(l.getColor() == Color.BLUE || l.getColor() == Color.RED)
								{
									l.setColor(Color.MAGENTA);
								}
							}
							if(property[0]._resultFilename!=null)
							{
								String result = "graph {\r\n" + 
										"	graph [layout=nop, splines=line, bb=\"-1524.82,-1487.86,4577.58,4034.63\"];\r\n" + 
										"	node [label=\"\", shape=point, height=0.05, width=0.05];\r\n" + 
										"	edge [len=1.00];\n";
								for(Link l : tp.getLinks())
								{
									result+=l.endpoint(0).getID() + "--" + l.endpoint(1).getID();
									if(l.getColor() == Color.MAGENTA)
									{
										result += "[color=\"magenta\",penwidth=\"5.0\"]";
									}
									result +=";\n";
								}
								for(Node n : tp.getNodes())
								{
									result += n.getID() + " [pos=\"" + n.getX() + "," + n.getY() + "\"];\n";
								}
								result +="}";
								try (FileWriter file = new FileWriter(property[0]._resultFilename))
						        {

						            file.write(result);
						            file.flush();

						        } 
						        catch (IOException e) 
						        {
						            e.printStackTrace();
						        }
							}
							
							
							
							
						}
						
					}
					
				}
			});
	        
	        tp.start();
    	}
    	
  
        
      
        
        
   
        
       
   
        ///TEST: for Log purpose
        

       
        
        ///END Test
        

        
        
        //viewer.onCommand(command);
        
        

        
        
        

        /*TopologyGenerator.generateRing(tp, 13);
        tp.setClockSpeed(50);
        tp.start();*/
      
    }
}    