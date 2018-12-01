import jbotsim.Message;
import jbotsim.Node;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import jbotsim.Color;


/*
 * Chaque sommet envoie un message à son voisin le plus proche
 * Chaque sommet à pour label le nombre de messages reçus
 */
public class BasicNode extends Node {
	BasicNode father; //Le père du sommet
	HashSet<BasicNode> children;
	
	@Override
	public void onStart() {
		super.disableWireless();
		father = null;
		children = new HashSet<BasicNode>() ;
		
		setColor(Color.getRandomColor());
		
		//On cherche le sommet le plus proche
		Distance closestDist = findClosestDistOutsideOfFragment();
		
		//Si on est le plus vieux, on devient le père sinon on devient le fils
		if(closestDist.isOlder()) {
			sendToDist(closestDist, new Message("IAMDAD"));
			if(getNodeFromDist(closestDist) != null)
				children.add(getNodeFromDist(closestDist));
		}
		else {
			sendToDist(closestDist, new Message("YOUAREDAD"));
			if(getNodeFromDist(closestDist) != null)
				father = getNodeFromDist(closestDist);
		}
		updateLabel();
	}
	
	@Override
	public void onClock() {
		updateLabel();
		if(father != null)
			setColor(father.getColor());
	}
	
	@Override
	public void onMessage(Message m) {
		updateLabel();
		switch ((String)m.getContent()) {
		case "IAMDAD":
			if(father != null && father != m.getSender()) {
				send(father, new Message("IAMDAD"));
				children.add(father);				
			}
			father = (BasicNode) m.getSender();
			break;
			
		case "YOUAREDAD":
			children.add((BasicNode) m.getSender());
			break;

		default:
			break;
		}
	}
	
	@Override
	public void onSelection() {
		
	}
	
	//Cherche la distance au voisin le plus proche qui n'est pas dans le fragment
	private Distance findClosestDistOutsideOfFragment() {
		List<Node> N = getNeighbors();
		Distance closestDist = null;
		if (!N.isEmpty())
			//Initialisation de closest
			closestDist = new Distance(getInLinkFrom(N.get(0)).getLength(), 
					Integer.min(this.getID(), N.get(0).getID()),
					Integer.max(this.getID(), N.get(0).getID()));			
			
		//Recherche du plus petit parmi les voisins
		for(Node v : N) {
			if(children.contains(v))
				continue;
			Distance distToV = new Distance(getInLinkFrom(v).getLength(), 
					Integer.min(this.getID(), v.getID()),
					Integer.max(this.getID(), v.getID()));
			if (distToV.compareTo(closestDist) == -1)
				closestDist = distToV;
		}
		
		//Set invader
		if (closestDist.getMaxID() == this.getID())
			closestDist.setOlder(true);
		else
			closestDist.setOlder(false);
		
		return closestDist;
	}
	
	/*
	 * Renvoie le sommet voisin correspondant à objet Distance
	 * Renvoie null si le sommet est introuvable
	 */
	private BasicNode getNodeFromDist(Distance dist) {
		List<Node> N = getNeighbors();
		if (N.isEmpty())
			return  null;
		
		for(Node v : N) {
			Distance distToV = new Distance(getInLinkFrom(v).getLength(), 
					Integer.min(this.getID(), v.getID()),
					Integer.max(this.getID(), v.getID()));
			if (distToV.compareTo(dist) == 0) {
				return (BasicNode) v;
			}
		}
		
		return null;
	}
	
	
	/*  Cherche parmi les voisins s'il y en a un à distance dist
	 *  et envoie le message m à ce voisin s'il le trouve
	 *  renvoie vrai si le sommet est trouvé et faux sinon
	 */ 
	private boolean sendToDist(Distance dist, Message m) {
		List<Node> N = getNeighbors();
		if (N.isEmpty())
			return  false;
		
		for(Node v : N) {
			Distance distToV = new Distance(getInLinkFrom(v).getLength(), 
					Integer.min(this.getID(), v.getID()),
					Integer.max(this.getID(), v.getID()));
			if (distToV.compareTo(dist) == 0) {
				this.send(v, m);
				return true;
			}
		}
		
		return false;
	}
	
	//Met à jour les infos affichées dans le label du sommet
	private void updateLabel() {
		String label = "";
		label += "Self : " + Integer.toString(getID()) + "  ";
		if(father != null)
			label += "Father : " + Integer.toString(father.getID()) + "  ";
		else
			label += "Father :   ";
		label += "Children : ";
		for (BasicNode v : children) {
			label += Integer.toString(v.getID()) + " ";
		}
		
		setLabel(label);
	}
}
