package com.unired.application.service;

import com.unired.application.dto.request.RegistroRequest;
import com.unired.application.dto.response.LoginResponse;
import com.unired.application.dto.response.RegistroResponse;
import com.unired.application.mapper.UsuarioMapper;
import com.unired.config.JwtConfig;
import com.unired.domain.model.CodigoVerificacion.TipoCodigo;
import com.unired.domain.model.ConfiguracionNotificaciones;
import com.unired.domain.model.Estudiante;
import com.unired.domain.model.Sesion;
import com.unired.domain.model.Usuario;
import com.unired.domain.repository.ConfiguracionNotificacionesRepository;
import com.unired.domain.repository.SesionRepository;
import com.unired.domain.repository.UsuarioRepository;
import com.unired.exception.custom.AccountLockedException;
import com.unired.exception.custom.DominioNoPermitidoException;
import com.unired.exception.custom.TokenInvalidoException;
import com.unired.infrastructure.security.AppUserDetails;
import com.unired.infrastructure.security.JwtUtil;
import com.unired.infrastructure.security.UserDetailsServiceImpl;
import com.unired.util.constants.SecurityConstants;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Pattern DOMAIN_PATTERN = Pattern.compile(SecurityConstants.UNIMINUTO_DOMAIN_REGEX);

    private final UsuarioRepository usuarioRepository;
    private final SesionRepository sesionRepository;
    private final ConfiguracionNotificacionesRepository configuracionNotificacionesRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;
    private final UsuarioMapper usuarioMapper;
    private final JwtConfig jwtConfig;
    private final EmailService emailService;

    @Transactional
    public LoginResponse login(String correo, String password) {
        validateDomain(correo);

        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new BadCredentialsException(SecurityConstants.INVALID_CREDENTIALS_MESSAGE));

        if (!usuario.getVerificado()) {
            throw new IllegalStateException("Debes verificar tu correo antes de iniciar sesión");
        }

        validateLockStatus(usuario);

        if (!passwordEncoder.matches(password, usuario.getPasswordHash())) {
            processFailedAttempt(usuario);
            throw new BadCredentialsException(SecurityConstants.INVALID_CREDENTIALS_MESSAGE);
        }

        usuario.setIntentosFallidos(0);
        usuario.setBloqueadoHasta(null);
        usuarioRepository.save(usuario);

        AppUserDetails userDetails = userDetailsService.buildUserDetails(usuario);
        String accessToken = jwtUtil.generateToken(userDetails, usuario.getId(), userDetails.getRole());
        String refreshToken = jwtUtil.generateRefreshToken(userDetails);

        Sesion sesion = Sesion.builder()
                .usuario(usuario)
                .tokenJwt(accessToken)
                .refreshToken(refreshToken)
                .fechaInicio(LocalDateTime.now())
                .fechaExpiracion(LocalDateTime.now().plus(Duration.ofMillis(jwtConfig.getExpirationMs())))
                .activo(true)
                .build();
        sesionRepository.save(sesion);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtConfig.getExpirationMs() / 1000)
                .usuario(usuarioMapper.toBasicoResponse(usuario))
                .build();
    }

    @Transactional
    public LoginResponse refreshToken(String refreshToken) {
        Sesion sesion = sesionRepository.findByRefreshTokenAndActivoTrue(refreshToken)
                .orElseThrow(() -> new TokenInvalidoException("Refresh token inválido"));

        LocalDateTime refreshExpiry = sesion.getFechaInicio().plus(Duration.ofMillis(jwtConfig.getRefreshExpirationMs()));
        if (refreshExpiry.isBefore(LocalDateTime.now())) {
            sesion.invalidar();
            sesionRepository.save(sesion);
            throw new TokenInvalidoException("Refresh token expirado");
        }

        AppUserDetails userDetails = userDetailsService.buildUserDetails(sesion.getUsuario());
        String newToken = jwtUtil.generateToken(userDetails, sesion.getUsuario().getId(), userDetails.getRole());
        sesion.setTokenJwt(newToken);
        sesion.setFechaExpiracion(LocalDateTime.now().plus(Duration.ofMillis(jwtConfig.getExpirationMs())));
        sesionRepository.save(sesion);

        return LoginResponse.builder()
                .accessToken(newToken)
                .refreshToken(sesion.getRefreshToken())
                .expiresIn(jwtConfig.getExpirationMs() / 1000)
                .usuario(usuarioMapper.toBasicoResponse(sesion.getUsuario()))
                .build();
    }

    @Transactional
    public void logout(String token) {
        String cleanedToken = extractToken(token);
        sesionRepository.findByTokenJwtAndActivoTrue(cleanedToken).ifPresent(sesion -> {
            sesion.invalidar();
            sesionRepository.save(sesion);
        });
    }

    @Transactional(readOnly = true)
    public Boolean validarToken(String token) {
        try {
            String cleanedToken = extractToken(token);
            String username = jwtUtil.extractUsername(cleanedToken);
            AppUserDetails userDetails = (AppUserDetails) userDetailsService.loadUserByUsername(username);

            Optional<Sesion> sesion = sesionRepository.findByTokenJwtAndActivoTrue(cleanedToken);
            return sesion.isPresent() && sesion.get().isValido() && jwtUtil.isTokenValid(cleanedToken, userDetails);
        } catch (Exception ex) {
            return false;
        }
    }

    @Transactional
    public void actualizarFcmToken(String token, String fcmToken) {
        String cleanedToken = extractToken(token);
        String correo = jwtUtil.extractUsername(cleanedToken);

        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new TokenInvalidoException("Token inválido"));

        ConfiguracionNotificaciones config = configuracionNotificacionesRepository.findByUsuarioId(usuario.getId())
                .orElseGet(() -> ConfiguracionNotificaciones.builder().usuario(usuario).build());
        config.setFcmToken(fcmToken);
        configuracionNotificacionesRepository.save(config);
    }

    @Transactional
    public RegistroResponse registrar(RegistroRequest request) {
        validateDomain(request.getCorreo());

        if (usuarioRepository.existsByCorreo(request.getCorreo())) {
            throw new IllegalStateException("El correo ya está registrado");
        }

        if (usuarioRepository.existsByNumeroDocumento(request.getNumeroDocumento())) {
            throw new IllegalStateException("El número de documento ya está registrado");
        }

        Estudiante estudiante = Estudiante.builder()
                .tipoDocumento(request.getTipoDocumento())
                .numeroDocumento(request.getNumeroDocumento())
                .primerNombre(request.getPrimerNombre())
                .primerApellido(request.getPrimerApellido())
                .programaAcademico(request.getProgramaAcademico())
                .semestre(request.getSemestre())
                .sede("Zipaquirá")
                .segundoNombre(request.getSegundoNombre())
                .segundoApellido(request.getSegundoApellido())
                .telefono(request.getTelefono())
                .correo(request.getCorreo())
                .correoInstitucional(request.getCorreo())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .activo(false)
                .verificado(false)
                .build();

        usuarioRepository.save(estudiante);

        emailService.generarCodigoVerificacion(request.getCorreo(), TipoCodigo.REGISTRO);

        return RegistroResponse.builder()
                .mensaje("Código de verificación enviado a tu correo")
                .correo(request.getCorreo())
                .verificado(false)
                .build();
    }

    @Transactional
    public LoginResponse verificarCorreo(String correo, String codigo) {
        boolean valido = emailService.verificarCodigo(correo, codigo, TipoCodigo.REGISTRO);

        if (!valido) {
            throw new BadCredentialsException("Código inválido o expirado");
        }

        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new TokenInvalidoException("Usuario no encontrado"));

        usuario.setVerificado(true);
        usuario.setActivo(true);
        usuarioRepository.save(usuario);

        AppUserDetails userDetails = userDetailsService.buildUserDetails(usuario);
        String accessToken = jwtUtil.generateToken(userDetails, usuario.getId(), userDetails.getRole());
        String refreshToken = jwtUtil.generateRefreshToken(userDetails);

        Sesion sesion = Sesion.builder()
                .usuario(usuario)
                .tokenJwt(accessToken)
                .refreshToken(refreshToken)
                .fechaInicio(LocalDateTime.now())
                .fechaExpiracion(LocalDateTime.now().plus(Duration.ofMillis(jwtConfig.getExpirationMs())))
                .activo(true)
                .build();
        sesionRepository.save(sesion);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtConfig.getExpirationMs() / 1000)
                .usuario(usuarioMapper.toBasicoResponse(usuario))
                .build();
    }

    @Transactional(readOnly = true)
    public void reenviarCodigo(String correo) {
        validateDomain(correo);

        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new IllegalArgumentException("Correo no registrado"));

        if (usuario.getVerificado()) {
            throw new IllegalStateException("El correo ya está verificado");
        }

        emailService.generarCodigoVerificacion(correo, TipoCodigo.REGISTRO);
    }

    public String makeFakeToken(String correo) {
        throw new UnsupportedOperationException("makeFakeToken no está permitido");
    }

    private void validateDomain(String correo) {
        if (!DOMAIN_PATTERN.matcher(correo).matches()) {
            throw new DominioNoPermitidoException("Solo se permiten correos @uniminuto.edu.co");
        }
    }

    private void validateLockStatus(Usuario usuario) {
        if (usuario.getBloqueadoHasta() != null && usuario.getBloqueadoHasta().isAfter(LocalDateTime.now())) {
            Duration remaining = Duration.between(LocalDateTime.now(), usuario.getBloqueadoHasta());
            long minutes = Math.max(1, remaining.toMinutes());
            throw new AccountLockedException("Cuenta bloqueada. Intenta de nuevo en " + minutes + " minutos");
        }
    }

    private void processFailedAttempt(Usuario usuario) {
        int attempts = usuario.getIntentosFallidos() == null ? 0 : usuario.getIntentosFallidos();
        attempts++;
        usuario.setIntentosFallidos(attempts);

        if (attempts >= 5) {
            usuario.setBloqueadoHasta(LocalDateTime.now().plusMinutes(15));
            usuario.setIntentosFallidos(0);
        }

        usuarioRepository.save(usuario);
    }

    private String extractToken(String token) {
        if (!StringUtils.hasText(token)) {
            throw new TokenInvalidoException("Token inválido");
        }
        return token.startsWith("Bearer ") ? token.substring(7) : token;
    }
}
