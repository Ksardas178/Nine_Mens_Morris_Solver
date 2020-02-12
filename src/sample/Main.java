package sample;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.util.HashMap;

public class Main extends Application {
    private HashMap<KeyCode, Boolean> keys = new HashMap<>();
    static Pane root = new Pane();
    final String fieldName = "src/resources/field_1";
    int team = 1;
    Turn t;
    static int turnsDepth = 0;
    TextField textField = new TextField();
    Button readTurnButton = new Button();
    Button newGameButton = new Button();

    public boolean isPressed(KeyCode key) {
        return keys.getOrDefault(key, false);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Field field = new Field(fieldName);

        textField.setMinWidth(200);
        root.getChildren().addAll(textField);

        readTurnButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                try {
                    textField.getText();
                    t = new Turn(textField.getText());
                    textField.clear();
                    field.makeTurnWithCheck(t, team);
                    {
                        root.getChildren().removeAll(field.shapes);
                        field.show();
                        root.getChildren().addAll(field.shapes);
                    }
                    //root.getChildren().addAll(field.indexes);
                    team = 2;//Меняем команду
                } catch (Exception e) {
                    textField.setText("Wrong input!");
                }
            }
        });
        readTurnButton.setTranslateX(200);
        readTurnButton.setMinWidth(80);
        readTurnButton.setText("Ready");
        root.getChildren().addAll(readTurnButton);

        newGameButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                textField.clear();
                {
                    root.getChildren().removeAll(field.shapes);
                    field.readField(fieldName);
                    field.show();
                    root.getChildren().addAll(field.shapes);
                    team = 1;
                }
            }
        });
        newGameButton.setTranslateX(800 - 120);
        newGameButton.setMinWidth(120);
        newGameButton.setText("Restart");
        root.getChildren().addAll(newGameButton);

        {
            field.show();
            root.getChildren().addAll(field.shapes);
            root.getChildren().addAll(field.indexes);
        }
        root.setPrefSize(800, 800);

        Scene scene = new Scene(root);
        scene.setOnKeyPressed(event -> keys.put(event.getCode(), true));
        scene.setOnKeyReleased(event -> {
            keys.put(event.getCode(), false);
        });
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (!field.checkEnd()) {
                    if (team == 2) {
                        if (field.currentTurnNumber < 3) t = field.findTurn(1);
                        else t = field.findTurn(turnsDepth);
                        field.makeTurn(t, team);
                        {
                            root.getChildren().removeAll(field.shapes);
                            field.show();
                            root.getChildren().addAll(field.shapes);
                        }
                        //root.getChildren().addAll(field.indexes);
                        team = 1;//Меняем команду
                    } /*else {
                        if (field.currentTurnNumber < 3) t = field.findTurn(3);
                        else t = field.findTurn(turnsDepth);
                        field.makeTurn(t, team);
                        {
                            root.getChildren().removeAll(field.shapes);
                            field.show();
                            root.getChildren().addAll(field.shapes);
                        }
                        //root.getChildren().addAll(field.indexes);
                        team = 2;//Меняем команду
                    }
                    System.out.println(t);*/
                } else textField.setText("Game ended. " + field.checkWin());
            }
        };
        primaryStage.setTitle("Nine Men's Morris");
        primaryStage.setScene(scene);
        timer.start();
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

