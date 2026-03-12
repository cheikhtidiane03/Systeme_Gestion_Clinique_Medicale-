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
import sn.cheikh.gestion_clinique_medicale.Service.PatientService;
import sn.cheikh.gestion_clinique_medicale.Service.RendezVousService;
import sn.cheikh.gestion_clinique_medicale.Service.UtilisateurService;
import sn.cheikh.gestion_clinique_medicale.enums.Role;
import sn.cheikh.gestion_clinique_medicale.enums.StatutRendezVous;
import sn.cheikh.gestion_clinique_medicale.model.Patient;
import sn.cheikh.gestion_clinique_medicale.model.RendezVous;
import sn.cheikh.gestion_clinique_medicale.model.SessionUtilisateur;
import sn.cheikh.gestion_clinique_medicale.model.Utilisateur;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class RendezVousController implements Initializable {

    @FXML private Button                         btnNouveauBar;
    @FXML private TableView<RendezVous>          tableView;
    @FXML private TableColumn<RendezVous,Long>   colId;
    @FXML private TableColumn<RendezVous,String> colPatient;
    @FXML private TableColumn<RendezVous,String> colMedecin;
    @FXML private TableColumn<RendezVous,String> colDateHeure;
    @FXML private TableColumn<RendezVous,String> colMotif;
    @FXML private TableColumn<RendezVous,String> colStatut;

    private final RendezVousService   rdvService  = new RendezVousService();
    private final PatientService      patService  = new PatientService();
    private final UtilisateurService  userService = new UtilisateurService();
    private final ObservableList<RendezVous> liste = FXCollections.observableArrayList();
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colPatient.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getPatient().getNomComplet()));
        colMedecin.setCellValueFactory(d -> new SimpleStringProperty(
                "Dr. " + d.getValue().getMedecin().getNomComplet()));
        colDateHeure.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getDateHeure().format(fmt)));
        colMotif.setCellValueFactory(new PropertyValueFactory<>("motif"));
        colStatut.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getStatut().name()));

        tableView.setItems(liste);
        tableView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 && tableView.getSelectionModel().getSelectedItem() != null)
                ouvrirModal(tableView.getSelectionModel().getSelectedItem());
        });

        // Mode lecture seule pour médecin
        Utilisateur connecte = SessionUtilisateur.getInstance().getUtilisateurConnecte();
        if (connecte != null && connecte.getRole() == Role.MEDECIN) {
            if (btnNouveauBar != null) { btnNouveauBar.setVisible(false); btnNouveauBar.setManaged(false); }
        }
        chargerTous();
    }

    @FXML public void chargerTous() {
        try {
            Utilisateur u = SessionUtilisateur.getInstance().getUtilisateurConnecte();
            List<RendezVous> data = (u != null && u.getRole() == Role.MEDECIN)
                    ? rdvService.getRendezVousParMedecin(u)
                    : rdvService.getTousRendezVous();
            liste.setAll(data);
        } catch (Exception e) { afficherErreur(e.getMessage()); }
    }

    @FXML public void afficherDuJour() {
        try { liste.setAll(rdvService.getRendezVousDuJour()); }
        catch (Exception e) { afficherErreur(e.getMessage()); }
    }

    @FXML public void nouveau() { ouvrirModal(null); }

    @FXML public void modifier() {
        RendezVous rdv = tableView.getSelectionModel().getSelectedItem();
        if (rdv == null) { afficherInfo("Sélectionnez un rendez-vous à modifier."); return; }
        ouvrirModal(rdv);
    }

    @FXML public void annuler() {
        RendezVous rdv = tableView.getSelectionModel().getSelectedItem();
        if (rdv == null) { afficherInfo("Sélectionnez un rendez-vous à annuler."); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Annuler ce rendez-vous ?", ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.YES) {
                try {
                    rdvService.annulerRendezVous(rdv.getId());
                    chargerTous();
                } catch (Exception ex) { afficherErreur(ex.getMessage()); }
            }
        });
    }

    private void ouvrirModal(RendezVous rdv) {
        Stage modal = new Stage();
        modal.initModality(Modality.APPLICATION_MODAL);
        modal.setTitle(rdv == null ? "Nouveau rendez-vous" : "Modifier rendez-vous");
        modal.setResizable(false);

        // Champs
        List<Patient>    patients = patService.getTousPatients();
        List<Utilisateur> medecins = userService.getMedecins();

        ComboBox<Patient>    cbPatient = new ComboBox<>(FXCollections.observableArrayList(patients));
        ComboBox<Utilisateur> cbMedecin = new ComboBox<>(FXCollections.observableArrayList(medecins));
        DatePicker dpDate = new DatePicker(LocalDate.now());
        ComboBox<String> cbHeure = new ComboBox<>();
        cbHeure.setEditable(true);
        for (int h = 7; h <= 19; h++) {
            cbHeure.getItems().add(String.format("%02d:00", h));
            cbHeure.getItems().add(String.format("%02d:30", h));
        }
        TextField tfMotif = new TextField();
        tfMotif.setPromptText("Motif de la consultation");
        Label lblMsg = new Label(); lblMsg.setWrapText(true);

        // Formatage ComboBox
        cbPatient.setConverter(new javafx.util.StringConverter<>() {
            public String toString(Patient p) { return p == null ? "" : p.getNomComplet(); }
            public Patient fromString(String s) { return null; }
        });
        cbMedecin.setConverter(new javafx.util.StringConverter<>() {
            public String toString(Utilisateur u) { return u == null ? "" : "Dr. " + u.getNomComplet(); }
            public Utilisateur fromString(String s) { return null; }
        });
        cbPatient.setMaxWidth(Double.MAX_VALUE);
        cbMedecin.setMaxWidth(Double.MAX_VALUE);
        dpDate.setMaxWidth(Double.MAX_VALUE);
        cbHeure.setMaxWidth(Double.MAX_VALUE);
        tfMotif.getStyleClass().add("form-input");

        // Pré-remplir
        if (rdv != null) {
            cbPatient.setValue(rdv.getPatient());
            cbMedecin.setValue(rdv.getMedecin());
            dpDate.setValue(rdv.getDateHeure().toLocalDate());
            cbHeure.setValue(rdv.getDateHeure().format(DateTimeFormatter.ofPattern("HH:mm")));
            tfMotif.setText(rdv.getMotif());
        }

        // Layout
        GridPane grid = new GridPane();
        grid.setHgap(16); grid.setVgap(12);
        ColumnConstraints c1 = new ColumnConstraints(); c1.setPercentWidth(50);
        ColumnConstraints c2 = new ColumnConstraints(); c2.setPercentWidth(50);
        grid.getColumnConstraints().addAll(c1, c2);

        grid.add(lbl("Patient *"),  0, 0); grid.add(cbPatient, 0, 1);
        grid.add(lbl("Médecin *"),  1, 0); grid.add(cbMedecin, 1, 1);
        grid.add(lbl("Date *"),     0, 2); grid.add(dpDate,    0, 3);
        grid.add(lbl("Heure *"),    1, 2); grid.add(cbHeure,   1, 3);
        grid.add(lbl("Motif"),      0, 4); GridPane.setColumnSpan(lbl("Motif"), 2);
        grid.add(tfMotif,           0, 5); GridPane.setColumnSpan(tfMotif, 2);

        Button btnSave   = new Button(rdv == null ? "Planifier" : "Mettre à jour");
        Button btnCancel = new Button("Annuler");
        btnSave.getStyleClass().add("btn-primary");
        btnCancel.getStyleClass().add("btn-secondary");
        HBox.setHgrow(btnSave, Priority.ALWAYS);
        HBox.setHgrow(btnCancel, Priority.ALWAYS);
        btnSave.setMaxWidth(Double.MAX_VALUE);
        btnCancel.setMaxWidth(Double.MAX_VALUE);
        HBox hBtns = new HBox(10, btnSave, btnCancel);
        hBtns.setPadding(new Insets(8, 0, 0, 0));
        btnCancel.setOnAction(e -> modal.close());

        btnSave.setOnAction(e -> {
            try {
                if (cbPatient.getValue() == null) throw new IllegalArgumentException("Sélectionnez un patient.");
                if (cbMedecin.getValue() == null) throw new IllegalArgumentException("Sélectionnez un médecin.");
                if (dpDate.getValue() == null)    throw new IllegalArgumentException("Choisissez une date.");
                String heure = cbHeure.getValue();
                if (heure == null || heure.isBlank()) throw new IllegalArgumentException("Choisissez une heure.");
                String[] parts = heure.split(":");
                LocalDateTime dt = dpDate.getValue().atTime(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
                if (rdv == null) {
                    rdvService.planifierRendezVous(dt, tfMotif.getText().trim(),
                            cbPatient.getValue(), cbMedecin.getValue());
                } else {
                    rdv.setPatient(cbPatient.getValue()); rdv.setMedecin(cbMedecin.getValue());
                    rdv.setDateHeure(dt); rdv.setMotif(tfMotif.getText().trim());
                    rdvService.modifierRendezVous(rdv);
                }
                chargerTous();
                modal.close();
            } catch (Exception ex) {
                lblMsg.setText(ex.getMessage());
                lblMsg.getStyleClass().setAll("msg-erreur");
            }
        });

        VBox content = new VBox(16);
        content.setPadding(new Insets(28));
        content.setStyle("-fx-background-color:white;");
        content.setPrefWidth(560);
        Label titre = new Label(rdv == null ? "Nouveau rendez-vous" : "Modifier rendez-vous");
        titre.setStyle("-fx-font-size:17px; -fx-font-weight:bold; -fx-text-fill:#0F172A;");
        content.getChildren().addAll(titre, new Separator(), grid, lblMsg, hBtns);

        Scene scene = new Scene(content);
        scene.getStylesheets().add(getClass().getResource(
                "/sn/cheikh/gestion_clinique_medicale/css/style.css").toExternalForm());
        modal.setScene(scene);
        modal.showAndWait();
    }

    private Label lbl(String t) { Label l = new Label(t); l.getStyleClass().add("form-label"); return l; }
    private void afficherErreur(String m) { new Alert(Alert.AlertType.ERROR, m, ButtonType.OK).showAndWait(); }
    private void afficherInfo(String m)   { new Alert(Alert.AlertType.INFORMATION, m, ButtonType.OK).showAndWait(); }
}