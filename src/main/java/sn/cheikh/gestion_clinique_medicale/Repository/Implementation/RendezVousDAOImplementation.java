package sn.cheikh.gestion_clinique_medicale.Repository.Implementation;

import sn.cheikh.gestion_clinique_medicale.Config.FactoryJPA;
import sn.cheikh.gestion_clinique_medicale.Repository.IRendezVousDAO;
import sn.cheikh.gestion_clinique_medicale.enums.StatutRendezVous;
import sn.cheikh.gestion_clinique_medicale.model.RendezVous;
import sn.cheikh.gestion_clinique_medicale.model.Utilisateur;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public abstract class RendezVousDAOImplementation extends GenericDAOImplementation<RendezVous, Long>
        implements IRendezVousDAO {

    public RendezVousDAOImplementation() {
        super(RendezVous.class);
    }

    @Override
    public List<RendezVous> findAll() {
        EntityManager em = FactoryJPA.getManager();
        try {
            return em.createQuery(
                            "SELECT r FROM RendezVous r " +
                                    "JOIN FETCH r.patient " +
                                    "JOIN FETCH r.medecin",
                            RendezVous.class)
                    .getResultList();
        } finally {
            if (em.isOpen()) em.close();
        }
    }

    @Override
    public List<RendezVous> findByDate(LocalDate date) {
        EntityManager em = FactoryJPA.getManager();
        try {
            LocalDateTime debut = date.atStartOfDay();
            LocalDateTime fin   = date.plusDays(1).atStartOfDay();

            return em.createQuery(
                            "SELECT r FROM RendezVous r " +
                                    "JOIN FETCH r.patient " +
                                    "JOIN FETCH r.medecin " +
                                    "WHERE r.dateHeure >= :debut AND r.dateHeure < :fin " +
                                    "ORDER BY r.dateHeure",
                            RendezVous.class)
                    .setParameter("debut", debut)
                    .setParameter("fin",   fin)
                    .getResultList();
        } finally {
            if (em.isOpen()) em.close();
        }
    }

    @Override
    public List<RendezVous> findByMedecin(Utilisateur medecin) {
        EntityManager em = FactoryJPA.getManager();
        try {
            return em.createQuery(
                            "SELECT r FROM RendezVous r " +
                                    "JOIN FETCH r.patient " +
                                    "JOIN FETCH r.medecin " +
                                    "WHERE r.medecin = :medecin " +
                                    "ORDER BY r.dateHeure",
                            RendezVous.class)
                    .setParameter("medecin", medecin)
                    .getResultList();
        } finally {
            if (em.isOpen()) em.close();
        }
    }

    @Override
    public boolean existsConflict(Utilisateur medecin, LocalDateTime dateHeure) {
        EntityManager em = FactoryJPA.getManager();
        try {
            Long count = em.createQuery(
                            "SELECT COUNT(r) FROM RendezVous r " +
                                    "WHERE r.medecin = :medecin " +
                                    "AND r.dateHeure = :dateHeure " +
                                    "AND r.statut != :statutAnnule",
                            Long.class)
                    .setParameter("medecin", medecin)
                    .setParameter("dateHeure", dateHeure)
                    .setParameter("statutAnnule", StatutRendezVous.ANNULE)
                    .getSingleResult();
            return count > 0;
        } finally {
            if (em.isOpen()) em.close();
        }
    }

    // Nouvelle méthode : trouver les rendez-vous sans consultation
    public List<RendezVous> findRendezVousSansConsultation() {
        EntityManager em = FactoryJPA.getManager();
        try {
            return em.createQuery(
                            "SELECT r FROM RendezVous r " +
                                    "WHERE r.consultation IS NULL " +
                                    "AND r.statut = :statut " +
                                    "ORDER BY r.dateHeure",
                            RendezVous.class)
                    .setParameter("statut", StatutRendezVous.PROGRAMME)
                    .getResultList();
        } finally {
            if (em.isOpen()) em.close();
        }
    }
}