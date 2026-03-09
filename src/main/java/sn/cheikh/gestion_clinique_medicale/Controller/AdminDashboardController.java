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
import javafx.scene.layout.VBox;
import sn.cheikh.gestion_clinique_medicale.Service.*;
import sn.cheikh.gestion_clinique_medicale.model.RendezVous;
import sn.cheikh.gestion_clinique_medicale.model.SessionUtilisateur;
import sn.cheikh.gestion_clinique_medicale.Utilitaire.Navigation;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class AdminDashboardController implements Initializable {

    @FXML private Label     lblNomUtilisateur;
    @FXML private Label     lblPageTitre;
    @FXML private Label     lblDate;
    @FXML private StackPane contentPane;
    @FXML private VBox      dashboardPane;

    @FXML private Label lblNbPatients;
    @FXML private Label lblNbRdvJour;
    @FXML private Label lblNbConsultations;
    @FXML private Label lblNbFacturesNonPayees;

    @FXML private TableView<RendezVous>           tableRdvJour;
    @FXML private TableColumn<RendezVous, String> colRdvPatient;
    @FXML private TableColumn<RendezVous, String> colRdvMedecin;
    @FXML private TableColumn<RendezVous, String> colRdvHeure;
    @FXML private TableColumn<RendezVous, String> colRdvMotif;
    @FXML private TableColumn<RendezVous, String> colRdvStatut;

    private final PatientService      patientService      = new PatientService();
    private final RendezVousService   rendezVousService   = new RendezVousService();
    private final ConsultationService consultationService = new ConsultationService();
    private final FactureService      factureService      = new FactureService();

    private final DateTimeFormatter fmtHeure = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        lblNomUtilisateur.setText(SessionUtilisateur.getInstance().getNomUtilisateur());
        lblDate.setText(LocalDate.now().format(
                DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy")));

        // Colonnes tableau RDV
        colRdvPatient.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getPatient().getNomComplet()));
        colRdvMedecin.setCellValueFactory(d ->
                new SimpleStringProperty("Dr. " + d.getValue().getMedecin().getNomComplet()));
        colRdvHeure.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getDateHeure().format(fmtHeure)));
        colRdvMotif.setCellValueFactory(new PropertyValueFactory<>("motif"));
        colRdvStatut.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getStatut().name()));

        // Charger après rendu complet de la vue
        Platform.runLater(this::chargerStats);
    }

    private void chargerStats() {
        try {
            List<RendezVous> rdvJour = rendezVousService.getRendezVousDuJour();

            lblNbPatients.setText(String.valueOf(
                    patientService.getTousPatients().size()));
            lblNbRdvJour.setText(String.valueOf(rdvJour.size()));
            lblNbConsultations.setText(String.valueOf(
                    consultationService.getToutesConsultations().size()));
            lblNbFacturesNonPayees.setText(String.valueOf(
                    factureService.getFacturesNonPayees().size()));

            tableRdvJour.setItems(FXCollections.observableArrayList(rdvJour));
            tableRdvJour.refresh();
        } catch (Exception e) {
            System.err.println("Erreur chargement stats : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void afficherDashboard() {
        lblPageTitre.setText("Tableau de bord");
        contentPane.getChildren().setAll(dashboardPane);
        dashboardPane.setVisible(true);
        Platform.runLater(this::chargerStats);
    }

    @FXML private void afficherUtilisateurs()  { chargerVue("admin/utilisateur-view.fxml",          "Utilisateurs"); }
    @FXML private void afficherPatients()      { chargerVue("patient/patient-view.fxml",             "Patients"); }
    @FXML private void afficherRendezVous()    { chargerVue("receptionniste/rendezVous-view.fxml",   "Rendez-vous"); }
    @FXML private void afficherConsultations() { chargerVue("medecin/consultation-view.fxml",        "Consultations"); }
    @FXML private void afficherFactures()      { chargerVue("receptionniste/facture-view.fxml",      "Facturation"); }

    @FXML
    private void deconnecter() {
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