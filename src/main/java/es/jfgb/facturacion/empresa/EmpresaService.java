package es.jfgb.facturacion.empresa;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class EmpresaService {

    private final EmpresaRepository empresaRepository;

    public EmpresaService(EmpresaRepository empresaRepository) {
        this.empresaRepository = empresaRepository;
    }

    @Transactional
    public EmpresaResponse crear(EmpresaRequest request) {

        String nif = request.getNif().trim().toUpperCase();

        if (empresaRepository.existsByNif(nif)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe una empresa con el NIF " + nif
            );
        }

        Empresa empresa = new Empresa();

        empresa.setNif(nif);
        empresa.setRazonSocial(request.getRazonSocial().trim());
        empresa.setNombreComercial(request.getNombreComercial());
        empresa.setDireccion(request.getDireccion());
        empresa.setCodigoPostal(request.getCodigoPostal());
        empresa.setMunicipio(request.getMunicipio());
        empresa.setProvincia(request.getProvincia());

        if (request.getCodigoPais() != null
                && !request.getCodigoPais().isBlank()) {

            empresa.setCodigoPais(
                    request.getCodigoPais().trim().toUpperCase()
            );
        }

        empresa.setTelefono(request.getTelefono());
        empresa.setEmail(request.getEmail());
        empresa.setTipoImpuesto(request.getTipoImpuesto());
        empresa.setActiva(true);

        Empresa guardada = empresaRepository.saveAndFlush(empresa);

        return convertir(guardada);
    }

    @Transactional(readOnly = true)
    public List<EmpresaResponse> listar() {

        return empresaRepository.findAll()
                .stream()
                .map(this::convertir)
                .toList();
    }

    @Transactional(readOnly = true)
    public EmpresaResponse obtener(Long id) {

        Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Empresa no encontrada"
                ));

        return convertir(empresa);
    }

    private EmpresaResponse convertir(Empresa empresa) {

        return new EmpresaResponse(
                empresa.getId(),
                empresa.getNif(),
                empresa.getRazonSocial(),
                empresa.getNombreComercial(),
                empresa.getDireccion(),
                empresa.getCodigoPostal(),
                empresa.getMunicipio(),
                empresa.getProvincia(),
                empresa.getCodigoPais(),
                empresa.getTelefono(),
                empresa.getEmail(),
                empresa.getTipoImpuesto(),
                empresa.isActiva(),
                empresa.getFechaCreacion(),
                empresa.getFechaModificacion()
        );
    }
}