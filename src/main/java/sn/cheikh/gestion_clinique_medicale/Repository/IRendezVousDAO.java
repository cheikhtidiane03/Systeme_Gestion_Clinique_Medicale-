package sn.cheikh.gestion_clinique_medicale.Repository;

import sn.cheikh.gestion_clinique_medicale.model.RendezVous;
import sn.cheikh.gestion_clinique_medicale.model.Utilisateur;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface IRendezVousDAO extends GenericDAO<RendezVous, Long> {

    List<RendezVous> findByDate(LocalDate date);
    List<RendezVous> findByMedecin(Utilisateur medecin);
    boolean existsConflict(Utilisateur medecin, LocalDateTime dateHeure);
}