package sample;

public class Turn {
    public int currentID;
    public int nextID;
    public int takenID;

    //region [Constructors]

    public Turn(Turn t) {
        currentID = t.currentID;
        nextID = t.nextID;
        takenID = t.takenID;
    }

    public Turn(int start, int finish, int got) {
        currentID = start;
        nextID = finish;
        takenID = got;
    }

    public Turn(int finish) {
        currentID = -1;
        nextID = finish;
        takenID = -1;
    }

    public Turn(String textTurn) throws Exception {
        String[] t = textTurn.split(" ");
        currentID = Integer.parseInt(t[0]);
        nextID = Integer.parseInt(t[1]);
        takenID = Integer.parseInt(t[2]);
    }

    //endregion [Constructors]

    //Переопределение вывода хода
    @Override
    public String toString() {
        return ("(" + currentID + ", " + nextID + ", " + takenID + ')');
    }

    //Переопределение сравнения
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Turn other = (Turn) obj;
        return other.takenID == this.takenID &&
                other.nextID == this.nextID &&
                other.currentID == this.currentID;
    }
}
