package sn.cheikh.gestion_clinique_medicale.Repository.Implementation;

import sn.cheikh.gestion_clinique_medicale.Repository.IConsultationDAO;
import sn.cheikh.gestion_clinique_medicale.Config.FactoryJPA;
import sn.cheikh.gestion_clinique_medicale.model.Consultation;
import sn.cheikh.gestion_clinique_medicale.model.Patient;
import sn.cheikh.gestion_clinique_medicale.model.Utilisateur;

import javax.persistence.EntityManager;
import java.util.Collections;
import java.util.List;

public abstract class ConsultationDAOImplementation extends GenericDAOImplementation<Consultation, Long>
        implements IConsultationDAO {

    public ConsultationDAOImplementation() {
        super(Consultation.class);
    }

    @Override
    public List<Consultation> findAll() {
        EntityManager em = FactoryJPA.getManager();
        try {
            // Charger sans JOIN FETCH sur facture pour éviter les problèmes
            return em.createQuery(
                            "SELECT c FROM Consultation c " +
                                    "JOIN FETCH c.patient " +
                                    "JOIN FETCH c.medecin " +
                                    "LEFT JOIN FETCH c.rendezVous",
                            Consultation.class)
                    .getResultList();
        } finally {
            if (em.isOpen()) em.close();
        }
    }

    @Override
    public List<Consultation> findByPatient(Patient patient) {
        EntityManager em = FactoryJPA.getManager();
        try {
            return em.createQuery(
                            "SELECT c FROM Consultation c " +
                                    "JOIN FETCH c.patient " +
                                    "JOIN FETCH c.medecin " +
                                    "LEFT JOIN FETCH c.rendezVous " +
                                    "WHERE c.patient = :patient",
                            Consultation.class)
                    .setParameter("patient", patient)
                    .getResultList();
        } finally {
            if (em.isOpen()) em.close();
        }
    }

    @Override
    public List<Consultation> findByMedecin(Utilisateur medecin) {
        EntityManager em = FactoryJPA.getManager();
        try {
            return em.createQuery(
                            "SELECT c FROM Consultation c " +
                                    "JOIN FETCH c.patient " +
                                    "JOIN FETCH c.medecin " +
                                    "LEFT JOIN FETCH c.rendezVous " +
                                    "WHERE c.medecin = :medecin",
                            Consultation.class)
                    .setParameter("medecin", medecin)
                    .getResultList();
        } finally {
            if (em.isOpen()) em.close();
        }
    }

    @Override
    public List<Consultation> findByRendezVousId(Long rendezVousId) {
        EntityManager em = FactoryJPA.getManager();
        try {
            List<Consultation> result = em.createQuery(
                            "SELECT c FROM Consultation c WHERE c.rendezVous.id = :rdvId",
                            Consultation.class)
                    .setParameter("rdvId", rendezVousId)
                    .setMaxResults(1)
                    .getResultList();
            return result.isEmpty() ? null : Collections.singletonList(result.get(0));
        } finally {
            if (em.isOpen()) em.close();
        }
    }
}