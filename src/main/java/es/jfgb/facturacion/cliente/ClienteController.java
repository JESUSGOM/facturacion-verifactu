package es.jfgb.facturacion.cliente;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/empresas/{empresaId}/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClienteResponse crear(
            @PathVariable Long empresaId,
            @Valid @RequestBody ClienteRequest request) {

        return clienteService.crear(empresaId, request);
    }

    @GetMapping
    public List<ClienteResponse> listar(
            @PathVariable Long empresaId) {

        return clienteService.listar(empresaId);
    }

    @GetMapping("/{clienteId}")
    public ClienteResponse obtener(
            @PathVariable Long empresaId,
            @PathVariable Long clienteId) {

        return clienteService.obtener(empresaId, clienteId);
    }
}