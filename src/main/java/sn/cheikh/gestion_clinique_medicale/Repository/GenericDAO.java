package sn.cheikh.gestion_clinique_medicale.Repository;

import java.util.List;
import java.util.Optional;

public interface GenericDAO<T, ID> {

    void save(T entity);
    void update(T entity);
    void delete(Long entity);
    void delete(T entity);      // Suppression par objet
    void deleteById(ID id);     // Suppression par ID
    Optional<T> findById(ID id);
    List<T> findAll();
}