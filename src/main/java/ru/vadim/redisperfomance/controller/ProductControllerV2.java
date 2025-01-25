package ru.vadim.redisperfomance.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import ru.vadim.redisperfomance.entity.Product;
import ru.vadim.redisperfomance.service.ProductServiceV2;

@RestController
@RequestMapping("product/v2")
public class ProductControllerV2 {
    @Autowired
    private ProductServiceV2 productService;

    @GetMapping("{id}")
    public Mono<Product> getProduct(@PathVariable int id) {
        return this.productService.getProduct(id);
    }

    @PutMapping("{id}")
    public Mono<Product> updateProduct(@PathVariable int id, @RequestBody Mono<Product> productMono) {
        return this.productService.updateProduct(id, productMono);
    }

    @DeleteMapping("{id}")
    public Mono<Void> deleteProduct(@PathVariable int id) {
        return this.productService.deleteProduct(id);
    }
}
