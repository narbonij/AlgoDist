import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.FileHandler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import jbotsim.Color;
import jbotsim.Link;
import jbotsim.Message;
import jbotsim.Node;

public class BasicNode extends Node
{
	private int phase;
	private Node father;
	private Set<Node> children;
	private int frag;
	private boolean readyToMerge;
	private boolean hasMerged;
	private Link out;
	private Node outNode;
	private int counterAckFrag;
	private int counterOut;
	//private int counterAckMerge;
	private Map<Integer,Integer> mapCounterAckMerge;
	private int counterEcho;
	private Set<Pair<Node,Integer>> listMerge;
	private Map<Pair<Integer,Integer>,Node> listAckFrag;
	
	public BasicNode()
	{
		super();
		children = new HashSet<>();
		listMerge = new HashSet<>();
		listAckFrag = new ConcurrentHashMap<>();
		mapCounterAckMerge = new ConcurrentHashMap<>();

		
		// TODO Auto-generated constructor stub
	}
	
	private void init()
	{
		setPhase(0);
		setWirelessStatus(false);
		disableWireless();
		setFather(null);
		children.clear();
		setFrag(getID());
		reset();
		counterAckFrag = 0;
		counterOut = 0;
		//counterAckMerge = 0;
		counterEcho = 0;
		listAckFrag.clear();
		listMerge.clear();
		mapCounterAckMerge.clear();

	}
	
	private void reset()
	{
		readyToMerge = false;
		hasMerged = false;
		out = null;
		outNode = null;
		if(!mapCounterAckMerge.containsKey(phase))
		{
			mapCounterAckMerge.put(phase, 0);
		}
		updateLabel();
	}
	
	private void start()
	{
		setPhase(phase+1);
		reset();
		for(Node n : getNeighbors())
		{
			send(n,new Message(phase,"FRAG"));
		}
		for(Node n : children)
		{
			send(n,new Message(null,"PULSE"));
		}
	}

	@Override
	public void onStart()
	{

		super.onStart();
		init();
		start();
		if(getID() == 0)
		{
			Log("----------------START ALGORITHM----------------",false);
		}
		

		
	}

	@Override
	public void onClock()
	{
		// TODO Auto-generated method stub
		//super.onClock();
		//
		List<Message> listmess = getMailbox();
		for(Message mess : listmess)
		{
			Log("NODE:" + this + " with " + children.size() + " children RECEIVE: " + mess.getFlag() + " from: " + mess.getSender());
			switch(mess.getFlag())
			{
			case "PULSE": //signal of new phase from father
				setPhase(phase+1);
				//sendAckFrag();
				reset();
				for(Node n : children)//forward it to your children
				{
					send(n,new Message(null,"PULSE"));
				}
				for(Node n : getNeighbors())//and check your neighbor for OUT edge
				{
					send(n, new Message(phase, "FRAG"));
				}
				
			break;
			
			case "FRAG": //ask from frag number
				if(phase >= (Integer)mess.getContent())
				{
					send(mess.getSender(),new Message(frag,"ACK_FRAG")); //answer it
				}
				else
				{
					listAckFrag.put(new Pair<Integer,Integer>(mess.getSender().getID(),(Integer) mess.getContent()),mess.getSender());
				}
				
			
				
				break;
				
			case "ACK_FRAG": //getting frag number of neighbor
				counterAckFrag++;
				Log(counterAckFrag + " on " + getNeighbors().size());
				//if frag number different of this.frag then check OUT for distance
				if((Integer)mess.getContent() != frag)
				{
					Link outgoingEdge = getCommonLinkWith(mess.getSender());
					if(isSmaller(outgoingEdge,out))
					{
						out = outgoingEdge;
						outNode = null;
					}
				}
				//checkAckFragOutMessage();
				break;
				
			case "OUT":
				counterOut++;
				Log(counterOut+ " on " + children.size());
				if(mess.getContent() != null)
				{
					Link outgoingEdge = (Link)mess.getContent();
					if(isSmaller(outgoingEdge, out))
					{
						out = outgoingEdge;
						outNode = (Node) mess.getSender();
					}
					
				}
				//checkAckFragOutMessage();
				break;
				
				
			case "ACK_NEW_ROOT":
				readyToMerge = true;
				for(Node n : children)
				{
					send(n,new Message(mess.getContent(), "ACK_NEW_ROOT"));
				}
				if(out != null && out.equals((Link)mess.getContent())) //if you have the same OUT, you are on the path 
					//of vertices that have to change their father or send MERGE
				{
					children.add(mess.getSender());
					checkAckNewRoot();
				}
				checkForMerge();
				break;
				
				
			case "MERGE"://root of another fragment ask for merge
				if(hasMerged)
				{
					children.add(mess.getSender());
					send(mess.getSender(),new Message(frag,"NEW"));
				}
				else
				{
					if(readyToMerge)
					{
						
						if(father == null)//if you are also the root of your fragment
						{
							if(getCommonLinkWith(mess.getSender()) == out) //you are the new root
							{
								children.add(mess.getSender());//adding the other node to children
								if((Integer)mess.getContent() > frag)
								{
									
									//and also adding pending merge
									for(Pair<Node,Integer> p : listMerge)
									{
										children.add(p.first());
									}
									listMerge.clear();
									for(Node n : children)//and sending children NEW
									{
										send(n, new Message(frag,"NEW"));
									}
									hasMerged = true;
									Log("---------MERGING FRAG: " + frag + " AND " + (Integer)mess.getContent() + "-----------");
									for(Node n : getNeighbors()) //acking neighbors that you have merged
									{
										send(n,new Message(phase,"ACK_MERGE"));
									}
								}
							}
							else
							{
								children.add(mess.getSender());//adding the other node to children
							}
						}
						else
						{
							children.add(mess.getSender());//adding the other node to children
						}
						
					}
					else
					{
						listMerge.add(new Pair<Node,Integer>(mess.getSender(),(Integer)mess.getContent()));
					}
				}
			break;
			
			
			case "NEW":
				//the OUT is now validated: color it blue
				getCommonLinkWith(mess.getSender()).setColor(Color.BLUE);
				
				
				
				setFather(mess.getSender());
				children.remove(father);
				setFrag((Integer)mess.getContent());
				for(Node n : children)//forwarding it to children
				{
					send(n, new Message(frag,"NEW"));
				}
				hasMerged = true;
				for(Node n : getNeighbors())//acking neighbors you have merged
				{
					send(n,new Message(phase,"ACK_MERGE"));
				}
				break;
				
			case "ACK_MERGE":
				//counterAckMerge++;
				if(!mapCounterAckMerge.containsKey((Integer)mess.getContent()))
				{
					mapCounterAckMerge.put((Integer)mess.getContent(), 1);
				}
					
				else
				{
					mapCounterAckMerge.put((Integer)mess.getContent(), mapCounterAckMerge.get((Integer)mess.getContent())+1);
				}
					
				Log(mapCounterAckMerge.get((Integer)mess.getContent()) + " on " + getNeighbors().size() + " for phase:" + (Integer)mess.getContent());
				break;
				
			case "ECHO":
				counterEcho++;
				Log(counterEcho + " on " + children.size());
				break;
				
			case "STOP":
				for(Node n : children)
				{
					send(n,new Message(null,"STOP"));
				}
				return;
			
			}
		}
		sendAckFrag();
		checkAckFragOutMessage();
		checkAckMergeEchoMessage();
	}

	@Override
	public void onMessage(Message mess)
	{
		
		
		
		//super.onMessage(arg0);
	}
	
	
	
	

	@Override
	public void send(Node n, Message mess)
	{	
		super.send(n, mess);
		Log("NODE:" + this + " with " + children.size() + " children SEND: " + mess.getFlag() + " to: " + n);
	}

	
	private void checkAckFragOutMessage()
	{
		if(counterAckFrag == getNeighbors().size() && counterOut == children.size())
		{
			counterAckFrag = 0;
			counterOut = 0;
			if(father == null) //if you are the root of the current fragment
			{
				if(out == null)
				{
					//end of the algorithm
					for(Node n : children)
					{
						send(n,new Message(null,"STOP"));
					}
					System.out.println("Algo terminated in " + phase + " phases");
					return;
				}
				else //a new OUT has been found for this fragment
				{
					//changing color of the edge
					out.setWidth(5);
					out.setColor(Color.RED);
					
					readyToMerge = true;
					for(Node n : children)
					{
						//ack children of new root
						send(n, new Message(out,"ACK_NEW_ROOT"));
						
					}
					checkAckNewRoot();
					checkForMerge();

				}
			}
			else//you forward your OUT to your father
			{
				send(father,new Message(out,"OUT"));
			}
		}
	}
	
	private void checkAckMergeEchoMessage()
	{
		if(mapCounterAckMerge.get(phase) > getNeighbors().size() || counterEcho > children.size())
			System.out.println("!!!!!!!!!!!!!!!!!!!!PROBLEM!!!!!!!!!!!!!!!!!!!");
		if(hasMerged && mapCounterAckMerge.get(phase) == getNeighbors().size() && counterEcho == children.size())
		{
			//counterAckMerge -=  getNeighbors().size();
			mapCounterAckMerge.put(phase, 0);
			counterEcho = 0;
			if(father == null)//you are the root of your fragment: signal for new phase
			{
				setPhase(phase+1);
				//sendAckFrag();
				reset();
				for(Node n : children)
				{
					send(n,new Message(null,"PULSE"));
				}
				for(Node n : getNeighbors())//and check your neighbors for OUT edge
				{
					send(n, new Message(phase, "FRAG"));
				}
			}
			else //forwarding echo to father
			{
				send(father,new Message(null,"ECHO"));
			}
		}
		
	}
	
	private void checkAckNewRoot()
	{
		if(outNode == null)//you are the new root of the fragment
		{
			setFather(null);
			send(out.getOtherEndpoint(this),new Message(frag,"MERGE"));
		}
		else
		{
			setFather(outNode);
			
		}
	}
	
	
	private void checkForMerge()
	{
		if(!listMerge.isEmpty())
		{
			if(father == null) //you are the root of your fragment
			{
				boolean root = false;
				for(Pair<Node,Integer> p : listMerge)
				{
					if(out.equals(getCommonLinkWith(p.first())))
					{
						children.add(p.first());
						if(frag < p.second())
						{
							root = true; //you are designated the new root
							
						}
						
					}
					else
					{
						children.add(p.first());
					}
					
					
				}
				if(root)//you are the new root of the merged fragment
				{
					
					for(Node n : children)
					{
						send(n,new Message(frag,"NEW"));
					}
					hasMerged = true;
					for(Node n : getNeighbors())
					{
						send(n,new Message(phase,"ACK_MERGE"));
					}
				}
			}
			else
			{
				for(Pair<Node,Integer> p: listMerge)
				{
					children.add(p.first());
				}
				
			}
			listMerge.clear();
		}	
	}
	
	
	private void sendAckFrag()
	{
		if(!listAckFrag.isEmpty())
		{
			for(Pair<Integer,Integer> p : listAckFrag.keySet())
			{
				if( phase >= p.second())
				{
					
					send(listAckFrag.get(p),new Message(frag,"ACK_FRAG"));
					listAckFrag.remove(p);
				}
			}
		}
	}
		
	

	private void Log(String s)
	{
		/*if( true || Main.listNodeLog.contains(getID()))
		{
			//Log(s,true);
		}*/
		
	}

	public static void Log(String s, boolean append)
	{
		Logger logger = Logger.getLogger("log");  
		logger.setUseParentHandlers(false);
	
	    FileHandler fh;  
	    boolean succeed = false;

	    while(!succeed)
	    {
	    	try {  

		        // This block configure the logger with handler and formatter  
		        fh = new FileHandler("./log.txt",append); 
		        logger.addHandler(fh);
		        fh.setFormatter(new SimpleFormatter() {
		            private static final String format = "%1$s %n";

		            @Override
		            public synchronized String format(LogRecord lr) {
		                return String.format(format,
		                        lr.getMessage()
		                );
		            }
		        }); 

		        // the following statement is used to log any messages  
		        logger.info(s);  
		        logger.removeHandler(fh);
		        fh.close();
		        succeed = true;

		    } catch (SecurityException e) {  
		       // e.printStackTrace();  
		    } catch (IOException e) {  
		       // e.printStackTrace();  
		    }  
		    
	    }
	}
	
	

	
	private static boolean isSmaller(Link l1,Link l2)
	{
		if(l2 == null)
		{
			return true;
		}
		else
		{
			if(l1.getLength()<l2.getLength())
			{
				return true;
			}
			else if(l1.getLength() > l2.getLength())
			{
				return false;
			}
			else //if length are equal: checking ids of the links
			{
				int minL1,minL2,maxL1,maxL2;
				minL1 = l1.endpoint(0).getID();
				maxL1 = l1.endpoint(1).getID();
				if(maxL1 < minL1)
				{
					int aux = minL1;
					minL1 = maxL1;
					maxL1 = aux;
				}
				
				minL2 = l2.endpoint(0).getID();
				maxL2 = l2.endpoint(1).getID();
				
				if(maxL2 < minL2)
				{
					int aux = minL2;
					minL2 = maxL2;
					maxL2 = aux;
				}
				
				if(minL1 < minL2)
				{
					return true;
				}
				else if(minL1 > minL2)
				{
					return false;
				}
				else
				{
					if(maxL1 < maxL2)
					{
						return true;
					}
					else
					{
						return false;
					}
				}
			}
		}
	}

	
	private void updateLabel()
	{
		String label = "";
		label += getID()+ ":";
		if(father == null)
		{
			label += "root";
		}
		else
		{
			label += father.getID();
		}
		 label+= ":f" + frag + "::" + phase;
		setLabel(label);
	}

	private void setFather(Node father)
	{
		this.father = father;
		if(father == null)
		{
			setColor(Color.RED);
		}
		else
		{
			setColor(Node.DEFAULT_COLOR);
		}
		updateLabel();
	}

	private void setFrag(int frag)
	{
		this.frag = frag;
		updateLabel();
	}


	private void setPhase(int phase)
	{
		this.phase = phase;
		updateLabel();
	}
}
