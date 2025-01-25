package ru.vadim.redisperfomance.service.util;

import org.redisson.api.RMapReactive;
import org.redisson.api.RedissonReactiveClient;
import org.redisson.codec.TypedJsonJacksonCodec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.vadim.redisperfomance.entity.Product;
import ru.vadim.redisperfomance.repository.ProductRepository;

//@Service
public class ProductCacheTemplate extends CacheTemplate<Integer, Product> {

    @Autowired
    private ProductRepository productRepository;
    private RMapReactive<Integer, Product> map;

    public ProductCacheTemplate(RedissonReactiveClient client) {
        this.map = client.getMap("product", new TypedJsonJacksonCodec(Integer.class, Product.class));
    }

    @Override
    protected Mono<Product> getFromSource(Integer id) {
        return this.productRepository.findById(id);
    }

    @Override
    protected Mono<Product> getFromCache(Integer id) {
        return this.map.get(id);
    }

    @Override
    protected Mono<Product> updateSource(Integer id, Product product) {
        return this.productRepository.findById(id)
                .doOnNext(p -> product.setId(id))
                .flatMap(p -> productRepository.save(product));
    }

    @Override
    protected Mono<Product> updateCache(Integer id, Product product) {
        return this.map.fastPut(id, product).thenReturn(product);
    }

    @Override
    protected Mono<Void> deleteFromSource(Integer id) {
        return this.productRepository.deleteById(id);
        // если удаление происходит из какойто внешней службы то можно просто вернуть Mono.empty
    }

    @Override
    protected Mono<Void> deleteFromCache(Integer id) {
        return this.map.fastRemove(id).then();
    }
}
