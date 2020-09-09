import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.io.File;
import java.text.DecimalFormat;
import java.util.*;

/**
 * This is the main class for the mapping program. It extends the GUI abstract
 * class and implements all the methods necessary, as well as having a main
 * function.
 * 
 *
 */
public class Mapper extends GUI {
	public static final Color NODE_COLOUR = new Color(77, 113, 255);
	public static final Color SEGMENT_COLOUR = new Color(130, 130, 130);
	public static final Color HIGHLIGHT_COLOUR = new Color(255, 219, 77);

	// these two constants define the size of the node squares at different zoom
	// levels; the equation used is node size = NODE_INTERCEPT + NODE_GRADIENT *
	// log(scale)
	public static final int NODE_INTERCEPT = 1;
	public static final double NODE_GRADIENT = 0.8;

	// defines how much you move per button press, and is dependent on scale.
	public static final double MOVE_AMOUNT = 100;
	// defines how much you zoom in/out per button press, and the maximum and
	// minimum zoom levels.
	public static final double ZOOM_FACTOR = 1.3;
	public static final double MIN_ZOOM = 1, MAX_ZOOM = 200;

	// how far away from a node you can click before it isn't counted.
	public static final double MAX_CLICKED_DISTANCE = 0.15;

	// these two define the 'view' of the program, ie. where you're looking and
	// how zoomed in you are.
	private Location origin;
	private double scale;

	// our data structures.
	private Graph graph;

	private boolean isTime = false;
	private boolean isDistance = true;

	private Node Start = null;
	private Node Goal = null;

	private List<Segment> ans = new ArrayList<>();

	private int Click_count = 0; //Stage of click


	protected void setDist(){}

	protected void setTime(){}

	@Override
	protected void redraw(Graphics g) {
		if (graph != null)
			graph.draw(g, getDrawingAreaDimension(), origin, scale);
	}

	@Override
	protected void onClick(MouseEvent e) {

		Location clicked = Location.newFromPoint(e.getPoint(), origin, scale);
		// find the closest node.
		double bestDist = Double.MAX_VALUE;
		Node closest = null;

		for (Node node : graph.nodes.values()) {
			double distance = clicked.distance(node.location);
			if (distance < bestDist) {
				bestDist = distance;
				closest = node;
			}
		}

		// if it's close enough, highlight it and show some information.
		if (clicked.distance(closest.location) < MAX_CLICKED_DISTANCE) {

			if(Click_count == 0) {
				Start = closest;
				graph.setHighlight(Start);
				Click_count = 1;
				getTextOutputArea().setText(closest.toString());
			}else if(Click_count == 1){
				Goal = closest; //Sets goal node
				graph.setHighlight(Shortest_Path_Finder(Start,Goal)); //Calls A* search method

				Click_count = 0;
			}

		}

	}

	@Override
	protected void onSearch() {
		// Does nothing
	}

	@Override
	protected void onMove(Move m) {
		if (m == GUI.Move.NORTH) {
			origin = origin.moveBy(0, MOVE_AMOUNT / scale);
		} else if (m == GUI.Move.SOUTH) {
			origin = origin.moveBy(0, -MOVE_AMOUNT / scale);
		} else if (m == GUI.Move.EAST) {
			origin = origin.moveBy(MOVE_AMOUNT / scale, 0);
		} else if (m == GUI.Move.WEST) {
			origin = origin.moveBy(-MOVE_AMOUNT / scale, 0);
		} else if (m == GUI.Move.ZOOM_IN) {
			if (scale < MAX_ZOOM) {
				// yes, this does allow you to go slightly over/under the
				// max/min scale, but it means that we always zoom exactly to
				// the centre.
				scaleOrigin(true);
				scale *= ZOOM_FACTOR;
			}
		} else if (m == GUI.Move.ZOOM_OUT) {
			if (scale > MIN_ZOOM) {
				scaleOrigin(false);
				scale /= ZOOM_FACTOR;
			}
		}
	}

	@Override
	protected void onLoad(File nodes, File roads, File segments, File polygons) {
		graph = new Graph(nodes, roads, segments, polygons);
		origin = new Location(-250, 250); // close enough
		scale = 1;

	}

	/**
	 * This method does the nasty logic of making sure we always zoom into/out
	 * of the centre of the screen. It assumes that scale has just been updated
	 * to be either scale * ZOOM_FACTOR (zooming in) or scale / ZOOM_FACTOR
	 * (zooming out). The passed boolean should correspond to this, ie. be true
	 * if the scale was just increased.
	 */
	private void scaleOrigin(boolean zoomIn) {
		Dimension area = getDrawingAreaDimension();
		double zoom = zoomIn ? 1 / ZOOM_FACTOR : ZOOM_FACTOR;

		int dx = (int) ((area.width - (area.width * zoom)) / 2);
		int dy = (int) ((area.height - (area.height * zoom)) / 2);

		origin = Location.newFromPoint(new Point(dx, dy), origin, scale);
	}

	/**
	 * Employs A* search to return a list of segments of the shortest path
	 *
	 */
	private List<Segment> Shortest_Path_Finder(Node start, Node goal) {
		if(this.Start==null || this.Goal == null){ return null; }

		List<Node> Path = new ArrayList<>();
		Queue<Tuple> Fringe = new PriorityQueue<>(1, new Comparator<Tuple>() { //Comparator returns lowest fcost
			@Override
			public int compare(Tuple o1, Tuple o2) {
				if (o1.getF_Cost() < o2.getF_Cost())
					return -1;
				else
					return 1;
			}
		});
		Set<Node> Visited = new HashSet<>(); //Nodes that have been visited

		List<Segment> Ans = new Stack<>(); //Final answer of segments to be returned
		start = graph.nodes.get(Start);
		goal = graph.nodes.get(Goal);
		double dist = 0;


		dist = Start.location.distance(Goal.location); //Dist from start to goal

		Tuple first = new Tuple(Start, null, 0, dist); //First tuple created
		Fringe.add(first);
		while (!Fringe.isEmpty()) {

			Tuple tuple = Fringe.poll();

			if (Visited.contains(tuple.getStart())) {
				continue;
			}
			Visited.add(tuple.getStart());

			if (tuple.getStart().equals(Goal)) {//Recontructs path when node equals the goal
				Path = Recontruct_Path(tuple);
				break;
			}
			Node Neigh = null;
			for (Segment s : tuple.getStart().segments) {//Goes through neighbours

				if (tuple.getStart().equals(s.end) && s.road.oneway==1) { //Checks for one way and if its the end of a segment
					continue;
				} else if(tuple.getStart().equals(s.start) && s.road.oneway==1) {
					Neigh = s.end;
				}else if(tuple.getStart().equals(s.end)){
					Neigh = s.start;
				}else if(tuple.getStart().equals(s.start)){
					Neigh = s.end;
				}

				if (!Visited.contains(Neigh)) {
					double g = tuple.getG_Cost() + s.length;
					double f = g + Goal.location.distance(Neigh.location);
					System.out.println(Goal.location.distance(Neigh.location));
					Tuple newElement = new Tuple(Neigh, tuple, g, f); //Creates new tuple
					Fringe.add(newElement); //Adds tuple to Fringe
				}
			}
		}
		double total = 0;

		List<Segment> segs = new ArrayList<>();

		Set<Group_Segments> Group_S = new HashSet<>();
		Set<String> r = new HashSet<>();
		for (int i = 0; i < Path.size() - 1; i++) { //Goes through path to get segments between nodes
			Node N1 = Path.get(i);
			Node N2 = Path.get(i + 1);
			Segment s = Get_Segment_Between_Nodes(N1, N2);
			Ans.add(s);
			r.add(s.road.name);

		}

		for(String roads:r){
			Group_Segments g = new Group_Segments(roads,segs,0);
			Group_S.add(g);
		}

		for (Segment s : Ans) {
			for(Group_Segments G_S:Group_S){
				if(G_S.get_Road().equals(s.road.name)){
						G_S.AddSegments(s);
						G_S.AddLength(s.length);
				}
			}
		}
		StringBuilder output = new StringBuilder();

		for (Group_Segments G_s : Group_S) {
			DecimalFormat df = new DecimalFormat("###.###");

			output.append(G_s.get_Road()).append(":").append("\t").append(df.format(G_s.getLength())).append("km").append("\n");
			total+=G_s.getLength();
		}

			DecimalFormat df2 = new DecimalFormat("###.###");

			getTextOutputArea().setText(output + "\n" + "Total: " + df2.format(total));
			return Ans;

	}
	/**gets segment between nodes.
	  */
	public Segment Get_Segment_Between_Nodes(Node first, Node Second){
		Segment ans = null;

		for(Segment s:first.segments){
			if(s.getStart().equals(Second)){
				ans = s;
			}else if(s.getEnd().equals(Second)){
				ans = s;
			}
		}
		return ans;

	}

	/**
	 * Traces back path to get final path of nodes
	 */
	public List<Node> Recontruct_Path(Tuple end){
		List<Node> Path = new ArrayList<>();
		while(!end.getStart().equals(Start)){
			Path.add(end.getStart());
			end = end.getPrev();

		}
		Path.add(Start);
		return Path;
	}


	/**
	 * Highlights all the segments that are considered oneway
	 */
	public List<Segment> Highlight_ALL_ONEWAY(){
		List<Segment> oneway = new ArrayList<>();
		for(Segment s : graph.segments){
			if(s.road.oneway==1){
				oneway.add(s);
			}
		}
		return oneway;
	}

	public static void main(String[] args) {
		new Mapper();
	}
}

