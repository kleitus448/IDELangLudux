package IDE.Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import org.fxmisc.richtext.InlineCssTextArea;

public class MainController {
    //-----------------------------AREAS VARIABLES AND METHODS------------------------------

    @FXML private ComboBox<String> caFont;
    @FXML private ComboBox<Integer> caSize;
    private InlineCssTextArea codeArea = new InlineCssTextArea();

    //LOG AREA VARIABLES
    @FXML private ComboBox<String> laFont;
    @FXML private ComboBox<Integer> laSize;
    @FXML private TextArea outArea;

    //MENU BAR VARIABLES
    @FXML private Button buttonOpenFile = new Button();
    @FXML private Button buttonSaveFile = new Button();
    @FXML private Button buttonSaveFileAs = new Button();
    @FXML private Button checkButton = new Button();
    @FXML private Button logsButton = new Button();

    //OTHER VARIABLES
    @FXML private GridPane mainGrid;

    @FXML public void initialize() {

        //Инициализация контроллера полей ввода
        new AreaController(mainGrid, codeArea, outArea,
                           caSize, caFont, laSize, laFont);

        //Инициализация контроллера меню и кнопок
        new MenuController(logsButton, checkButton, buttonOpenFile,
                           buttonSaveFile, buttonSaveFileAs,
                           codeArea, outArea);
    }
}
