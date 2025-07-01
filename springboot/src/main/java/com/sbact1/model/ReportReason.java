package com.sbact1.model;

public enum ReportReason {
    EVENTO_FALSO("Evento falso"),
    CONTENIDO_INAPROPIADO("Contenido inapropiado"),
    SPAM("Spam"),
    INFORMACIÓN_INCORRECTA("Información incorrecta"),
    LENGUAJE_OFENSIVO("Lenguaje ofensivo"),
    VIOLENCIA("Violencia"),
    UBICACIÓN_ERRÓNEA("Ubicación errónea"),
    OTRO("Otro");

    private final String descripcion;

    ReportReason(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
