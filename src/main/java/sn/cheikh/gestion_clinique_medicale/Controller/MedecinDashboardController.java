package sn.cheikh.gestion_clinique_medicale.Controller;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import sn.cheikh.gestion_clinique_medicale.Service.ConsultationService;
import sn.cheikh.gestion_clinique_medicale.Service.PatientService;
import sn.cheikh.gestion_clinique_medicale.Service.RendezVousService;
import sn.cheikh.gestion_clinique_medicale.Utilitaire.Navigation;
import sn.cheikh.gestion_clinique_medicale.model.RendezVous;
import sn.cheikh.gestion_clinique_medicale.model.SessionUtilisateur;
import sn.cheikh.gestion_clinique_medicale.model.Utilisateur;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class MedecinDashboardController implements Initializable {

    @FXML private Label      lblNomUtilisateur;
    @FXML private Label      lblDate;
    @FXML private Label      lblPageTitre;
    @FXML private Label      lblMesRdvJour;
    @FXML private Label      lblMesConsultations;
    @FXML private Label      lblMesPatients;

    @FXML private StackPane  contentPane;
    @FXML private javafx.scene.layout.VBox dashboardPane;

    @FXML private TableView<RendezVous>            tableRdvJour;
    @FXML private TableColumn<RendezVous, String>  colRdvPatient;
    @FXML private TableColumn<RendezVous, String>  colRdvHeure;
    @FXML private TableColumn<RendezVous, String>  colRdvMotif;
    @FXML private TableColumn<RendezVous, String>  colRdvStatut;

    private final RendezVousService   rdvService          = new RendezVousService();
    private final ConsultationService consultationService = new ConsultationService();
    private final PatientService      patientService      = new PatientService();

    private final DateTimeFormatter fmtDate  = DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy", java.util.Locale.FRENCH);
    private final DateTimeFormatter fmtHeure = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Nom utilisateur connecté
        Utilisateur connecte = SessionUtilisateur.getInstance().getUtilisateurConnecte();
        if (connecte != null) {
            lblNomUtilisateur.setText("Dr. " + connecte.getNomComplet());
        }
        lblDate.setText(LocalDate.now().format(fmtDate));

        // Colonnes tableau RDV
        colRdvPatient.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getPatient() != null
                        ? d.getValue().getPatient().getNomComplet() : ""));
        colRdvHeure.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getDateHeure() != null
                        ? d.getValue().getDateHeure().format(fmtHeure) : ""));
        colRdvMotif.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getMotif() != null
                        ? d.getValue().getMotif() : ""));
        colRdvStatut.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getStatut() != null
                        ? d.getValue().getStatut().name() : ""));

        Platform.runLater(this::chargerStats);
    }

    private void chargerStats() {
        try {
            Utilisateur connecte = SessionUtilisateur.getInstance().getUtilisateurConnecte();
            if (connecte == null) return;

            // RDV du jour pour ce médecin
            List<RendezVous> rdvJour = rdvService.getRendezVousDuJour().stream()
                    .filter(r -> r.getMedecin() != null
                            && r.getMedecin().getId().equals(connecte.getId()))
                    .collect(Collectors.toList());

            lblMesRdvJour.setText(String.valueOf(rdvJour.size()));
            tableRdvJour.setItems(FXCollections.observableArrayList(rdvJour));

            // Total consultations du médecin
            long nbConsultations = consultationService
                    .getConsultationsParMedecin(connecte).size();
            lblMesConsultations.setText(String.valueOf(nbConsultations));

            // Nombre de patients distincts
            long nbPatients = consultationService
                    .getConsultationsParMedecin(connecte).stream()
                    .map(c -> c.getPatient().getId())
                    .distinct()
                    .count();
            lblMesPatients.setText(String.valueOf(nbPatients));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ── Navigation ──

    @FXML
    private void afficherDashboard() {
        lblPageTitre.setText("Tableau de bord");
        dashboardPane.setVisible(true);
        // Masquer les autres vues chargées dynamiquement
        contentPane.getChildren().stream()
                .filter(n -> n != dashboardPane)
                .forEach(n -> n.setVisible(false));
        Platform.runLater(this::chargerStats);
    }

    @FXML
    private void afficherPatients() {
        chargerVue("patient/patient-view.fxml", "Mes Patients");
    }

    @FXML
    private void afficherRendezVous() {
        chargerVue("receptionniste/rendezVous-view.fxml", "Mes Rendez-vous");
    }

    @FXML
    private void afficherConsultations() {
        chargerVue("medecin/consultation-view.fxml", "Consultations");
    }

    @FXML
    private void deconnecter() {
        SessionUtilisateur.getInstance().fermerSession();
        Navigation.naviguerVers("auth/login-view.fxml");
    }

    private void chargerVue(String fxmlPath, String titre) {
        try {
            lblPageTitre.setText(titre);
            URL fxmlUrl = getClass().getResource(
                    "/sn/cheikh/gestion_clinique_medicale/" + fxmlPath);
            if (fxmlUrl == null) {
                System.err.println("FXML introuvable : " + fxmlPath);
                return;
            }
            Node vue = FXMLLoader.load(fxmlUrl);

            // Masquer le dashboardPane, afficher la nouvelle vue
            dashboardPane.setVisible(false);

            // Retirer les vues dynamiques précédentes (garder dashboardPane)
            contentPane.getChildren().removeIf(n -> n != dashboardPane);
            contentPane.getChildren().add(vue);

        } catch (Exception e) {
            System.err.println("Erreur chargement vue : " + fxmlPath);
            e.printStackTrace();
        }
    }
}