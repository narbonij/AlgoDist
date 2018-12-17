
public class GHSProperty
{
	
	public boolean _log;
	public String _graphFilename;
	public String _resultFilename;
	public boolean _verboseMode;
	public int _verboseStep;
	public int _clockSpeed;
	public boolean _shuffleIds;
	public boolean _display;
	public String _perfFilename;

	public GHSProperty(String[] args)
	{
		//default values
		_log = false;
		_graphFilename = null;
		_resultFilename = null;
		_verboseMode = false;
		_verboseStep = 1;
		_clockSpeed = 50;
		_shuffleIds = false;
		_display = false;
		_perfFilename = null;
		for(int i = 0;i<args.length;i++)
		{
			String arg = args[i];
			if(arg.startsWith("-"))
			{
				switch (arg)
				{
				case "-log":
					_log = true;
					break;
					
				case "-s":
					if( i+1< args.length && !args[i+1].startsWith("-"))
					{
						i++;
						_resultFilename = args[i];
					}
					else
					{
						System.out.println("Invalid filename for result.");
					}
					break;
				case "-v":
					_verboseMode = true;
					if(i+1<args.length && !args[i+1].startsWith("-"))
					{
						String strVerboseStep = args[i+1];
						try
						{
							_verboseStep = Integer.parseInt(strVerboseStep);
							i++;
						}
						catch (NumberFormatException e) 
						{
							
						}
					}
					break;
					
				case "-cs":
					if(i+1<args.length && !args[i+1].startsWith("-"))
					{
						String strVerboseStep = args[i+1];
						try
						{
							_clockSpeed = Integer.parseInt(strVerboseStep);
							i++;
						}
						catch (NumberFormatException e) 
						{
							
						}
					}
					break;
					
				case "-si":
					_shuffleIds = true;
					break;
					
				case "-d":
					_display = true;
					break;
					
				case "-p":
					if( i+1< args.length && !args[i+1].startsWith("-"))
					{
						i++;
						_perfFilename = args[i];
					}
					else
					{
						System.out.println("Invalid filename for performances.");
					}
					break;

				default:
					break;
				}
		
			}
			else
			{
				_graphFilename = arg;
			}
		}
		
	}

}
