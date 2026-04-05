package com.unired.application.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RecordatorioResponse {

    private Long id;
    private String descripcion;
    private LocalDateTime fechaVencimiento;
    private String prioridad;
}
