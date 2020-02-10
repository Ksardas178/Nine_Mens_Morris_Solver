package sample;

public class FieldInfo {

    private int[] elements = new int[2];

    public FieldInfo(int blue, int red) {
        elements[0] = blue;
        elements[1] = red;
    }

    public FieldInfo() {
        elements[0] = 0;
        elements[1] = 0;
    }

    //Убирает фишку команды
    public void remove(int team) {
        elements[team - 1] -= 1;
    }

    //Добавляет фишку команде
    public void add(int team) {
        elements[team - 1] += 1;
    }

    //Возвращает разницу между фишками команды и соперника
    public int getDifference(int team, int opponent) {
        return elements[team - 1] - elements[opponent - 1];
    }

    //Возвращает количество элементов команды
    public int getElements(int team) {
        return elements[team - 1];
    }
}

