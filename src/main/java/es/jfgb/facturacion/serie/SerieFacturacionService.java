package es.jfgb.facturacion.serie;

import es.jfgb.facturacion.empresa.Empresa;
import es.jfgb.facturacion.empresa.EmpresaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class SerieFacturacionService {

    private final SerieFacturacionRepository serieRepository;
    private final EmpresaRepository empresaRepository;

    public SerieFacturacionService(
            SerieFacturacionRepository serieRepository,
            EmpresaRepository empresaRepository) {

        this.serieRepository = serieRepository;
        this.empresaRepository = empresaRepository;
    }

    @Transactional
    public SerieFacturacionResponse crear(
            Long empresaId,
            SerieFacturacionRequest request) {

        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Empresa no encontrada"
                ));

        String codigo = request.getCodigo()
                .trim()
                .toUpperCase();

        if (serieRepository
                .existsByEmpresa_IdAndCodigoIgnoreCase(
                        empresaId,
                        codigo)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe una serie de facturación con el código "
                            + codigo
            );
        }

        SerieFacturacion serie = new SerieFacturacion();

        serie.setEmpresa(empresa);
        serie.setCodigo(codigo);
        serie.setDescripcion(
                request.getDescripcion().trim()
        );
        serie.setUltimoNumero(0L);
        serie.setActiva(true);

        SerieFacturacion guardada =
                serieRepository.saveAndFlush(serie);

        return convertir(guardada);
    }

    @Transactional(readOnly = true)
    public List<SerieFacturacionResponse> listar(
            Long empresaId) {

        comprobarEmpresa(empresaId);

        return serieRepository
                .findAllByEmpresa_IdOrderByCodigoAsc(empresaId)
                .stream()
                .map(this::convertir)
                .toList();
    }

    @Transactional(readOnly = true)
    public SerieFacturacionResponse obtener(
            Long empresaId,
            Long serieId) {

        SerieFacturacion serie = serieRepository
                .findByIdAndEmpresa_Id(serieId, empresaId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Serie de facturación no encontrada"
                ));

        return convertir(serie);
    }

    private void comprobarEmpresa(Long empresaId) {

        if (!empresaRepository.existsById(empresaId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Empresa no encontrada"
            );
        }
    }

    private SerieFacturacionResponse convertir(
            SerieFacturacion serie) {

        return new SerieFacturacionResponse(
                serie.getId(),
                serie.getEmpresa().getId(),
                serie.getCodigo(),
                serie.getDescripcion(),
                serie.getUltimoNumero(),
                serie.isActiva(),
                serie.getFechaCreacion(),
                serie.getFechaModificacion()
        );
    }
}