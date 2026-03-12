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
            public void delete(Long entity) {

            }

            @Override
            public void delete(Facture entity) {}

            @Override
            public void deleteById(Long aLong) {}
        };
    }

    public void creerFacture(Consultation consultation, double montantTotal, String modePaiement) {

        // ── Validations métier ──
        if (consultation == null)
            throw new IllegalArgumentException("La consultation est obligatoire.");
        if (montantTotal <= 0)
            throw new IllegalArgumentException("Le montant doit être supérieur à 0.");
        if (modePaiement == null || modePaiement.isBlank())
            throw new IllegalArgumentException("Le mode de paiement est obligatoire.");

        // ── Vérifier que cette consultation n'a pas déjà une facture ──
        boolean dejaFacturee = factureDAO.findAll().stream()
                .anyMatch(f -> f.getConsultation() != null
                        && f.getConsultation().getId().equals(consultation.getId()));

        if (dejaFacturee) {
            throw new IllegalArgumentException(
                    "Cette consultation a déjà une facture enregistrée.\n" +
                            "Sélectionnez une autre consultation."
            );
        }

        // ── Création de la facture ──
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

    // Vérifie si une consultation est déjà facturée (utile dans le controller)
    public boolean estDejaFacturee(Consultation consultation) {
        if (consultation == null) return false;
        return factureDAO.findAll().stream()
                .anyMatch(f -> f.getConsultation() != null
                        && f.getConsultation().getId().equals(consultation.getId()));
    }
}