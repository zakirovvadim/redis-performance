package ru.vadim.redisperfomance.service.util;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
/*
абстрактный класс для работы с кешом, чтобы не писать постоянно один и тот же функционал
также в обработки абстрактных методов можно добавить обработку ошибок
 */
public abstract class CacheTemplate<KEY, ENTITY> {

    public Mono<ENTITY> get(KEY key) {
        return getFromCache(key)
                .switchIfEmpty(getFromSource(key)
                        .flatMap(e -> updateCache(key, e))
                );
    }

    public Mono<ENTITY> update(KEY key, ENTITY entity) {
        return  updateSource(key, entity) // важно, при обновлении сначала обновляем источник
                .flatMap(e -> deleteFromCache(key).thenReturn(e)); // можно как удалить из кеша не актуальную запись, так и обновить, вызвав updateCache
    }

    public Mono<Void> delete(KEY key) {
        return deleteFromSource(key)
                .then(deleteFromCache(key));
    }

    abstract protected Mono<ENTITY> getFromSource(KEY key);
    abstract protected Mono<ENTITY> getFromCache(KEY key);
    public abstract Flux<ENTITY> getTopThree();
    abstract protected Mono<ENTITY> updateSource(KEY key, ENTITY entity);
    abstract protected Mono<ENTITY> updateCache(KEY key, ENTITY entity);
    abstract protected Mono<Void> deleteFromSource(KEY key);
    abstract protected Mono<Void> deleteFromCache(KEY key);
}
