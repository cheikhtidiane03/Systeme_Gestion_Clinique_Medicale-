package sn.cheikh.gestion_clinique_medicale;

import sn.cheikh.gestion_clinique_medicale.Service.UtilisateurService;
import sn.cheikh.gestion_clinique_medicale.Service.PatientService;
import sn.cheikh.gestion_clinique_medicale.enums.Role;
import sn.cheikh.gestion_clinique_medicale.enums.Sexe;

public class DataInitializer {

    public static void main(String[] args) {
        UtilisateurService utilisateurService = new UtilisateurService();
        PatientService patientService = new PatientService();

        System.out.println("Insertion des utilisateurs...");

        // ── ADMIN ──────────────────────────────────────────────
        utilisateurService.ajouterUtilisateur(
                "Ba", "Cheikh Tidiane",
                "admin",        // login
                "passer",     // mot de passe
                Role.ADMIN
        );

        // ── MÉDECIN ────────────────────────────────────────────
        utilisateurService.ajouterUtilisateur(
                "Ndiaye", "Fatou",
                "medecin",      // login
                "passer",   // mot de passe
                Role.MEDECIN
        );

        // ── RÉCEPTIONNISTE ─────────────────────────────────────
        utilisateurService.ajouterUtilisateur(
                "Adja", "Gueye",
                "reception",    // login
                "passer", // mot de passe
                Role.RECEPTIONNISTE
        );

        System.out.println("Utilisateurs inseres avec succes !");

        // ── PATIENTS DE TEST ───────────────────────────────────
        System.out.println("Insertion des patients...");

        patientService.ajouterPatient(
                "Sow", "Aminata",
                java.time.LocalDate.of(1990, 5, 15),
                Sexe.FEMININ,
                "771234567",
                "Dakar, Plateau",
                "A+",
                "Diabète type 2"
        );

        patientService.ajouterPatient(
                "Mbaye", "Ousmane",
                java.time.LocalDate.of(1985, 3, 22),
                Sexe.MASCULIN,
                "781234567",
                "Dakar, Parcelles",
                "O+",
                "Hypertension"
        );

        patientService.ajouterPatient(
                "Diop", "Aissatou",
                java.time.LocalDate.of(2000, 11, 8),
                Sexe.FEMININ,
                "761234567",
                "Thiès",
                "B+",
                "Aucun"
        );

        System.out.println("Patients inseres avec succes !");
        System.out.println("=== INITIALISATION TERMINEE ===");
        System.out.println("Comptes de connexion :");
        System.out.println("  ADMIN        -> login: admin      / mdp: passer");
        System.out.println("  MEDECIN      -> login: medecin    / mdp: passer");
        System.out.println("  RECEPTIONNISTE -> login: reception / mdp: passer");

        // Fermer JPA proprement
        sn.cheikh.gestion_clinique_medicale.Config.FactoryJPA.close();
    }
}