package com.unired.infrastructure.external;

import com.unired.application.dto.response.PublicacionRRSSDTO;
import java.util.List;

public interface RedSocialAdapter {

    List<PublicacionRRSSDTO> obtenerPublicaciones();

    String getNombre();
}
