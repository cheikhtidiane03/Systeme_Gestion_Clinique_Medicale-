package sn.cheikh.gestion_clinique_medicale.Controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import sn.cheikh.gestion_clinique_medicale.Service.UtilisateurService;
import sn.cheikh.gestion_clinique_medicale.model.SessionUtilisateur;
import sn.cheikh.gestion_clinique_medicale.model.Utilisateur;
import sn.cheikh.gestion_clinique_medicale.Utilitaire.Navigation;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML private TextField tfLogin;
    @FXML private PasswordField pfMotDePasse;
    @FXML private Label lblMessage;

    private final UtilisateurService utilisateurService = new UtilisateurService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        pfMotDePasse.setOnAction(e -> connecter());
    }

    @FXML
    private void connecter() {
        String login = tfLogin.getText().trim();
        String motDePasse = pfMotDePasse.getText();

        if (login.isEmpty() || motDePasse.isEmpty()) {
            afficherErreur("Veuillez remplir tous les champs.");
            return;
        }

        try {
            Optional<Utilisateur> utilisateur = utilisateurService.connecter(login, motDePasse);
            if (utilisateur.isPresent()) {
                SessionUtilisateur.getInstance().ouvrirSession(utilisateur.get());
                Platform.runLater(() -> Navigation.naviguerVers("dashboard-view.fxml"));
            } else {
                afficherErreur("Login ou mot de passe incorrect.");
                pfMotDePasse.clear();
            }
        } catch (Exception e) {
            afficherErreur(e.getMessage());
        }
    }

    private void afficherErreur(String msg) {
        lblMessage.setText("✘ " + msg);
        lblMessage.getStyleClass().setAll("msg-erreur");
    }
}