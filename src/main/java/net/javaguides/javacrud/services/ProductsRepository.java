package net.javaguides.javacrud.services;

import net.javaguides.javacrud.models.Products;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductsRepository extends JpaRepository<Products, Integer> {
}
