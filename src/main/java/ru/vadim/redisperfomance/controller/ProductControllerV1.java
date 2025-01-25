package ru.vadim.redisperfomance.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import ru.vadim.redisperfomance.entity.Product;
import ru.vadim.redisperfomance.service.ProductServiceV1;


// jmeter для старта из терминала jmeter -n -t <script name with path> -l <log file name with path>
// заполненный пример jmeter -n -t redis-cource/product-service.jmx -l redis-cource/v1.jtl
@RestController
@RequestMapping("product")
public class ProductControllerV1 {

    @Autowired
    private ProductServiceV1 productService;

    @GetMapping("{id}")
    public Mono<Product> getProduct(@PathVariable int id) {
        return this.productService.getProduct(id);
    }

    @PutMapping("{id}")
    public Mono<Product> updateProduct(@PathVariable int id, @RequestBody Mono<Product> productMono) {
        return this.productService.updateProduct(id, productMono);
    }
}
