package sn.cheikh.gestion_clinique_medicale.Controller;

import javafx.application.Platform;
import javafx.fxml.Initializable;
import sn.cheikh.gestion_clinique_medicale.enums.Role;
import sn.cheikh.gestion_clinique_medicale.model.SessionUtilisateur;
import sn.cheikh.gestion_clinique_medicale.Utilitaire.Navigation;

import java.net.URL;
import java.util.ResourceBundle;

public class DashboardRouterController implements Initializable {

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Platform.runLater — attend que la scène courante soit entièrement chargée
        Platform.runLater(() -> {
            Role role = SessionUtilisateur.getInstance().getRoleUtilisateur();

            if (role == null) {
                Navigation.naviguerVers("auth/login-view.fxml");
                return;
            }

            switch (role) {
                case ADMIN          -> Navigation.naviguerVers("admin/admin-dashboard.fxml");
                case MEDECIN        -> Navigation.naviguerVers("medecin/medecin-dashboard.fxml");
                case RECEPTIONNISTE -> Navigation.naviguerVers("receptionniste/receptionniste-dashboard.fxml");
                default             -> Navigation.naviguerVers("auth/login-view.fxml");
            }
        });
    }
}