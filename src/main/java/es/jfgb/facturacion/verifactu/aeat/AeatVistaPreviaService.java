package es.jfgb.facturacion.verifactu.aeat;

import es.jfgb.facturacion.verifactu.RegistroFacturacion;
import es.jfgb.facturacion.verifactu.RegistroFacturacionRepository;
import es.jfgb.facturacion.verifactu.TipoRegistroFacturacion;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AeatVistaPreviaService {

    private final RegistroFacturacionRepository
            registroRepository;

    private final AeatSolicitudAltaXmlBuilder
            altaXmlBuilder;

    private final AeatSolicitudAnulacionXmlBuilder
            anulacionXmlBuilder;

    public AeatVistaPreviaService(
            RegistroFacturacionRepository registroRepository,
            AeatSolicitudAltaXmlBuilder altaXmlBuilder,
            AeatSolicitudAnulacionXmlBuilder anulacionXmlBuilder) {

        this.registroRepository =
                registroRepository;

        this.altaXmlBuilder =
                altaXmlBuilder;

        this.anulacionXmlBuilder =
                anulacionXmlBuilder;
    }

    /**
     * Genera localmente el XML VERI*FACTU
     * correspondiente a un registro de facturacion.
     *
     * IMPORTANTE:
     *
     * - No modifica el registro.
     * - No incrementa intentos.
     * - No cambia PENDIENTE a ENVIANDO.
     * - No realiza ninguna conexion con AEAT.
     */
    @Transactional(readOnly = true)
    public String generarXml(
            Long empresaId,
            Long registroId) {

        RegistroFacturacion registro =
                registroRepository
                        .findByIdAndEmpresa_Id(
                                registroId,
                                empresaId
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Registro de facturacion "
                                                + "no encontrado"
                                ));

        TipoRegistroFacturacion tipoRegistro =
                registro.getTipoRegistro();

        if (tipoRegistro == null) {
            throw new IllegalStateException(
                    "El registro de facturacion "
                            + "no tiene tipo de registro"
            );
        }

        /*
         * La generacion se realiza dentro de esta
         * transaccion para que Hibernate pueda cargar
         * las relaciones LAZY necesarias.
         *
         * Para ALTA pueden ser necesarias, entre otras:
         *
         * - desgloses
         * - facturas rectificadas
         * - registro anterior
         *
         * Para ANULACION:
         *
         * - registro anterior
         */
        return switch (tipoRegistro) {

            case ALTA ->
                    altaXmlBuilder.generarXml(
                            registro
                    );

            case ANULACION ->
                    anulacionXmlBuilder.generarXml(
                            registro
                    );
        };
    }
}
