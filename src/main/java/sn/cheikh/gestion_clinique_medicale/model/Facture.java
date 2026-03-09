package sn.cheikh.gestion_clinique_medicale.model;

import lombok.Data;
import sn.cheikh.gestion_clinique_medicale.enums.StatutPaiement;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "factures")
public class Facture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date_facture", nullable = false)
    private LocalDateTime dateFacture = LocalDateTime.now();

    @Column(name = "montant_total", nullable = false)
    private double montantTotal;

    @Column(name = "mode_paiement")
    private String modePaiement;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut_paiement", nullable = false)
    private StatutPaiement statutPaiement = StatutPaiement.NON_PAYE;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consultation_id", nullable = false, unique = true) // Important : unique=true
    private Consultation consultation;
}