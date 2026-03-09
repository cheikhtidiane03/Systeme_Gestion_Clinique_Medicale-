package sn.cheikh.gestion_clinique_medicale.Service;

import sn.cheikh.gestion_clinique_medicale.Config.FactoryJPA;
import sn.cheikh.gestion_clinique_medicale.Repository.IConsultationDAO;
import sn.cheikh.gestion_clinique_medicale.Repository.Implementation.ConsultationDAOImplementation;
import sn.cheikh.gestion_clinique_medicale.enums.StatutRendezVous;
import sn.cheikh.gestion_clinique_medicale.model.Consultation;
import sn.cheikh.gestion_clinique_medicale.model.Patient;
import sn.cheikh.gestion_clinique_medicale.model.RendezVous;
import sn.cheikh.gestion_clinique_medicale.model.Utilisateur;

import javax.persistence.EntityManager;
import java.util.List;

public class ConsultationService {

    private final IConsultationDAO consultationDAO;

    public ConsultationService() {
        this.consultationDAO = new ConsultationDAOImplementation() {
            @Override
            public void delete(Consultation entity) {

            }

            @Override
            public void deleteById(Long aLong) {

            }
        };
    }

    public void enregistrerConsultation(Patient patient, Utilisateur medecin,
                                        RendezVous rendezVous, String diagnostic,
                                        String observations, String prescription) {

        // Validations
        if (patient == null) throw new IllegalArgumentException("Le patient est obligatoire");
        if (medecin == null) throw new IllegalArgumentException("Le médecin est obligatoire");
        if (diagnostic == null || diagnostic.trim().isEmpty())
            throw new IllegalArgumentException("Le diagnostic est obligatoire");

        EntityManager em = FactoryJPA.getManager();
        try {
            em.getTransaction().begin();

            Consultation consultation = new Consultation();
            consultation.setPatient(patient);
            consultation.setMedecin(medecin);
            consultation.setRendezVous(rendezVous);
            consultation.setDiagnostic(diagnostic.trim());
            consultation.setObservations(observations != null ? observations.trim() : null);
            consultation.setPrescription(prescription != null ? prescription.trim() : null);

            // Si lié à un rendez-vous, mettre à jour son statut
            if (rendezVous != null) {
                RendezVous managedRdv = em.merge(rendezVous);
                managedRdv.setStatut(StatutRendezVous.TERMINE);
                consultation.setRendezVous(managedRdv);
            }

            consultationDAO.save(consultation);
            em.getTransaction().commit();

        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Erreur lors de l'enregistrement de la consultation: " + e.getMessage(), e);
        } finally {
            if (em.isOpen()) em.close();
        }
    }

    public void modifierConsultation(Consultation consultation) {
        if (consultation == null || consultation.getId() == null) {
            throw new IllegalArgumentException("Consultation invalide");
        }

        EntityManager em = FactoryJPA.getManager();
        try {
            em.getTransaction().begin();
            consultationDAO.update(consultation);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Erreur lors de la modification de la consultation: " + e.getMessage(), e);
        } finally {
            if (em.isOpen()) em.close();
        }
    }

    public void supprimerConsultation(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID de consultation invalide");
        }

        EntityManager em = FactoryJPA.getManager();
        try {
            em.getTransaction().begin();
            consultationDAO.delete(id);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Erreur lors de la suppression de la consultation: " + e.getMessage(), e);
        } finally {
            if (em.isOpen()) em.close();
        }
    }

    public List<Consultation> getToutesConsultations() {
        try {
            return consultationDAO.findAll();
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors du chargement des consultations: " + e.getMessage(), e);
        }
    }

    public List<Consultation> getConsultationsParMedecin(Utilisateur medecin) {
        if (medecin == null || medecin.getId() == null) {
            return List.of();
        }
        try {
            return consultationDAO.findByMedecin(medecin);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors du chargement des consultations du médecin: " + e.getMessage(), e);
        }
    }

    public List<Consultation> getConsultationsParPatient(Patient patient) {
        if (patient == null || patient.getId() == null) {
            return List.of();
        }
        try {
            return consultationDAO.findByPatient(patient);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors du chargement des consultations du patient: " + e.getMessage(), e);
        }
    }

    public Consultation getConsultationParRendezVous(Long rendezVousId) {
        if (rendezVousId == null) {
            return null;
        }
        try {
            return (Consultation) consultationDAO.findByRendezVousId(rendezVousId);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la recherche de consultation par rendez-vous: " + e.getMessage(), e);
        }
    }

    public long getNombreConsultations() {
        try {
            return consultationDAO.findAll().size();
        } catch (Exception e) {
            return 0;
        }
    }

    public long getNombreConsultationsAujourdhui() {
        EntityManager em = FactoryJPA.getManager();
        try {
            return em.createQuery(
                            "SELECT COUNT(c) FROM Consultation c WHERE DATE(c.dateConsultation) = CURRENT_DATE",
                            Long.class)
                    .getSingleResult();
        } catch (Exception e) {
            return 0;
        } finally {
            if (em.isOpen()) em.close();
        }
    }
}