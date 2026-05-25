package com.unired.application.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardResponse {

    private UsuarioBasicoResponse usuario;
    private EstadisticasResponse estadisticas;
    private List<RecordatorioResponse> recordatoriosUrgentes;
    private List<ActividadResponse> proximosEventos;
}
