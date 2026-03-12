package sn.cheikh.gestion_clinique_medicale.Controller;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import sn.cheikh.gestion_clinique_medicale.Service.*;
import sn.cheikh.gestion_clinique_medicale.model.RendezVous;
import sn.cheikh.gestion_clinique_medicale.model.SessionUtilisateur;
import sn.cheikh.gestion_clinique_medicale.model.Utilisateur;
import sn.cheikh.gestion_clinique_medicale.Utilitaire.Navigation;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class MedecinDashboardController implements Initializable {

    @FXML private Label     lblNomUtilisateur;
    @FXML private Label     lblDate;
    @FXML private Label     lblPageTitre;
    @FXML private Label     lblTopDate;
    @FXML private StackPane contentPane;
    @FXML private VBox      dashboardPane;

    @FXML private Label lblMesRdvJour;
    @FXML private Label lblMesConsultations;
    @FXML private Label lblMesPatients;

    @FXML private TableView<RendezVous>           tableRdvJour;
    @FXML private TableColumn<RendezVous, String> colRdvPatient;
    @FXML private TableColumn<RendezVous, String> colRdvHeure;
    @FXML private TableColumn<RendezVous, String> colRdvMotif;
    @FXML private TableColumn<RendezVous, String> colRdvStatut;

    // Boutons sidebar
    @FXML private Button btnDashboard;
    @FXML private Button btnPatients;
    @FXML private Button btnRendezVous;
    @FXML private Button btnConsultations;

    private final RendezVousService   rendezVousService   = new RendezVousService();
    private final ConsultationService consultationService = new ConsultationService();
    private final PatientService      patientService      = new PatientService();

    private final DateTimeFormatter fmtHeure = DateTimeFormatter.ofPattern("HH:mm");
    private final DateTimeFormatter fmtDate  = DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Utilisateur connecte = SessionUtilisateur.getInstance().getUtilisateurConnecte();
        lblNomUtilisateur.setText("Dr. " + SessionUtilisateur.getInstance().getNomUtilisateur());
        String dateStr = LocalDate.now().format(fmtDate);
        lblDate.setText(dateStr);
        if (lblTopDate != null) lblTopDate.setText(dateStr);

        colRdvPatient.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getPatient().getNomComplet()));
        colRdvHeure.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getDateHeure().format(fmtHeure)));
        colRdvMotif.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getMotif()));
        colRdvStatut.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getStatut().name()));

        setActif(btnDashboard);
        Platform.runLater(this::chargerStats);
    }

    private void setActif(Button boutonActif) {
        Button[] tous = { btnDashboard, btnPatients, btnRendezVous, btnConsultations };
        for (Button btn : tous) {
            if (btn == null) continue;
            btn.getStyleClass().remove("sidebar-btn-active");
            if (!btn.getStyleClass().contains("sidebar-btn"))
                btn.getStyleClass().add("sidebar-btn");
        }
        if (boutonActif != null) {
            boutonActif.getStyleClass().remove("sidebar-btn");
            if (!boutonActif.getStyleClass().contains("sidebar-btn-active"))
                boutonActif.getStyleClass().add("sidebar-btn-active");
        }
    }

    private void chargerStats() {
        try {
            Utilisateur connecte = SessionUtilisateur.getInstance().getUtilisateurConnecte();
            List<RendezVous> mesRdvJour = rendezVousService.getRendezVousParMedecin(connecte)
                    .stream()
                    .filter(r -> r.getDateHeure().toLocalDate().equals(LocalDate.now()))
                    .toList();

            lblMesRdvJour.setText(String.valueOf(mesRdvJour.size()));
            lblMesConsultations.setText(String.valueOf(
                    consultationService.getConsultationsParMedecin(connecte).size()));
            lblMesPatients.setText(String.valueOf(
                    patientService.getTousPatients().size()));

            tableRdvJour.setItems(FXCollections.observableArrayList(mesRdvJour));
            tableRdvJour.refresh();
        } catch (Exception e) {
            System.err.println("Erreur chargement stats médecin : " + e.getMessage());
        }
    }

    @FXML private void afficherDashboard() {
        setActif(btnDashboard);
        lblPageTitre.setText("Tableau de bord");
        contentPane.getChildren().setAll(dashboardPane);
        dashboardPane.setVisible(true);
        Platform.runLater(this::chargerStats);
    }

    @FXML private void afficherPatients() {
        setActif(btnPatients);
        chargerVue("patient/patient-view.fxml", "Mes Patients");
    }

    @FXML private void afficherRendezVous() {
        setActif(btnRendezVous);
        chargerVue("receptionniste/rendezVous-view.fxml", "Mes Rendez-vous");
    }

    @FXML private void afficherConsultations() {
        setActif(btnConsultations);
        chargerVue("medecin/consultation-view.fxml", "Consultations");
    }

    @FXML private void deconnecter() {
        SessionUtilisateur.getInstance().fermerSession();
        Navigation.naviguerVers("auth/login-view.fxml");
    }

    private void chargerVue(String fxmlPath, String titre) {
        try {
            lblPageTitre.setText(titre);
            URL resource = getClass().getResource(
                    "/sn/cheikh/gestion_clinique_medicale/" + fxmlPath);
            if (resource == null) {
                System.err.println("FXML introuvable : " + fxmlPath);
                return;
            }
            Node vue = FXMLLoader.load(resource);
            contentPane.getChildren().setAll(vue);
        } catch (Exception e) {
            System.err.println("Erreur chargement vue : " + fxmlPath);
            e.printStackTrace();
        }
    }
}