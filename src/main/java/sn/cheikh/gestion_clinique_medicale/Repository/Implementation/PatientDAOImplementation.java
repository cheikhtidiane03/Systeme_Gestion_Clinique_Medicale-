package sn.cheikh.gestion_clinique_medicale.Repository.Implementation;

import sn.cheikh.gestion_clinique_medicale.Repository.IPatientDAO;
import sn.cheikh.gestion_clinique_medicale.Config.FactoryJPA;
import sn.cheikh.gestion_clinique_medicale.model.Patient;

import javax.persistence.EntityManager;
import java.util.List;

public abstract class PatientDAOImplementation extends GenericDAOImplementation<Patient, Long>
        implements IPatientDAO {

    public PatientDAOImplementation() { super(Patient.class); }

    public List<Patient> findByNom(String nom) {
        EntityManager em = FactoryJPA.getManager();
        try {
            return em.createQuery(
                            "SELECT p FROM Patient p WHERE LOWER(p.nom) = LOWER(:nom)", Patient.class)
                    .setParameter("nom", nom)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Patient> findByNomOrPrenom(String keyword) {
        EntityManager em = FactoryJPA.getManager();
        try {
            String kw = "%" + keyword.toLowerCase() + "%";
            return em.createQuery(
                            "SELECT p FROM Patient p WHERE LOWER(p.nom) LIKE :kw OR LOWER(p.prenom) LIKE :kw",
                            Patient.class)
                    .setParameter("kw", kw)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}