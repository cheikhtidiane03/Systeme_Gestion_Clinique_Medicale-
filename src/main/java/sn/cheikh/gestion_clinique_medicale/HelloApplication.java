package sn.cheikh.gestion_clinique_medicale;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import sn.cheikh.gestion_clinique_medicale.Utilitaire.Navigation;

import java.io.IOException;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        // Enregistrer le stage principal dans Navigation
        Navigation.setPrimaryStage(stage);

        // Charger la vue de login
        FXMLLoader fxmlLoader = new FXMLLoader(
                HelloApplication.class.getResource(
                        "/sn/cheikh/gestion_clinique_medicale/auth/login-view.fxml"
                )
        );

        Scene scene = new Scene(fxmlLoader.load(), 900, 600);
        stage.setTitle("Clinique Médicale — Gestion");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.centerOnScreen();
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}