package ru.vadim.redisperfomance.service;

import jakarta.annotation.PostConstruct;
import org.redisson.api.BatchOptions;
import org.redisson.api.RBatchReactive;
import org.redisson.api.RScoredSortedSetReactive;
import org.redisson.api.RedissonReactiveClient;
import org.redisson.client.codec.IntegerCodec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
/*
пример из курса сделан круче, он демонстрирует поток текущих трех популярных продуктов каждые три секунды
 */
@Service
public class ProductVisitServiceVinoth {

    @Autowired
    private RedissonReactiveClient client;
    private Sinks.Many<Integer> sink;

    public ProductVisitServiceVinoth() {
        this.sink = Sinks.many().unicast().onBackpressureBuffer();
    }

    @PostConstruct
    private void init() {
        this.sink.asFlux()// приемник с синка
                .buffer(Duration.ofSeconds(3)) // list (1,2,3,1,1,3,5,1...) - буферизуем каждый три секунды
                .map(l -> l.stream().collect(// 1:4, 5:1 - группируем буффер по объекту и количеству повторов в буфере, т.е если было три  вызова с айди  1,1,3, то продукт с айди = 1 вызвали 2 раз, соответственно группируем по айди : кол-во вызовов каждые 3 секунды
                        Collectors.groupingBy(
                                Function.identity(), // айди
                                Collectors.counting()      // сколько повторений в трехсекундном буффере
                        )
                ))
                .flatMap(this::updateBatch)//
                .subscribe();
    }

    public void addVisit(int productId) {
        this.sink.tryEmitNext(productId); // эмитим посещения в синк. Синк юникастовый, так как подписчик всего один и это один из сервисов
    }

    private Mono<Void> updateBatch(Map<Integer, Long> map) {
        RBatchReactive batch = this.client.createBatch(BatchOptions.defaults());
        String format = DateTimeFormatter.ofPattern("YYYYMMdd").format(LocalDate.now());
        RScoredSortedSetReactive<Integer> set = batch.getScoredSortedSet("product:visit:" + format, IntegerCodec.INSTANCE); // тут создаем батч с ключом содержащим дату
        return Flux.fromIterable(map.entrySet())
                        .map(e -> set.addScore(e.getKey(), e.getValue()))// добавляем в сортированынй список методом, который увеличивает у объекта по ключу e.getKey(), балл/ранг элемента на  e.getValue() - т.е. на количество посещений за тот трехсекундный период буферизации
                .then(batch.execute()) // сохраняет батч
                .then();
    }
}
