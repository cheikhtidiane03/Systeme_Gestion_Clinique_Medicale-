package sn.cheikh.gestion_clinique_medicale.Controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import sn.cheikh.gestion_clinique_medicale.Service.UtilisateurService;
import sn.cheikh.gestion_clinique_medicale.Utilitaire.PasswordUtil;
import sn.cheikh.gestion_clinique_medicale.enums.Role;
import sn.cheikh.gestion_clinique_medicale.model.Utilisateur;

import java.net.URL;
import java.util.ResourceBundle;

public class UtilisateurController implements Initializable {

    @FXML private TextField      tfNom;
    @FXML private TextField      tfPrenom;
    @FXML private TextField      tfLogin;
    @FXML private PasswordField  pfMotDePasse;
    @FXML private ComboBox<Role> cbRole;
    @FXML private CheckBox       cbActif;
    @FXML private Label          lblMessage;

    @FXML private TableView<Utilisateur>            tableView;
    @FXML private TableColumn<Utilisateur, Long>    colId;
    @FXML private TableColumn<Utilisateur, String>  colNom;
    @FXML private TableColumn<Utilisateur, String>  colPrenom;
    @FXML private TableColumn<Utilisateur, String>  colLogin;
    @FXML private TableColumn<Utilisateur, String>  colRole;
    @FXML private TableColumn<Utilisateur, String>  colActif;

    private final UtilisateurService utilisateurService = new UtilisateurService();
    private Utilisateur utilisateurSelectionne = null;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cbRole.setItems(FXCollections.observableArrayList(Role.values()));

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colLogin.setCellValueFactory(new PropertyValueFactory<>("login"));
        colRole.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getRole().name()));
        colActif.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().isActif() ? "Oui" : "Non"));

        tableView.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, sel) -> remplirFormulaire(sel));

        chargerUtilisateurs();
    }

    private void chargerUtilisateurs() {
        tableView.setItems(FXCollections.observableArrayList(utilisateurService.getTousUtilisateurs()));
    }

    @FXML
    private void sauvegarder() {
        try {
            validerFormulaire();

            if (utilisateurSelectionne == null) {
                // AJOUT
                if (pfMotDePasse.getText().trim().isEmpty())
                    throw new IllegalArgumentException("Le mot de passe est obligatoire pour un nouvel utilisateur.");
                utilisateurService.ajouterUtilisateur(
                        tfNom.getText().trim(),
                        tfPrenom.getText().trim(),
                        tfLogin.getText().trim(),
                        pfMotDePasse.getText(),
                        cbRole.getValue()
                );
                afficherSucces("Utilisateur cree avec succes.");
            } else {
                // MODIFICATION
                utilisateurSelectionne.setNom(tfNom.getText().trim());
                utilisateurSelectionne.setPrenom(tfPrenom.getText().trim());
                utilisateurSelectionne.setLogin(tfLogin.getText().trim());
                utilisateurSelectionne.setRole(cbRole.getValue());
                utilisateurSelectionne.setActif(cbActif.isSelected());
                if (!pfMotDePasse.getText().trim().isEmpty()) {
                    utilisateurSelectionne.setMotDePasse(PasswordUtil.hasher(pfMotDePasse.getText()));
                }
                utilisateurService.modifierUtilisateur(utilisateurSelectionne);
                afficherSucces("Utilisateur modifie avec succes.");
            }

            nouveau();
            chargerUtilisateurs();

        } catch (IllegalArgumentException e) {
            afficherErreur(e.getMessage());
        } catch (Exception e) {
            afficherErreur("Erreur : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void supprimer() {
        if (utilisateurSelectionne == null) {
            afficherErreur("Selectionnez un utilisateur a supprimer.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer l'utilisateur « " + utilisateurSelectionne.getLogin() + " » ?",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                try {
                    utilisateurService.supprimerUtilisateur(utilisateurSelectionne.getId());
                    afficherSucces("Utilisateur supprime.");
                    nouveau();
                    chargerUtilisateurs();
                } catch (Exception e) {
                    afficherErreur("Impossible de supprimer : " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void nouveau() {
        utilisateurSelectionne = null;
        tfNom.clear();
        tfPrenom.clear();
        tfLogin.clear();
        pfMotDePasse.clear();
        cbRole.setValue(null);
        cbActif.setSelected(true);
        lblMessage.setText("");
        tableView.getSelectionModel().clearSelection();
    }

    private void remplirFormulaire(Utilisateur u) {
        if (u == null) return;
        utilisateurSelectionne = u;
        tfNom.setText(u.getNom());
        tfPrenom.setText(u.getPrenom());
        tfLogin.setText(u.getLogin());
        pfMotDePasse.clear();
        cbRole.setValue(u.getRole());
        cbActif.setSelected(u.isActif());
        lblMessage.setText("");
    }

    private void validerFormulaire() {
        if (tfNom.getText().trim().isEmpty())
            throw new IllegalArgumentException("Le nom est obligatoire.");
        if (tfPrenom.getText().trim().isEmpty())
            throw new IllegalArgumentException("Le prenom est obligatoire.");
        if (tfLogin.getText().trim().isEmpty())
            throw new IllegalArgumentException("Le login est obligatoire.");
        if (cbRole.getValue() == null)
            throw new IllegalArgumentException("Le role est obligatoire.");
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