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

        Empresa empresa =
                obtenerEmpresa(empresaId);

        String codigo =
                normalizarCodigo(
                        request.getCodigo()
                );

        if (serieRepository
                .existsByEmpresa_IdAndCodigoIgnoreCase(
                        empresaId,
                        codigo
                )) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe una serie de facturación con el código "
                            + codigo
            );
        }

        SerieFacturacion serie =
                new SerieFacturacion();

        serie.setEmpresa(empresa);

        serie.setCodigo(codigo);

        serie.setDescripcion(
                request.getDescripcion().trim()
        );

        serie.setTipoSerie(
                request.getTipoSerie() != null
                        ? request.getTipoSerie()
                        : TipoSerieFacturacion.ORDINARIA
        );

        serie.setUltimoNumero(0L);
        serie.setActiva(true);

        return convertir(
                serieRepository.saveAndFlush(
                        serie
                )
        );
    }

    @Transactional
    public SerieFacturacionResponse modificar(
            Long empresaId,
            Long serieId,
            SerieFacturacionRequest request) {

        SerieFacturacion serie =
                serieRepository
                        .findConBloqueo(
                                serieId,
                                empresaId
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Serie de facturación no encontrada"
                                ));

        String codigoNuevo =
                normalizarCodigo(
                        request.getCodigo()
                );

        String codigoActual =
                normalizarCodigo(
                        serie.getCodigo()
                );

        boolean cambiaCodigo =
                !codigoNuevo.equals(
                        codigoActual
                );

        TipoSerieFacturacion tipoSerieNuevo =
                request.getTipoSerie() != null
                        ? request.getTipoSerie()
                        : serie.getTipoSerie();

        boolean cambiaTipoSerie =
                tipoSerieNuevo
                        != serie.getTipoSerie();

        /*
         * Después de haber numerado una factura,
         * el código de la serie pasa a ser histórico.
         */
        if (cambiaCodigo
                && serie.getUltimoNumero() != null
                && serie.getUltimoNumero() > 0L) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No se puede cambiar el código de una serie "
                            + "que ya tiene facturas emitidas"
            );
        }

        if (cambiaTipoSerie
                && serie.getUltimoNumero() != null
                && serie.getUltimoNumero() > 0L) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No se puede cambiar el tipo de una serie "
                            + "que ya tiene facturas emitidas"
            );
        }
        if (cambiaCodigo
                && serieRepository
                .existsByEmpresa_IdAndCodigoIgnoreCase(
                        empresaId,
                        codigoNuevo
                )) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe una serie de facturación con el código "
                            + codigoNuevo
            );
        }

        serie.setCodigo(
                codigoNuevo
        );

        serie.setDescripcion(
                request.getDescripcion().trim()
        );

        serie.setTipoSerie(
                tipoSerieNuevo
        );

        return convertir(
                serieRepository.saveAndFlush(
                        serie
                )
        );
    }

    @Transactional
    public SerieFacturacionResponse cambiarActiva(
            Long empresaId,
            Long serieId,
            boolean activa) {

        SerieFacturacion serie =
                serieRepository
                        .findConBloqueo(
                                serieId,
                                empresaId
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Serie de facturación no encontrada"
                                ));

        serie.setActiva(activa);

        return convertir(
                serieRepository.saveAndFlush(
                        serie
                )
        );
    }

    @Transactional(readOnly = true)
    public List<SerieFacturacionResponse> listar(
            Long empresaId) {

        comprobarEmpresa(
                empresaId
        );

        return serieRepository
                .findAllByEmpresa_IdOrderByCodigoAsc(
                        empresaId
                )
                .stream()
                .map(this::convertir)
                .toList();
    }

    @Transactional(readOnly = true)
    public SerieFacturacionResponse obtener(
            Long empresaId,
            Long serieId) {

        SerieFacturacion serie =
                serieRepository
                        .findByIdAndEmpresa_Id(
                                serieId,
                                empresaId
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Serie de facturación no encontrada"
                                ));

        return convertir(serie);
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

    private String normalizarCodigo(
            String codigo) {

        return codigo.trim().toUpperCase();
    }

    private SerieFacturacionResponse convertir(
            SerieFacturacion serie) {

        return new SerieFacturacionResponse(
                serie.getId(),
                serie.getEmpresa().getId(),
                serie.getCodigo(),
                serie.getDescripcion(),
                serie.getTipoSerie(),
                serie.getUltimoNumero(),
                serie.isActiva(),
                serie.getFechaCreacion(),
                serie.getFechaModificacion()
        );
    }
}