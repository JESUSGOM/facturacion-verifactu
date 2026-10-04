package es.jfgb.facturacion.verifactu;

import es.jfgb.facturacion.verifactu.provider.EnvioVerifactuInciertoException;
import es.jfgb.facturacion.verifactu.provider.ResultadoEnvioVerifactu;
import es.jfgb.facturacion.verifactu.provider.VerifactuProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ServicioEnvioVerifactu {

    private final EstadoEnvioVerifactuService estadoEnvioService;
    private final VerifactuProvider provider;
    private final boolean envioHabilitado;

    public ServicioEnvioVerifactu(
            EstadoEnvioVerifactuService estadoEnvioService,
            VerifactuProvider provider,
            @Value("${verifactu.envio-habilitado:false}")
            boolean envioHabilitado) {

        this.estadoEnvioService = estadoEnvioService;
        this.provider = provider;
        this.envioHabilitado = envioHabilitado;
    }

    public void enviarAlta(
            Long empresaId,
            Long registroId) {

        enviar(
                empresaId,
                registroId,
                TipoRegistroFacturacion.ALTA
        );
    }

    public void enviarAnulacion(
            Long empresaId,
            Long registroId) {

        enviar(
                empresaId,
                registroId,
                TipoRegistroFacturacion.ANULACION
        );
    }

    private void enviar(
            Long empresaId,
            Long registroId,
            TipoRegistroFacturacion tipoRegistro) {

        /*
         * PRIMER CERROJO:
         * debe habilitarse expresamente.
         *
         * Se comprueba antes de tocar la BD.
         */
        if (!envioHabilitado) {

            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "El envio VERI*FACTU esta deshabilitado "
                            + "por configuracion"
            );
        }

        /*
         * SEGUNDO CERROJO:
         * el proveedor puede bloquear cualquier envio.
         *
         * Tambien se comprueba antes de tocar la BD.
         */
        if (provider.bloqueaEnvio()) {

            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "No existe ningun proveedor VERI*FACTU "
                            + "habilitado para el perfil actual"
            );
        }

        /*
         * Mantenemos la llamada historica de dos argumentos
         * para ALTA y utilizamos la nueva comprobacion
         * explicita de tipo para ANULACION.
         */
        RegistroFacturacion registro;

        if (tipoRegistro
                == TipoRegistroFacturacion.ALTA) {

            registro =
                    estadoEnvioService
                            .iniciarEnvio(
                                    empresaId,
                                    registroId
                            );

        } else {

            registro =
                    estadoEnvioService
                            .iniciarEnvio(
                                    empresaId,
                                    registroId,
                                    tipoRegistro
                            );
        }

        String nombreProveedor =
                provider.getNombre();

        ResultadoEnvioVerifactu resultado;

        try {

            resultado =
                    switch (tipoRegistro) {

                        case ALTA ->
                                provider.enviarAlta(
                                        registro
                                );

                        case ANULACION ->
                                provider.enviarAnulacion(
                                        registro
                                );
                    };

        } catch (EnvioVerifactuInciertoException e) {

            estadoEnvioService.registrarIncierto(
                    empresaId,
                    registroId,
                    nombreProveedor,
                    e
            );

            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "El resultado del envio al proveedor "
                            + "es incierto y debe reconciliarse "
                            + "antes de reenviar",
                    e
            );

        } catch (Exception e) {

            estadoEnvioService.registrarError(
                    empresaId,
                    registroId,
                    nombreProveedor,
                    e
            );

            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Error al enviar el registro al proveedor",
                    e
            );
        }

        /*
         * El proveedor ya ha respondido.
         *
         * Si esta persistencia falla, el registro
         * no debe reenviarse hasta reconciliarlo.
         */
        try {

            estadoEnvioService.finalizarEnvio(
                    empresaId,
                    registroId,
                    nombreProveedor,
                    resultado
            );

        } catch (Exception e) {

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "El proveedor respondio, pero no se pudo "
                            + "guardar el resultado del envio. "
                            + "El registro no debe reenviarse "
                            + "hasta reconciliar su estado.",
                    e
            );
        }
    }
}