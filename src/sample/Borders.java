package sample;

public class Borders {

    public Point maxPoint;
    public Point minPoint;
    public Point centerAlignOffset;
    public double scale;
    public int margin = 120;

    private static Point window = new Point(800, 800);

    public Borders() {
        scale = 1;
    }

    //Автоматическое ремасштабирование
    public void setScale() {
        int w = maxPoint.x - minPoint.x;
        int h = maxPoint.y - minPoint.y;
        scale = Math.min((window.x - 2 * margin) / w, (window.y - 2 * margin) / h);
        setOffset();
    }

    //Ручное ремасштабирование
    public void setScale(double newScale) {
        scale = newScale;
        setOffset();
    }

    //Подстройка границ окна под новую точку
    public void checkPoint(Point place) {
        if (maxPoint == null) {//Если еще нет точек привязки, создаем новую
            maxPoint = new Point(place);
            minPoint = new Point(place);
        } else {
            if (place.x > maxPoint.x) maxPoint.x = place.x;
            if (place.y > maxPoint.y) maxPoint.y = place.y;
            if (place.x < minPoint.x) minPoint.x = place.x;
            if (place.y < minPoint.y) minPoint.y = place.y;
        }
    }

    //Переопределение точки привязки
    private void setOffset() {
        int w = maxPoint.x - minPoint.x;
        int h = maxPoint.y - minPoint.y;
        int newX = (int) ((window.x - w * scale) / 2);
        int newY = (int) ((window.y - h * scale) / 2);
        centerAlignOffset = new Point(newX, newY);
    }
}

