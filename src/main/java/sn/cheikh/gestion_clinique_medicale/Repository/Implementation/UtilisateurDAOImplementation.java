package sn.cheikh.gestion_clinique_medicale.Repository.Implementation;

import sn.cheikh.gestion_clinique_medicale.Repository.IUtilisateurDAO;
import sn.cheikh.gestion_clinique_medicale.Config.FactoryJPA;
import sn.cheikh.gestion_clinique_medicale.enums.Role;
import sn.cheikh.gestion_clinique_medicale.model.Utilisateur;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.util.List;
import java.util.Optional;

public abstract class UtilisateurDAOImplementation extends GenericDAOImplementation<Utilisateur, Long>
        implements IUtilisateurDAO {

    public UtilisateurDAOImplementation() { super(Utilisateur.class); }

    @Override
    public Optional<Utilisateur> findByLogin(String login) {
        EntityManager em = FactoryJPA.getManager();
        try {
            Utilisateur u = em.createQuery(
                            "SELECT u FROM Utilisateur u WHERE u.login = :login", Utilisateur.class)
                    .setParameter("login", login)
                    .getSingleResult();
            return Optional.of(u);
        } catch (NoResultException e) {
            return Optional.empty();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Utilisateur> findByRole(Role role) {
        EntityManager em = FactoryJPA.getManager();
        try {
            return em.createQuery(
                            "SELECT u FROM Utilisateur u WHERE u.role = :role", Utilisateur.class)
                    .setParameter("role", role)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}