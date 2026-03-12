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
import sn.cheikh.gestion_clinique_medicale.Service.ConsultationService;
import sn.cheikh.gestion_clinique_medicale.Service.FactureService;
import sn.cheikh.gestion_clinique_medicale.enums.StatutPaiement;
import sn.cheikh.gestion_clinique_medicale.model.Consultation;
import sn.cheikh.gestion_clinique_medicale.model.Facture;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class FactureController implements Initializable {

    @FXML private Label                           lblMessage;
    @FXML private TableView<Facture>              tableView;
    @FXML private TableColumn<Facture,Long>       colId;
    @FXML private TableColumn<Facture,String>     colPatient;
    @FXML private TableColumn<Facture,String>     colDate;
    @FXML private TableColumn<Facture,Double>     colMontant;
    @FXML private TableColumn<Facture,String>     colMode;
    @FXML private TableColumn<Facture,String>     colStatut;

    private final FactureService      factureService = new FactureService();
    private final ConsultationService consultService = new ConsultationService();
    private final ObservableList<Facture> liste = FXCollections.observableArrayList();
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colPatient.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getConsultation().getPatient().getNomComplet()));
        colDate.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getDateFacture() != null
                        ? d.getValue().getDateFacture().format(fmt) : ""));
        colMontant.setCellValueFactory(new PropertyValueFactory<>("montantTotal"));
        colMode.setCellValueFactory(new PropertyValueFactory<>("modePaiement"));
        colStatut.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getStatutPaiement().name()));

        tableView.setItems(liste);
        tableView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 && tableView.getSelectionModel().getSelectedItem() != null)
                ouvrirModal(tableView.getSelectionModel().getSelectedItem());
        });
        chargerTout();
    }

    @FXML public void afficherTout()     { chargerTout(); }
    @FXML public void filtrerNonPaye()   {
        try { liste.setAll(factureService.getFacturesNonPayees()); }
        catch (Exception e) { afficherErreur(e.getMessage()); }
    }
    @FXML public void nouveau()  { ouvrirModal(null); }
    @FXML public void modifier() {
        Facture f = tableView.getSelectionModel().getSelectedItem();
        if (f == null) { afficherInfo("Sélectionnez une facture."); return; }
        ouvrirModal(f);
    }

    @FXML public void marquerPaye() {
        Facture f = tableView.getSelectionModel().getSelectedItem();
        if (f == null) { afficherInfo("Sélectionnez une facture."); return; }
        try {
            factureService.marquerCommePaye(f.getId());
            chargerTout();
            msg("Facture marquée comme payée.", "msg-succes");
        } catch (Exception e) { afficherErreur(e.getMessage()); }
    }

    @FXML public void imprimerFacture() {
        Facture f = tableView.getSelectionModel().getSelectedItem();
        if (f == null) { afficherInfo("Sélectionnez une facture."); return; }
        try {
            String chemin = sn.cheikh.gestion_clinique_medicale.Utilitaire.PDFGenerator.genererFacture(f);
            msg("✓ Facture générée : " + chemin, "msg-succes");
        } catch (Exception e) { afficherErreur(e.getMessage()); }
    }

    private void ouvrirModal(Facture facture) {
        Stage modal = new Stage();
        modal.initModality(Modality.APPLICATION_MODAL);
        modal.setTitle(facture == null ? "Nouvelle facture" : "Modifier facture");
        modal.setResizable(false);

        ComboBox<Consultation> cbConsult = new ComboBox<>(
                FXCollections.observableArrayList(consultService.getToutesConsultations()));
        cbConsult.setConverter(new javafx.util.StringConverter<>() {
            public String toString(Consultation c) {
                return c == null ? "" : c.getPatient().getNomComplet() + " — " +
                        (c.getDateConsultation() != null
                                ? c.getDateConsultation().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "");
            }
            public Consultation fromString(String s) { return null; }
        });
        cbConsult.setMaxWidth(Double.MAX_VALUE);

        TextField tfMontant = new TextField();
        tfMontant.setPromptText("Ex: 15000");
        tfMontant.getStyleClass().add("form-input");

        ComboBox<String> cbMode = new ComboBox<>(
                FXCollections.observableArrayList("Espèces", "Carte bancaire", "Virement", "Chèque", "Mobile Money"));
        cbMode.setMaxWidth(Double.MAX_VALUE);
        cbMode.setPromptText("Mode de paiement");

        Label lblMsg = new Label(); lblMsg.setWrapText(true); lblMsg.setMaxWidth(Double.MAX_VALUE);

        if (facture != null) {
            cbConsult.setValue(facture.getConsultation());
            tfMontant.setText(String.valueOf(facture.getMontantTotal()));
            cbMode.setValue(facture.getModePaiement());
        }

        VBox form = new VBox(12);
        form.getChildren().addAll(
                lbl("Consultation *"), cbConsult,
                lbl("Montant (FCFA) *"), tfMontant,
                lbl("Mode de paiement *"), cbMode
        );

        Button btnSave   = new Button(facture == null ? "Générer la facture" : "Mettre à jour");
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
                if (cbConsult.getValue() == null) throw new IllegalArgumentException("Sélectionnez une consultation.");
                if (tfMontant.getText().trim().isEmpty()) throw new IllegalArgumentException("Entrez le montant.");
                if (cbMode.getValue() == null) throw new IllegalArgumentException("Choisissez un mode de paiement.");
                double montant = Double.parseDouble(tfMontant.getText().trim());
                if (facture == null) {
                    factureService.creerFacture(cbConsult.getValue(), montant, cbMode.getValue());
                } else {
                    facture.setConsultation(cbConsult.getValue());
                    facture.setMontantTotal(montant);
                    facture.setModePaiement(cbMode.getValue());
                }
                chargerTout();
                modal.close();
            } catch (NumberFormatException ex) {
                lblMsg.setText("Le montant doit être un nombre valide.");
                lblMsg.getStyleClass().setAll("msg-erreur");
            } catch (Exception ex) {
                lblMsg.setText(ex.getMessage());
                lblMsg.getStyleClass().setAll("msg-erreur");
            }
        });

        VBox content = new VBox(16);
        content.setPadding(new Insets(28));
        content.setStyle("-fx-background-color:white;");
        content.setPrefWidth(480);
        Label titre = new Label(facture == null ? "Nouvelle facture" : "Modifier facture");
        titre.setStyle("-fx-font-size:17px; -fx-font-weight:bold; -fx-text-fill:#0F172A;");
        content.getChildren().addAll(titre, new Separator(), form, lblMsg, hBtns);

        Scene scene = new Scene(content);
        scene.getStylesheets().add(getClass().getResource(
                "/sn/cheikh/gestion_clinique_medicale/css/style.css").toExternalForm());
        modal.setScene(scene);
        modal.showAndWait();
    }

    private void chargerTout() {
        try { liste.setAll(factureService.getToutesFactures()); }
        catch (Exception e) { afficherErreur(e.getMessage()); }
    }
    private Label lbl(String t) { Label l = new Label(t); l.getStyleClass().add("form-label"); return l; }
    private void msg(String m, String cls) {
        if (lblMessage != null) { lblMessage.setText(m); lblMessage.getStyleClass().setAll(cls); }
    }
    private void afficherErreur(String m) { new Alert(Alert.AlertType.ERROR, m, ButtonType.OK).showAndWait(); }
    private void afficherInfo(String m)   { new Alert(Alert.AlertType.INFORMATION, m, ButtonType.OK).showAndWait(); }
}