import java.util.List;

public class Group_Segments {

    private String road;
    private double length;
    private List<Segment> segments;

    public Group_Segments(String road, List<Segment> Segments, double length){
        this.road = road;
        this.segments= Segments;
        this.length = length;

    }

    public String get_Road() {
        return road;
    }

    public List<Segment> getSegments() {
        return segments;
    }

    public double getLength() {
        return length;
    }

    public void AddLength(double length) {
        this.length += length;
    }

    public void AddSegments(Segment s){segments.add(s);}
}
