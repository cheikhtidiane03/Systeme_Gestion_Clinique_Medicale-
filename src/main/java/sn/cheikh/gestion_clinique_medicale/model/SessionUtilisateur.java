package sn.cheikh.gestion_clinique_medicale.model;

import sn.cheikh.gestion_clinique_medicale.enums.Role;

public class SessionUtilisateur {

    private static SessionUtilisateur instance;
    private Utilisateur utilisateurConnecte;

    private SessionUtilisateur() {}

    public static SessionUtilisateur getInstance() {
        if (instance == null) instance = new SessionUtilisateur();
        return instance;
    }

    public void ouvrirSession(Utilisateur utilisateur) {
        this.utilisateurConnecte = utilisateur;
    }

    public void fermerSession() {
        this.utilisateurConnecte = null;
    }

    public boolean estConnecte() {
        return utilisateurConnecte != null;
    }

    public Utilisateur getUtilisateurConnecte() {
        return utilisateurConnecte;
    }

    public Role getRoleUtilisateur() {
        return estConnecte() ? utilisateurConnecte.getRole() : null;
    }

    public String getNomUtilisateur() {
        return estConnecte() ? utilisateurConnecte.getNomComplet() : "Inconnu";
    }

    public boolean estAdmin() {
        return estConnecte() && utilisateurConnecte.getRole() == Role.ADMIN;
    }

    public boolean estMedecin() {
        return estConnecte() && utilisateurConnecte.getRole() == Role.MEDECIN;
    }

    public boolean estReceptionniste() {
        return estConnecte() && utilisateurConnecte.getRole() == Role.RECEPTIONNISTE;
    }
}