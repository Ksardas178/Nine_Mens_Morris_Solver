package sample;

public class Mill {

    public int[] elements = new int[3];

    //region [Constructors]

    public Mill(int first, int second, int third) {
        elements[0] = first;
        elements[1] = second;
        elements[2] = third;
    }

    public Mill(String s) {
        String[] idxs = s.split(" ");
        for (int i = 0; i <= 2; i++) {
            elements[i] = Integer.parseInt(idxs[i]);
        }
    }

    //endregion [Constructors]

    public boolean hasElement(int e) {
        return (elements[0] == e) || (elements[1] == e) || (elements[2] == e);
    }
}
