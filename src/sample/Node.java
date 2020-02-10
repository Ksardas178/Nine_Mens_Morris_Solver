package sample;

public class Node {

    public Point placement;
    public int[] linkedNodes;
    public int team = 0;

    public Node(Point place) {
        this.placement = place;
    }

    public Node() {
        this.placement = new Point();
    }

}

