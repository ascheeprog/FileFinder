package programmUtils;

import javafx.scene.control.TreeItem;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Objects;

public class SimpleFileTreeItem {

    private String searchString;
    private String pathFolder;
    private String suffix;

    public SimpleFileTreeItem(String searchString, String pathFolder, String suffix) {
        this.searchString = searchString;
        this.pathFolder = pathFolder;
        this.suffix = suffix;
    }

    private TreeItem<File> getNodesForDirectory(File directory, String suffix) {
        TreeItem<File> root = new TreeItem<>(directory);
        for (File file : Objects.requireNonNull(directory.listFiles())) {
            String fileName = file.getName();
            if (fileName.endsWith("." + suffix) && readAndSearchInFile(file.getAbsolutePath())) {
                root.getChildren().add(new TreeItem<>(file));
            } else if (file.isDirectory()) {
                TreeItem<File> node = getNodesForDirectory(file, suffix);
                if (!node.getChildren().isEmpty()) {
                    root.getChildren().add(node);
                }
            }
        }
        return root;

    }

    private boolean readAndSearchInFile(String absolutePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(absolutePath))) {
            String line;
            while ((line = reader.readLine()) != null) { // используем буферизированное чтение файла, для ускорения работы
                if (line.contains(this.searchString)) { //Если нужное выражение присутствует в файле, тогда сохраняем путь до файла P.S. ищется только первое совпадение, чтобы не перебирать остальную часть файла
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private File getFile() {
        return new File(this.pathFolder);
    }

    public TreeItem<File> getTree() {
        return getNodesForDirectory(getFile(), this.suffix);
    }

}