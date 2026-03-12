package sn.cheikh.gestion_clinique_medicale.Repository.Implementation;

import sn.cheikh.gestion_clinique_medicale.Config.FactoryJPA;
import sn.cheikh.gestion_clinique_medicale.Repository.GenericDAO;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;

public abstract class GenericDAOImplementation<T, ID extends Serializable> implements GenericDAO<T, ID> {

    private final Class<T> entityClass;

    public GenericDAOImplementation(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    @Override
    public void save(T entity) {
        EntityManager em = FactoryJPA.getManager();
        try {
            em.getTransaction().begin();
            em.persist(entity);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public void update(T entity) {
        EntityManager em = FactoryJPA.getManager();
        try {
            em.getTransaction().begin();
            em.merge(entity);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    // ── delete(T entity) ── trouve l'entité par son id et la supprime
    @Override
    public void delete(T entity) {
        EntityManager em = FactoryJPA.getManager();
        try {
            em.getTransaction().begin();
            T managed = em.merge(entity);
            em.remove(managed);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    // ── deleteById(ID id) ── supprime directement par l'identifiant
    @Override
    public void deleteById(ID id) {
        EntityManager em = FactoryJPA.getManager();
        try {
            em.getTransaction().begin();
            T entity = em.find(entityClass, id);
            if (entity != null) {
                em.remove(entity);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<T> findById(ID id) {
        EntityManager em = FactoryJPA.getManager();
        try {
            List<T> results = em.createQuery(
                            "SELECT DISTINCT e FROM " + entityClass.getSimpleName() + " e WHERE e.id = :id",
                            entityClass)
                    .setParameter("id", id)
                    .getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } catch (NoResultException e) {
            return Optional.empty();
        } finally {
            em.close();
        }
    }

    @Override
    public List<T> findAll() {
        EntityManager em = FactoryJPA.getManager();
        try {
            return em.createQuery(
                            "SELECT DISTINCT e FROM " + entityClass.getSimpleName() + " e",
                            entityClass)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}