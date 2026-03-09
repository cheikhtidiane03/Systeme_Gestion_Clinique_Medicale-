package sn.cheikh.gestion_clinique_medicale.Repository;

import sn.cheikh.gestion_clinique_medicale.model.Consultation;
import sn.cheikh.gestion_clinique_medicale.model.Patient;
import sn.cheikh.gestion_clinique_medicale.model.Utilisateur;

import java.util.List;

public interface IConsultationDAO extends GenericDAO<Consultation, Long> {

    List<Consultation> findByMedecin(Utilisateur medecin);
    List<Consultation> findByPatient(Patient patient);
    List<Consultation> findByRendezVousId(Long patient);


}