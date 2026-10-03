package es.jfgb.facturacion.cliente;

import es.jfgb.facturacion.empresa.Empresa;
import es.jfgb.facturacion.empresa.EmpresaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final EmpresaRepository empresaRepository;

    public ClienteService(
            ClienteRepository clienteRepository,
            EmpresaRepository empresaRepository) {

        this.clienteRepository = clienteRepository;
        this.empresaRepository = empresaRepository;
    }

    @Transactional
    public ClienteResponse crear(Long empresaId, ClienteRequest request) {

        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Empresa no encontrada"
                ));

        String nif = normalizarNif(request.getNif());

        if (nif != null &&
                clienteRepository.existsByEmpresa_IdAndNifIgnoreCase(
                        empresaId, nif)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe un cliente con el NIF " + nif
            );
        }

        Cliente cliente = new Cliente();

        cliente.setEmpresa(empresa);
        cliente.setNif(nif);
        cliente.setNombre(request.getNombre().trim());
        cliente.setNombreComercial(request.getNombreComercial());
        cliente.setDireccion(request.getDireccion());
        cliente.setCodigoPostal(request.getCodigoPostal());
        cliente.setMunicipio(request.getMunicipio());
        cliente.setProvincia(request.getProvincia());

        if (request.getCodigoPais() != null
                && !request.getCodigoPais().isBlank()) {

            cliente.setCodigoPais(
                    request.getCodigoPais().trim().toUpperCase()
            );
        }

        cliente.setTelefono(request.getTelefono());
        cliente.setEmail(request.getEmail());
        cliente.setActivo(true);

        Cliente guardado = clienteRepository.saveAndFlush(cliente);

        return convertir(guardado);
    }

    @Transactional(readOnly = true)
    public List<ClienteResponse> listar(Long empresaId) {

        comprobarEmpresa(empresaId);

        return clienteRepository
                .findAllByEmpresa_IdOrderByNombreAsc(empresaId)
                .stream()
                .map(this::convertir)
                .toList();
    }

    @Transactional(readOnly = true)
    public ClienteResponse obtener(Long empresaId, Long clienteId) {

        Cliente cliente = clienteRepository
                .findByIdAndEmpresa_Id(clienteId, empresaId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Cliente no encontrado"
                ));

        return convertir(cliente);
    }

    private void comprobarEmpresa(Long empresaId) {

        if (!empresaRepository.existsById(empresaId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Empresa no encontrada"
            );
        }
    }

    private String normalizarNif(String nif) {

        if (nif == null || nif.isBlank()) {
            return null;
        }

        return nif.trim().toUpperCase();
    }

    private ClienteResponse convertir(Cliente cliente) {

        return new ClienteResponse(
                cliente.getId(),
                cliente.getEmpresa().getId(),
                cliente.getNif(),
                cliente.getNombre(),
                cliente.getNombreComercial(),
                cliente.getDireccion(),
                cliente.getCodigoPostal(),
                cliente.getMunicipio(),
                cliente.getProvincia(),
                cliente.getCodigoPais(),
                cliente.getTelefono(),
                cliente.getEmail(),
                cliente.isActivo(),
                cliente.getFechaCreacion(),
                cliente.getFechaModificacion()
        );
    }
}