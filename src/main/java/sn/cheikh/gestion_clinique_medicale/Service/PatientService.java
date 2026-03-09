package sn.cheikh.gestion_clinique_medicale.Service;

import sn.cheikh.gestion_clinique_medicale.Repository.IPatientDAO;
import sn.cheikh.gestion_clinique_medicale.Repository.Implementation.PatientDAOImplementation;
import sn.cheikh.gestion_clinique_medicale.enums.Sexe;
import sn.cheikh.gestion_clinique_medicale.model.Patient;
import sn.cheikh.gestion_clinique_medicale.Utilitaire.ValidationUtil;

import java.time.LocalDate;
import java.util.List;
public class PatientService {

    private final IPatientDAO patientDAO;

    public PatientService() {
        this.patientDAO = new PatientDAOImplementation() {
            @Override
            public void delete(Patient entity) {

            }

            @Override
            public void deleteById(Long aLong) {

            }
        };
    }
    public void ajouterPatient(String nom, String prenom, LocalDate dateNaissance,
                               Sexe sexe, String telephone, String adresse,
                               String groupeSanguin, String antecedentsMedicaux) {
        if (nom == null || nom.isBlank())     throw new IllegalArgumentException("Le nom est obligatoire.");
        if (prenom == null || prenom.isBlank()) throw new IllegalArgumentException("Le prenom est obligatoire.");
        if (telephone == null || telephone.isBlank()) throw new IllegalArgumentException("Le telephone est obligatoire.");
        if (!ValidationUtil.telephoneValide(telephone)) throw new IllegalArgumentException("Format téléphone invalide (ex: 784061514).");
        if (dateNaissance != null && dateNaissance.isAfter(LocalDate.now())) throw new IllegalArgumentException("La date de naissance ne peut pas être dans le futur.");

        Patient patient = new Patient();
        patient.setNom(nom.trim());
        patient.setPrenom(prenom.trim());
        patient.setDateNaissance(dateNaissance);
        patient.setSexe(sexe);
        patient.setTelephone(telephone.trim());
        patient.setAdresse(adresse);
        patient.setGroupeSanguin(groupeSanguin);
        patient.setAntecedentsMedicaux(antecedentsMedicaux);

        patientDAO.save(patient);
    }

    public void modifierPatient(Patient patient) {
        if (patient == null) throw new IllegalArgumentException("Patient invalide.");
        patientDAO.update(patient);
    }

    public void supprimerPatient(Long id) {
        patientDAO.findById(id).ifPresent(patientDAO::delete);
    }


    public List<Patient> getTousPatients() {
        return patientDAO.findAll();
    }

    public List<Patient> rechercherPatient(String keyword) {
        if (keyword == null || keyword.isBlank()) return patientDAO.findAll();
        return patientDAO.findByNomOrPrenom(keyword.trim());
    }
}