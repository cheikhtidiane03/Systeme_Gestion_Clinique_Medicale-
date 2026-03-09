package sn.cheikh.gestion_clinique_medicale.Repository;

import sn.cheikh.gestion_clinique_medicale.model.Patient;

import java.util.List;

public interface IPatientDAO extends GenericDAO<Patient, Long> {

    List<Patient> findByNomOrPrenom(String keyword);
}