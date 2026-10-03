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

        /*
         * =========================================================
         * PRIMER CERROJO DE SEGURIDAD
         * =========================================================
         *
         * El envío debe estar habilitado expresamente.
         *
         * Esta comprobación se realiza ANTES de:
         *
         * - bloquear el registro,
         * - incrementar intentos,
         * - crear historial,
         * - marcarlo como ENVIANDO,
         * - llamar al proveedor.
         */

        if (!envioHabilitado) {

            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "El envío VERI*FACTU está deshabilitado "
                            + "por configuración"
            );
        }

        /*
         * =========================================================
         * SEGUNDO CERROJO DE SEGURIDAD
         * =========================================================
         *
         * Aunque alguien habilitase accidentalmente
         * verifactu.envio-habilitado=true, un proveedor
         * deliberadamente deshabilitado impide continuar.
         *
         * También se comprueba ANTES de modificar la BD.
         */

        if (provider.bloqueaEnvio()) {

            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "No existe ningún proveedor VERI*FACTU "
                            + "habilitado para el perfil actual"
            );
        }

        /*
         * =========================================================
         * TRANSACCION 1
         * =========================================================
         *
         * Bloquea el registro, valida su estado,
         * incrementa el número de intento,
         * crea el intento histórico,
         * lo marca como ENVIANDO
         * y hace COMMIT.
         */

        RegistroFacturacion registro =
                estadoEnvioService.iniciarEnvio(
                        empresaId,
                        registroId
                );

        String nombreProveedor =
                provider.getNombre();

        /*
         * =========================================================
         * LLAMADA EXTERNA AL PROVEEDOR
         * =========================================================
         *
         * Aquí no existe una transacción de base de datos.
         */

        ResultadoEnvioVerifactu resultado;

        try {

            resultado =
                    provider.enviarAlta(
                            registro
                    );

        } catch (EnvioVerifactuInciertoException e) {

            /*
             * La petición puede haber llegado al proveedor,
             * pero no conocemos con certeza el resultado.
             *
             * NO debe convertirse en ERROR porque eso
             * permitiría un posible reenvío.
             */

            estadoEnvioService.registrarIncierto(
                    empresaId,
                    registroId,
                    nombreProveedor,
                    e
            );

            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "El resultado del envío al proveedor "
                            + "es incierto y debe reconciliarse "
                            + "antes de reenviar",
                    e
            );

        } catch (Exception e) {

            /*
             * Error considerado seguro.
             */

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
         * =========================================================
         * TRANSACCION 2
         * =========================================================
         *
         * El proveedor YA ha respondido.
         *
         * Si falla la persistencia de esta respuesta,
         * no debemos convertir el registro en ERROR.
         *
         * Permanecerá normalmente en ENVIANDO y el
         * mecanismo de recuperación lo convertirá
         * posteriormente en INCIERTO.
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
                    "El proveedor respondió, pero no se pudo "
                            + "guardar el resultado del envío. "
                            + "El registro no debe reenviarse "
                            + "hasta reconciliar su estado.",
                    e
            );
        }
    }
}