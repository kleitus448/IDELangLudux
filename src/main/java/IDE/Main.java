package IDE;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import LuduxLang.Lexer;

import java.util.Objects;

public class Main extends Application {

    public static final String name = "LuDux IDE 1.1";
    public static Lexer lexer;
    public static StringBuilder logs;
    public static StringBuilder output;
    public static Stage primaryStage;

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getClassLoader().getResource("sample.fxml")));
        Main.primaryStage = primaryStage;
        Main.primaryStage.setTitle(name);
        Main.primaryStage.setResizable(false);
        Main.primaryStage.setScene(new Scene(root));
        Main.primaryStage.show();
    }

    public static void main(String[] args) {
        Main.logs = new StringBuilder();
        Main.output = new StringBuilder();
        lexer = new Lexer();
        lexer.printLexemes();
        launch(args);
    }
}
