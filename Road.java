import java.util.Collection;
import java.util.HashSet;

/**
 * Road represents ... a road ... in our graph, which is some metadata and a
 * collection of Segments. We have lots of information about Roads, but don't
 * use much of it.
 * 
 * @author tony
 */
public class Road {
	public final int roadID;
	public final String name, city;
	public final Collection<Segment> components;
	public int oneway;
	public int notforcar;
	public int notforpede;
	public int notforbicy;
	public int speed;

	public Road(int roadID, int type, String label, String city, int oneway,
			int speed, int roadclass, int notforcar, int notforpede,
			int notforbicy) {
		this.roadID = roadID;
		this.city = city;
		this.name = label;
		this.components = new HashSet<Segment>();
		this.oneway = oneway;
		this.notforcar = notforcar;
		this.notforpede = notforpede;
		this.notforbicy = notforbicy;
		this.speed = speed;
	}

	public void addSegment(Segment seg) {
		components.add(seg);
	}

	public int getSpeed(){return this.speed;}

	public int getOneway() {
		return oneway;
	}
}

// code for COMP261 assignments