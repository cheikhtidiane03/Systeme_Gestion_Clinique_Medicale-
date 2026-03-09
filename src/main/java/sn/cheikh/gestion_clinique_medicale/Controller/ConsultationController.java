package sn.cheikh.gestion_clinique_medicale.Controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;
import sn.cheikh.gestion_clinique_medicale.Service.ConsultationService;
import sn.cheikh.gestion_clinique_medicale.Service.PatientService;
import sn.cheikh.gestion_clinique_medicale.Service.RendezVousService;
import sn.cheikh.gestion_clinique_medicale.Service.UtilisateurService;
import sn.cheikh.gestion_clinique_medicale.Utilitaire.PDFGenerator;
import sn.cheikh.gestion_clinique_medicale.enums.Role;
import sn.cheikh.gestion_clinique_medicale.model.Consultation;
import sn.cheikh.gestion_clinique_medicale.model.Patient;
import sn.cheikh.gestion_clinique_medicale.model.RendezVous;
import sn.cheikh.gestion_clinique_medicale.model.SessionUtilisateur;
import sn.cheikh.gestion_clinique_medicale.model.Utilisateur;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class ConsultationController implements Initializable {

    @FXML private ComboBox<Patient>     cbPatient;
    @FXML private ComboBox<Utilisateur> cbMedecin;
    @FXML private ComboBox<RendezVous>  cbRendezVous;
    @FXML private TextArea              taDiagnostic;
    @FXML private TextArea              taObservations;
    @FXML private TextArea              taPrescription;
    @FXML private Label                 lblMessage;

    @FXML private TableView<Consultation>            tableView;
    @FXML private TableColumn<Consultation, Long>    colId;
    @FXML private TableColumn<Consultation, String>  colPatient;
    @FXML private TableColumn<Consultation, String>  colMedecin;
    @FXML private TableColumn<Consultation, String>  colDate;
    @FXML private TableColumn<Consultation, String>  colDiagnostic;

    private final ConsultationService consultationService = new ConsultationService();
    private final PatientService      patientService      = new PatientService();
    private final UtilisateurService  utilisateurService  = new UtilisateurService();
    private final RendezVousService   rdvService          = new RendezVousService();

    private Consultation consultationSelectionnee = null;
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        cbPatient.setItems(FXCollections.observableArrayList(patientService.getTousPatients()));
        cbPatient.setConverter(new StringConverter<>() {
            public String toString(Patient p)   { return p == null ? "" : p.getNomComplet(); }
            public Patient fromString(String s) { return null; }
        });
        cbPatient.valueProperty().addListener((obs, old, patient) -> {
            if (patient != null) {
                cbRendezVous.setItems(FXCollections.observableArrayList(
                        rdvService.getTousRendezVous().stream()
                                .filter(r -> r.getPatient().getId().equals(patient.getId()))
                                .toList()));
            } else {
                cbRendezVous.setItems(FXCollections.emptyObservableList());
            }
        });

        cbMedecin.setItems(FXCollections.observableArrayList(utilisateurService.getMedecins()));
        cbMedecin.setConverter(new StringConverter<>() {
            public String toString(Utilisateur u)   { return u == null ? "" : "Dr. " + u.getNomComplet(); }
            public Utilisateur fromString(String s) { return null; }
        });
        Utilisateur connecte = SessionUtilisateur.getInstance().getUtilisateurConnecte();
        if (connecte != null && connecte.getRole() == Role.MEDECIN) {
            cbMedecin.setValue(connecte);
            cbMedecin.setDisable(true);
        }

        cbRendezVous.setConverter(new StringConverter<>() {
            public String toString(RendezVous r) {
                return r == null ? "" : r.getDateHeure().format(fmt) + " — " + r.getMotif();
            }
            public RendezVous fromString(String s) { return null; }
        });

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colPatient.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getPatient().getNomComplet()));
        colMedecin.setCellValueFactory(d ->
                new SimpleStringProperty("Dr. " + d.getValue().getMedecin().getNomComplet()));
        colDate.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getDateConsultation() != null
                        ? d.getValue().getDateConsultation().format(fmt) : ""));
        colDiagnostic.setCellValueFactory(new PropertyValueFactory<>("diagnostic"));

        tableView.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, sel) -> remplirFormulaire(sel));

        chargerConsultations();
    }

    private void chargerConsultations() {
        Utilisateur connecte = SessionUtilisateur.getInstance().getUtilisateurConnecte();
        if (connecte != null && connecte.getRole() == Role.MEDECIN) {
            tableView.setItems(FXCollections.observableArrayList(
                    consultationService.getConsultationsParMedecin(connecte)));
        } else {
            tableView.setItems(FXCollections.observableArrayList(
                    consultationService.getToutesConsultations()));
        }
    }

    @FXML
    private void sauvegarder() {
        try {
            validerFormulaire();

            if (consultationSelectionnee == null) {
                consultationService.enregistrerConsultation(
                        cbPatient.getValue(),
                        cbMedecin.getValue(),
                        cbRendezVous.getValue(),
                        taDiagnostic.getText().trim(),
                        taObservations.getText().trim(),
                        taPrescription.getText().trim()
                );
                afficherSucces("Consultation enregistree avec succes.");
            } else {
                consultationSelectionnee.setPatient(cbPatient.getValue());
                consultationSelectionnee.setMedecin(cbMedecin.getValue());
                consultationSelectionnee.setDiagnostic(taDiagnostic.getText().trim());
                consultationSelectionnee.setObservations(taObservations.getText().trim());
                consultationSelectionnee.setPrescription(taPrescription.getText().trim());
                consultationService.modifierConsultation(consultationSelectionnee);
                afficherSucces("Consultation modifiee avec succes.");
            }

            nouveau();
            chargerConsultations();

        } catch (IllegalArgumentException e) {
            afficherErreur(e.getMessage());
        } catch (Exception e) {
            afficherErreur("Erreur : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void genererOrdonnance() {
        if (consultationSelectionnee == null) {
            afficherErreur("Selectionnez d'abord une consultation dans le tableau.");
            return;
        }
        try {
            String chemin = "ordonnance_" + consultationSelectionnee.getId() + ".pdf";
            PDFGenerator.genererOrdonnance(consultationSelectionnee, chemin);
            afficherSucces("Ordonnance generee : " + chemin);
        } catch (Exception e) {
            afficherErreur("Erreur PDF : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void nouveau() {
        consultationSelectionnee = null;
        cbPatient.setValue(null);
        cbRendezVous.setItems(FXCollections.emptyObservableList());
        cbRendezVous.setValue(null);
        taDiagnostic.clear();
        taObservations.clear();
        taPrescription.clear();
        lblMessage.setText("");
        tableView.getSelectionModel().clearSelection();
        Utilisateur connecte = SessionUtilisateur.getInstance().getUtilisateurConnecte();
        if (connecte == null || connecte.getRole() != Role.MEDECIN) {
            cbMedecin.setValue(null);
        }
    }

    private void remplirFormulaire(Consultation c) {
        if (c == null) return;
        consultationSelectionnee = c;
        cbPatient.setValue(c.getPatient());
        cbMedecin.setValue(c.getMedecin());
        taDiagnostic.setText(c.getDiagnostic()     != null ? c.getDiagnostic()     : "");
        taObservations.setText(c.getObservations() != null ? c.getObservations()   : "");
        taPrescription.setText(c.getPrescription() != null ? c.getPrescription()   : "");
        lblMessage.setText("");
    }

    private void validerFormulaire() {
        if (cbPatient.getValue() == null)
            throw new IllegalArgumentException("Selectionnez un patient.");
        if (cbMedecin.getValue() == null)
            throw new IllegalArgumentException("Selectionnez un medecin.");
        if (taDiagnostic.getText().trim().isEmpty())
            throw new IllegalArgumentException("Le diagnostic est obligatoire.");
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