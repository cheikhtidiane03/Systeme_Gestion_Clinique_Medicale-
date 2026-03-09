package sn.cheikh.gestion_clinique_medicale.Config;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class FactoryJPA {

    private static EntityManagerFactory emf;

    private FactoryJPA() {}

    public static EntityManagerFactory getEmf() {
        if (emf == null) emf = Persistence.createEntityManagerFactory("clinique");
        return emf;
    }

    public static EntityManager getManager() {
        return getEmf().createEntityManager();
    }

    public static void close() {
        if (emf != null && emf.isOpen()) emf.close();
    }
}