package ru.vadim.redisperfomance.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.vadim.redisperfomance.entity.Product;
import ru.vadim.redisperfomance.repository.ProductRepository;
import ru.vadim.redisperfomance.service.util.CacheTemplate;

@Service
public class ProductServiceV2 {

    @Autowired
    private CacheTemplate<Integer, Product> cacheTemplate;

    //GET
    public Mono<Product> getProduct(int id) {
        return this.cacheTemplate.get(id);
    }

    // PUT
    public Mono<Product> updateProduct(int id, Mono<Product> productMono) {
        return productMono
                .flatMap(p -> this.cacheTemplate.update(id, p));
    }

    // DELTE
    public Mono<Void> deleteProduct(int id) {
        return this.cacheTemplate.delete(id);
    }

    // INSERT

}
