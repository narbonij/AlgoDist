import jbotsim.Message;
import jbotsim.Node;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import jbotsim.Color;
import jbotsim.Link;

/*
 * Chaque sommet envoie un message à son voisin le plus proche
 * Chaque sommet à pour label le nombre de messages reçus
 */
public class BasicNode extends Node
{
	private BasicNode father; // Le père du sommet
	private Set<Node> children;
	private int frag;
	private int phase;
	private int counterAckFragNeighbors;
	private int counterChildrenOut;
	private Link out;
	private BasicNode outNode;
	private boolean ready;
	private boolean root;
	private boolean hasMerged;
	private int counterEcho;
	private Message futureNew;
	Set<Pair<Node, Integer>> listMerge;
	Set<Message> listAckFragToSend;
	private int nbMess;

	public BasicNode()
	{
		super();

		init();

	}
	
	private void init()
	{
		father = null;
		setWirelessStatus(true);
		this.setColor(Color.RED);
		children = new HashSet<>();
		listMerge = new HashSet<>();
		listAckFragToSend = new HashSet<>();
		frag = 0;
		phase = 0;
		counterAckFragNeighbors = 0;
		counterChildrenOut = 0;
		counterEcho = 0;
		out = null;
		outNode = null;
		ready = false;
		root = false;
		hasMerged = false;
		futureNew = null;
		nbMess = 0;
		
		
		
		
	}

	@Override
	public void onStart()
	{

		/*
		 * setColor(Color.getRandomColor());
		 * 
		 * //On cherche le sommet le plus proche Distance closestDist =
		 * findClosestDistOutsideOfFragment();
		 * 
		 * //Si on est le plus vieux, on devient le père sinon on devient le fils
		 * if(closestDist.isOlder()) { sendToDist(closestDist, new Message("IAMDAD"));
		 * if(getNodeFromDist(closestDist) != null)
		 * children.add(getNodeFromDist(closestDist)); } else { sendToDist(closestDist,
		 * new Message("YOUAREDAD")); if(getNodeFromDist(closestDist) != null) father =
		 * getNodeFromDist(closestDist); } updateLabel();
		 */
		// send(this,new Message(null, "PULSE"));

		// System.out.println("node:" + getID() + " get PULSE message");
		// when receiving message pulse (from father) we increment the phase (reset all
		// var that has to be) and forward the message
		init();
		
		
		phase++;
		frag = getID();
		if(getID()==0)
			Log("----------START OF ALGORITHM-------------",false);
		setLabel(getID() + ":" + frag);
		reset();
	

		// it sends a message frag to know the minimum cost outgoing (from the fragment)
		// edge
		for (Node n : getNeighbors())
		{
			send(n, new Message(phase, "FRAG"));
		}

	}
	
	private void checkCouterACK_FRAGCounterOUT()
	{
		if (counterAckFragNeighbors == getNeighbors().size() && counterChildrenOut == children.size())
		{
			counterAckFragNeighbors = 0;
			counterChildrenOut = 0;
			/*System.out.println(
					"all " + children.size() + " children and all neighbors of node:" + getID() + " answered.");
			System.out.println("OUT=" + out);*/
			// as soon as we receive the OUT from all children and frag from other
			// neighbors, we forward the computed out value to the father
			if (father != null)
			{
				send(father, new Message(out, "OUT"));
			} else
			{
				if (out == null)
				{
					System.out.println("--------------------Algo Terminted in " + phase + " phases---------------------");
					//send(this,new Message(null,"STOP"));
					//getTopology().pause();
					for(Node n : children)
					{
						send(n,new Message(null,"STOP"));
					}
					return;
				} else
				{
					out.setColor(Color.RED);
					out.setWidth(5);
					// if this is the root node, then we send a message to every children that there
					// is a new root for the fragment
					for (Node n : children)
					{
						send(n, new Message(out, "ACK_NEW_ROOT"));
					}
					// if the OUT is not incident to the previous root of the tree, we also change
					// the father/children of the previous root

					if (this != out.endpoint(0) && this != out.endpoint(1))
					{

						father = outNode;
						this.setColor(DEFAULT_COLOR);
						children.remove(outNode);
						//System.out.println("changing father of:" + getID() + " into:" + outNode);
					} else
					{
						father = null;
						this.setColor(Color.RED);
						//System.out.println("sending merge");
						send(out.getOtherEndpoint(this), new Message(frag, "MERGE"));
					}
					ready = true;
				}

			}

		} 
	}

	@Override
	public void onClock()
	{
		/*
		 * updateLabel(); if(father != null) setColor(father.getColor());
		 */
		if (!hasMerged && ready && listMerge.size() != 0)
		{
			// check if in the merge list there s a vertex u s.t. the edge (this,u)=out &&
			// this.frag<u.frag
			//System.out.println(
					//"-----------checking for merge:" + getID() + " father:" + father + " ---------------------");
			/*
			 * if(father == null)//this is the new root of its fragment {
			 */

			for (Pair<Node, Integer> p : listMerge)
			{
				children.add(p.first());
				if(father == null)
				{
					if (out.getOtherEndpoint(this).equals(p.first()) && frag < p.second())
					{
						/*System.out.println(
								"------------------node:" + getID() + " becomes the new root.--------------------");
						System.out.println("--------------MERGING FRAG:" + frag + " AND FRAG:" + p.second());*/
						root = true;
						hasMerged = true;
					}
				}
			}

			if (root)
			{
				if(father == null && out != null)
				{
					out.setColor(Color.BLUE);
				}
				for (Node n : children)
				{
					//System.out.println("sending to child:" + n);
					send(n, new Message(frag, "NEW"));

				}
			}
			listMerge.clear();
			ready = false;
			
		} //else 
		else if (hasMerged && listMerge.size() !=0)
		{
			for (Pair<Node, Integer> p : listMerge)
			{
				if(p.first() != father)
				{
					children.add(p.first());
					send(p.first(),new Message(frag, "NEW"));
				}
			}
			listMerge.clear();
			
			
		}
		else if (!hasMerged && futureNew != null)
		{
			frag = (Integer) futureNew.getContent();
			setLabel(getID() + ":" + frag);
			hasMerged = true;
			father = (BasicNode) futureNew.getSender();
			this.setColor(DEFAULT_COLOR);
			children.remove(father);// only needed for the other vertex that compose the loop of length 1 that
									// starts the merge
			if (children.size() != 0)
			{
				for (Node n : children)
				{
					send(n, new Message(futureNew.getContent(),futureNew.getFlag()));
				}
			} else
			{
				send(father, new Message(null, "ECHO"));
			}
			futureNew = null;
		}
	}

	@Override
	public void onMessage(Message m)
	{
		/*
		 * updateLabel(); switch ((String)m.getContent()) { case "IAMDAD": if(father !=
		 * null && father != m.getSender()) { send(father, new Message("IAMDAD"));
		 * children.add(father); } father = (BasicNode) m.getSender(); break;
		 * 
		 * case "YOUAREDAD": children.add((BasicNode) m.getSender()); break;
		 * 
		 * default: break; }
		 */
		nbMess++;
		 Log("PHASE:" + phase + "::" + "NODE:" + getID() + " from frag:" + frag + " with father:" + father + " and " + children.size() + " children"+ " RECEIVE message:" + m.getFlag() + " from sender:" + m.getSender());

		switch (m.getFlag())
		{
		case "PULSE":
			// when receiving message pulse (from father) we increment the phase (reset all
			// var that has to be) and forward the message
			phase++;
			sendAckFrag();
			reset();
			for (Node n : children)
			{
				//System.out.println("sending PULSE to child:" + n);
				send(n, new Message(null,"PULSE"));
			}
			// moreover it send a message frag to know the minimum cost outgoing (from the
			// fragment) edge
			for (Node n : getNeighbors())
			{
				send(n, new Message(phase, "FRAG"));
				//System.out.println("PHASE:" + phase + "::" + "NODE:" + getID() + " from frag:" + frag + " with father:" + father + " send FRAG message to:" + n );
			}
			break;

		case "FRAG":
			// just answer with your frag number
			if (phase >= (Integer) m.getContent())
			{
				send(m.getSender(), new Message(frag, "ACK_FRAG"));
			} else
			{
				//System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!PROBLEM!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
				listAckFragToSend.add(m);
			}

			break;

		case "ACK_FRAG":
			// if we receive a frag message from a vertex of another fragment
			counterAckFragNeighbors++;
			Log(counterAckFragNeighbors + " on " + getNeighbors().size() + "ACK_FRAG RECEIVED");
			if ((Integer) m.getContent() != frag)
			{
				// if it is the first, we take it as min
				if (out == null)
				{
					out = getCommonLinkWith(m.getSender());
					//System.out.println("first out found:" + out);
					outNode = null;
				} 
				else // else we take it if the distance is smaller than the previous out
				{
					if (Distance.smaller(getCommonLinkWith(m.getSender()), out))
					{
						//System.out.println("new OUT=" + getCommonLinkWith(m.getSender()) + ":"
								//+ getCommonLinkWith(m.getSender()).getLength() + " is smaller than " + out + ":"
								//+ out.getLength());
						out = getCommonLinkWith(m.getSender());
						outNode = null;
					}
				}
			}
			checkCouterACK_FRAGCounterOUT();
			
			break;

		case "OUT":
			counterChildrenOut++;
			Log(counterChildrenOut + " on " + children.size() + " OUT RECEIVED");
			// when we receive an potential outgoing edge from sons: check and replace out
			// if smaller
			if (m.getContent() != null)
			{
				if (out == null)
				{
					out = (Link) m.getContent();
					outNode = (BasicNode) m.getSender();

				} else if (Distance.smaller((Link) m.getContent(), out))
				{
					out = (Link) m.getContent();
					outNode = (BasicNode) m.getSender();
				}
			}
			checkCouterACK_FRAGCounterOUT();
			/*
			 * else { if(father == null) {
			 * 
			 * System.out.println("-------------------algo terminated----------------------"
			 * ); return;
			 * 
			 * } else { send(father, m); } }
			 */
			
			break;

		case "ACK_NEW_ROOT":

			// if you have to change your father in the tree
			for (Node n : children)
			{
				send(n, new Message (m.getContent(),m.getFlag()));
			}
			if (out != null && ((Link) m.getContent()).equals(out))
			{
				if (outNode != null)
				{
					//System.out.println("changing father of:" + getID() + " into:" + outNode);
					children.add(m.getSender());
					children.remove(outNode);
					father = outNode;
					this.setColor(DEFAULT_COLOR);
				} else // you are the new root
				{
					children.add(father);
					father = null;
					this.setColor(Color.RED);
					//System.out.println("---------------" + getID() + " becomes new ROOT");

					send(out.getOtherEndpoint(this), new Message(frag, "MERGE"));
				}
			}

			ready = true;
			break;

		case "MERGE":
		{
			
			
			if (!hasMerged)
			{
				//System.out.println("adding:" + m.getSender() + " to list merge");
				listMerge.add(new Pair<Node, Integer>(m.getSender(), (Integer) m.getContent()));
			} else
			{
				children.add(m.getSender());
				send(m.getSender(), new Message(frag, "NEW"));
			}

		}
			break;

		case "NEW":

			if (true)
			{
				//System.out.println("Message from sender:" + m.getSender().getID());
				//System.out.println("________________Putting node:" + getID() + " from frag:" + frag +  " into frag:" + m.getContent() + "______");
				frag = (Integer) m.getContent();
				setLabel(getID() + ":" + frag);
				hasMerged = true;
				if(father == null && out != null)
				{
					out.setColor(Color.BLUE);
				}
				father = (BasicNode) m.getSender();
				this.setColor(DEFAULT_COLOR);
				children.remove(father);// only needed for the other vertex that compose the loop of length 1 that
										// starts the merge
				if (children.size() != 0)
				{
					for (Node n : children)
					{
						send(n, m);
					}
				} else
				{
					send(father, new Message(null, "ECHO"));
				}
				ready = false;
			} else
			{
				futureNew = m;
			}
			break;

		case "ECHO":
			counterEcho++;
			Log(counterEcho + " on " + children.size() + " ECHO messages RECEIVED");
			//System.out.println("message ECHO:" + getID() + " with father:" + father + " from node:" + m.getSender().getID());
			if (counterEcho >= children.size())
			{
				counterEcho = 0;
				if (father != null)
				{
					send(father, new Message(m.getContent(),m.getFlag()));
				} 
				else
				{
					phase++;
					sendAckFrag();
					//System.out.println("----------------PHASE:" + phase + " from node:" + getID() + "-------------------");
					reset();

					for (Node n : children)
					{
						send(n, new Message(null, "PULSE"));
					}
					// moreover it send a message frag to know the minimum cost outgoing (from the
					// fragment) edge
					for (Node n : getNeighbors())
					{
						send(n, new Message(phase, "FRAG"));
					}
				}
			}
			break;
			
		case "STOP":
			for(Node n : children)
			{
				send(n,new Message(null,"STOP"));
			}
			return;
			//break;
		}
		
		
	}

	@Override
	public void onSelection()
	{

	}

	// Cherche la distance au voisin le plus proche qui n'est pas dans le fragment
	private Distance findClosestDistOutsideOfFragment()
	{
		List<Node> N = getNeighbors();
		Distance closestDist = null;
		if (!N.isEmpty())
			// Initialisation de closest
			closestDist = new Distance(getInLinkFrom(N.get(0)).getLength(), Integer.min(this.getID(), N.get(0).getID()),
					Integer.max(this.getID(), N.get(0).getID()));

		// Recherche du plus petit parmi les voisins
		for (Node v : N)
		{
			if (children.contains(v))
				continue;
			Distance distToV = new Distance(getInLinkFrom(v).getLength(), Integer.min(this.getID(), v.getID()),
					Integer.max(this.getID(), v.getID()));
			if (distToV.compareTo(closestDist) == -1)
				closestDist = distToV;
		}

		// Set invader
		if (closestDist.getMaxID() == this.getID())
			closestDist.setOlder(true);
		else
			closestDist.setOlder(false);

		return closestDist;
	}

	/*
	 * Renvoie le sommet voisin correspondant à objet Distance Renvoie null si le
	 * sommet est introuvable
	 */
	private BasicNode getNodeFromDist(Distance dist)
	{
		List<Node> N = getNeighbors();
		if (N.isEmpty())
			return null;

		for (Node v : N)
		{
			Distance distToV = new Distance(getInLinkFrom(v).getLength(), Integer.min(this.getID(), v.getID()),
					Integer.max(this.getID(), v.getID()));
			if (distToV.compareTo(dist) == 0)
			{
				return (BasicNode) v;
			}
		}

		return null;
	}

	/*
	 * Cherche parmi les voisins s'il y en a un à distance dist et envoie le message
	 * m à ce voisin s'il le trouve renvoie vrai si le sommet est trouvé et faux
	 * sinon
	 */
	private boolean sendToDist(Distance dist, Message m)
	{
		List<Node> N = getNeighbors();
		if (N.isEmpty())
			return false;

		for (Node v : N)
		{
			Distance distToV = new Distance(getInLinkFrom(v).getLength(), Integer.min(this.getID(), v.getID()),
					Integer.max(this.getID(), v.getID()));
			if (distToV.compareTo(dist) == 0)
			{
				this.send(v, m);
				return true;
			}
		}

		return false;
	}

	// Met à jour les infos affichées dans le label du sommet
	private void updateLabel()
	{
		String label = "";
		label += "Self : " + Integer.toString(getID()) + "  ";
		if (father != null)
			label += "Father : " + Integer.toString(father.getID()) + "  ";
		else
			label += "Father :   ";
		label += "Children : ";
		for (Node v : children)
		{
			label += Integer.toString(v.getID()) + " ";
		}

		setLabel(label);
	}

	private void reset()
	{
		root = false;
		ready = false;
		hasMerged = false;
		out = null;
		outNode = null;
		counterAckFragNeighbors = 0;
		counterChildrenOut = 0;
		counterEcho = 0;
		//futureNew = null;
		//listMerge.clear();
		
		//System.out.println("______________RESET OF NODE:" + getID() + " of FRAG:" + frag + " with father:" + father + " _______________");
		for(Node n : children)
		{
			//System.out.println("with child:" + n.getID());
		}
		//System.out.println("______________END::PHASE: " + (phase-1) + " ______________");
		
	}

	private void sendAckFrag()
	{
		Set<Message> fragMessToRemove = new HashSet<>();
		for (Message m : listAckFragToSend)
		{
			if((Integer)m.getContent() == phase)
			{
				send(m.getSender(), new Message(frag, "ACK_FRAG"));
				fragMessToRemove.add(m);
			}
			
		}
		
		listAckFragToSend.removeAll(fragMessToRemove);
		//listAckFragToSend.clear();
	}
	
	
	
	@Override
	public void send(Node n, Message mess)
	{
		// TODO Auto-generated method stub
		Log("PHASE:" + phase + "::" + "NODE:" + getID() + " from frag:" + frag + " with father:" + father + " and " + children.size() + " children" + " SEND message:" + mess.getFlag() + " to node:" + n.getID());
		super.send(n,mess);
	}
	
	private void Log(String s)
	{
		Log(s,true);
	}

	private void Log(String s, boolean append)
	{
		Logger logger = Logger.getLogger("MyLog");  
		logger.setUseParentHandlers(false);
	
	    FileHandler fh;  

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

	    } catch (SecurityException e) {  
	        e.printStackTrace();  
	    } catch (IOException e) {  
	        e.printStackTrace();  
	    }  
	    
	    

		
	}

	public int getNbMess()
	{
		return nbMess;
	}

}
