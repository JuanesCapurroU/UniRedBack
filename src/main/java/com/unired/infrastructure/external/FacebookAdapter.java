package com.unired.infrastructure.external;

import com.unired.application.dto.response.PublicacionRRSSDTO;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FacebookAdapter implements RedSocialAdapter {

    @Value("${FACEBOOK_TOKEN:}")
    private String token;

    @Override
    public List<PublicacionRRSSDTO> obtenerPublicaciones() {
        return Collections.emptyList();
    }

    @Override
    public String getNombre() {
        return "FACEBOOK";
    }
}
