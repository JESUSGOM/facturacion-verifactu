package es.jfgb.facturacion.verifactu;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Profile("dev")
@RequestMapping("/api/dev/verifactu")
public class RecuperacionEnviosVerifactuController {

    private final RecuperacionEnviosVerifactuService recuperacionService;
    private final EstadoEnvioVerifactuService estadoEnvioService;

    public RecuperacionEnviosVerifactuController(
            RecuperacionEnviosVerifactuService recuperacionService,
            EstadoEnvioVerifactuService estadoEnvioService) {

        this.recuperacionService = recuperacionService;
        this.estadoEnvioService = estadoEnvioService;
    }

    @PostMapping("/recuperar-envios-antiguos")
    public Map<String, Object> recuperarEnviosAntiguos() {

        int recuperados =
                recuperacionService.recuperarEnviosAntiguos();

        return Map.of(
                "correcto", true,
                "registrosMarcadosComoInciertos", recuperados
        );
    }

    @PostMapping(
            "/empresas/{empresaId}/registros/{registroId}/iniciar-sin-enviar"
    )
    public Map<String, Object> iniciarSinEnviar(
            @PathVariable Long empresaId,
            @PathVariable Long registroId) {

        RegistroFacturacion registro =
                estadoEnvioService.iniciarEnvio(
                        empresaId,
                        registroId
                );

        return Map.of(
                "correcto", true,
                "registroId", registro.getId(),
                "estadoEnvio", registro.getEstadoEnvio().name(),
                "intentosEnvio", registro.getIntentosEnvio()
        );
    }
}