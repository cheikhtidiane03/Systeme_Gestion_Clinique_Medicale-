module sn.cheikh.gestion_clinique_medicale {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    requires java.persistence;
    requires org.hibernate.orm.core;
    requires java.naming;
    requires java.sql;

    requires jbcrypt;

    requires itextpdf;

    requires static lombok;
    requires java.desktop;

    opens sn.cheikh.gestion_clinique_medicale to javafx.fxml, javafx.graphics;
    opens sn.cheikh.gestion_clinique_medicale.Controller to javafx.fxml;
    opens sn.cheikh.gestion_clinique_medicale.model to javafx.base, org.hibernate.orm.core;
    opens sn.cheikh.gestion_clinique_medicale.enums to javafx.base;

    exports sn.cheikh.gestion_clinique_medicale;
    exports sn.cheikh.gestion_clinique_medicale.Controller;
    exports sn.cheikh.gestion_clinique_medicale.Service;
    exports sn.cheikh.gestion_clinique_medicale.model;
    exports sn.cheikh.gestion_clinique_medicale.enums;
    exports sn.cheikh.gestion_clinique_medicale.Utilitaire;
    exports sn.cheikh.gestion_clinique_medicale.Repository;
    exports sn.cheikh.gestion_clinique_medicale.Repository.Implementation;
    exports sn.cheikh.gestion_clinique_medicale.Config;
}