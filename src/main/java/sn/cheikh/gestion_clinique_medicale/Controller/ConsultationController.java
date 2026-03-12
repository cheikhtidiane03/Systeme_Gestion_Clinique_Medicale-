package sn.cheikh.gestion_clinique_medicale.Controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import sn.cheikh.gestion_clinique_medicale.Service.*;
import sn.cheikh.gestion_clinique_medicale.Utilitaire.PDFGenerator;
import sn.cheikh.gestion_clinique_medicale.enums.Role;
import sn.cheikh.gestion_clinique_medicale.model.*;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class ConsultationController implements Initializable {

    @FXML private Button                              btnNouveau;
    @FXML private Button                              btnModifier;
    @FXML private Label                               lblMessage;
    @FXML private TableView<Consultation>             tableView;
    @FXML private TableColumn<Consultation,Long>      colId;
    @FXML private TableColumn<Consultation,String>    colPatient;
    @FXML private TableColumn<Consultation,String>    colMedecin;
    @FXML private TableColumn<Consultation,String>    colDate;
    @FXML private TableColumn<Consultation,String>    colDiagnostic;
    @FXML private TableColumn<Consultation,String>    colRdv;

    private final ConsultationService consultService = new ConsultationService();
    private final PatientService      patService    = new PatientService();
    private final UtilisateurService  userService   = new UtilisateurService();
    private final RendezVousService   rdvService    = new RendezVousService();
    private final ObservableList<Consultation> liste = FXCollections.observableArrayList();
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colPatient.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getPatient().getNomComplet()));
        colMedecin.setCellValueFactory(d -> new SimpleStringProperty(
                "Dr. " + d.getValue().getMedecin().getNomComplet()));
        colDate.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getDateConsultation() != null
                        ? d.getValue().getDateConsultation().format(fmt) : ""));
        colDiagnostic.setCellValueFactory(new PropertyValueFactory<>("diagnostic"));
        colRdv.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getRendezVous() != null
                        ? d.getValue().getRendezVous().getDateHeure().format(
                        DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "—"));

        tableView.setItems(liste);
        tableView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 && tableView.getSelectionModel().getSelectedItem() != null)
                ouvrirModal(tableView.getSelectionModel().getSelectedItem());
        });

        Utilisateur u = SessionUtilisateur.getInstance().getUtilisateurConnecte();
        boolean estMedecin = u != null && u.getRole() == Role.MEDECIN;
        if (btnNouveau != null) { btnNouveau.setVisible(!estMedecin); btnNouveau.setManaged(!estMedecin); }
        if (btnModifier != null) { btnModifier.setVisible(!estMedecin); btnModifier.setManaged(!estMedecin); }

        chargerConsultations();
    }

    private void chargerConsultations() {
        try {
            Utilisateur u = SessionUtilisateur.getInstance().getUtilisateurConnecte();
            List<Consultation> data = (u != null && u.getRole() == Role.MEDECIN)
                    ? consultService.getConsultationsParMedecin(u)
                    : consultService.getToutesConsultations();
            liste.setAll(data);
        } catch (Exception e) { afficherErreur(e.getMessage()); }
    }

    @FXML public void nouveau()  { ouvrirModal(null); }

    @FXML public void modifier() {
        Consultation c = tableView.getSelectionModel().getSelectedItem();
        if (c == null) { afficherInfo("Sélectionnez une consultation à modifier."); return; }
        ouvrirModal(c);
    }

    @FXML public void genererOrdonnance() {
        Consultation c = tableView.getSelectionModel().getSelectedItem();
        if (c == null) { afficherInfo("Sélectionnez une consultation."); return; }
        try {
            String chemin = PDFGenerator.genererOrdonnance(c);
            if (lblMessage != null) {
                lblMessage.setText("✓ Ordonnance générée : " + chemin);
                lblMessage.getStyleClass().setAll("msg-succes");
            }
        } catch (Exception e) {
            if (lblMessage != null) {
                lblMessage.setText("Erreur PDF : " + e.getMessage());
                lblMessage.getStyleClass().setAll("msg-erreur");
            }
        }
    }

    private void ouvrirModal(Consultation consultation) {
        Stage modal = new Stage();
        modal.initModality(Modality.APPLICATION_MODAL);
        modal.setTitle(consultation == null ? "Nouvelle consultation" : "Modifier consultation");
        modal.setResizable(false);

        ComboBox<Patient>     cbPatient = new ComboBox<>(
                FXCollections.observableArrayList(patService.getTousPatients()));
        ComboBox<Utilisateur> cbMedecin = new ComboBox<>(
                FXCollections.observableArrayList(userService.getMedecins()));
        ComboBox<RendezVous>  cbRdv     = new ComboBox<>(
                FXCollections.observableArrayList(rdvService.getTousRendezVous()));
        TextArea taDiag = ta("Diagnostic...");
        TextArea taObs  = ta("Observations cliniques...");
        TextArea taPres = ta("Médicaments, posologie...");
        Label lblMsg = new Label(); lblMsg.setWrapText(true); lblMsg.setMaxWidth(Double.MAX_VALUE);

        cbPatient.setConverter(new javafx.util.StringConverter<>() {
            public String toString(Patient p) { return p == null ? "" : p.getNomComplet(); }
            public Patient fromString(String s) { return null; }
        });
        cbMedecin.setConverter(new javafx.util.StringConverter<>() {
            public String toString(Utilisateur u) { return u == null ? "" : "Dr. " + u.getNomComplet(); }
            public Utilisateur fromString(String s) { return null; }
        });
        cbRdv.setConverter(new javafx.util.StringConverter<>() {
            public String toString(RendezVous r) {
                return r == null ? "" : r.getPatient().getNomComplet() + " — " +
                        r.getDateHeure().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            }
            public RendezVous fromString(String s) { return null; }
        });
        cbPatient.setMaxWidth(Double.MAX_VALUE);
        cbMedecin.setMaxWidth(Double.MAX_VALUE);
        cbRdv.setMaxWidth(Double.MAX_VALUE);

        if (consultation != null) {
            cbPatient.setValue(consultation.getPatient());
            cbMedecin.setValue(consultation.getMedecin());
            cbRdv.setValue(consultation.getRendezVous());
            taDiag.setText(consultation.getDiagnostic());
            taObs.setText(consultation.getObservations());
            taPres.setText(consultation.getPrescription());
        } else {
            Utilisateur u = SessionUtilisateur.getInstance().getUtilisateurConnecte();
            if (u != null && u.getRole() == Role.MEDECIN) cbMedecin.setValue(u);
        }

        GridPane grid = new GridPane();
        grid.setHgap(16); grid.setVgap(12);
        ColumnConstraints c1 = new ColumnConstraints(); c1.setPercentWidth(50);
        ColumnConstraints c2 = new ColumnConstraints(); c2.setPercentWidth(50);
        grid.getColumnConstraints().addAll(c1, c2);

        grid.add(lbl("Patient *"),        0, 0); grid.add(cbPatient, 0, 1);
        grid.add(lbl("Médecin *"),        1, 0); grid.add(cbMedecin, 1, 1);
        grid.add(lbl("Rendez-vous lié"),  0, 2); GridPane.setColumnSpan(lbl("Rendez-vous lié"), 2);
        grid.add(cbRdv,                   0, 3); GridPane.setColumnSpan(cbRdv, 2);
        grid.add(lbl("Diagnostic *"),     0, 4); GridPane.setColumnSpan(lbl("Diagnostic *"), 2);
        grid.add(taDiag,                  0, 5); GridPane.setColumnSpan(taDiag, 2);
        grid.add(lbl("Observations"),     0, 6); GridPane.setColumnSpan(lbl("Observations"), 2);
        grid.add(taObs,                   0, 7); GridPane.setColumnSpan(taObs, 2);
        grid.add(lbl("Prescription"),     0, 8); GridPane.setColumnSpan(lbl("Prescription"), 2);
        grid.add(taPres,                  0, 9); GridPane.setColumnSpan(taPres, 2);

        Button btnSave   = new Button(consultation == null ? "Enregistrer" : "Mettre à jour");
        Button btnCancel = new Button("Annuler");
        btnSave.getStyleClass().add("btn-primary");
        btnCancel.getStyleClass().add("btn-secondary");
        btnSave.setMaxWidth(Double.MAX_VALUE);
        btnCancel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btnSave, Priority.ALWAYS);
        HBox.setHgrow(btnCancel, Priority.ALWAYS);
        HBox hBtns = new HBox(10, btnSave, btnCancel);
        hBtns.setPadding(new Insets(8, 0, 0, 0));
        btnCancel.setOnAction(e -> modal.close());

        btnSave.setOnAction(e -> {
            try {
                if (cbPatient.getValue() == null) throw new IllegalArgumentException("Sélectionnez un patient.");
                if (cbMedecin.getValue() == null) throw new IllegalArgumentException("Sélectionnez un médecin.");
                if (taDiag.getText().trim().isEmpty()) throw new IllegalArgumentException("Le diagnostic est obligatoire.");
                if (consultation == null) {
                    consultService.enregistrerConsultation(
                            cbPatient.getValue(), cbMedecin.getValue(), cbRdv.getValue(),
                            taDiag.getText().trim(), taObs.getText().trim(), taPres.getText().trim());
                } else {
                    consultation.setPatient(cbPatient.getValue());
                    consultation.setMedecin(cbMedecin.getValue());
                    consultation.setRendezVous(cbRdv.getValue());
                    consultation.setDiagnostic(taDiag.getText().trim());
                    consultation.setObservations(taObs.getText().trim());
                    consultation.setPrescription(taPres.getText().trim());
                    consultService.modifierConsultation(consultation);
                }
                chargerConsultations();
                modal.close();
            } catch (Exception ex) {
                lblMsg.setText(ex.getMessage());
                lblMsg.getStyleClass().setAll("msg-erreur");
            }
        });

        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color:white; -fx-border-color:transparent;");
        VBox content = new VBox(16);
        content.setPadding(new Insets(28));
        content.setStyle("-fx-background-color:white;");
        content.setPrefWidth(620);
        Label titre = new Label(consultation == null ? "Nouvelle consultation" : "Modifier consultation");
        titre.setStyle("-fx-font-size:17px; -fx-font-weight:bold; -fx-text-fill:#0F172A;");
        content.getChildren().addAll(titre, new Separator(), grid, lblMsg, hBtns);
        scroll.setContent(content);

        Scene scene = new Scene(scroll, 640, 680);
        scene.getStylesheets().add(getClass().getResource(
                "/sn/cheikh/gestion_clinique_medicale/css/style.css").toExternalForm());
        modal.setScene(scene);
        modal.showAndWait();
    }

    private TextArea ta(String p) {
        TextArea ta = new TextArea(); ta.setPromptText(p);
        ta.setPrefRowCount(3); ta.setWrapText(true);
        ta.getStyleClass().add("form-input"); return ta;
    }
    private Label lbl(String t) { Label l = new Label(t); l.getStyleClass().add("form-label"); return l; }
    private void afficherErreur(String m) { new Alert(Alert.AlertType.ERROR, m, ButtonType.OK).showAndWait(); }
    private void afficherInfo(String m)   { new Alert(Alert.AlertType.INFORMATION, m, ButtonType.OK).showAndWait(); }
}