import jbotsim.Message;
import jbotsim.Node;

import java.util.List;

import jbotsim.Color;


/*
 * Chaque sommet envoie un message à son voisin le plus proche
 * Chaque sommet à pour label le nombre de messages reçus
 */
public class BasicNode extends Node {
	boolean rootMode; //Si rootMode = true, le sommet et la racine de son fragment
	BasicNode father; //Le père du sommet
	int d;
	
	@Override
	public void onStart() {
		super.disableWireless();
		rootMode = true;
		father = null;
		d = 0;
	}
	
	@Override
	public void onClock() {
		sendToDist(findClosestDist(), new Message("hello"));
		this.setLabel(d);
		d = 0;
	}
	
	@Override
	public void onMessage(Message message) {
		if(message.getContent() == "hello")
			d++;
	}
	
	@Override
	public void onSelection() {
		
	}
	
	//Cherche la distance au voisin le plus proche
	public Distance findClosestDist() {
		List<Node> N = getNeighbors();
		Distance closestDist = null;
		if (!N.isEmpty())
			//Initialisation de closest
			closestDist = new Distance(getInLinkFrom(N.get(0)).getLength(), 
					Integer.min(this.getID(), N.get(0).getID()),
					Integer.max(this.getID(), N.get(0).getID()));			
			
		for(Node v : N) {
			Distance distToV = new Distance(getInLinkFrom(v).getLength(), 
					Integer.min(this.getID(), v.getID()),
					Integer.max(this.getID(), v.getID()));
			if (distToV.compareTo(closestDist) == -1)
				closestDist = distToV;
		}
		return closestDist;
	}
	
	/*  Cherche parmi les voisins s'il y en a un à distance dist
	 *  et envoie le message m à ce voisin s'il le trouve
	 *  renvoie vrai si le sommet est trouvé et faux sinon
	 */ 
	public boolean sendToDist(Distance dist, Message m) {
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
}
