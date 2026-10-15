package es.jfgb.facturacion.verifactu.aeat;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint manual de reconciliacion VERI*FACTU.
 *
 * Solo existe en el perfil de preproduccion AEAT.
 *
 * La existencia del endpoint NO habilita por si sola
 * ninguna consulta externa. El servicio conserva el
 * segundo cerrojo:
 *
 * verifactu.consulta-habilitada=false
 *
 * salvo habilitacion expresa.
 */
@RestController
@Profile("aeat-pre & !dev")
@RequestMapping(
        "/api/empresas/{empresaId}/registros-facturacion"
)
public class ReconciliacionVerifactuController {

    private final ServicioReconciliacionVerifactu
            servicioReconciliacion;

    public ReconciliacionVerifactuController(
            ServicioReconciliacionVerifactu
                    servicioReconciliacion) {

        if (servicioReconciliacion == null) {

            throw new IllegalArgumentException(
                    "El servicio de reconciliacion "
                            + "VERI*FACTU no puede ser nulo"
            );
        }

        this.servicioReconciliacion =
                servicioReconciliacion;
    }

    @PostMapping(
            "/{registroId}/reconciliar"
    )
    public ResultadoConsultaRegistroAeat reconciliar(
            @PathVariable Long empresaId,
            @PathVariable Long registroId) {

        return servicioReconciliacion.reconciliar(
                empresaId,
                registroId
        );
    }
}