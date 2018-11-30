
public class Distance implements Comparable {
	double distance;
	int minID;
	int maxID;
    public Distance(double distance, int minID, int maxID) {
		this.distance = distance;
		this.minID = minID;
		this.maxID = maxID;
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
