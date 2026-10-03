package es.jfgb.facturacion.cliente;

import es.jfgb.facturacion.empresa.Empresa;
import es.jfgb.facturacion.empresa.EmpresaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;

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
    public ClienteResponse crear(
            Long empresaId,
            ClienteRequest request) {

        Empresa empresa = obtenerEmpresa(empresaId);

        String nif =
                normalizarNif(request.getNif());

        comprobarNifDisponible(
                empresaId,
                nif,
                null
        );

        Cliente cliente = new Cliente();

        cliente.setEmpresa(empresa);

        aplicarDatos(
                cliente,
                request,
                nif
        );

        cliente.setActivo(true);

        return convertir(
                clienteRepository.saveAndFlush(cliente)
        );
    }

    @Transactional
    public ClienteResponse modificar(
            Long empresaId,
            Long clienteId,
            ClienteRequest request) {

        Cliente cliente =
                clienteRepository
                        .findByIdAndEmpresa_Id(
                                clienteId,
                                empresaId
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Cliente no encontrado"
                                ));

        String nif =
                normalizarNif(request.getNif());

        comprobarNifDisponible(
                empresaId,
                nif,
                normalizarNif(cliente.getNif())
        );

        aplicarDatos(
                cliente,
                request,
                nif
        );

        return convertir(
                clienteRepository.saveAndFlush(cliente)
        );
    }

    @Transactional
    public ClienteResponse cambiarActivo(
            Long empresaId,
            Long clienteId,
            boolean activo) {

        Cliente cliente =
                clienteRepository
                        .findByIdAndEmpresa_Id(
                                clienteId,
                                empresaId
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Cliente no encontrado"
                                ));

        cliente.setActivo(activo);

        return convertir(
                clienteRepository.saveAndFlush(cliente)
        );
    }

    @Transactional(readOnly = true)
    public List<ClienteResponse> listar(
            Long empresaId) {

        comprobarEmpresa(empresaId);

        return clienteRepository
                .findAllByEmpresa_IdOrderByNombreAsc(
                        empresaId
                )
                .stream()
                .map(this::convertir)
                .toList();
    }

    @Transactional(readOnly = true)
    public ClienteResponse obtener(
            Long empresaId,
            Long clienteId) {

        Cliente cliente =
                clienteRepository
                        .findByIdAndEmpresa_Id(
                                clienteId,
                                empresaId
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Cliente no encontrado"
                                ));

        return convertir(cliente);
    }

    private Empresa obtenerEmpresa(
            Long empresaId) {

        Empresa empresa =
                empresaRepository
                        .findById(empresaId)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Empresa no encontrada"
                                ));

        if (!empresa.isActiva()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La empresa no está activa"
            );
        }

        return empresa;
    }

    private void comprobarEmpresa(
            Long empresaId) {

        if (!empresaRepository.existsById(
                empresaId
        )) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Empresa no encontrada"
            );
        }
    }

    private void comprobarNifDisponible(
            Long empresaId,
            String nifNuevo,
            String nifActual) {

        if (nifNuevo == null
                || Objects.equals(
                        nifNuevo,
                        nifActual
                )) {

            return;
        }

        if (clienteRepository
                .existsByEmpresa_IdAndNifIgnoreCase(
                        empresaId,
                        nifNuevo
                )) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe un cliente con el NIF "
                            + nifNuevo
            );
        }
    }

    private void aplicarDatos(
            Cliente cliente,
            ClienteRequest request,
            String nif) {

        cliente.setNif(nif);

        cliente.setNombre(
                request.getNombre().trim()
        );

        cliente.setNombreComercial(
                request.getNombreComercial()
        );

        cliente.setDireccion(
                request.getDireccion()
        );

        cliente.setCodigoPostal(
                request.getCodigoPostal()
        );

        cliente.setMunicipio(
                request.getMunicipio()
        );

        cliente.setProvincia(
                request.getProvincia()
        );

        if (request.getCodigoPais() != null
                && !request.getCodigoPais().isBlank()) {

            cliente.setCodigoPais(
                    request.getCodigoPais()
                            .trim()
                            .toUpperCase()
            );
        }

        cliente.setTelefono(
                request.getTelefono()
        );

        cliente.setEmail(
                request.getEmail()
        );
    }

    private String normalizarNif(
            String nif) {

        if (nif == null
                || nif.isBlank()) {

            return null;
        }

        return nif.trim().toUpperCase();
    }

    private ClienteResponse convertir(
            Cliente cliente) {

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