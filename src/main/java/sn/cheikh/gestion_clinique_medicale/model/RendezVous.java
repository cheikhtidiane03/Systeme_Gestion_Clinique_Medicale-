package sn.cheikh.gestion_clinique_medicale.model;

import lombok.Data;
import lombok.ToString;
import sn.cheikh.gestion_clinique_medicale.enums.StatutRendezVous;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "rendez_vous")
@ToString(exclude = {"consultation"}) // Évite les boucles infinies
public class RendezVous {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date_heure", nullable = false)
    private LocalDateTime dateHeure;

    @Column(name = "motif")
    private String motif;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false)
    private StatutRendezVous statut = StatutRendezVous.PROGRAMME;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medecin_id", nullable = false)
    private Utilisateur medecin;

    @OneToOne(mappedBy = "rendezVous", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = true)
    private Consultation consultation;
}