package ru.vadim.redisperfomance.service.util;

import org.redisson.api.RMapReactive;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RScoredSortedSetReactive;
import org.redisson.api.RedissonReactiveClient;
import org.redisson.client.protocol.ScoredEntry;
import org.redisson.codec.TypedJsonJacksonCodec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.vadim.redisperfomance.entity.Product;
import ru.vadim.redisperfomance.repository.ProductRepository;

import java.util.function.Function;

@Service
public class ProductVisitTemplate extends CacheTemplate<Integer, Product> {
    @Autowired
    private ProductRepository productRepository;
    private RScoredSortedSetReactive<Integer> sortedSet;
    private RMapReactive<Integer, Product> map;

    public ProductVisitTemplate(RedissonReactiveClient client) {
        this.sortedSet = client.getScoredSortedSet("scoreProduct", new TypedJsonJacksonCodec(Integer.class));
        this.map = client.getMap("product", new TypedJsonJacksonCodec(Integer.class, Product.class));
    }

    @Override
    protected Mono<Product> getFromSource(Integer id) {
        return this.productRepository.findById(id);
    }

    @Override
    protected Mono<Product> getFromCache(Integer id) {
        sortedSet.addScore(id, 1).subscribe();
        return map.get(id);

    }

    @Override
    public Flux<Product> getTopThree() {
        return this.sortedSet.entryRangeReversed(0 , 3)
                .flatMapIterable(Function.identity())
                .map(ScoredEntry::getValue)
                .concatMap(map::get)
                .doOnNext(System.out::println);
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
