package es.jfgb.facturacion.verifactu.aeat;

import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile("dev")
@RequestMapping("/api/dev/verifactu")
public class AeatVistaPreviaDevController {

    private final AeatVistaPreviaService
            vistaPreviaService;

    public AeatVistaPreviaDevController(
            AeatVistaPreviaService vistaPreviaService) {

        this.vistaPreviaService =
                vistaPreviaService;
    }

    /**
     * Genera una vista previa local del XML que
     * correspondería al registro VERI*FACTU.
     *
     * SOLO PERFIL DEV.
     *
     * Este endpoint:
     *
     * - no envía nada a AEAT;
     * - no cambia el estado del registro;
     * - no incrementa intentos;
     * - no modifica la base de datos.
     */
    @GetMapping(
            value = "/empresas/{empresaId}"
                    + "/registros/{registroId}/xml",
            produces = MediaType.APPLICATION_XML_VALUE
    )
    public String generarXml(
            @PathVariable Long empresaId,
            @PathVariable Long registroId) {

        return vistaPreviaService.generarXml(
                empresaId,
                registroId
        );
    }
}