package sn.cheikh.gestion_clinique_medicale.Controller;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;
import sn.cheikh.gestion_clinique_medicale.Service.ConsultationService;
import sn.cheikh.gestion_clinique_medicale.Service.FactureService;
import sn.cheikh.gestion_clinique_medicale.Utilitaire.PDFGenerator;
import sn.cheikh.gestion_clinique_medicale.enums.StatutPaiement;
import sn.cheikh.gestion_clinique_medicale.model.Consultation;
import sn.cheikh.gestion_clinique_medicale.model.Facture;

import java.io.File;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class FactureController implements Initializable {

    @FXML private ComboBox<Consultation> cbConsultation;
    @FXML private TextField              tfMontant;
    @FXML private ComboBox<String>       cbModePaiement;
    @FXML private Label                  lblMessage;

    @FXML private TableView<Facture>            tableView;
    @FXML private TableColumn<Facture, Long>    colId;
    @FXML private TableColumn<Facture, String>  colPatient;
    @FXML private TableColumn<Facture, String>  colDate;
    @FXML private TableColumn<Facture, String>  colMontant;
    @FXML private TableColumn<Facture, String>  colMode;
    @FXML private TableColumn<Facture, String>  colStatut;

    private final FactureService      factureService      = new FactureService();
    private final ConsultationService consultationService = new ConsultationService();

    private Facture factureSelectionnee = null;
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        // ComboBox Consultation — affiche patient + date
        cbConsultation.setConverter(new StringConverter<>() {
            public String toString(Consultation c) {
                if (c == null) return "";
                String patient = c.getPatient() != null ? c.getPatient().getNomComplet() : "?";
                String date    = c.getDateConsultation() != null
                        ? c.getDateConsultation().format(fmt) : "?";
                return patient + " — " + date;
            }
            public Consultation fromString(String s) { return null; }
        });

        // ComboBox Mode de paiement
        cbModePaiement.setItems(FXCollections.observableArrayList(
                "Especes", "Carte bancaire", "Orange Money", "Wave", "Free Money", "Cheque"
        ));
        cbModePaiement.setEditable(true);

        // Colonnes tableau
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colPatient.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getConsultation() != null
                        ? d.getValue().getConsultation().getPatient().getNomComplet() : "—"));
        colDate.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getDateFacture() != null
                        ? d.getValue().getDateFacture().format(fmt) : ""));
        colMontant.setCellValueFactory(d -> new SimpleStringProperty(
                String.format("%,.0f FCFA", d.getValue().getMontantTotal())));
        colMode.setCellValueFactory(new PropertyValueFactory<>("modePaiement"));
        colStatut.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getStatutPaiement().name()));

        // Coloration selon statut paiement
        tableView.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Facture item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) setStyle("");
                else if (item.getStatutPaiement() == StatutPaiement.PAYE)
                    setStyle("-fx-background-color: #E8F5E9;");
                else
                    setStyle("-fx-background-color: #FFEBEE;");
            }
        });

        tableView.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, sel) -> remplirFormulaire(sel));

        // Charger après rendu
        Platform.runLater(this::chargerDonnees);
    }

    private void chargerDonnees() {
        try {
            // Charger consultations pour le ComboBox
            cbConsultation.setItems(FXCollections.observableArrayList(
                    consultationService.getToutesConsultations()));

            // Charger factures dans le tableau
            tableView.setItems(FXCollections.observableArrayList(
                    factureService.getToutesFactures()));
            tableView.refresh();
        } catch (Exception e) {
            afficherErreur("Erreur chargement : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void afficherTout() {
        try {
            tableView.setItems(FXCollections.observableArrayList(
                    factureService.getToutesFactures()));
            tableView.refresh();
        } catch (Exception e) {
            afficherErreur("Erreur : " + e.getMessage());
        }
    }

    @FXML
    private void filtrerNonPaye() {
        try {
            tableView.setItems(FXCollections.observableArrayList(
                    factureService.getFacturesNonPayees()));
            tableView.refresh();
        } catch (Exception e) {
            afficherErreur("Erreur : " + e.getMessage());
        }
    }

    @FXML
    private void genererFacture() {
        try {
            if (cbConsultation.getValue() == null)
                throw new IllegalArgumentException("Selectionnez une consultation.");
            if (tfMontant.getText().trim().isEmpty())
                throw new IllegalArgumentException("Le montant est obligatoire.");

            String modePaiement = cbModePaiement.getValue() != null
                    ? cbModePaiement.getValue().trim() : "";
            if (modePaiement.isEmpty())
                throw new IllegalArgumentException("Selectionnez un mode de paiement.");

            double montant;
            try {
                montant = Double.parseDouble(tfMontant.getText().trim().replace(",", "."));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Le montant doit etre un nombre valide.");
            }
            if (montant <= 0)
                throw new IllegalArgumentException("Le montant doit etre positif.");

            factureService.creerFacture(
                    cbConsultation.getValue(),
                    montant,
                    modePaiement
            );

            afficherSucces("Facture generee avec succes.");
            nouveau();
            Platform.runLater(this::afficherTout);

        } catch (IllegalArgumentException e) {
            afficherErreur(e.getMessage());
        } catch (Exception e) {
            afficherErreur("Erreur : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void marquerPaye() {
        if (factureSelectionnee == null) {
            afficherErreur("Selectionnez une facture.");
            return;
        }
        try {
            factureService.marquerCommePaye(factureSelectionnee.getId());
            afficherSucces("Facture marquee comme payee.");
            nouveau();
            Platform.runLater(this::afficherTout);
        } catch (Exception e) {
            afficherErreur("Erreur : " + e.getMessage());
        }
    }

    @FXML
    private void imprimerFacture() {
        if (factureSelectionnee == null) {
            afficherErreur("Selectionnez une facture a imprimer.");
            return;
        }
        try {
            // Chemin absolu dans le dossier utilisateur pour trouver facilement le PDF
            String chemin = System.getProperty("user.home") + File.separator
                    + "facture_" + factureSelectionnee.getId() + ".pdf";
            PDFGenerator.genererFacture(factureSelectionnee, chemin);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("PDF généré");
            alert.setHeaderText(null);
            alert.setContentText("PDF enregistré dans :\n" + chemin);
            alert.showAndWait();

        } catch (Exception e) {
            afficherErreur("Erreur PDF : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void nouveau() {
        factureSelectionnee = null;
        cbConsultation.setValue(null);
        tfMontant.clear();
        cbModePaiement.setValue(null);
        lblMessage.setText("");
        tableView.getSelectionModel().clearSelection();
    }

    private void remplirFormulaire(Facture f) {
        if (f == null) return;
        factureSelectionnee = f;
        cbConsultation.setValue(f.getConsultation());
        tfMontant.setText(String.valueOf(f.getMontantTotal()));
        cbModePaiement.setValue(f.getModePaiement() != null ? f.getModePaiement() : "");
        lblMessage.setText("");
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