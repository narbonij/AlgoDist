import jbotsim.Message;
import jbotsim.Node;

import java.util.List;

import jbotsim.Color;


/*
 * Pour l'instant on a juste des sommets (u) qui comptent le nombre de sommets voisins (de u) pour lesquels u est le voisin le plus proche.
 */
public class BasicNode extends Node {
	int d = 0;
	
	@Override
	public void onStart() {
		
	}
	
	@Override
	public void onClock() {
		List<Node> N = getNeighbors();
		Node closest = null;
		if (!N.isEmpty())
			closest = N.get(0);
		for(Node n : N) {
			if (getInLinkFrom(n).getLength() < getInLinkFrom(closest).getLength())
				closest = n;
		}
		if (closest != null)
			send(closest, new Message("hell"));
		
		switch (d) {
		case 0:
			setColor(Color.red);
			break;
			
		case 1:
			setColor(Color.orange);
			break;
			
		case 2:
			setColor(Color.yellow);
			break;

		default:
			setColor(Color.green);
			break;
		}
		
		setLabel(d);
		d = 0;
	}
	
	@Override
	public void onMessage(Message message) {
		if(message.getContent() == "hell")
			d++;
	}
	
	@Override
	public void onSelection() {
		
	}
}
