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
import sn.cheikh.gestion_clinique_medicale.Utilitaire.Navigation;
import sn.cheikh.gestion_clinique_medicale.Utilitaire.PDFGenerator;
import sn.cheikh.gestion_clinique_medicale.model.Consultation;
import sn.cheikh.gestion_clinique_medicale.model.Facture;
import sn.cheikh.gestion_clinique_medicale.model.RendezVous;
import sn.cheikh.gestion_clinique_medicale.model.SessionUtilisateur;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ReceptionisteDashboardController implements Initializable {

    @FXML private Label     lblNomUtilisateur;
    @FXML private Label     lblDate;
    @FXML private Label     lblPageTitre;
    @FXML private Label     lblTopDate;
    @FXML private StackPane contentPane;
    @FXML private VBox      dashboardPane;

    @FXML private Label lblNbPatients;
    @FXML private Label lblRdvJour;
    @FXML private Label lblFacturesImpayees;

    @FXML private TableView<RendezVous>           tableRdvJour;
    @FXML private TableColumn<RendezVous, String> colRdvPatient;
    @FXML private TableColumn<RendezVous, String> colRdvMedecin;
    @FXML private TableColumn<RendezVous, String> colRdvHeure;
    @FXML private TableColumn<RendezVous, String> colRdvMotif;
    @FXML private TableColumn<RendezVous, String> colRdvStatut;

    @FXML private Button btnDashboard;
    @FXML private Button btnPatients;
    @FXML private Button btnRendezVous;
    @FXML private Button btnFactures;

    private final PatientService      patientService    = new PatientService();
    private final RendezVousService   rendezVousService = new RendezVousService();
    private final FactureService      factureService    = new FactureService();
    private final ConsultationService consultationService = new ConsultationService();

    private final DateTimeFormatter fmtHeure = DateTimeFormatter.ofPattern("HH:mm");
    private final DateTimeFormatter fmtDate  = DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        lblNomUtilisateur.setText(SessionUtilisateur.getInstance().getNomUtilisateur());
        String dateStr = LocalDate.now().format(fmtDate);
        lblDate.setText(dateStr);
        if (lblTopDate != null) lblTopDate.setText(dateStr);

        colRdvPatient.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPatient().getNomComplet()));
        colRdvMedecin.setCellValueFactory(d -> new SimpleStringProperty("Dr. " + d.getValue().getMedecin().getNomComplet()));
        colRdvHeure.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDateHeure().format(fmtHeure)));
        colRdvMotif.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMotif()));
        colRdvStatut.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatut().name()));

        setActif(btnDashboard);
        Platform.runLater(this::chargerStats);
    }

    private void setActif(Button boutonActif) {
        Button[] tous = { btnDashboard, btnPatients, btnRendezVous, btnFactures };
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
            List<RendezVous> rdvJour = rendezVousService.getRendezVousDuJour();
            lblNbPatients.setText(String.valueOf(patientService.getTousPatients().size()));
            lblRdvJour.setText(String.valueOf(rdvJour.size()));
            lblFacturesImpayees.setText(String.valueOf(factureService.getFacturesNonPayees().size()));
            tableRdvJour.setItems(FXCollections.observableArrayList(rdvJour));
            tableRdvJour.refresh();
        } catch (Exception e) {
            System.err.println("Erreur chargement stats réceptionniste : " + e.getMessage());
        }
    }

    @FXML private void afficherDashboard() {
        setActif(btnDashboard);
        lblPageTitre.setText("Tableau de bord");
        contentPane.getChildren().setAll(dashboardPane);
        dashboardPane.setVisible(true);
        Platform.runLater(this::chargerStats);
    }

    @FXML private void afficherPatients()   { setActif(btnPatients);   chargerVue("patient/patient-view.fxml",           "Patients"); }
    @FXML private void afficherRendezVous() { setActif(btnRendezVous); chargerVue("receptionniste/rendezVous-view.fxml", "Rendez-vous"); }
    @FXML private void afficherFactures()   { setActif(btnFactures);   chargerVue("receptionniste/facture-view.fxml",    "Facturation"); }

    @FXML private void genererRapport() {
        try {
            LocalDate aujourd = LocalDate.now();

            List<RendezVous> rdvJour = rendezVousService.getRendezVousDuJour();

            List<Consultation> consultationsJour = consultationService.getToutesConsultations()
                    .stream()
                    .filter(c -> c.getDateConsultation() != null &&
                            c.getDateConsultation().toLocalDate().equals(aujourd))
                    .collect(Collectors.toList());

            List<Facture> facturesJour = factureService.getToutesFactures()
                    .stream()
                    .filter(f -> f.getDateFacture() != null &&
                            f.getDateFacture().toLocalDate().equals(aujourd))
                    .collect(Collectors.toList());

            String chemin = PDFGenerator.genererRapportJournalier(
                    aujourd, rdvJour, consultationsJour, facturesJour);

            Alert info = new Alert(Alert.AlertType.INFORMATION);
            info.setTitle("Rapport généré");
            info.setHeaderText("Rapport journalier du " + aujourd.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            info.setContentText("Fichier généré :\n" + chemin);
            info.showAndWait();

        } catch (Exception e) {
            Alert err = new Alert(Alert.AlertType.ERROR);
            err.setTitle("Erreur");
            err.setHeaderText("Impossible de générer le rapport");
            err.setContentText(e.getMessage());
            err.showAndWait();
        }
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
            if (resource == null) { System.err.println("FXML introuvable : " + fxmlPath); return; }
            Node vue = FXMLLoader.load(resource);
            contentPane.getChildren().setAll(vue);
        } catch (Exception e) {
            System.err.println("Erreur chargement vue : " + e.getMessage());
            e.printStackTrace();
        }
    }
}