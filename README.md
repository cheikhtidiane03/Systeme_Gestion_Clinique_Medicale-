# Système de Gestion d'une Clinique Médicale

**Module :** Génie Logiciel  
**Niveau :** Licence 3 — Génie Logiciel  
**Étudiant :** Cheikh  
**Année académique :** 2025 / 2026


## 1. Description du projet

Ce projet est une **application desktop** de gestion d'une clinique médicale privée,
développée dans le cadre du module de Génie Logiciel (L3).  
Elle prend en charge la gestion des patients, des rendez-vous, des consultations,
de la facturation et des utilisateurs selon trois profils métier distincts.

L'application repose sur une architecture en couches stricte (Présentation, Service,
Repository, Modèle, Utilitaire) et respecte les principes SOLID et DRY.

---

## 2. Objectifs pédagogiques couverts

| Objectif | Statut |
|---|---|
| Conception orientée objet structurée | ✅ |
| Application des principes SOLID | ✅ |
| Respect du principe DRY | ✅ |
| Interface graphique avec JavaFX | ✅ |
| Persistance des données avec JPA / Hibernate | ✅ |
| Authentification sécurisée (mot de passe hashé BCrypt) | ✅ |
| Génération de documents PDF dynamiques (iText) | ✅ |
| Diagramme de classes UML cohérent | ✅ |


## 3. Fonctionnalités implémentées

### 3.1 Authentification
- Interface de connexion avec validation des champs
- Gestion de trois rôles : **ADMIN**, **MEDECIN**, **RECEPTIONNISTE**
- Mots de passe stockés sous forme hashée avec **BCrypt**
- Refus d'accès en cas d'identifiants incorrects
- Session utilisateur gérée par la classe **SessionUtilisateur** (Singleton)
- Redirection automatique vers le dashboard selon le rôle connecté

### 3.2 Gestion des patients
- Ajouter, modifier, supprimer un patient
- Rechercher un patient par nom
- Afficher la liste complète des patients
- Champs : identifiant, nom, prénom, date de naissance, sexe, téléphone, adresse, groupe sanguin, antécédents médicaux

### 3.3 Gestion des rendez-vous
- Planifier, modifier, annuler un rendez-vous
- Afficher les rendez-vous du jour
- Filtrer les rendez-vous par médecin *(+2 bonus)*
- Statuts gérés : `PROGRAMME`, `ANNULE`, `TERMINE`
- Coloration des lignes du tableau selon le statut
- **Médecin** : vue en lecture seule (ses propres rendez-vous uniquement)
- **Admin / Réceptionniste** : accès complet à tous les rendez-vous

### 3.4 Gestion des consultations
- Enregistrer une consultation liée à un patient, un médecin et un rendez-vous
- Saisir : diagnostic, observations, prescription
- **Médecin** : vue en lecture seule + génération d'ordonnance PDF
- **Admin** : accès complet

### 3.5 Gestion de la facturation
- Générer une facture après une consultation
- Renseigner le montant total et le mode de paiement
- Gérer les statuts : `PAYE`, `NON_PAYE`
- Marquer une facture comme payée
- Consulter l'historique complet des factures
- Filtrer les factures impayées

### 3.6 Gestion des utilisateurs *(Admin uniquement)*
- Ajouter, modifier, supprimer des comptes utilisateurs
- Attribuer les rôles : ADMIN, MEDECIN, RECEPTIONNISTE
- Activer / désactiver les comptes

### 3.7 Tableaux de bord personnalisés
- **Admin** : statistiques globales (patients, RDV du jour, consultations, factures impayées)
- **Médecin** : ses RDV du jour, ses consultations, ses patients
- **Réceptionniste** : RDV du jour, patients enregistrés, factures impayées

---

## 4. Architecture logicielle

L'application respecte une **architecture en 5 couches** :

```
┌─────────────────────────────────────────────┐
│       Couche Présentation (JavaFX)          │
│   Controllers + Views FXML + CSS            │
├─────────────────────────────────────────────┤
│          Couche Service                     │
│     Logique métier et règles fonctionnelles │
├─────────────────────────────────────────────┤
│         Couche Repository (DAO)             │
│   Interfaces + Implémentations JPA/Hibernate│
├─────────────────────────────────────────────┤
│           Couche Modèle                     │
│       Entités JPA + Énumérations            │
├─────────────────────────────────────────────┤
│          Couche Utilitaire                  │
│  PasswordUtil, Navigation, PDFGenerator,    │
│  ValidationUtil, AccesUtil                  │
└─────────────────────────────────────────────┘
```

### Pattern DAO générique

```java
public interface GenericDAO<T, ID> {
    T save(T entity);
    T update(T entity);
    void delete(ID id);
    Optional<T> findById(ID id);
    List<T> findAll();
}
```


Chaque entité possède une interface DAO spécialisée (`IPatientDAO`, `IRendezVousDAO`, etc.)
et une implémentation concrète (`PatientDAOImplementation`, etc.), toutes héritant de `GenericDAOImplementation<T>`.

---



## 6. Technologies utilisées

| Technologie | Version | Rôle 

| Java | 21 | Langage principal |
| JavaFX | 21.0.6 | Interface graphique desktop |
| JPA (javax.persistence) | 2.2 | API de persistance |
| Hibernate | 5.6.15.Final | Implémentation JPA (ORM) |
| MySQL | 8.0 | Base de données relationnelle |
| Maven | 3.x | Gestion des dépendances et build |
| iText | 5.5.13.5 | Génération de documents PDF |
| jBCrypt | 0.4 | Hachage sécurisé des mots de passe |
| Lombok | 1.18.42 | Réduction du code boilerplate |


## 7. Prérequis

- **JDK 21** ou supérieur
- **Maven 3.6+**
- **MySQL 8.0+** démarré et accessible
- IDE Java (IntelliJ IDEA)


### Étape 5 — Configurer `persistence.xml`

Fichier : `src/main/resources/META-INF/persistence.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<persistence xmlns="http://xmlns.jcp.org/xml/ns/persistence" version="2.2">
    <persistence-unit name="clinique" transaction-type="RESOURCE_LOCAL">
        <provider>org.hibernate.jpa.HibernatePersistenceProvider</provider>

        <class>sn.cheikh.gestion_clinique_medicale.model.Utilisateur</class>
        <class>sn.cheikh.gestion_clinique_medicale.model.Patient</class>
        <class>sn.cheikh.gestion_clinique_medicale.model.RendezVous</class>
        <class>sn.cheikh.gestion_clinique_medicale.model.Consultation</class>
        <class>sn.cheikh.gestion_clinique_medicale.model.Facture</class>

        <properties>
            <property name="javax.persistence.jdbc.driver"
                      value="com.mysql.cj.jdbc.Driver"/>
            <property name="javax.persistence.jdbc.url"
                      value="jdbc:mysql://localhost:3306/clinique_medicale?useSSL=false&amp;serverTimezone=UTC"/>
            <property name="javax.persistence.jdbc.user"     value="root"/>
            <property name="javax.persistence.jdbc.password" value=""/>
            <property name="hibernate.dialect"
                      value="org.hibernate.dialect.MySQL8Dialect"/>
            <property name="hibernate.show_sql"        value="false"/>
            <property name="hibernate.hbm2ddl.auto"    value="validate"/>
        </properties>
    </persistence-unit>
</persistence>
```

## 10. Comptes de test

| Rôle | Login | Mot de passe 

| Administrateur | `admin` | `passer` |
| Médecin | `medecin` | `passer` |
| Réceptionniste | `reception` | `passer` |


## 11. Rôles et permissions

| Fonctionnalité | Admin | Médecin | Réceptionniste 

| Gérer les utilisateurs | ✅ | ❌ | ❌ |
| Gérer les patients | ✅ | ✅ | ✅ |
| Voir les rendez-vous | ✅ Tous | ✅ Ses RDV (lecture seule) | ✅ Tous |
| Créer / modifier des RDV | ✅ | ❌ | ✅ |
| Voir les consultations | ✅ Toutes | ✅ Ses consultations (lecture seule) | ❌ |
| Créer / modifier consultations | ✅ | ❌ | ❌ |
| Générer ordonnance PDF | ✅ | ✅ | ❌ |
| Gérer la facturation | ✅ | ❌ | ✅ |
| Générer facture PDF | ✅ | ❌ | ✅ |
| Dashboard personnalisé | ✅ | ✅ | ✅ |

## 12. Structure du projet

```
src/
└── main/
    ├── java/sn/cheikh/gestion_clinique_medicale/
    │   ├── Config/
    │   │   └── FactoryJPA.java
    │   ├── enums/
    │   │   ├── Role.java
    │   │   ├── Sexe.java
    │   │   ├── StatutRendezVous.java
    │   │   └── StatutPaiement.java
    │   ├── model/
    │   │   ├── Utilisateur.java
    │   │   ├── Patient.java
    │   │   ├── RendezVous.java
    │   │   ├── Consultation.java
    │   │   ├── Facture.java
    │   │   └── SessionUtilisateur.java
    │   ├── Repository/
    │   │   ├── GenericDAO.java
    │   │   ├── GenericDAOImplementation.java
    │   │   ├── IPatientDAO.java / PatientDAOImplementation.java
    │   │   ├── IRendezVousDAO.java / RendezVousDAOImplementation.java
    │   │   ├── IConsultationDAO.java / ConsultationDAOImplementation.java
    │   │   ├── IFactureDAO.java / FactureDAOImplementation.java
    │   │   └── IUtilisateurDAO.java / UtilisateurDAOImplementation.java
    │   ├── Service/
    │   │   ├── PatientService.java
    │   │   ├── RendezVousService.java
    │   │   ├── ConsultationService.java
    │   │   ├── FactureService.java
    │   │   └── UtilisateurService.java
    │   ├── Utilitaire/
    │   │   ├── PasswordUtil.java
    │   │   ├── Navigation.java
    │   │   ├── PDFGenerator.java
    │   │   ├── ValidationUtil.java
    │   │   └── AccesUtil.java
    │   └── Controller/
    │       ├── LoginController.java
    │       ├── DashboardRouterController.java
    │       ├── AdminDashboardController.java
    │       ├── MedecinDashboardController.java
    │       ├── ReceptionisteDashboardController.java
    │       ├── PatientController.java
    │       ├── RendezVousController.java
    │       ├── ConsultationController.java
    │       ├── FactureController.java
    │       └── UtilisateurController.java
    └── resources/sn/cheikh/gestion_clinique_medicale/
        ├── auth/login-view.fxml
        ├── admin/
        │   ├── admin-dashboard.fxml
        │   └── utilisateur-view.fxml
        ├── medecin/
        │   ├── medecin-dashboard.fxml
        │   └── consultation-view.fxml
        ├── patient/patient-view.fxml
        ├── receptionniste/
        │   ├── receptionniste-dashboard.fxml
        │   ├── rendezVous-view.fxml
        │   └── facture-view.fxml
        ├── css/style.css
        └── dashboard-view.fxml
```

## 13. Génération des documents PDF

La génération PDF est assurée par la bibliothèque **iText 5**.

| Document | Méthode | Fichier généré 

| Ordonnance médicale | `PDFGenerator.genererOrdonnance(consultation, chemin)` | `~/ordonnance_<id>.pdf` |
| Facture | `PDFGenerator.genererFacture(facture, chemin)` | `~/facture_<id>.pdf` |

Chaque PDF est sauvegardé dans le répertoire personnel de l'utilisateur système.
Un dialogue d'information affiche le chemin exact après génération.


## 14. Principes SOLID appliqués

| Principe | Application dans le projet 

| **S** — Single Responsibility | Chaque classe a un rôle unique : Controller (UI), Service (métier), DAO (données) |
| **O** — Open/Closed | `GenericDAO<T>` extensible sans modification ; chaque DAO spécialisé l'étend |
| **L** — Liskov Substitution | `PatientDAOImplementation` remplace `GenericDAOImplementation<Patient>` sans effet de bord |
| **I** — Interface Segregation | Interfaces DAO séparées par entité (`IPatientDAO`, `IRendezVousDAO`, etc.) |
| **D** — Dependency Inversion | Les Services dépendent des interfaces DAO, pas des implémentations concrètes |

*Projet académique — Module Génie Logiciel — L3 — Année 2025/2026*  
*Cordialement Cheikh Tidiane ba L3 GL ISI-KM*