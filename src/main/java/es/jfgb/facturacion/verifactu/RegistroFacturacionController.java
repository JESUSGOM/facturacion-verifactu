package es.jfgb.facturacion.verifactu;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(
        "/api/empresas/{empresaId}/registros-facturacion"
)
public class RegistroFacturacionController {

    private final RegistroFacturacionService service;
    private final ServicioEnvioVerifactu servicioEnvio;
    private final RegistroFacturacionIntentoService intentoService;

    public RegistroFacturacionController(
            RegistroFacturacionService service,
            ServicioEnvioVerifactu servicioEnvio,
            RegistroFacturacionIntentoService intentoService) {

        this.service = service;
        this.servicioEnvio = servicioEnvio;
        this.intentoService = intentoService;
    }

    @GetMapping
    public List<RegistroFacturacionResponse> listar(
            @PathVariable Long empresaId) {

        return service.listar(empresaId);
    }

    @GetMapping("/{registroId}")
    public RegistroFacturacionResponse obtener(
            @PathVariable Long empresaId,
            @PathVariable Long registroId) {

        return service.obtener(
                empresaId,
                registroId
        );
    }

    @GetMapping("/{registroId}/intentos")
    public List<RegistroFacturacionIntentoResponse> listarIntentos(
            @PathVariable Long empresaId,
            @PathVariable Long registroId) {

        return intentoService.listar(
                empresaId,
                registroId
        );
    }

    @PostMapping("/{registroId}/enviar")
    public RegistroFacturacionResponse enviar(
            @PathVariable Long empresaId,
            @PathVariable Long registroId) {

        servicioEnvio.enviarAlta(
                empresaId,
                registroId
        );

        return service.obtener(
                empresaId,
                registroId
        );
    }
}