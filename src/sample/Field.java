package sample;

import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Field {

    public int currentTurnNumber = 0;
    public String name;
    public Set<Shape> shapes = new HashSet<>();
    public Set<Text> indexes = new HashSet<>();

    private Node[] nodes;
    private Borders borderPoints = new Borders();
    private Mill[] mills;
    private FieldInfo things = new FieldInfo(0, 0);
    private int thingsAmount;

    //region [Constructors]

    public Field() {
        name = "default_name";
    }

    public Field(String fileName) {
        readField(fileName);
        name = fileName;
    }

    public Field(String fileName, String nName) {
        readField(fileName);
        name = nName;
    }

    public Field(Node[] nNodes, String nName) {
        nodes = nNodes;
        name = nName;
    }

    //endregion [Constructors]

    //region [ID/convert functions]

    //endregion [ID/convert functions]

    //region [Turns]

    //Поиск хода для текущей команды
    public Turn findTurn(int turnsDepth) {
        List<Turn> turns = new LinkedList<Turn>();
        Turn newTurn;
        int currTeam = currentTurnNumber % 2 + 1;
        double quality = -Double.MAX_VALUE;//Качество лучшего хода
        double rate;
        Turn result;

        if (currentTurnNumber < thingsAmount * 2)
            turns = whereToPlaceNew(currTeam);
        else turns = whereToMove(currTeam);

        result = new Turn(turns.get(0));

        for (Turn t : turns) {
            makeTurn(t, currTeam);
            rate = predictRate(turnsDepth*2+1);//Предсказываем последствия (для себя)
            if (rate > quality) {
                quality = rate;
                result = new Turn(t);
            }
            reverseTurn(t);
        }
        return result;
    }

    //Ход
    public void makeTurn(Turn t, int team) {
        int opponent = (team == 1) ? 2 : 1;
        if (t.currentID != -1) team = leave(t.currentID);
        else things.add(team);
        if (t.takenID != -1) {
            leave(t.takenID);
            things.remove(opponent);
        }
        occupy(t.nextID, team);
        currentTurnNumber += 1;
    }

    public void makeTurnWithCheck(Turn t, int team) throws IllegalArgumentException {
        List<Turn> turns = new LinkedList<Turn>();
        if (currentTurnNumber < thingsAmount * 2)
            turns = whereToPlaceNew(team);
        else turns = whereToMove(team);
        if (turns.contains(t)) makeTurn(t, team);
        else throw new IllegalArgumentException("Невозможный ход");
    }

    //Отмена хода
    private void reverseTurn(Turn t) {
        int team, opponent;
        team = leave(t.nextID);
        opponent = (team == 1) ? 2 : 1;
        if (t.takenID != -1) {
            occupy(t.takenID, opponent);
            things.add(opponent);
        }
        if (t.currentID != -1) occupy(t.currentID, team);
        else things.remove(team);
        currentTurnNumber--;
    }

    //Постановка фишки
    private void occupy(int idx, int team) {
        nodes[idx].team = team;
    }

    //Снятие фишки с доски
    private int leave(int idx) {
        int result = nodes[idx].team;
        nodes[idx].team = 0;
        return result;
    }

    //endregion [Turns]

    //region [Graphics]

    //Автоматическое ремасштабирование
    public void setScale() {
        borderPoints.setScale();
    }

    //Ручное ремасштабирование
    public void setScale(double coeff) {
        borderPoints.setScale(coeff);
    }

    //Вывод поля на экран
    public void show() {
        shapes.clear();
        indexes.clear();
        if (borderPoints.centerAlignOffset == null)
            setScale();//Перед отображением нового поля происходит автомасштабирование
        for (int i = 0; i < nodes.length; i++) {
            Node node = nodes[i];
            Point currPoint = node.placement;
            shapes.add(currPoint.markOccupied(borderPoints.centerAlignOffset, borderPoints.minPoint, borderPoints.scale, node.team));//Отмечаем клетки цветом команды
            for (int linkedNode : node.linkedNodes) {
                Point linkedPoint = nodes[linkedNode].placement;
                shapes.add(currPoint.line(linkedPoint, borderPoints.centerAlignOffset, borderPoints.minPoint, borderPoints.scale));
            }
            indexes.add(currPoint.subscribeIdx(i, borderPoints.centerAlignOffset, borderPoints.minPoint, borderPoints.scale));
        }
    }

    //endregion [Graphics]

    //region [IO]

    public void readField(String fileName) {
        try (FileReader reader = new FileReader(fileName + ".txt")) {
            Scanner scan = new Scanner(reader);
            int i, x, y, snap;
            LinkedList<LinkedList<Integer>> snaps = new LinkedList<LinkedList<Integer>>();
            Point newPoint;
            thingsAmount = Integer.parseInt(scan.nextLine());
            things = new FieldInfo();
            borderPoints = new Borders();
            currentTurnNumber = 0;
            String line = scan.nextLine();
            List<Point> points = new LinkedList<>();
            i = 1;
            do {
                String[] info = line.replaceAll("[a-z]+: *", "").split(" ");
                i = Integer.parseInt(info[0]);
                x = Integer.parseInt(info[1]);
                y = Integer.parseInt(info[2]);
                LinkedList<Integer> linkedNodes = new LinkedList<>();//Создаем список под связанные с данным узлы
                for (int j = 3; j < info.length; j++) {//Перебираем индексы связанных с текущей точек
                    snap = Integer.parseInt(info[j]);
                    linkedNodes.add(snap);//Заполняем список связей
                }
                snaps.add(linkedNodes);//Добавляем список привязок в список узлов
                newPoint = new Point(x, y);
                points.add(newPoint);
                borderPoints.checkPoint(newPoint);//Ремасштабирование
                line = scan.nextLine();
            } while (!line.equals("mills:"));//Пока не доберемся до перечисления позиций для мельниц

            //Запись узлов и связей в массивы
            nodes = new Node[snaps.size()];
            i = 0;
            for (Point p : points) {
                nodes[i] = new Node(p);
                i++;
            }
            i = 0;
            for (LinkedList<Integer> n : snaps) {
                nodes[i].linkedNodes = new int[n.size()];
                int j = 0;
                for (Integer s : n) {
                    nodes[i].linkedNodes[j] = s;
                    j++;
                }
                i++;
            }

            //Считываем мельницы
            List<Mill> millList = new LinkedList<>();
            do {
                line = scan.nextLine();
                millList.add(new Mill(line));
            } while (scan.hasNextLine());
            mills = new Mill[millList.size()];
            i = 0;
            for (Mill m : millList) {
                mills[i] = m;
                i++;
            }
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    //endregion [IO]

    //region [FieldInfo]

    //Возвращает фишки команды
    private List<Integer> getThings(int team) {
        int i = 0;
        int toFind = things.getElements(team);
        List<Integer> result = new LinkedList<Integer>();
        while (result.size() < toFind) {
            if (nodes[i].team == team)
                result.add(i);
            i += 1;
        }
        return result;
    }

    //Разница в свободе хода между соперниками
    private int paths4teamAdvantage(int team, int opponent) {
        int result = 0;
        for (Node n : nodes) {//Для всех клеток команды
            if (n.team == 0) for (Integer p : n.linkedNodes) {//Суммируем возможные ходы
                int t = nodes[p].team;
                if (t == team) result++;
                else if (t == opponent) result--;
            }
        }
        return result;
    }

    //Перебор простых ходов (напрямую не вызывать!)
    private List<Turn> whereToPlaceNewAll() {
        List<Turn> result = new LinkedList<Turn>();
        for (int j = 0; j < nodes.length; j++) {
            if (nodes[j].team == 0) result.add(new Turn(j));
        }
        return result;
    }

    //Выставление на поле фишки с умом
    private List<Turn> whereToPlaceNew(int team) {
        int check;
        List<Integer> thingsToGet = new LinkedList<Integer>();
        int opponent = (team == 1) ? 2 : 1;
        List<Turn> result = new LinkedList<Turn>();

        //Можем ли построить мельницу?
        for (Mill m : mills) {
            check = checkNearBuildMill(team, m);
            if (check != -1) {
                thingsToGet = getThings(opponent);
                for (Integer t : thingsToGet) {
                    result.add(new Turn(-1, check, t));
                }
            }
        }

        //Можем ли помешать построить мельницу?
        if (result.size() == 0) for (Mill m : mills) {
            check = checkNearBuildMill(opponent, m);
            if (check != -1) result.add(new Turn(check));
        }

        //Выбираем, куда вообще можем сходить, если все плохо
        if (result.size() == 0) result = whereToPlaceNewAll();

        return result;
    }

    private List<Turn> whereToMoveAll(int team) {//Все возможные ходы (напрямую не вызывать!)
        List<Integer> thingsToGet = new LinkedList<Integer>();
        int check;
        List<Integer> startNodes = new LinkedList<Integer>();
        List<Turn> result = new LinkedList<Turn>();
        for (int i = 0; i < nodes.length; i++)
            if (nodes[i].team == 0) {
                startNodes = (things.getElements(team) == 3 ? getThings(team) : canOccupy(team, i));//Откуда можем ходить?
                for (Integer n : startNodes) result.add(new Turn(n, i, -1));
            }
        return result;
    }

    private List<Turn> whereToMove(int team) {//Все умные ходы для team
        List<Integer> thingsToGet = new LinkedList<Integer>();
        List<Integer> startNodes = new LinkedList<Integer>();
        int opponent = (team == 1) ? 2 : 1;
        int things = this.things.getElements(team);
        List<Turn> result = new LinkedList<Turn>();

        //Можем ли построить мельницу?
        for (Mill m : mills) {//Смотрим на все мельницы
            int check = checkNearBuildMill(team, m);
            if (check != -1) {//Если мельница почти достроена
                startNodes = (things == 3 ? getThings(team) : canOccupy(team, check));//Смотрим, откуда можем достроить мельницу
                for (Integer n : startNodes)//Для каждого хода постройки мельницы
                    if (!m.hasElement(n))//Не ломаем мельницу для ее же постройки!
                    {
                        thingsToGet = getThings(opponent);//Смотрим, какие фишки можем забрать
                        for (Integer t : thingsToGet)//Какую фишку возьмем?
                            result.add(new Turn(n, check, t));//Идем из старта строить мельницу, забираем фишку
                    }
            }
        }

        //Выбираем, куда вообще можем сходить, если все плохо
        if (result.size() == 0) result = whereToMoveAll(team);

        return result;
    }

    private List<Integer> canOccupy(int team, int idx) {//Проверяем, можем ли занять клетку следующим ходом, возвращаем стартовые индексы или пустой лист
        List<Integer> result = new LinkedList<Integer>();
        for (Integer n : nodes[idx].linkedNodes)
            if (nodes[n].team == team) result.add(n);
        return result;
    }

    private int checkNearBuildMill(int team, Mill observeMill) {//Проверяет, близка ли мельница team к завершению, и возвращает индекс пустой клетки или -1
        int busy = 0;
        int result = -1;
        for (Integer m : observeMill.elements) {
            if (nodes[m].team == team) busy++;
            else if (nodes[m].team == 0) result = m;
        }
        if (busy != 2) result = -1;
        return result;
    }

    private int getThingsAmount(int team) {//Возвращает количество фишек команды
        int result = 0;
        for (Node n : nodes)
            if (n.team == team) result++;
        return result;
    }

    private boolean wayExists(int team) {//Есть ли свобода передвижения?
        int i = 0;
        int j;
        if (currentTurnNumber < thingsAmount * 2) return true;
        while (i < nodes.length) {
            Node n = nodes[i];
            if (n.team == team) {
                j = 0;
                while (j < n.linkedNodes.length) {
                    int linkedNodeIdx = n.linkedNodes[j];
                    if (nodes[linkedNodeIdx].team == 0) return true;
                    j += 1;
                }
            }
            i += 1;
        }
        return false;
    }

    public Boolean checkEnd() {
        return (thingsAmount * 2 < currentTurnNumber) //Если наступил второй этап игры
                && ((things.getElements(1) == 2) //Если у первой команды осталось две фишки
                || (things.getElements(2) == 2) //Если у второй команды осталось две фишки
                || !wayExists(1) && (things.getElements(1) > 3)//Если у первой команды нет ходов
                || !wayExists(2) && (things.getElements(2) > 3)); //Если у второй команды нет ходов
    }

    public String checkWin() {
        if (checkEnd()) {
            if ((things.getElements(1) == 2) || !wayExists(1)) return "2nd player wins";
            else return "1st player wins";
        }
        return "";
    }


    //endregion [FieldInfo]

    //region [MinMax]

    //Предсказывает развитие игры на текущем поле и выдает оценку
    private double predictRate(int turnsDepth) {
        List<Turn> turns = new LinkedList<Turn>();
        int currTeam = currentTurnNumber % 2 + 1;
        double quality;//Качество текущего хода
        double rate;
        double result;
        boolean getLower = (turnsDepth % 2) == 1;

        if (currentTurnNumber < thingsAmount * 2)
            turns = whereToPlaceNew(currTeam);
        else turns = whereToMove(currTeam);

        result = (getLower ? Double.MAX_VALUE : -Double.MAX_VALUE);//Рейтинг текущего хода. Минимакс

        if (turnsDepth == 0) //Завершение рекурсии
            for (Turn t : turns) {//Для всех возможных ходов оцениваем последствия
                makeTurn(t, currTeam);
                rate = getRate(currTeam);
                if (rate>result) result = rate;//И возвращаем лучший положительный результат
                reverseTurn(t);
            }
        else {
            for (Turn t : turns) {//Для каждого возможного хода
                makeTurn(t, currTeam);
                quality = predictRate(turnsDepth - 1);//Предсказываем его последствия
                if (getLower) result = Math.min(result, quality);//И возвращаем лучший/худший результат
                else result = Math.max(result, quality);
                reverseTurn(t);
            }
        }
        return result;
    }

    //Оценка позиции
    private double getRate(int team) {
        int difference;
        int opponent = team % 2 + 1;
        int ways = paths4teamAdvantage(team, opponent);
        difference = things.getDifference(team, opponent);
        if (!wayExists(team)) return Double.MIN_VALUE;
        return (difference + (ways / 10.0)) * 10.0 / (currentTurnNumber + 10.0);//Примерная оценка свободы хода. Для ранних ходов выше
    }

    //endregion [MinMax]
}

