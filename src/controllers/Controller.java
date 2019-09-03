package controllers;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import javafx.util.Callback;
import org.fxmisc.richtext.StyleClassedTextArea;
import programmUtils.Context;
import programmUtils.ProgressBytes;
import programmUtils.SimpleFileTreeItem;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;

public class Controller {
    @FXML
    private TabPane tabPane;

    @FXML
    private Button searchButton;

    @FXML
    private TextField pathSearchField;

    @FXML
    private TextField suffixSearchField;

    @FXML
    private TextField textSearchField;

    @FXML
    private TreeView<File> pathToFileTreeView;

    @FXML
    private StyleClassedTextArea outputTextArea;

    @FXML
    private Button nextElemButton;

    @FXML
    private Button previousElemButton;

    @FXML
    private Button highlightTextButton;

    @FXML
    private Button openNewTabButton;

    @FXML
    private Button fileChooserButton;

    @FXML
    private ProgressIndicator indicator;

    TextField getTextSearchField() {
        return textSearchField;
    }

    TreeView<File> getPathToFileTreeView() {
        return pathToFileTreeView;
    }

    @FXML
    void initialize() {
        Context.getInstance().setController(this);
        propertyOutputTextArea();
        AtomicInteger atomicInteger = new AtomicInteger();
        searchByPath();
        readFileByCurrentPath();
        indicateDirection();

        openNewTabButton.setOnAction(event -> getNewTab());

        highlightTextButton.setOnAction(event ->
                highlightText(outputTextArea, textSearchField.getText())
        );

        nextElemButton.setOnAction(event -> moveToElem(outputTextArea,
                textSearchField.getText(),
                atomicInteger,
                true));

        previousElemButton.setOnAction(event -> moveToElem(outputTextArea,
                textSearchField.getText(),
                atomicInteger,
                false));
    }

    private void indicateDirection() {

        fileChooserButton.setOnAction(event -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            if (!pathSearchField.getText().isEmpty()) {
                directoryChooser.setInitialDirectory(new File(pathSearchField.getText()));
            }
            File directory = directoryChooser.showDialog(null);
            if (directory != null) {
                pathSearchField.setText(directory.getAbsolutePath());
            }
        });
    }

    private void searchByPath() {
        searchButton.setOnAction(event -> {
            pathToFileTreeView.setRoot(
                    new SimpleFileTreeItem(
                            textSearchField.getText(),
                            pathSearchField.getText().trim(),
                            suffixSearchField.getText().trim()).getTree()
            );
            pathToFileTreeView.setCellFactory(new Callback<TreeView<File>, TreeCell<File>>() {
                public TreeCell<File> call(TreeView<File> tv) {
                    return new TreeCell<File>() {
                        @Override
                        protected void updateItem(File item, boolean empty) {
                            super.updateItem(item, empty);
                            setText((empty || item == null) ? "" : item.getName());
                        }
                    };
                }
            });
        });
    }

    void moveToElem(StyleClassedTextArea area, String searchText, AtomicInteger count, boolean flag) {
        int index = !flag ? area.getText().lastIndexOf(searchText, count.decrementAndGet()) : area.getText().indexOf(searchText, count.incrementAndGet());
        if (index != -1) {
            area.moveTo(index);
            area.requestFollowCaret();
            count.set(index);
        } else {
            if (!flag) {
                count.set(count.get() + 1);
            } else {
                count.set(count.get() - 1);
            }
        }
    }

    private void readFileByCurrentPath() {
        pathToFileTreeView
                .getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
                    if (pathToFileTreeView.isFocused()) {
                        try {
                            if (!newValue.getValue().isDirectory()) {
                                outputTextArea.clear();
                                Task<Void> task = readTextFile(newValue.getValue().getAbsolutePath(), outputTextArea);
                                indicator.progressProperty().bind(task.progressProperty());
                                new Thread(task).start();
                            }

                        } catch (NullPointerException | IndexOutOfBoundsException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    private void propertyOutputTextArea() {
        outputTextArea.setWrapText(true);
        outputTextArea.setMaxWidth(100);
        outputTextArea.setEditable(false);
        outputTextArea.requestFollowCaret();
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(outputTextArea);
    }

    void highlightText(StyleClassedTextArea area, String searchText) {
        area.clearStyle(0, area.getText().length());
        int index = 0;
        while (index != -1) {
            index = area.getText().indexOf(searchText, index);
            if (index != -1) {
                int finalIndex = index;
                Platform.runLater(() -> area.setStyleClass(finalIndex, finalIndex + searchText.length(), "text-highlight"));
                index++;
            }
        }
    }

    private void getNewTab() {
        Tab newTab = new Tab();
        try {
            newTab.setText(pathToFileTreeView
                    .getSelectionModel()
                    .selectedItemProperty()
                    .getValue()
                    .getValue()
                    .getName());
            newTab.setClosable(true);
            newTab.setId("tab");
            AnchorPane anchorPane = FXMLLoader.load(getClass().getResource("newTab.fxml"));
            newTab.setContent(anchorPane);

        } catch (IOException e) {
            e.getMessage();
        }
        if (!tabPane.getTabs().contains(newTab)) {
            tabPane.getTabs().add(newTab);
        }
        tabPane.getSelectionModel().select(newTab);
    }

    Task<Void> readTextFile(String absolutePathFile, StyleClassedTextArea area) {
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try (ProgressBytes progressBytes = new ProgressBytes(Files.newInputStream(Paths.get(absolutePathFile)));
                     BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(progressBytes))) {
                    long totalBytes = Files.size(Paths.get(absolutePathFile));
                    bufferedReader.lines()
                            .peek(line ->
                                    updateProgress(progressBytes.getReadBytes(), totalBytes))
                            .forEach(line ->
                                    Platform.runLater(() ->
                                            area.appendText("\n" + line)));
                }
                return null;
            }
        };
    }
}
