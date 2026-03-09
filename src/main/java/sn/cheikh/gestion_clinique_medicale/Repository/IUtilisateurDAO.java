package sn.cheikh.gestion_clinique_medicale.Repository;

import sn.cheikh.gestion_clinique_medicale.model.Utilisateur;
import sn.cheikh.gestion_clinique_medicale.enums.Role;

import java.util.List;
import java.util.Optional;

public interface IUtilisateurDAO extends GenericDAO<Utilisateur, Long> {

    Optional<Utilisateur> findByLogin(String login);
    List<Utilisateur> findByRole(Role role);
}