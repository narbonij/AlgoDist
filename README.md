# AlgoDist

-to create runnable jar from source: use ant command when located in the same directory as build.xml
-to use GHS: load a topology (right click -> load topology)
			 start the topology (or remove a node).
			 
BEWARE:-adding a node stops the topology
	   -removing a node resart the algorithm
	   
	   
COLOR CODE:-RED NODE consider themselves as root of their fragment
		   -RED EDGE are OUT edge (minimum fragment-outgoing edge) selected by a fragment
		   -BLUE EDGE are edges of the substree in the fragment
		   -MAGENTA EDGE: final MST.