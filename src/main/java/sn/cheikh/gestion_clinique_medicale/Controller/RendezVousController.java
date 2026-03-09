package sn.cheikh.gestion_clinique_medicale.Controller;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class RendezVousController implements Initializable {

    @FXML private ComboBox<Patient>     cbPatient;
    @FXML private ComboBox<Utilisateur> cbMedecin;
    @FXML private DatePicker            dpDate;
    @FXML private ComboBox<String>      cbHeure;
    @FXML private TextField             tfMotif;
    @FXML private Label                 lblMessage;

    @FXML private TableView<RendezVous>            tableView;
    @FXML private TableColumn<RendezVous, Long>    colId;
    @FXML private TableColumn<RendezVous, String>  colPatient;
    @FXML private TableColumn<RendezVous, String>  colMedecin;
    @FXML private TableColumn<RendezVous, String>  colDateHeure;
    @FXML private TableColumn<RendezVous, String>  colMotif;
    @FXML private TableColumn<RendezVous, String>  colStatut;

    private final RendezVousService   rdvService         = new RendezVousService();
    private final PatientService      patientService     = new PatientService();
    private final UtilisateurService  utilisateurService = new UtilisateurService();

    private RendezVous rdvSelectionne = null;
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        cbPatient.setItems(FXCollections.observableArrayList(patientService.getTousPatients()));
        cbPatient.setConverter(new javafx.util.StringConverter<>() {
            public String toString(Patient p)   { return p == null ? "" : p.getNomComplet(); }
            public Patient fromString(String s) { return null; }
        });

        cbMedecin.setItems(FXCollections.observableArrayList(utilisateurService.getMedecins()));
        cbMedecin.setConverter(new javafx.util.StringConverter<>() {
            public String toString(Utilisateur u)   { return u == null ? "" : "Dr. " + u.getNomComplet(); }
            public Utilisateur fromString(String s) { return null; }
        });

        // Si médecin connecté → verrouiller sur lui-même
        Utilisateur connecte = SessionUtilisateur.getInstance().getUtilisateurConnecte();
        if (connecte != null && connecte.getRole() == Role.MEDECIN) {
            cbMedecin.setValue(connecte);
            cbMedecin.setDisable(true);
        }

        cbHeure.setItems(FXCollections.observableArrayList(genererCreneaux()));
        cbHeure.setEditable(true);

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colPatient.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getPatient().getNomComplet()));
        colMedecin.setCellValueFactory(d ->
                new SimpleStringProperty("Dr. " + d.getValue().getMedecin().getNomComplet()));
        colDateHeure.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getDateHeure().format(fmt)));
        colMotif.setCellValueFactory(new PropertyValueFactory<>("motif"));
        colStatut.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getStatut().name()));

        tableView.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(RendezVous item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) setStyle("");
                else if (item.getStatut() == StatutRendezVous.ANNULE)
                    setStyle("-fx-background-color: #FFEBEE;");
                else if (item.getStatut() == StatutRendezVous.TERMINE)
                    setStyle("-fx-background-color: #E8F5E9;");
                else setStyle("");
            }
        });

        tableView.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, sel) -> remplirFormulaire(sel));

        Platform.runLater(this::chargerTous);
    }

    private List<String> genererCreneaux() {
        List<String> creneaux = new ArrayList<>();
        for (int h = 7; h <= 19; h++) {
            creneaux.add(String.format("%02d:00", h));
            if (h < 19) creneaux.add(String.format("%02d:30", h));
        }
        return creneaux;
    }

    @FXML
    public void chargerTous() {
        try {
            Utilisateur connecte = SessionUtilisateur.getInstance().getUtilisateurConnecte();
            List<RendezVous> liste;

            // ── Filtrage selon le rôle ──
            if (connecte != null && connecte.getRole() == Role.MEDECIN) {
                // Le médecin voit seulement ses propres RDV
                liste = rdvService.getRendezVousParMedecin(connecte);
            } else {
                // Admin et réceptionniste voient tout
                liste = rdvService.getTousRendezVous();
            }

            tableView.setItems(FXCollections.observableArrayList(liste));
            tableView.refresh();
        } catch (Exception e) {
            afficherErreur("Erreur chargement : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void afficherDuJour() {
        try {
            Utilisateur connecte = SessionUtilisateur.getInstance().getUtilisateurConnecte();
            List<RendezVous> liste = rdvService.getRendezVousDuJour();

            // Si médecin → filtrer sur ses RDV du jour uniquement
            if (connecte != null && connecte.getRole() == Role.MEDECIN) {
                final Long medecinId = connecte.getId();
                liste = liste.stream()
                        .filter(r -> r.getMedecin() != null
                                && r.getMedecin().getId().equals(medecinId))
                        .collect(Collectors.toList());
            }

            tableView.setItems(FXCollections.observableArrayList(liste));
            tableView.refresh();
        } catch (Exception e) {
            afficherErreur("Erreur chargement : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void sauvegarder() {
        try {
            validerFormulaire();
            LocalDateTime dateHeure = parseDateTime();

            if (rdvSelectionne == null) {
                rdvService.planifierRendezVous(
                        dateHeure,
                        tfMotif.getText().trim(),
                        cbPatient.getValue(),
                        cbMedecin.getValue()
                );
                afficherSucces("Rendez-vous planifie avec succes.");
            } else {
                rdvSelectionne.setPatient(cbPatient.getValue());
                rdvSelectionne.setMedecin(cbMedecin.getValue());
                rdvSelectionne.setDateHeure(dateHeure);
                rdvSelectionne.setMotif(tfMotif.getText().trim());
                rdvService.modifierRendezVous(rdvSelectionne);
                afficherSucces("Rendez-vous modifie avec succes.");
            }

            nouveau();
            Platform.runLater(this::chargerTous);

        } catch (IllegalArgumentException e) {
            afficherErreur(e.getMessage());
        } catch (Exception e) {
            afficherErreur("Erreur : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void annuler() {
        if (rdvSelectionne == null) {
            afficherErreur("Selectionnez un rendez-vous a annuler.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Annuler ce rendez-vous ?", ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                rdvService.annulerRendezVous(rdvSelectionne.getId());
                afficherSucces("Rendez-vous annule.");
                nouveau();
                Platform.runLater(this::chargerTous);
            }
        });
    }

    @FXML
    private void nouveau() {
        rdvSelectionne = null;
        cbPatient.setValue(null);
        dpDate.setValue(null);
        cbHeure.setValue(null);
        tfMotif.clear();
        lblMessage.setText("");
        tableView.getSelectionModel().clearSelection();
        Utilisateur connecte = SessionUtilisateur.getInstance().getUtilisateurConnecte();
        if (connecte == null || connecte.getRole() != Role.MEDECIN) {
            cbMedecin.setValue(null);
        }
    }

    private void remplirFormulaire(RendezVous r) {
        if (r == null) return;
        rdvSelectionne = r;
        cbPatient.setValue(r.getPatient());
        cbMedecin.setValue(r.getMedecin());
        dpDate.setValue(r.getDateHeure().toLocalDate());
        cbHeure.setValue(r.getDateHeure().toLocalTime()
                .format(DateTimeFormatter.ofPattern("HH:mm")));
        tfMotif.setText(r.getMotif() != null ? r.getMotif() : "");
        lblMessage.setText("");
    }

    private void validerFormulaire() {
        if (cbPatient.getValue() == null)
            throw new IllegalArgumentException("Selectionnez un patient.");
        if (cbMedecin.getValue() == null)
            throw new IllegalArgumentException("Selectionnez un medecin.");
        if (dpDate.getValue() == null)
            throw new IllegalArgumentException("Selectionnez une date.");
        if (cbHeure.getValue() == null || cbHeure.getValue().trim().isEmpty())
            throw new IllegalArgumentException("Selectionnez une heure.");
    }

    private LocalDateTime parseDateTime() {
        String[] parts = cbHeure.getValue().trim().split(":");
        if (parts.length < 2)
            throw new IllegalArgumentException("Format heure invalide. Exemple : 09:30");
        try {
            int h = Integer.parseInt(parts[0].trim());
            int m = Integer.parseInt(parts[1].trim());
            if (h < 0 || h > 23 || m < 0 || m > 59)
                throw new IllegalArgumentException("Heure invalide (00:00 a 23:59).");
            return dpDate.getValue().atTime(h, m);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Format heure invalide. Exemple : 09:30");
        }
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