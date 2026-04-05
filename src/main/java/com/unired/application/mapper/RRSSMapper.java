package com.unired.application.mapper;

import com.unired.application.dto.response.PublicacionRRSSDTO;
import com.unired.application.dto.response.PublicacionRRSSResponse;
import com.unired.domain.model.PublicacionRRSS;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RRSSMapper {

    PublicacionRRSSResponse toResponse(PublicacionRRSS publicacionRRSS);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCache", ignore = true)
    @Mapping(target = "sincronizada", constant = "true")
    PublicacionRRSS toEntity(PublicacionRRSSDTO dto);
}
