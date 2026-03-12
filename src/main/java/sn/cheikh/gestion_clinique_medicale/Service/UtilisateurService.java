package sn.cheikh.gestion_clinique_medicale.Service;

import sn.cheikh.gestion_clinique_medicale.Repository.IUtilisateurDAO;
import sn.cheikh.gestion_clinique_medicale.Repository.Implementation.UtilisateurDAOImplementation;
import sn.cheikh.gestion_clinique_medicale.Utilitaire.PasswordUtil;
import sn.cheikh.gestion_clinique_medicale.enums.Role;
import sn.cheikh.gestion_clinique_medicale.model.Utilisateur;

import java.util.List;
import java.util.Optional;

public class UtilisateurService {

    private final IUtilisateurDAO utilisateurDAO;

    public UtilisateurService() {
        // Plus d'overrides vides — delete et deleteById sont dans GenericDAOImplementation
        this.utilisateurDAO = new UtilisateurDAOImplementation() {
            @Override
            public void delete(Long entity) {

            }
        };
    }

    public Optional<Utilisateur> connecter(String login, String motDePasse) {
        if (login == null || login.isBlank())      throw new IllegalArgumentException("Login obligatoire.");
        if (motDePasse == null || motDePasse.isBlank()) throw new IllegalArgumentException("Mot de passe obligatoire.");
        Optional<Utilisateur> utilisateur = utilisateurDAO.findByLogin(login.trim());
        if (utilisateur.isPresent() && PasswordUtil.verifier(motDePasse, utilisateur.get().getMotDePasse()))
            return utilisateur;
        return Optional.empty();
    }

    public void ajouterUtilisateur(String nom, String prenom, String login, String motDePasse, Role role) {
        if (nom == null || nom.isBlank())           throw new IllegalArgumentException("Le nom est obligatoire.");
        if (prenom == null || prenom.isBlank())     throw new IllegalArgumentException("Le prénom est obligatoire.");
        if (login == null || login.isBlank())       throw new IllegalArgumentException("Le login est obligatoire.");
        if (motDePasse == null || motDePasse.isBlank()) throw new IllegalArgumentException("Le mot de passe est obligatoire.");
        if (role == null)                           throw new IllegalArgumentException("Le rôle est obligatoire.");
        if (utilisateurDAO.findByLogin(login.trim()).isPresent())
            throw new IllegalArgumentException("Ce login existe déjà.");

        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setNom(nom.trim());
        utilisateur.setPrenom(prenom.trim());
        utilisateur.setLogin(login.trim());
        utilisateur.setMotDePasse(PasswordUtil.hasher(motDePasse));
        utilisateur.setRole(role);
        utilisateur.setActif(true);
        utilisateurDAO.save(utilisateur);
    }

    public void modifierUtilisateur(Utilisateur utilisateur) {
        if (utilisateur == null || utilisateur.getId() == null)
            throw new IllegalArgumentException("Utilisateur invalide.");
        utilisateurDAO.update(utilisateur);
    }

    public void supprimerUtilisateur(Long id) {
        if (id == null) throw new IllegalArgumentException("ID utilisateur invalide.");
        utilisateurDAO.deleteById(id);
    }

    public Optional<Utilisateur> findById(Long id) {
        if (id == null) return Optional.empty();
        return utilisateurDAO.findById(id);
    }

    public List<Utilisateur> getTousUtilisateurs() { return utilisateurDAO.findAll(); }
    public List<Utilisateur> getMedecins()          { return utilisateurDAO.findByRole(Role.MEDECIN); }

    public void activerUtilisateur(Long id) {
        utilisateurDAO.findById(id).ifPresent(u -> { u.setActif(true);  utilisateurDAO.update(u); });
    }

    public void desactiverUtilisateur(Long id) {
        utilisateurDAO.findById(id).ifPresent(u -> { u.setActif(false); utilisateurDAO.update(u); });
    }

    public long getNombreUtilisateurs() { return utilisateurDAO.findAll().size(); }
    public long getNombreMedecins()     { return utilisateurDAO.findByRole(Role.MEDECIN).size(); }
}