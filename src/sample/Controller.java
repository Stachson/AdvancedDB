package sample;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;

public class Controller {

    private String url = "jdbc:postgresql://mydbinstance.czue2casejbn.us-east-1.rds.amazonaws.com:5432/ZaawansowaneBazyDanych";
    private String user = "postgres";
    private String password = "pwritehaslo";

    @FXML
    private TextArea queryInput;

    @FXML
    private TextField queryCount;

    @FXML
    private TextArea queryResults;

    @FXML
    private Label queryTime;

    @FXML
    private Label averageQueryTime;

    @FXML
    private TextArea queryHistory;

    @FXML
    private TextArea indexInfo;

    @FXML
    public void exportHistoryToCSV() {
        if (queryHistory.getText().isEmpty()) {
            queryResults.setText("Brak historii do eksportu.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Zapisz historię do pliku CSV");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Pliki CSV", "*.csv")
        );

        File file = fileChooser.showSaveDialog(null);

        if (file != null) {
            try (FileWriter fileWriter = new FileWriter(file)) {
                // Nagłówki kolumn
                fileWriter.write("Zapytanie,Ilość powtórzeń,Całkowity czas (ms),Średni czas (ms)\n");

                String[] historyEntries = queryHistory.getText().split("------------------------------------------------\n\n");
                for (String entry : historyEntries) {
                    String[] lines = entry.split("\n");
                    String query = "", count = "", totalTime = "", avgTime = "";
                    for (String line : lines) {
                        if (line.startsWith("Zapytanie: ")) {
                            query = line.replace("Zapytanie: ", "").replace(",", "");
                        } else if (line.startsWith("Ilość powtórzeń: ")) {
                            count = line.replace("Ilość powtórzeń: ", "");
                        } else if (line.startsWith("Całkowity czas: ")) {
                            String[] times = line.replace("Całkowity czas: ", "").split(", Średni czas: ");
                            totalTime = times[0].replace(" ms", "");
                            avgTime = times[1].replace(" ms", "");
                        }
                    }
                    fileWriter.write(query + "," + count + "," + totalTime + "," + avgTime + "\n");
                }
                queryResults.setText("Historia została zapisana do pliku CSV.");
            } catch (IOException e) {
                queryResults.setText("Błąd podczas zapisu do pliku: " + e.getMessage());
            }
        } else {
            queryResults.setText("Nie wybrano pliku do zapisu.");
        }
    }

    @FXML
    public void saveHistoryToFile() {
        // Sprawdź, czy historia nie jest pusta
        if (queryHistory.getText().isEmpty()) {
            queryResults.setText("Historia jest pusta. Nie można zapisać.");
            return;
        }

        // Wybór pliku do zapisu
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Wybierz plik do zapisania historii");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Pliki tekstowe", "*.txt")
        );

        File file = fileChooser.showSaveDialog(null); // Otwórz okno dialogowe zapisu

        if (file != null) { // Jeśli użytkownik wybrał plik
            try {
                FileWriter fileWriter = new FileWriter(file);
                fileWriter.write(queryHistory.getText()); // Zapis historii do pliku
                fileWriter.flush(); // Upewnij się, że wszystko jest zapisane
                queryResults.setText("Historia została zapisana do pliku.");
            } catch (IOException e) {
                queryResults.setText("Błąd podczas zapisu do pliku: " + e.getMessage());
            }
        } else {
            queryResults.setText("Nie wybrano pliku do zapisu.");
        }
    }

    @FXML
    public void executeQuery() throws SQLException {
        String query = queryInput.getText();
        Integer count = 1;
        StringBuilder results = new StringBuilder();
        long executionTime;
        long startTime;
        long endTime;




        if (query == null || query.isEmpty()) {
            queryResults.setText("Wpisz zapytanie SQL, zanim naciśniesz 'Wykonaj zapytanie'.");
            return;
        }

        try{
            if(queryCount.getText().isEmpty())
                count = 1;
            else{
                count = Integer.parseInt(queryCount.getText());
            }
            Connection connection = DriverManager.getConnection(url, user, password);


            if (query.trim().toUpperCase().startsWith("CREATE INDEX")) {
                try (Statement statement = connection.createStatement()) {
                    statement.executeUpdate(query);
                    String indexName = extractIndexName(query);
                    String tableName = extractTableName(query);
                    String indexMessage = "Indeks: " + indexName + " został nałożony na tabelę: " + tableName;
                    queryHistory.appendText(indexMessage + ": " + query + "\n\n");
                    indexInfo.appendText(indexMessage + "\n");
                    //showAlert("Informacja", indexMessage);
                } catch (SQLException e) {
                    indexInfo.appendText("Błąd podczas tworzenia indeksu: " + e.getMessage());
                    //showAlert("Błąd", "Błąd podczas tworzenia indeksu: " + e.getMessage());
                }
                return;
            }

            if (query.trim().toUpperCase().startsWith("DROP INDEX")) {
                try (Statement statement = connection.createStatement()) {
                    statement.executeUpdate(query);
                    String indexName = extractIndexName(query);
                    String indexMessage = "Indeks: " + indexName + " został usunięty";
                    queryHistory.appendText(indexMessage + "\n\n");
                    indexInfo.appendText(indexMessage + "\n");
                    //showAlert("Informacja", indexMessage);
                } catch (SQLException e) {
                    indexInfo.appendText("Błąd podczas usuwania indeksu: " + e.getMessage());
                    //showAlert("Błąd", "Błąd podczas tworzenia indeksu: " + e.getMessage());
                }
                return;
            }


            startTime = System.currentTimeMillis();

            for(int i = 0;i < count;i++) {

                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(query);

                results = new StringBuilder();
                ResultSetMetaData metaData = resultSet.getMetaData();
                int columnCount = metaData.getColumnCount();

                for (int j = 1; j <= columnCount; j++) {
                    results.append(metaData.getColumnName(j)).append("\t");
                }
                results.append("\n");

                while (resultSet.next()) {
                    for (int j = 1; j <= columnCount; j++) {
                        results.append(resultSet.getString(j)).append("\t");
                    }
                    results.append("\n");
                }
            }
            endTime = System.currentTimeMillis();
            executionTime = endTime - startTime;

            queryResults.setText(results.toString());
            queryTime.setText("Całkowity czas wykonywania: " + executionTime + " ms");
            averageQueryTime.setText("Średni czas wykonania: " + executionTime/count + " ms");

            queryHistory.appendText("Zapytanie: " + query + "\n");
            queryHistory.appendText("Ilość powtórzeń: " + count + "\n");
            queryHistory.appendText("Całkowity czas: " + executionTime + " ms, Średni czas: " + (executionTime / count) + " ms\n");
            queryHistory.appendText("------------------------------------------------\n\n");

            if (query.toUpperCase().contains("CREATE INDEX") || query.toUpperCase().contains("ALTER TABLE") && query.toUpperCase().contains("ADD INDEX")) {
                String indexName = extractIndexName(query);
                String tableName = extractTableName(query);
                String indexMessage = "Indeks: " + indexName + " został nałożony na tabelę: " + tableName;
                indexInfo.appendText(indexMessage + "\n");
                //showAlert("Informacja", indexMessage);
            }

        } catch(SQLException e){
            queryResults.setText("Nieprawidłowe zapytanie SQL");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String extractIndexName(String query) {
        String upperQuery = query.toUpperCase();
        int indexStart = upperQuery.indexOf("INDEX") + 6; // Start position after "INDEX "
        int indexEnd = upperQuery.indexOf(" ", indexStart);
        if (indexEnd == -1) {
            indexEnd = query.length();
        }
        return query.substring(indexStart, indexEnd).trim();
    }

    private String extractTableName(String query) {
        String upperQuery = query.toUpperCase();
        int tableStart = upperQuery.indexOf("ON") + 3; // Start position after "ON "
        int tableEnd = upperQuery.indexOf("(", tableStart);
        return query.substring(tableStart, tableEnd).trim();
    }
}

