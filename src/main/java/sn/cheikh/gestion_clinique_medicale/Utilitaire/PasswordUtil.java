package sn.cheikh.gestion_clinique_medicale.Utilitaire;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {

    private PasswordUtil() {}

    public static String hasher(String motDePasse) {
        return BCrypt.hashpw(motDePasse, BCrypt.gensalt());
    }

    public static boolean verifier(String motDePasse, String hash) {
        return BCrypt.checkpw(motDePasse, hash);
    }
}