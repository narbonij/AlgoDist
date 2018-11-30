
public class Distance implements Comparable {
	private double distance; //distance euclidienne au sommet
	private int minID; // plus petit ID des deux sommets
	private int maxID; // plus grand ID des deux sommets
	private boolean older; //en cas de merge est-ce que mon fragment va devenir le père de l'autre (est-ce que le sommet de plus grand ID est dans mon fragment ?)
	
    public Distance(double distance, int minID, int maxID) {
		this.distance = distance;
		this.minID = minID;
		this.maxID = maxID;
	}
    
    public boolean isOlder() {
    	return older;
    }
    
    public void setOlder(boolean invader) {
    	this.older = invader;
    }
    
    public int getMaxID() {
    	return maxID;
    }
	
	@Override
	public int compareTo(Object arg0) {
		Distance d = (Distance) arg0;
		if(distance < d.distance)
			return -1;
		else if (distance > d.distance)
			return 1;
		else {
			if (minID < d.minID)
				return -1;
			else if (minID > d.minID)
				return 1;
			else {
				if (maxID < d.maxID)
					return -1;
				else if (maxID > d.maxID)
					return 1;
			}
		}
		return 0;

	}
	
	
}
