package com.unired.infrastructure.health;

import com.google.firebase.FirebaseApp;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component("uniRedHealth")
@RequiredArgsConstructor
public class UniRedHealthIndicator implements HealthIndicator {

    private final DataSource dataSource;
    private final ObjectProvider<FirebaseApp> firebaseAppProvider;
    private final JdbcTemplate jdbcTemplate;

    @Value("${firebase.enabled:false}")
    private boolean firebaseEnabled;

    @Override
    public Health health() {
        boolean dbUp = checkDatabase();
        boolean firebaseUp = checkFirebase();
        boolean sesionesUp = checkSesionesTable();

        boolean firebaseCheck = !firebaseEnabled || firebaseUp;
        Health.Builder builder = (dbUp && firebaseCheck && sesionesUp) ? Health.up() : Health.down();

        String firebaseDetail;
        if (!firebaseEnabled) {
            firebaseDetail = "DISABLED";
        } else {
            firebaseDetail = firebaseUp ? "UP" : "DOWN";
        }

        return builder
                .withDetail("postgres", dbUp ? "UP" : "DOWN")
                .withDetail("firebase", firebaseDetail)
                .withDetail("sesionesTable", sesionesUp ? "UP" : "DOWN")
                .build();
    }

    private boolean checkDatabase() {
        try (java.sql.Connection connection = dataSource.getConnection()) {
            return connection.isValid(2);
        } catch (Exception ex) {
            return false;
        }
    }

    private boolean checkFirebase() {
        try {
            FirebaseApp firebaseApp = firebaseAppProvider.getIfAvailable();
            if (firebaseApp == null) {
                return false;
            }
            return firebaseApp != null && firebaseApp.getName() != null;
        } catch (Exception ex) {
            return false;
        }
    }

    private boolean checkSesionesTable() {
        try {
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sesiones", Integer.class);
            return count != null;
        } catch (Exception ex) {
            return false;
        }
    }
}
