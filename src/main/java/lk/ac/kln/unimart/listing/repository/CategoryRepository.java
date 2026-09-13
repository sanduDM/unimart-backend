package lk.ac.kln.unimart.listing.repository;

import lk.ac.kln.unimart.listing.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Integer> {

    Optional<Category> findByName(String name);
}
