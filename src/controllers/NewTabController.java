package controllers;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import org.fxmisc.richtext.StyleClassedTextArea;
import programmUtils.Context;

import java.util.concurrent.atomic.AtomicInteger;

public class NewTabController {
    @FXML
    private Button btnNextItem;

    @FXML
    private Button btnPreviousItem;

    @FXML
    private Button btnSelectText;

    @FXML
    private StyleClassedTextArea outputText;

    @FXML
    void initialize() {
        AtomicInteger atomicInteger = new AtomicInteger();

        outputText.setWrapText(true);
        outputText.setEditable(false);

        Controller controller = Context.getInstance().getController();
        String searchText = controller.getTextSearchField().getText();
        String absPath = controller
                .getPathToFileTreeView()
                .getSelectionModel()
                .selectedItemProperty()
                .getValue()
                .getValue()
                .getAbsolutePath();

        Task<Void> task = controller.readTextFile(absPath, outputText);
        new Thread(task).start();
        btnSelectText.setOnAction(event ->
                controller.highlightText(outputText, searchText)
        );
        btnNextItem.setOnAction(event -> controller.moveToElem(outputText, searchText, atomicInteger, true));
        btnPreviousItem.setOnAction(event -> controller.moveToElem(outputText, searchText, atomicInteger, false));
    }

}

