package com.unired.application.mapper;

import com.unired.application.dto.response.MentorDetalleResponse;
import com.unired.application.dto.response.MentorResponse;
import com.unired.application.dto.response.SolicitudResponse;
import com.unired.domain.model.Mentor;
import com.unired.domain.model.SolicitudMentoria;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MentoriaMapper {

    @Mapping(target = "nombre", expression = "java(mentor.getEstudiante().getPrimerNombre() + \" \" + mentor.getEstudiante().getPrimerApellido())")
    @Mapping(target = "porcentajeCompatibilidad", ignore = true)
    @Mapping(target = "numeroWhatsapp", source = "estudiante.telefono")
    MentorResponse toMentorResponse(Mentor mentor);

    @Mapping(target = "nombre", expression = "java(mentor.getEstudiante().getPrimerNombre() + \" \" + mentor.getEstudiante().getPrimerApellido())")
    @Mapping(target = "correo", source = "estudiante.correo")
    @Mapping(target = "porcentajeCompatibilidad", ignore = true)
    MentorDetalleResponse toMentorDetalleResponse(Mentor mentor);

    @Mapping(target = "mentorId", source = "mentor.id")
    @Mapping(target = "estudianteId", source = "estudiante.id")
    @Mapping(target = "estudianteNombre", expression = "java(solicitud.getEstudiante().getPrimerNombre() + \" \" + solicitud.getEstudiante().getPrimerApellido())")
    @Mapping(target = "mentorNombre", expression = "java(solicitud.getMentor().getEstudiante().getPrimerNombre() + \" \" + solicitud.getMentor().getEstudiante().getPrimerApellido())")
    @Mapping(target = "puedeCalificar", ignore = true)
    @Mapping(target = "horasRestantesCalificacion", ignore = true)
    SolicitudResponse toSolicitudResponse(SolicitudMentoria solicitud);
}
