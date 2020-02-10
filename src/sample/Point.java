package sample;

import com.sun.javafx.scene.control.skin.LabeledText;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

public class Point {
    public int x;
    public int y;
    private Circle circle;
    private final int thingSize = 10;
    private Color emptyColor = Color.LIGHTGRAY;
    private Text index = new Text("empty");

    //region [Constructors]

    public Point() {
        x = 0;
        y = 0;
        circle = new Circle(x, y, thingSize, emptyColor);
    }


    public Point(int newX, int newY) {
        x = newX;
        y = newY;
        circle = new Circle(x, y, thingSize, emptyColor);
    }

    public Point(Point other) {
        x = other.x;
        y = other.y;
        circle = new Circle(x, y, thingSize, emptyColor);
    }

    //endregion [Constructors]

    //Переопределение вывода координат
    @Override
    public String toString() {
        return ("(" + x + ", " + y + ')');
    }

    //region [Drawing]

    //Отмечает точку цветом команды
    public Circle markOccupied(Point offset, Point toResize, double scale, int c) {
        Point newPoint = getNewCoord(offset, toResize, scale);
        Color color;
        if (c == 1) color = Color.BLUE;
        else if (c == 2) color = Color.RED;
        else color = emptyColor;
        //System.out.println(newPoint);
        circle = new Circle(newPoint.x, newPoint.y, thingSize, color);//Подписываем
        return circle;
    }

    //Подписывает точку
    public Text subscribeIdx(Integer idx, Point offset, Point toResize, double scale) {
        Point newPoint = getNewCoord(offset, toResize, scale);
        return new Text(newPoint.x + thingSize, newPoint.y, idx.toString());
    }

    //Проводит линию между двумя точками
    private Line line(Point other) {
        return new Line(this.x, this.y, other.x, other.y);
    }

    //Проводит линию между точками с учетом ремасштабирования
    public Line line(Point other, Point offset, Point toResize, double scale) {
        Point place1 = getNewCoord(offset, toResize, scale);
        Point place2 = other.getNewCoord(offset, toResize, scale);
        return place1.line(place2);
    }

    //endregion [Drawing]

    //Пересчет координат в новом масштабе
    private Point getNewCoord(Point offset, Point toResize, double scale) {
        int newX = (int) ((x - toResize.x) * scale) + offset.x;
        int newY = (int) ((y - toResize.y) * scale) + offset.y;
        return new Point(newX, newY);
    }

}