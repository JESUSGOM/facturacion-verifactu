package es.jfgb.facturacion.verifactu;

import java.time.Instant;

public record RegistroFacturacionIntentoResponse(

        Long id,
        Long registroFacturacionId,
        Integer numeroIntento,
        EstadoEnvioRegistro estado,
        String proveedor,
        String identificadorExterno,
        String peticion,
        String respuesta,
        String error,
        Instant fechaInicio,
        Instant fechaFin

) {

    public static RegistroFacturacionIntentoResponse from(
            RegistroFacturacionIntento intento) {

        return new RegistroFacturacionIntentoResponse(
                intento.getId(),
                intento.getRegistroFacturacion().getId(),
                intento.getNumeroIntento(),
                intento.getEstado(),
                intento.getProveedor(),
                intento.getIdentificadorExterno(),
                intento.getPeticion(),
                intento.getRespuesta(),
                intento.getError(),
                intento.getFechaInicio(),
                intento.getFechaFin()
        );
    }
}