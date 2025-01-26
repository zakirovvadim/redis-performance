package ru.vadim.redisperfomance.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import ru.vadim.redisperfomance.service.BusinessMetricsService;

import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("product/metrics")
public class BusinessMetricsController {

    @Autowired
    private BusinessMetricsService metricsService;

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Map<Integer, Double>> getMetricsEvent() {
        return this.metricsService.top3Products()
                .repeatWhen(l -> Flux.interval(Duration.ofSeconds(3))); // каждый три секунды паблишер флакса в интервале будет вызывать repeatWhen, который в свою очередь будет дергать метод top3Products
    }
}
