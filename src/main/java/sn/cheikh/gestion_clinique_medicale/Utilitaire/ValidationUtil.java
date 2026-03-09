package sn.cheikh.gestion_clinique_medicale.Utilitaire;

/**
 * Centralise toutes les validations (principe DRY).
 * Chaque méthode a une seule responsabilité (principe SRP).
 */
public class ValidationUtil {

    private ValidationUtil() {}

    public static boolean telephoneValide(String telephone) {
        if (telephone == null) return false;
        return telephone.matches("^(7[0-9]{8})$");
    }

    public static boolean champValide(String valeur) {
        return valeur != null && !valeur.isBlank();
    }

    public static boolean montantValide(double montant) {
        return montant > 0;
    }

    public static boolean heureValide(String heure) {
        if (heure == null) return false;
        return heure.matches("^([01]\\d|2[0-3]):[0-5]\\d$");
    }
}