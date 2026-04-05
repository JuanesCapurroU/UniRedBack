package com.unired.application.mapper;

import com.unired.application.dto.response.ConfiguracionNotificacionesResponse;
import com.unired.application.dto.response.NotificacionResponse;
import com.unired.application.dto.response.RecordatorioResponse;
import com.unired.domain.model.ConfiguracionNotificaciones;
import com.unired.domain.model.Notificacion;
import com.unired.domain.model.Recordatorio;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NotificacionMapper {

    NotificacionResponse toResponse(Notificacion notificacion);

    ConfiguracionNotificacionesResponse toConfigResponse(ConfiguracionNotificaciones configuracionNotificaciones);

    RecordatorioResponse toRecordatorioResponse(Recordatorio recordatorio);
}
