public class Tuple {

    private Node Start;
    private Tuple Prev;
    private double G_Cost;
    private double F_Cost;

    public Tuple(Node Start, Tuple Prev, double G_Cost, double F_Cost){
        this.Start = Start;
        this.Prev = Prev;
        this.G_Cost = G_Cost;
        this.F_Cost = F_Cost;

    }
    public double getF_Cost(){return F_Cost;}
    public double getG_Cost(){return G_Cost;}
    public Node getStart(){return Start;}
    public Tuple getPrev(){return Prev; }




}
