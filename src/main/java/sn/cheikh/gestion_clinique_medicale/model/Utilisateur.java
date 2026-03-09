package sn.cheikh.gestion_clinique_medicale.model;

import lombok.Data;
import sn.cheikh.gestion_clinique_medicale.enums.Role;

import javax.persistence.*;

@Data
@Entity
@Table(name = "utilisateurs")
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nom", nullable = false)
    private String nom;

    @Column(name = "prenom", nullable = false)
    private String prenom;

    @Column(name = "login", nullable = false, unique = true)
    private String login;

    @Column(name = "mot_de_passe", nullable = false)
    private String motDePasse;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @Column(name = "actif")
    private boolean actif = true;

    public String getNomComplet() {
        return prenom + " " + nom;
    }
}