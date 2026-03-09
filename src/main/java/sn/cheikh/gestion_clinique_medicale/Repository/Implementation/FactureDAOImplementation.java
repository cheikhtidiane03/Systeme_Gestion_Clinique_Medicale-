package sn.cheikh.gestion_clinique_medicale.Repository.Implementation;

import sn.cheikh.gestion_clinique_medicale.Repository.IFactureDAO;
import sn.cheikh.gestion_clinique_medicale.Config.FactoryJPA;
import sn.cheikh.gestion_clinique_medicale.enums.StatutPaiement;
import sn.cheikh.gestion_clinique_medicale.model.Facture;

import javax.persistence.EntityManager;
import java.util.List;

public abstract class FactureDAOImplementation extends GenericDAOImplementation<Facture, Long>
        implements IFactureDAO {

    public FactureDAOImplementation() { super(Facture.class); }

    @Override
    public List<Facture> findAll() {
        EntityManager em = FactoryJPA.getManager();
        try {
            return em.createQuery(
                            "SELECT DISTINCT f FROM Facture f " +
                                    "JOIN FETCH f.consultation c " +
                                    "JOIN FETCH c.patient " +
                                    "JOIN FETCH c.medecin",
                            Facture.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Facture> findByStatut(StatutPaiement statut) {
        EntityManager em = FactoryJPA.getManager();
        try {
            return em.createQuery(
                            "SELECT DISTINCT f FROM Facture f " +
                                    "JOIN FETCH f.consultation c " +
                                    "JOIN FETCH c.patient " +
                                    "JOIN FETCH c.medecin " +
                                    "WHERE f.statutPaiement = :statut",
                            Facture.class)
                    .setParameter("statut", statut)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}