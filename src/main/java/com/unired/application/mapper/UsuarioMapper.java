package com.unired.application.mapper;

import com.unired.application.dto.response.EstudianteResponse;
import com.unired.application.dto.response.PerfilResponse;
import com.unired.application.dto.response.UsuarioBasicoResponse;
import com.unired.domain.model.Administrador;
import com.unired.domain.model.Estudiante;
import com.unired.domain.model.Usuario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {

    @Mapping(target = "rol", expression = "java(resolveRole(usuario))")
    @Mapping(target = "programaAcademico", expression = "java(usuario instanceof com.unired.domain.model.Estudiante e ? e.getProgramaAcademico() : null)")
    @Mapping(target = "semestre", expression = "java(usuario instanceof com.unired.domain.model.Estudiante e ? e.getSemestre() : null)")
    UsuarioBasicoResponse toBasicoResponse(Usuario usuario);

    EstudianteResponse toEstudianteResponse(Estudiante estudiante);

    @Mapping(target = "rol", constant = "ESTUDIANTE")
    PerfilResponse toPerfilResponse(Estudiante estudiante);

    default String resolveRole(Usuario usuario) {
        if (usuario == null) {
            return "";
        }
        if (usuario instanceof Administrador) {
            return "ADMINISTRADOR";
        }

        String className = usuario.getClass().getSimpleName().toUpperCase();
        return className.contains("ADMINISTRADOR") ? "ADMINISTRADOR" : "ESTUDIANTE";
    }
}
