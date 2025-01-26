package ru.vadim.redisperfomance.service;

import org.redisson.api.RScoredSortedSetReactive;
import org.redisson.api.RedissonReactiveClient;
import org.redisson.client.codec.IntegerCodec;
import org.redisson.client.protocol.ScoredEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BusinessMetricsService {

    @Autowired
    private RedissonReactiveClient client;
    /*
        example respobnse id:score
        {
            189:  11423,
            165: 232,
            534: 222
        }
         */
    public Mono<Map<Integer, Double>> top3Products() {
        String format = DateTimeFormatter.ofPattern("YYYYMMdd").format(LocalDate.now());
        RScoredSortedSetReactive<Integer> set = client.getScoredSortedSet("product:visit:" + format, IntegerCodec.INSTANCE); // получаем наш сортетСет по ключу
        return set.entryRangeReversed(0, 2) // list of scored entry - вытаскиваем нужное количество
                .map(listSe -> listSe.stream().collect( // так как возвращается лист, мапим в мапу, чтобы отображалось айди продукта против количества его ГЕТ вызовов
                        Collectors.toMap(
                                ScoredEntry::getValue,
                                ScoredEntry::getScore,
                                (a, b) -> a,
                                LinkedHashMap::new
                        )
                ));
    }
}