package es.jfgb.facturacion.verifactu.aeat;

import es.jfgb.facturacion.verifactu.RegistroFacturacion;
import es.jfgb.facturacion.verifactu.RegistroFacturacionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AeatVistaPreviaService {

    private final RegistroFacturacionRepository
            registroRepository;

    private final AeatSolicitudAltaXmlBuilder
            xmlBuilder;

    public AeatVistaPreviaService(
            RegistroFacturacionRepository registroRepository,
            AeatSolicitudAltaXmlBuilder xmlBuilder) {

        this.registroRepository =
                registroRepository;

        this.xmlBuilder =
                xmlBuilder;
    }

    /**
     * Genera localmente el XML VERI*FACTU
     * correspondiente a un registro de facturación.
     *
     * IMPORTANTE:
     *
     * - No modifica el registro.
     * - No incrementa intentos.
     * - No cambia PENDIENTE a ENVIANDO.
     * - No realiza ninguna conexión con AEAT.
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
                                        "Registro de facturación "
                                                + "no encontrado"
                                ));

        /*
         * La generación se realiza dentro de esta
         * transacción para que Hibernate pueda cargar
         * las relaciones LAZY necesarias:
         *
         * - desgloses
         * - registro anterior
         */
        return xmlBuilder.generarXml(
                registro
        );
    }
}