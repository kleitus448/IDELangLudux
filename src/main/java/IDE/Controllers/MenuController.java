package IDE.Controllers;

import IDE.Main;
import LuduxLang.Start;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.fxmisc.richtext.InlineCssTextArea;
import java.io.*;

class MenuController {

    private static final String SAVED = " (Сохранён)";
    private FileChooser fileChooser;
    private File currentFile;

    MenuController(Button logsButton, Button checkButton, Button buttonOpenFile,
                   Button buttonSaveFile, Button buttonSaveFileAs,
                   InlineCssTextArea codeArea, TextArea outArea) {
        this.fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("LuDux", "*.ldx"),
                                                 new FileChooser.ExtensionFilter("All Files", "*.*"));

        fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        codeArea.textProperty().addListener((obs, oldText, newText) -> {
                if (currentFile != null) Main.primaryStage.setTitle(Main.name + " - " + currentFile.getAbsolutePath());
        });
        buttonOpenFile.setOnAction(event -> onMenuOpenFile(codeArea));
        buttonSaveFile.setOnAction(event -> onMenuSaveFile(codeArea));
        buttonSaveFileAs.setOnAction(event -> onMenuSaveFileAs(codeArea));
        checkButton.setOnAction(event -> onButtonCheckClick(codeArea, outArea));
        logsButton.setOnAction(event -> onButtonLogsClick());
    }

    private void onButtonLogsClick() {
        System.out.println("logs");
        TextArea logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setText(Main.logs.toString());
        Group logGroup = new Group(logArea);
        Stage logStage = new Stage();
        logStage.setTitle("Логи");
        logStage.setScene(new Scene(logGroup));
        logStage.show();
    }

    private void onMenuOpenFile(InlineCssTextArea codeArea) {
        StringBuilder codeBuilder = new StringBuilder();
        try {
            currentFile = fileChooser.showOpenDialog(Main.primaryStage);
            if (currentFile != null) {
                BufferedReader in = new BufferedReader(new FileReader(currentFile));
                String s;
                while ((s = in.readLine()) != null) codeBuilder.append(s).append("\n");
                in.close();
                codeArea.replaceText(0, 0, codeBuilder.toString());
                Main.primaryStage.setTitle(Main.name + " - " + currentFile.getAbsolutePath());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void onMenuSaveFile (InlineCssTextArea codeArea) {
        if (currentFile != null) {
            try {
                PrintWriter out = new PrintWriter(currentFile.getAbsolutePath());
                out.print(codeArea.getText());
                out.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            Main.primaryStage.setTitle(Main.name + " - " + currentFile.getAbsolutePath() + SAVED);
        }
        else onMenuSaveFileAs(codeArea);
    }

    private void onMenuSaveFileAs (InlineCssTextArea codeArea) {
        currentFile = fileChooser.showSaveDialog(Main.primaryStage);
        if (currentFile != null) {
            Main.primaryStage.setTitle(Main.name + " - " + currentFile.getAbsolutePath());
            onMenuSaveFile(codeArea);
        }
    }

    //Нажатие кнопки "Проверить программу"
    private void onButtonCheckClick(InlineCssTextArea codeArea, TextArea outArea) {
        try {
            outArea.setText(Start.start(codeArea.getText()) ? Main.output.toString()
                    : "При проверке возникли ошибки:\n" + Main.output.toString());
            Main.output.setLength(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
