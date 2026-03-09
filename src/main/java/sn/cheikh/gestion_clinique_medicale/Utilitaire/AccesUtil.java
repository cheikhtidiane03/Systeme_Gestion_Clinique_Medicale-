package sn.cheikh.gestion_clinique_medicale.Utilitaire;

import sn.cheikh.gestion_clinique_medicale.enums.Role;
import sn.cheikh.gestion_clinique_medicale.model.SessionUtilisateur;

/**
 * Centralise le contrôle d'accès par rôle (principe SRP).
 * Les Controllers l'utilisent pour vérifier les permissions.
 */
public class AccesUtil {

    private AccesUtil() {}

    public static void verifierAcces(Role... rolesAutorises) {
        SessionUtilisateur session = SessionUtilisateur.getInstance();
        if (!session.estConnecte()) {
            throw new SecurityException("Accès refusé : vous n'êtes pas connecté.");
        }
        Role roleActuel = session.getRoleUtilisateur();
        for (Role role : rolesAutorises) {
            if (roleActuel == role) return;
        }
        throw new SecurityException("Accès refusé ");
    }

    public static boolean estAutorise(Role... rolesAutorises) {
        try {
            verifierAcces(rolesAutorises);
            return true;
        } catch (SecurityException e) {
            return false;
        }
    }
}