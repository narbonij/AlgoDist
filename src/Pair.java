
public class Pair<T1, T2>
{
	
	private T1 first;
	private T2 second;

	public Pair(T1 first, T2 second)
	{
		this.setFirst(first);
		this.setSecond(second);
	}

	public T1 first()
	{
		return first;
	}

	public void setFirst(T1 first)
	{
		this.first = first;
	}

	public T2 second()
	{
		return second;
	}

	public void setSecond(T2 second)
	{
		this.second = second;
	}
	
	

}
