package sn.cheikh.gestion_clinique_medicale.Repository;

import sn.cheikh.gestion_clinique_medicale.enums.StatutPaiement;
import sn.cheikh.gestion_clinique_medicale.model.Facture;

import java.util.List;

public interface IFactureDAO extends GenericDAO<Facture, Long> {

    List<Facture> findByStatut(StatutPaiement statut);
    List<Facture> findAll();
}