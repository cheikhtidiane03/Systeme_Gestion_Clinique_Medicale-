package sn.cheikh.gestion_clinique_medicale.Service;

import sn.cheikh.gestion_clinique_medicale.Repository.IRendezVousDAO;
import sn.cheikh.gestion_clinique_medicale.Repository.Implementation.RendezVousDAOImplementation;
import sn.cheikh.gestion_clinique_medicale.enums.StatutRendezVous;
import sn.cheikh.gestion_clinique_medicale.model.Patient;
import sn.cheikh.gestion_clinique_medicale.model.RendezVous;
import sn.cheikh.gestion_clinique_medicale.model.Utilisateur;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class RendezVousService {

    private final IRendezVousDAO rendezVousDAO;

    public RendezVousService() {
        this.rendezVousDAO = new RendezVousDAOImplementation() {
            @Override
            public void delete(RendezVous entity) {

            }

            @Override
            public void deleteById(Long aLong) {

            }
        };
    }

    // Correction ici
    public void planifierRendezVous(LocalDateTime dateHeure, String motif,
                                    Patient patient, Utilisateur medecin) {

        if (dateHeure == null)
            throw new IllegalArgumentException("La date est obligatoire.");

        if (patient == null)
            throw new IllegalArgumentException("Le patient est obligatoire.");

        if (medecin == null)
            throw new IllegalArgumentException("Le médecin est obligatoire.");

        if (dateHeure.isBefore(LocalDateTime.now()))
            throw new IllegalArgumentException("La date doit être dans le futur.");

        if (rendezVousDAO.existsConflict(medecin, dateHeure)) {
            throw new IllegalArgumentException("Ce médecin a déjà un rendez-vous à cet horaire.");
        }

        RendezVous rdv = new RendezVous();
        rdv.setDateHeure(dateHeure);
        rdv.setMotif(motif);
        rdv.setPatient(patient);
        rdv.setMedecin(medecin);
        rdv.setStatut(StatutRendezVous.PROGRAMME);

        rendezVousDAO.save(rdv);
    }

    public void modifierRendezVous(RendezVous rendezVous) {
        if (rendezVous == null)
            throw new IllegalArgumentException("Rendez-vous invalide.");

        rendezVousDAO.update(rendezVous);
    }

    public void annulerRendezVous(Long id) {
        Optional<RendezVous> rdv = rendezVousDAO.findById(id);

        if (rdv.isPresent()) {
            rdv.get().setStatut(StatutRendezVous.ANNULE);
            rendezVousDAO.update(rdv.get());
        }
    }

    public void terminerRendezVous(Long id) {
        Optional<RendezVous> rdv = rendezVousDAO.findById(id);

        if (rdv.isPresent()) {
            rdv.get().setStatut(StatutRendezVous.TERMINE);
            rendezVousDAO.update(rdv.get());
        }
    }

    public List<RendezVous> getRendezVousDuJour() {
        return rendezVousDAO.findByDate(LocalDate.now());
    }

    public List<RendezVous> getRendezVousParMedecin(Utilisateur medecin) {
        return rendezVousDAO.findByMedecin(medecin);
    }

    public List<RendezVous> getTousRendezVous() {
        return rendezVousDAO.findAll();
    }
    // Ajouter cette méthode dans RendezVousService.java

    public List<RendezVous> getRendezVousDisponiblesPourConsultation() {
        try {
            return rendezVousDAO.findAll().stream()
                    .filter(r -> r.getStatut() == StatutRendezVous.PROGRAMME)
                    .filter(r -> r.getConsultation() == null)
                    .filter(r -> r.getDateHeure().isBefore(LocalDateTime.now()) ||
                            r.getDateHeure().isEqual(LocalDateTime.now()))
                    .toList();
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors du chargement des RDV disponibles", e);
        }
    }
}