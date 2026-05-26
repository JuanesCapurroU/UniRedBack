package com.unired.application.service;

import com.unired.domain.model.CodigoVerificacion;
import com.unired.domain.model.CodigoVerificacion.TipoCodigo;
import com.unired.domain.repository.CodigoVerificacionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private static final String CODIGO_CHARS = "0123456789";
    private static final int CODIGO_LENGTH = 6;
    private static final int MAX_INTENTOS = 3;
    private static final int MINUTOS_EXPIRACION = 15;
    private static final String RESEND_API_URL = "https://api.resend.com/emails";

    private final RestTemplate restTemplate;
    private final CodigoVerificacionRepository codigoVerificacionRepository;

    @Value("${resend.api-key}")
    private String resendApiKey;

    @Value("${resend.from-email}")
    private String fromEmail;

    @Transactional
    public String generarCodigoVerificacion(String correo, TipoCodigo tipo) {
        Optional<CodigoVerificacion> existente = codigoVerificacionRepository.findTopByCorreoAndTipoOrderByCreatedAtDesc(correo, tipo);

        if (existente.isPresent()) {
            CodigoVerificacion codigoActual = existente.get();
            if (codigoActual.getIntentos() >= MAX_INTENTOS) {
                throw new IllegalStateException("Demasiados intentos. Solicita un nuevo código en " + MINUTOS_EXPIRACION + " minutos");
            }
            if (codigoActual.isValido()) {
                return codigoActual.getCodigo();
            }
        }

        String codigo = generarCodigo();

        CodigoVerificacion nuevoCodigo = CodigoVerificacion.builder()
                .correo(correo)
                .codigo(codigo)
                .tipo(tipo)
                .usado(false)
                .intentos(0)
                .expiresAt(LocalDateTime.now().plusMinutes(MINUTOS_EXPIRACION))
                .build();

        codigoVerificacionRepository.save(nuevoCodigo);

        try {
            enviarEmailCodigoVerificacion(correo, codigo, tipo);
        } catch (Exception e) {
            log.error("Error al enviar email de verificación a {}: {}", correo, e.getMessage());
        }

        return codigo;
    }

    @Transactional
    public boolean verificarCodigo(String correo, String codigoIngresado, TipoCodigo tipo) {
        Optional<CodigoVerificacion> codigoOpt = codigoVerificacionRepository
                .findTopByCorreoAndTipoAndUsadoFalseOrderByCreatedAtDesc(correo, tipo);

        if (codigoOpt.isEmpty()) {
            return false;
        }

        CodigoVerificacion codigo = codigoOpt.get();

        if (!codigo.isValido()) {
            return false;
        }

        if (codigo.getCodigo().equals(codigoIngresado)) {
            codigo.setUsado(true);
            codigoVerificacionRepository.save(codigo);
            return true;
        }

        codigo.incrementarIntento();
        codigoVerificacionRepository.save(codigo);
        return false;
    }

    private String generarCodigo() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(CODIGO_LENGTH);
        for (int i = 0; i < CODIGO_LENGTH; i++) {
            sb.append(CODIGO_CHARS.charAt(random.nextInt(CODIGO_CHARS.length())));
        }
        return sb.toString();
    }

    private void enviarEmailCodigoVerificacion(String correo, String codigo, TipoCodigo tipo) {
        String asunto;
        String cuerpoHtml;

        if (tipo == TipoCodigo.REGISTRO) {
            asunto = "Código de verificación - UniRed";
            cuerpoHtml = buildHtmlRegistro(codigo);
        } else {
            asunto = "Código de recuperación - UniRed";
            cuerpoHtml = buildHtmlRecuperacion(codigo);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(resendApiKey);

        Map<String, Object> body = Map.of(
                "from", fromEmail,
                "to", List.of(correo),
                "subject", asunto,
                "html", cuerpoHtml
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(RESEND_API_URL, request, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            log.info("Email de verificación enviado a {}", correo);
        } else {
            throw new RuntimeException("Resend API respondió con estado: " + response.getStatusCode());
        }
    }

    private String buildHtmlRegistro(String codigo) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                </head>
                <body style="font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px; margin: 0;">
                    <div style="max-width: 500px; margin: 0 auto; background-color: #ffffff; border-radius: 10px; padding: 30px; box-shadow: 0 2px 10px rgba(0,0,0,0.1);">
                        <div style="text-align: center; margin-bottom: 30px;">
                            <h1 style="color: #1a73e8; margin: 0;">UniRed</h1>
                            <p style="color: #666; margin: 5px 0 0 0; font-size: 14px;">Universidad Minuto de Dios</p>
                        </div>
                        <h2 style="color: #333; margin: 0 0 20px 0;">Código de Verificación</h2>
                        <p style="color: #555; line-height: 1.6;">Tu código de verificación para completar el registro es:</p>
                        <div style="background-color: #f8f9fa; border: 2px dashed #1a73e8; border-radius: 8px; padding: 20px; text-align: center; margin: 20px 0;">
                            <span style="font-size: 32px; font-weight: bold; color: #1a73e8; letter-spacing: 8px;">%s</span>
                        </div>
                        <p style="color: #777; font-size: 12px; margin: 20px 0 0 0;">Este código expira en %d minutos.</p>
                        <p style="color: #777; font-size: 12px; margin: 5px 0 0 0;">Si no solicitaste este código, puedes ignorar este correo.</p>
                    </div>
                </body>
                </html>
                """.formatted(codigo, MINUTOS_EXPIRACION);
    }

    private String buildHtmlRecuperacion(String codigo) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                </head>
                <body style="font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px; margin: 0;">
                    <div style="max-width: 500px; margin: 0 auto; background-color: #ffffff; border-radius: 10px; padding: 30px; box-shadow: 0 2px 10px rgba(0,0,0,0.1);">
                        <div style="text-align: center; margin-bottom: 30px;">
                            <h1 style="color: #1a73e8; margin: 0;">UniRed</h1>
                            <p style="color: #666; margin: 5px 0 0 0; font-size: 14px;">Universidad Minuto de Dios</p>
                        </div>
                        <h2 style="color: #333; margin: 0 0 20px 0;">Recuperar Contraseña</h2>
                        <p style="color: #555; line-height: 1.6;">Tu código para recuperar tu contraseña es:</p>
                        <div style="background-color: #f8f9fa; border: 2px dashed #ea4335; border-radius: 8px; padding: 20px; text-align: center; margin: 20px 0;">
                            <span style="font-size: 32px; font-weight: bold; color: #ea4335; letter-spacing: 8px;">%s</span>
                        </div>
                        <p style="color: #777; font-size: 12px; margin: 20px 0 0 0;">Este código expira en %d minutos.</p>
                        <p style="color: #777; font-size: 12px; margin: 5px 0 0 0;">Si no solicitaste este código, cambia tu contraseña inmediatamente.</p>
                    </div>
                </body>
                </html>
                """.formatted(codigo, MINUTOS_EXPIRACION);
    }
}
