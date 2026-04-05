package com.unired.application.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EstadisticasResponse {

    private Integer eventosInscritos;
    private Integer eventosAsistidos;
    private Integer mentoriasSolicitadas;
    private Integer mentoriasActivas;
}
