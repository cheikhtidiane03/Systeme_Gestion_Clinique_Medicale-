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
import sn.cheikh.gestion_clinique_medicale.enums.Sexe;
import sn.cheikh.gestion_clinique_medicale.model.Patient;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class PatientController implements Initializable {

    @FXML private TextField                    tfRecherche;
    @FXML private Label                        lblMessage;
    @FXML private TableView<Patient>           tableView;
    @FXML private TableColumn<Patient, Long>   colId;
    @FXML private TableColumn<Patient, String> colNom;
    @FXML private TableColumn<Patient, String> colPrenom;
    @FXML private TableColumn<Patient, String> colDateNaissance;
    @FXML private TableColumn<Patient, String> colSexe;
    @FXML private TableColumn<Patient, String> colTelephone;
    @FXML private TableColumn<Patient, String> colGroupeSanguin;
    @FXML private TableColumn<Patient, String> colAdresse;

    private final PatientService patientService = new PatientService();
    private final ObservableList<Patient> liste = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colDateNaissance.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getDateNaissance() != null ? d.getValue().getDateNaissance().toString() : ""));
        colSexe.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getSexe() != null ? d.getValue().getSexe().name() : ""));
        colTelephone.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        colGroupeSanguin.setCellValueFactory(new PropertyValueFactory<>("groupeSanguin"));
        colAdresse.setCellValueFactory(new PropertyValueFactory<>("adresse"));

        tableView.setItems(liste);
        tableView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 && tableView.getSelectionModel().getSelectedItem() != null)
                ouvrirModal(tableView.getSelectionModel().getSelectedItem());
        });
        chargerTous();
    }

    @FXML public void afficherTous() { chargerTous(); }

    @FXML public void rechercher() {
        String q = tfRecherche.getText().trim();
        if (q.isEmpty()) { chargerTous(); return; }
        try {
            liste.setAll(patientService.rechercherPatient(q));
            if (liste.isEmpty()) msg("Aucun patient trouvé pour \"" + q + "\".", "msg-info");
            else msg(liste.size() + " patient(s) trouvé(s).", "msg-succes");
        } catch (Exception e) { msg("Erreur recherche : " + e.getMessage(), "msg-erreur"); }
    }

    @FXML public void nouveau() { ouvrirModal(null); }

    @FXML public void modifier() {
        Patient p = tableView.getSelectionModel().getSelectedItem();
        if (p == null) { msg("⚠ Sélectionnez un patient à modifier.", "msg-warning"); return; }
        ouvrirModal(p);
    }

    @FXML public void supprimer() {
        Patient p = tableView.getSelectionModel().getSelectedItem();
        if (p == null) { msg("⚠ Sélectionnez un patient à supprimer.", "msg-warning"); return; }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation de suppression");
        confirm.setHeaderText("Supprimer " + p.getNomComplet() + " ?");
        confirm.setContentText("Cette action est irréversible.");
        confirm.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.YES) {
                try {
                    patientService.supprimerPatient(p.getId());
                    chargerTous();
                    msg("✓ Patient \"" + p.getNomComplet() + "\" supprimé avec succès.", "msg-succes");
                } catch (Exception ex) {
                    msg("✗ Erreur suppression : " + ex.getMessage(), "msg-erreur");
                }
            } else {
                msg("Suppression annulée.", "msg-info");
            }
        });
    }

    private void ouvrirModal(Patient patient) {
        Stage modal = new Stage();
        modal.initModality(Modality.APPLICATION_MODAL);
        modal.setTitle(patient == null ? "Nouveau patient" : "Modifier patient");
        modal.setResizable(false);

        TextField   tfNom    = champ("Ex: Ndiaye");
        TextField   tfPrenom = champ("Ex: Fatou");
        DatePicker  dpNaiss  = new DatePicker();
        dpNaiss.getStyleClass().add("form-input"); dpNaiss.setMaxWidth(Double.MAX_VALUE);
        ComboBox<Sexe> cbSexe = new ComboBox<>();
        cbSexe.getItems().addAll(Sexe.values());
        cbSexe.setMaxWidth(Double.MAX_VALUE); cbSexe.getStyleClass().add("form-input");
        TextField tfTel     = champ("77XXXXXXX");
        TextField tfAdresse = champ("Quartier, ville...");
        TextField tfGroupe  = champ("A+, O-, AB+...");
        TextArea  taAnt     = ta("Allergies, maladies chroniques...");
        Label     lblMsg    = new Label(); lblMsg.setWrapText(true); lblMsg.setMaxWidth(Double.MAX_VALUE);

        if (patient != null) {
            tfNom.setText(patient.getNom()); tfPrenom.setText(patient.getPrenom());
            dpNaiss.setValue(patient.getDateNaissance()); cbSexe.setValue(patient.getSexe());
            tfTel.setText(patient.getTelephone()); tfAdresse.setText(patient.getAdresse());
            tfGroupe.setText(patient.getGroupeSanguin()); taAnt.setText(patient.getAntecedentsMedicaux());
        }

        GridPane grid = new GridPane();
        grid.setHgap(16); grid.setVgap(12);
        ColumnConstraints c1 = new ColumnConstraints(); c1.setPercentWidth(50);
        ColumnConstraints c2 = new ColumnConstraints(); c2.setPercentWidth(50);
        grid.getColumnConstraints().addAll(c1, c2);

        grid.add(lbl("Nom *"),                    0, 0); grid.add(tfNom,    0, 1);
        grid.add(lbl("Prénom *"),                 1, 0); grid.add(tfPrenom, 1, 1);
        grid.add(lbl("Date de naissance *"),      0, 2); grid.add(dpNaiss,  0, 3);
        grid.add(lbl("Sexe"),                     1, 2); grid.add(cbSexe,   1, 3);
        grid.add(lbl("Téléphone *"),              0, 4); grid.add(tfTel,    0, 5);
        grid.add(lbl("Groupe sanguin"),           1, 4); grid.add(tfGroupe, 1, 5);
        grid.add(lbl("Adresse"),                  0, 6); GridPane.setColumnSpan(lbl("Adresse"), 2);
        grid.add(tfAdresse, 0, 7);                       GridPane.setColumnSpan(tfAdresse, 2);
        grid.add(lbl("Antécédents médicaux"),     0, 8); GridPane.setColumnSpan(lbl("Antécédents médicaux"), 2);
        grid.add(taAnt, 0, 9);                           GridPane.setColumnSpan(taAnt, 2);

        Button btnSave   = new Button(patient == null ? "Enregistrer" : "Mettre à jour");
        Button btnCancel = new Button("Annuler");
        btnSave.getStyleClass().add("btn-primary"); btnCancel.getStyleClass().add("btn-secondary");
        btnSave.setMaxWidth(Double.MAX_VALUE); btnCancel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btnSave, Priority.ALWAYS); HBox.setHgrow(btnCancel, Priority.ALWAYS);
        HBox hBtns = new HBox(10, btnSave, btnCancel);
        hBtns.setPadding(new Insets(8, 0, 0, 0));
        btnCancel.setOnAction(e -> modal.close());

        btnSave.setOnAction(e -> {
            try {
                String nom    = tfNom.getText().trim();
                String prenom = tfPrenom.getText().trim();
                String tel    = tfTel.getText().trim();
                LocalDate naiss = dpNaiss.getValue();
                if (nom.isEmpty())    throw new IllegalArgumentException("Le nom est obligatoire.");
                if (prenom.isEmpty()) throw new IllegalArgumentException("Le prénom est obligatoire.");
                if (tel.isEmpty())    throw new IllegalArgumentException("Le téléphone est obligatoire.");
                if (naiss == null)    throw new IllegalArgumentException("La date de naissance est obligatoire.");

                if (patient == null) {
                    patientService.ajouterPatient(nom, prenom, naiss, cbSexe.getValue(), tel,
                            tfAdresse.getText().trim(), tfGroupe.getText().trim(), taAnt.getText().trim());
                    chargerTous(); modal.close();
                    msg("✓ Patient \"" + nom + " " + prenom + "\" ajouté avec succès.", "msg-succes");
                } else {
                    patient.setNom(nom); patient.setPrenom(prenom);
                    patient.setDateNaissance(naiss); patient.setSexe(cbSexe.getValue());
                    patient.setTelephone(tel); patient.setAdresse(tfAdresse.getText().trim());
                    patient.setGroupeSanguin(tfGroupe.getText().trim());
                    patient.setAntecedentsMedicaux(taAnt.getText().trim());
                    patientService.modifierPatient(patient);
                    chargerTous(); modal.close();
                    msg("✓ Patient \"" + nom + " " + prenom + "\" modifié avec succès.", "msg-succes");
                }
            } catch (Exception ex) {
                lblMsg.setText("✗ " + ex.getMessage());
                lblMsg.getStyleClass().setAll("msg-erreur");
            }
        });

        VBox content = new VBox(16);
        content.setPadding(new Insets(28));
        content.setStyle("-fx-background-color:white;");
        content.setPrefWidth(600);
        Label titre = new Label(patient == null ? "Nouveau patient" : "Modifier — " + patient.getNomComplet());
        titre.setStyle("-fx-font-size:17px; -fx-font-weight:bold; -fx-text-fill:#0F172A;");
        content.getChildren().addAll(titre, new Separator(), grid, lblMsg, hBtns);

        Scene scene = new Scene(content);
        scene.getStylesheets().add(getClass().getResource(
                "/sn/cheikh/gestion_clinique_medicale/css/style.css").toExternalForm());
        modal.setScene(scene);
        modal.showAndWait();
    }

    private void chargerTous() {
        try { liste.setAll(patientService.getTousPatients()); }
        catch (Exception e) { msg("✗ Erreur chargement : " + e.getMessage(), "msg-erreur"); }
    }
    private void msg(String m, String cls) {
        if (lblMessage != null) { lblMessage.setText(m); lblMessage.getStyleClass().setAll(cls); }
    }
    private TextField champ(String p) {
        TextField tf = new TextField(); tf.setPromptText(p);
        tf.getStyleClass().add("form-input"); return tf;
    }
    private TextArea ta(String p) {
        TextArea ta = new TextArea(); ta.setPromptText(p);
        ta.setPrefRowCount(3); ta.setWrapText(true);
        ta.getStyleClass().add("form-input"); return ta;
    }
    private Label lbl(String t) { Label l = new Label(t); l.getStyleClass().add("form-label"); return l; }
}