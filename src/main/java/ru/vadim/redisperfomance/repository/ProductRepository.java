package ru.vadim.redisperfomance.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import ru.vadim.redisperfomance.entity.Product;

@Repository
public interface ProductRepository extends ReactiveCrudRepository<Product, Integer> {
}
