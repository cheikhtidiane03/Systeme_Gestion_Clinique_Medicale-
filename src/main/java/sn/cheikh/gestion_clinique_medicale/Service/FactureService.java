package sn.cheikh.gestion_clinique_medicale.Service;

import sn.cheikh.gestion_clinique_medicale.Repository.IFactureDAO;
import sn.cheikh.gestion_clinique_medicale.Repository.Implementation.FactureDAOImplementation;
import sn.cheikh.gestion_clinique_medicale.enums.StatutPaiement;
import sn.cheikh.gestion_clinique_medicale.model.Consultation;
import sn.cheikh.gestion_clinique_medicale.model.Facture;

import java.util.List;
import java.util.Optional;

public class FactureService {

    private final IFactureDAO factureDAO;

    public FactureService() {
        this.factureDAO = new FactureDAOImplementation() {
            @Override
            public void delete(Facture entity) {

            }

            @Override
            public void deleteById(Long aLong) {

            }
        };
    }

    /**
     * Crée et sauvegarde une nouvelle facture.
     * Utilisé par FactureController via creerFacture(consultation, montant, mode).
     */
    public void creerFacture(Consultation consultation, double montantTotal, String modePaiement) {
        if (consultation == null)
            throw new IllegalArgumentException("La consultation est obligatoire.");
        if (montantTotal <= 0)
            throw new IllegalArgumentException("Le montant doit être supérieur à 0.");
        if (modePaiement == null || modePaiement.isBlank())
            throw new IllegalArgumentException("Le mode de paiement est obligatoire.");

        Facture facture = new Facture();
        facture.setConsultation(consultation);
        facture.setMontantTotal(montantTotal);
        facture.setModePaiement(modePaiement.trim());
        facture.setStatutPaiement(StatutPaiement.NON_PAYE);

        factureDAO.save(facture);
    }

    public void marquerCommePaye(Long id) {
        Optional<Facture> facture = factureDAO.findById(id);
        if (facture.isPresent()) {
            facture.get().setStatutPaiement(StatutPaiement.PAYE);
            factureDAO.update(facture.get());
        }
    }

    public List<Facture> getToutesFactures() {
        return factureDAO.findAll();
    }

    public List<Facture> getFacturesNonPayees() {
        return factureDAO.findByStatut(StatutPaiement.NON_PAYE);
    }
}