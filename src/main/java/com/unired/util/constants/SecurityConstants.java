package com.unired.util.constants;

public final class SecurityConstants {

    public static final String INVALID_CREDENTIALS_MESSAGE = "Credenciales inválidas";
    /** Estudiantes usan @uniminuto.edu.co; los docentes, @uniminuto.edu. */
    public static final String UNIMINUTO_DOMAIN_REGEX = "^[a-zA-Z0-9._%+\\-]+@uniminuto\\.edu(\\.co)?$";
    /** Dominio exclusivo de docentes y personal administrativo. */
    public static final String DOCENTE_DOMAIN_SUFFIX = "@uniminuto.edu";
    public static final String CORRELATION_ID_KEY = "correlationId";

    private SecurityConstants() {
    }
}
