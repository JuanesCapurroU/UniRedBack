package com.unired.config;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    /** Frontends oficiales de UniRed. Siempre permitidos, no dependen de variables de entorno. */
    private static final List<String> ORIGENES_FIJOS = List.of(
            "https://uni-red-web.vercel.app",
            "http://localhost:3000",
            "http://127.0.0.1:3000"
    );

    /** Origenes extra separados por coma. Se SUMAN a los fijos, no los reemplazan. */
    @Value("${CORS_ALLOWED_ORIGINS:}")
    private String corsAllowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        Set<String> origenes = new LinkedHashSet<>(ORIGENES_FIJOS);
        Arrays.stream(corsAllowedOrigins.split(","))
                .map(String::trim)
                .filter(origen -> !origen.isEmpty())
                .forEach(origenes::add);

        registry.addMapping("/**")
                .allowedOriginPatterns(origenes.toArray(String[]::new))
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("X-Correlation-Id")
                .allowCredentials(true);
    }
}
