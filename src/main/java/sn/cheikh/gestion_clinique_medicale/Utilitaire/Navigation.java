package sn.cheikh.gestion_clinique_medicale.Utilitaire;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class Navigation {

    private static Stage primaryStage;

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    public static void naviguerVers(String fxmlPath) {
        try {
            String fullPath = "/sn/cheikh/gestion_clinique_medicale/" + fxmlPath;
            URL url = Navigation.class.getResource(fullPath);

            // DEBUG — affiche le chemin testé
            System.out.println(">>> Chemin testé : " + fullPath);
            System.out.println(">>> URL trouvée  : " + url);

            if (url == null) {
                System.err.println(">>> FICHIER INTROUVABLE : " + fullPath);
                return;
            }

            FXMLLoader loader = new FXMLLoader(url);
            Scene scene = new Scene(loader.load());
            primaryStage.setScene(scene);
            primaryStage.centerOnScreen();
        } catch (Exception e) {
            System.err.println(">>> Erreur chargement : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void ouvrirFenetre(String fxmlPath, String titre) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    Navigation.class.getResource(
                            "/sn/cheikh/gestion_clinique_medicale/" + fxmlPath
                    )
            );
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle(titre);
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void fermerFenetre(Stage stage) {
        if (stage != null) stage.close();
    }
}