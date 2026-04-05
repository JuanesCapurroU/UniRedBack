package com.unired.infrastructure.scheduler;

import com.unired.application.service.RRSSService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RRSSScheduler {

    private final RRSSService rrssService;

    @Scheduled(fixedRate = 900_000)
    public void sincronizarRedes() {
        LocalDateTime start = LocalDateTime.now();
        log.info("Inicio de sincronización RRSS: {}", start);
        try {
            rrssService.sincronizarFeed();
            log.info("Sincronización RRSS finalizada: {}", LocalDateTime.now());
        } catch (Exception ex) {
            log.error("Error en sincronización RRSS", ex);
        }
    }
}
