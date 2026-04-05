package com.unired.application.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardResponse {

    private String nombreEstudiante;
    private String programaAcademico;
    private Double promedioAcademico;
    private Integer eventosAsistidos;
    private Integer mentoriasActivas;
    private Integer notificacionesSinLeer;
    private List<RecordatorioResponse> recordatoriosUrgentes;
    private List<ActividadResponse> proximosEventos;
}
