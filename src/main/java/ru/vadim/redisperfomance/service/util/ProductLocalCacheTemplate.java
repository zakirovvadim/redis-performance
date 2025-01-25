package ru.vadim.redisperfomance.service.util;

import org.redisson.api.RLocalCachedMap;
import org.redisson.api.RedissonClient;
import org.redisson.api.options.LocalCachedMapOptions;
import org.redisson.codec.TypedJsonJacksonCodec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.vadim.redisperfomance.entity.Product;
import ru.vadim.redisperfomance.repository.ProductRepository;

@Service
public class ProductLocalCacheTemplate extends CacheTemplate<Integer, Product> {
    @Autowired
    private ProductRepository productRepository;
    private RLocalCachedMap<Integer, Product> map;

    public ProductLocalCacheTemplate(RedissonClient client) {
        LocalCachedMapOptions<Integer, Product> mapOptions = LocalCachedMapOptions.<Integer, Product>name("product")
                .codec(new TypedJsonJacksonCodec(Integer.class, Product.class))
                .syncStrategy(LocalCachedMapOptions.SyncStrategy.UPDATE)
                .reconnectionStrategy(LocalCachedMapOptions.ReconnectionStrategy.CLEAR);
        this.map = client.getLocalCachedMap(mapOptions);
    }

    @Override
    protected Mono<Product> getFromSource(Integer id) {
        return this.productRepository.findById(id);
    }

    @Override
    protected Mono<Product> getFromCache(Integer id) {
        return Mono.justOrEmpty(this.map.get(id));
    }

    @Override
    protected Mono<Product> updateSource(Integer id, Product product) {
        return this.productRepository.findById(id)
                .doOnNext(p -> product.setId(id))
                .flatMap(p -> productRepository.save(product));
    }

    @Override
    protected Mono<Product> updateCache(Integer id, Product product) {
        return Mono.create(sink -> this.map.fastPutAsync(id, product)
                .thenAccept(b -> sink.success(product))
                .exceptionally(ex -> {
                    sink.error(ex);
                    return null;
                })
        );
    }

    @Override
    protected Mono<Void> deleteFromSource(Integer id) {
        return this.productRepository.deleteById(id);
        // если удаление происходит из какойто внешней службы то можно просто вернуть Mono.empty
    }

    @Override
    protected Mono<Void> deleteFromCache(Integer id) {
        return Mono.create(sink -> this.map.fastRemoveAsync(id)
                .thenAccept(b -> sink.success())
                .exceptionally(ex -> {
                    sink.error(ex);
                    return null;
                })
        );
    }
}
