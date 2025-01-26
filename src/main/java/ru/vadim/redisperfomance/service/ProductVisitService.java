//package ru.vadim.redisperfomance.service;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import reactor.core.publisher.Flux;
//import reactor.core.publisher.Mono;
//import ru.vadim.redisperfomance.entity.Product;
//import ru.vadim.redisperfomance.service.util.CacheTemplate;
//
//import java.util.List;
//
//@Service
//public class ProductVisitService  {
//
//    @Autowired
//    private CacheTemplate<Integer, Product> cacheTemplate;
//
//    public Mono<List<Product>> getTopThreeProducts() {
//        return cacheTemplate.getTopThree()
//                .collectList();
//    }
//
//}
