package com.catjard.solicitudes.model;

// Estados alineados 1:1 con el workflow de Jira (3 categorias):
// Por hacer -> En curso -> Finalizado. No mantenemos un flujo propio.
public enum EstadoSolicitud {
    por_hacer, en_curso, finalizado
}
