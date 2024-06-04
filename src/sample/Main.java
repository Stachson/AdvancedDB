package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.util.Optional;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("SQL Query Executor");
        primaryStage.setScene(new Scene(root, 700, 550));

        primaryStage.setOnCloseRequest(event -> {
            if (!confirmExit()) {
                event.consume();
            }
        });

        primaryStage.show();
    }

    private boolean confirmExit() {
        // Tworzymy nowy alert z opcjami "Tak" i "Nie"
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Potwierdzenie zamknięcia");
        alert.setHeaderText(null);
        alert.setContentText("Na pewno chcesz zamknąć? Niezapisane dane zostaną utracone.");

        // Dodanie przycisków do alertu
        Optional<ButtonType> result = alert.showAndWait(); // Oczekuje na odpowiedź użytkownika

        // Jeśli użytkownik kliknie "Tak", zwracamy true (zamknięcie), w przeciwnym razie false (kontynuacja)
        return result.isPresent() && result.get() == ButtonType.OK;
    }


    public static void main(String[] args) {
        launch(args);
    }
}
