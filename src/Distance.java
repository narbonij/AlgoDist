import jbotsim.Link;

public class Distance  {
	/*private double distance; //distance euclidienne au sommet
	private int minID; // plus petit ID des deux sommets
	private int maxID; // plus grand ID des deux sommets
	private boolean older; //en cas de merge est-ce que mon fragment va devenir le pere de l'autre (est-ce que le sommet de plus grand ID est dans mon fragment ?)
	
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
	
	//comparison function for Link => total order on link 
	public static boolean smaller(Link l1, Link l2)
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
	*/
	
}
