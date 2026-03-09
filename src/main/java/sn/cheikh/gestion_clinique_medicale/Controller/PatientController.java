package sn.cheikh.gestion_clinique_medicale.Controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import sn.cheikh.gestion_clinique_medicale.Service.PatientService;
import sn.cheikh.gestion_clinique_medicale.enums.Sexe;
import sn.cheikh.gestion_clinique_medicale.model.Patient;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class PatientController implements Initializable {

    @FXML private TextField       tfNom;
    @FXML private TextField       tfPrenom;
    @FXML private DatePicker      dpDateNaissance;
    @FXML private ComboBox<Sexe>  cbSexe;
    @FXML private TextField       tfTelephone;
    @FXML private TextField       tfAdresse;
    @FXML private TextField       tfGroupeSanguin;
    @FXML private TextArea        taAntecedents;
    @FXML private TextField       tfRecherche;
    @FXML private Label           lblMessage;
    @FXML private Button          btnSupprimer;

    @FXML private TableView<Patient>            tableView;
    @FXML private TableColumn<Patient, Long>    colId;
    @FXML private TableColumn<Patient, String>  colNom;
    @FXML private TableColumn<Patient, String>  colPrenom;
    @FXML private TableColumn<Patient, String>  colDateNaissance;
    @FXML private TableColumn<Patient, String>  colSexe;
    @FXML private TableColumn<Patient, String>  colTelephone;
    @FXML private TableColumn<Patient, String>  colGroupeSanguin;

    private final PatientService patientService = new PatientService();
    private Patient patientSelectionne = null;
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cbSexe.setItems(FXCollections.observableArrayList(Sexe.values()));

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colDateNaissance.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getDateNaissance() != null
                        ? d.getValue().getDateNaissance().format(fmt) : ""));
        colSexe.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getSexe() != null ? d.getValue().getSexe().name() : ""));
        colTelephone.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        colGroupeSanguin.setCellValueFactory(new PropertyValueFactory<>("groupeSanguin"));

        tableView.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, sel) -> remplirFormulaire(sel));

        btnSupprimer.setDisable(true);
        chargerPatients();
    }

    private void chargerPatients() {
        tableView.setItems(FXCollections.observableArrayList(patientService.getTousPatients()));
    }

    @FXML
    private void sauvegarder() {
        try {
            validerFormulaire();

            if (patientSelectionne == null) {
                patientService.ajouterPatient(
                        tfNom.getText().trim(),
                        tfPrenom.getText().trim(),
                        dpDateNaissance.getValue(),
                        cbSexe.getValue(),
                        tfTelephone.getText().trim(),
                        tfAdresse.getText().trim(),
                        tfGroupeSanguin.getText().trim(),
                        taAntecedents.getText().trim()
                );
                afficherSucces("Patient ajoute avec succes.");
            } else {
                patientSelectionne.setNom(tfNom.getText().trim());
                patientSelectionne.setPrenom(tfPrenom.getText().trim());
                patientSelectionne.setDateNaissance(dpDateNaissance.getValue());
                patientSelectionne.setSexe(cbSexe.getValue());
                patientSelectionne.setTelephone(tfTelephone.getText().trim());
                patientSelectionne.setAdresse(tfAdresse.getText().trim());
                patientSelectionne.setGroupeSanguin(tfGroupeSanguin.getText().trim());
                patientSelectionne.setAntecedentsMedicaux(taAntecedents.getText().trim());
                patientService.modifierPatient(patientSelectionne);
                afficherSucces("Patient modifie avec succes.");
            }

            nouveau();
            chargerPatients();

        } catch (IllegalArgumentException e) {
            afficherErreur(e.getMessage());
        } catch (Exception e) {
            afficherErreur("Erreur : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void supprimer() {
        if (patientSelectionne == null) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer le patient « " + patientSelectionne.getNomComplet() + " » ?",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                try {
                    patientService.supprimerPatient(patientSelectionne.getId());
                    afficherSucces("Patient supprime.");
                    nouveau();
                    chargerPatients();
                } catch (Exception e) {
                    afficherErreur("Impossible de supprimer : " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void nouveau() {
        patientSelectionne = null;
        tfNom.clear();
        tfPrenom.clear();
        dpDateNaissance.setValue(null);
        cbSexe.setValue(null);
        tfTelephone.clear();
        tfAdresse.clear();
        tfGroupeSanguin.clear();
        taAntecedents.clear();
        lblMessage.setText("");
        btnSupprimer.setDisable(true);
        tableView.getSelectionModel().clearSelection();
    }

    @FXML
    private void rechercher() {
        String terme = tfRecherche.getText().trim().toLowerCase();
        if (terme.isEmpty()) { chargerPatients(); return; }
        ObservableList<Patient> filtres = FXCollections.observableArrayList(
                patientService.getTousPatients().stream()
                        .filter(p -> p.getNom().toLowerCase().contains(terme)
                                || p.getPrenom().toLowerCase().contains(terme)
                                || (p.getTelephone() != null && p.getTelephone().contains(terme)))
                        .toList());
        tableView.setItems(filtres);
    }

    @FXML
    private void afficherTous() {
        tfRecherche.clear();
        chargerPatients();
    }

    private void remplirFormulaire(Patient p) {
        if (p == null) return;
        patientSelectionne = p;
        tfNom.setText(p.getNom());
        tfPrenom.setText(p.getPrenom());
        dpDateNaissance.setValue(p.getDateNaissance());
        cbSexe.setValue(p.getSexe());
        tfTelephone.setText(p.getTelephone() != null ? p.getTelephone() : "");
        tfAdresse.setText(p.getAdresse() != null ? p.getAdresse() : "");
        tfGroupeSanguin.setText(p.getGroupeSanguin() != null ? p.getGroupeSanguin() : "");
        taAntecedents.setText(p.getAntecedentsMedicaux() != null ? p.getAntecedentsMedicaux() : "");
        lblMessage.setText("");
        btnSupprimer.setDisable(false);
    }

    private void validerFormulaire() {
        if (tfNom.getText().trim().isEmpty())
            throw new IllegalArgumentException("Le nom est obligatoire.");
        if (tfPrenom.getText().trim().isEmpty())
            throw new IllegalArgumentException("Le prenom est obligatoire.");
        if (tfTelephone.getText().trim().isEmpty())
            throw new IllegalArgumentException("Le telephone est obligatoire.");
        if (dpDateNaissance.getValue() == null)
            throw new IllegalArgumentException("La date de naissance est obligatoire.");
    }

    private void afficherSucces(String msg) {
        lblMessage.setText("✔  " + msg);
        lblMessage.getStyleClass().setAll("msg-succes");
    }

    private void afficherErreur(String msg) {
        lblMessage.setText("✘  " + msg);
        lblMessage.getStyleClass().setAll("msg-erreur");
    }
}