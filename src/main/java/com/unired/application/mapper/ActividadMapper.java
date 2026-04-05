package com.unired.application.mapper;

import com.unired.application.dto.request.ActividadRequest;
import com.unired.application.dto.response.ActividadResponse;
import com.unired.application.dto.response.InscripcionResponse;
import com.unired.domain.model.Actividad;
import com.unired.domain.model.Inscripcion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ActividadMapper {

    @Mapping(target = "categoria", expression = "java(actividad.getCategoria().name())")
    @Mapping(target = "inscrito", ignore = true)
    ActividadResponse toResponse(Actividad actividad);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cupoDisponible", source = "cupoTotal")
    @Mapping(target = "activa", constant = "true")
    @Mapping(target = "recordatorioWa", constant = "false")
    @Mapping(target = "administrador", ignore = true)
    @Mapping(target = "inscripciones", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    Actividad toEntity(ActividadRequest request);

    @Mapping(target = "actividadId", source = "actividad.id")
    @Mapping(target = "actividadNombre", source = "actividad.nombre")
    InscripcionResponse toInscripcionResponse(Inscripcion inscripcion);
}
