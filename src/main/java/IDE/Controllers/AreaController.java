package IDE.Controllers;

import IDE.TextHighlight;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.InlineCssTextArea;
import org.fxmisc.richtext.LineNumberFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class AreaController {

    private static final ObservableList<String> FONTS = FXCollections.observableArrayList(Font.getFamilies());
    private static final ObservableList<Integer> SIZES = FXCollections.observableArrayList
            (IntStream.rangeClosed(0, 100).boxed().collect(Collectors.toList()));

    //ZOOM CTRL CHECK
    private boolean ctrlPressed = false;
    private Properties config;

    AreaController(GridPane mainGrid,
                   InlineCssTextArea codeArea, TextArea outArea,
                   ComboBox<Integer> caSize, ComboBox<String> caFont,
                   ComboBox<Integer> laSize, ComboBox<String> laFont) {

        caFont.setItems(FONTS); laFont.setItems(FONTS);
        caSize.setItems(SIZES); laSize.setItems(SIZES);
        areaInitialize(codeArea, outArea, caSize, caFont, laSize, laFont);

        //Scroll панель для прокрутки кода
        VirtualizedScrollPane codePane = new VirtualizedScrollPane<>(codeArea);
        codePane.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        codePane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        mainGrid.add(codePane, 0, 1, 3, 1);
    }

    //Метод изменения шрифта
    private void selectFontSize(String name, Node area, ComboBox<String> font, ComboBox<Integer> size) {
        area.setStyle("-fx-font-family: " + font.getValue()
                    + "; -fx-font-size: " + size.getValue());
        try {
            config.setProperty(name + "Font", font.getValue());
            config.setProperty(name + "Size", size.getValue().toString());
            config.store(new FileOutputStream("config.properties"), null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Методы масштабирования кода
    private void zoomText(KeyEvent event) { //Обработка клавиши Control
        if (event.getCode() == KeyCode.CONTROL)
            if (event.getEventType() == KeyEvent.KEY_PRESSED) ctrlPressed = true;
            else if (event.getEventType() == KeyEvent.KEY_RELEASED) ctrlPressed = false;
    }
    private void zoomText(ScrollEvent event, ComboBox<Integer> size) { //Обработка колеса мыши
        if (ctrlPressed) {
            if (event.getDeltaY() > 0) size.setValue(size.getValue() + 1);
            else size.setValue(size.getValue() - 1);
            event.consume();
        }
    }

    private void areaInitialize(InlineCssTextArea codeArea, TextArea logsArea,
                                ComboBox<Integer> caSize, ComboBox<String> caFont,
                                ComboBox<Integer> laSize, ComboBox<String> laFont) {
        config = new Properties();
        try {
            config.load(new FileReader(new File("config.properties")));
            caFont.setValue(config.getProperty("caFont"));
            laFont.setValue(config.getProperty("laFont"));
            caSize.setValue(Integer.valueOf(config.getProperty("caSize")));
            laSize.setValue(Integer.valueOf(config.getProperty("laSize")));
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Подсветка синтаксиса
        codeArea.textProperty().addListener((obs, oldText, newText) -> {
            try {
                TextHighlight.computeHighlighting(codeArea, newText);}
            catch (Exception e) {e.printStackTrace();}
        });

        //Нумерация строк
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));

        //Добавление метода масштабирования текста
        codeArea.addEventFilter(KeyEvent.ANY, event -> zoomText(event));
        logsArea.addEventFilter(KeyEvent.ANY, event -> zoomText(event));
        codeArea.addEventFilter(ScrollEvent.ANY, event -> zoomText(event, caSize));
        logsArea.addEventFilter(ScrollEvent.ANY, event -> zoomText(event, laSize));

        //Добавление метод изменения шрифта
        EventHandler<ActionEvent> caSelectFontHandler = event -> selectFontSize("ca", codeArea, caFont, caSize);
        EventHandler<ActionEvent> laSelectFontHandler = event -> selectFontSize("la", logsArea, laFont, laSize);
        caFont.addEventHandler(ActionEvent.ANY, caSelectFontHandler);
        caSize.addEventHandler(ActionEvent.ANY, caSelectFontHandler);
        laFont.addEventHandler(ActionEvent.ANY, laSelectFontHandler);
        laSize.addEventHandler(ActionEvent.ANY, laSelectFontHandler);
        selectFontSize("ca", codeArea, caFont, caSize);
        selectFontSize("la", logsArea, laFont, laSize);
    }
}
