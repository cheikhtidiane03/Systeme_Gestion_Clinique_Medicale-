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
import sn.cheikh.gestion_clinique_medicale.Service.UtilisateurService;
import sn.cheikh.gestion_clinique_medicale.enums.Role;
import sn.cheikh.gestion_clinique_medicale.model.Utilisateur;

import java.net.URL;
import java.util.ResourceBundle;

public class UtilisateurController implements Initializable {

    @FXML private Label                               lblMessage;
    @FXML private TableView<Utilisateur>              tableView;
    @FXML private TableColumn<Utilisateur,Long>       colId;
    @FXML private TableColumn<Utilisateur,String>     colNom;
    @FXML private TableColumn<Utilisateur,String>     colPrenom;
    @FXML private TableColumn<Utilisateur,String>     colLogin;
    @FXML private TableColumn<Utilisateur,String>     colRole;
    @FXML private TableColumn<Utilisateur,String>     colActif;

    private final UtilisateurService userService = new UtilisateurService();
    private final ObservableList<Utilisateur> liste = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colLogin.setCellValueFactory(new PropertyValueFactory<>("login"));
        colRole.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getRole().name()));
        colActif.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().isActif() ? "✓ Actif" : "✗ Inactif"));

        tableView.setItems(liste);
        tableView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 && tableView.getSelectionModel().getSelectedItem() != null)
                ouvrirModal(tableView.getSelectionModel().getSelectedItem());
        });
        chargerTous();
    }

    @FXML public void nouveau()  { ouvrirModal(null); }

    @FXML public void modifier() {
        Utilisateur u = tableView.getSelectionModel().getSelectedItem();
        if (u == null) { afficherInfo("Sélectionnez un utilisateur à modifier."); return; }
        ouvrirModal(u);
    }

    @FXML public void supprimer() {
        Utilisateur u = tableView.getSelectionModel().getSelectedItem();
        if (u == null) { afficherInfo("Sélectionnez un utilisateur à supprimer."); return; }
        Alert c = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer " + u.getNomComplet() + " ?", ButtonType.YES, ButtonType.NO);
        c.setHeaderText(null);
        c.showAndWait().ifPresent(r -> {
            if (r == ButtonType.YES) {
                try { userService.supprimerUtilisateur(u.getId()); chargerTous(); }
                catch (Exception ex) { afficherErreur(ex.getMessage()); }
            }
        });
    }

    private void ouvrirModal(Utilisateur user) {
        Stage modal = new Stage();
        modal.initModality(Modality.APPLICATION_MODAL);
        modal.setTitle(user == null ? "Nouvel utilisateur" : "Modifier utilisateur");
        modal.setResizable(false);

        TextField    tfNom    = champ("Ex: Diallo");
        TextField    tfPrenom = champ("Ex: Aminata");
        TextField    tfLogin  = champ("Identifiant unique");
        PasswordField pfMdp   = new PasswordField();
        pfMdp.setPromptText(user == null ? "Mot de passe" : "Laisser vide pour ne pas changer");
        pfMdp.getStyleClass().add("form-input");
        ComboBox<Role> cbRole = new ComboBox<>(FXCollections.observableArrayList(Role.values()));
        cbRole.setMaxWidth(Double.MAX_VALUE);
        cbRole.getStyleClass().add("form-input");
        CheckBox cbActif = new CheckBox("Compte actif"); cbActif.setSelected(true);
        Label lblMsg = new Label(); lblMsg.setWrapText(true); lblMsg.setMaxWidth(Double.MAX_VALUE);

        if (user != null) {
            tfNom.setText(user.getNom()); tfPrenom.setText(user.getPrenom());
            tfLogin.setText(user.getLogin()); cbRole.setValue(user.getRole());
            cbActif.setSelected(user.isActif());
        }

        GridPane grid = new GridPane();
        grid.setHgap(16); grid.setVgap(12);
        ColumnConstraints c1 = new ColumnConstraints(); c1.setPercentWidth(50);
        ColumnConstraints c2 = new ColumnConstraints(); c2.setPercentWidth(50);
        grid.getColumnConstraints().addAll(c1, c2);

        grid.add(lbl("Nom *"),          0, 0); grid.add(tfNom,    0, 1);
        grid.add(lbl("Prénom *"),       1, 0); grid.add(tfPrenom, 1, 1);
        grid.add(lbl("Login *"),        0, 2); grid.add(tfLogin,  0, 3);
        grid.add(lbl("Mot de passe"),   1, 2); grid.add(pfMdp,    1, 3);
        grid.add(lbl("Rôle *"),         0, 4); grid.add(cbRole,   0, 5);
        grid.add(cbActif,               1, 5);

        Button btnSave   = new Button(user == null ? "Créer le compte" : "Mettre à jour");
        Button btnCancel = new Button("Annuler");
        btnSave.getStyleClass().add("btn-primary");
        btnCancel.getStyleClass().add("btn-secondary");
        btnSave.setMaxWidth(Double.MAX_VALUE); btnCancel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btnSave, Priority.ALWAYS); HBox.setHgrow(btnCancel, Priority.ALWAYS);
        HBox hBtns = new HBox(10, btnSave, btnCancel);
        hBtns.setPadding(new Insets(8, 0, 0, 0));
        btnCancel.setOnAction(e -> modal.close());

        btnSave.setOnAction(e -> {
            try {
                if (tfNom.getText().trim().isEmpty())    throw new IllegalArgumentException("Le nom est obligatoire.");
                if (tfPrenom.getText().trim().isEmpty()) throw new IllegalArgumentException("Le prénom est obligatoire.");
                if (tfLogin.getText().trim().isEmpty())  throw new IllegalArgumentException("Le login est obligatoire.");
                if (cbRole.getValue() == null)           throw new IllegalArgumentException("Sélectionnez un rôle.");
                if (user == null) {
                    if (pfMdp.getText().trim().isEmpty()) throw new IllegalArgumentException("Le mot de passe est obligatoire.");
                    userService.ajouterUtilisateur(tfNom.getText().trim(), tfPrenom.getText().trim(),
                            tfLogin.getText().trim(), pfMdp.getText().trim(), cbRole.getValue());
                } else {
                    user.setNom(tfNom.getText().trim()); user.setPrenom(tfPrenom.getText().trim());
                    user.setLogin(tfLogin.getText().trim()); user.setRole(cbRole.getValue());
                    user.setActif(cbActif.isSelected());
                    if (!pfMdp.getText().isBlank())
                        user.setMotDePasse(sn.cheikh.gestion_clinique_medicale.Utilitaire.PasswordUtil.hasher(pfMdp.getText()));                    userService.modifierUtilisateur(user);
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
        Label titre = new Label(user == null ? "Nouvel utilisateur" : "Modifier — " + user.getNomComplet());
        titre.setStyle("-fx-font-size:17px; -fx-font-weight:bold; -fx-text-fill:#0F172A;");
        content.getChildren().addAll(titre, new Separator(), grid, lblMsg, hBtns);

        Scene scene = new Scene(content);
        scene.getStylesheets().add(getClass().getResource(
                "/sn/cheikh/gestion_clinique_medicale/css/style.css").toExternalForm());
        modal.setScene(scene);
        modal.showAndWait();
    }

    private void chargerTous() {
        try { liste.setAll(userService.getTousUtilisateurs()); }
        catch (Exception e) { afficherErreur(e.getMessage()); }
    }
    private TextField champ(String p) {
        TextField tf = new TextField(); tf.setPromptText(p);
        tf.getStyleClass().add("form-input"); return tf;
    }
    private Label lbl(String t) { Label l = new Label(t); l.getStyleClass().add("form-label"); return l; }
    private void afficherErreur(String m) { new Alert(Alert.AlertType.ERROR, m, ButtonType.OK).showAndWait(); }
    private void afficherInfo(String m)   { new Alert(Alert.AlertType.INFORMATION, m, ButtonType.OK).showAndWait(); }
}