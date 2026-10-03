package es.jfgb.facturacion.producto;

import es.jfgb.facturacion.empresa.Empresa;
import es.jfgb.facturacion.empresa.EmpresaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final EmpresaRepository empresaRepository;

    public ProductoService(
            ProductoRepository productoRepository,
            EmpresaRepository empresaRepository) {

        this.productoRepository = productoRepository;
        this.empresaRepository = empresaRepository;
    }

    @Transactional
    public ProductoResponse crear(
            Long empresaId,
            ProductoRequest request) {

        Empresa empresa =
                obtenerEmpresa(empresaId);

        String codigo =
                normalizarCodigo(
                        request.getCodigo()
                );

        comprobarCodigoDisponible(
                empresaId,
                codigo,
                null
        );

        Producto producto =
                new Producto();

        producto.setEmpresa(empresa);

        aplicarDatos(
                producto,
                request,
                codigo
        );

        producto.setActivo(true);

        return convertir(
                productoRepository.saveAndFlush(
                        producto
                )
        );
    }

    @Transactional
    public ProductoResponse modificar(
            Long empresaId,
            Long productoId,
            ProductoRequest request) {

        Producto producto =
                productoRepository
                        .findByIdAndEmpresa_Id(
                                productoId,
                                empresaId
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Producto o servicio no encontrado"
                                ));

        String codigo =
                normalizarCodigo(
                        request.getCodigo()
                );

        comprobarCodigoDisponible(
                empresaId,
                codigo,
                normalizarCodigo(
                        producto.getCodigo()
                )
        );

        aplicarDatos(
                producto,
                request,
                codigo
        );

        return convertir(
                productoRepository.saveAndFlush(
                        producto
                )
        );
    }

    @Transactional
    public ProductoResponse cambiarActivo(
            Long empresaId,
            Long productoId,
            boolean activo) {

        Producto producto =
                productoRepository
                        .findByIdAndEmpresa_Id(
                                productoId,
                                empresaId
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Producto o servicio no encontrado"
                                ));

        producto.setActivo(activo);

        return convertir(
                productoRepository.saveAndFlush(
                        producto
                )
        );
    }

    @Transactional(readOnly = true)
    public List<ProductoResponse> listar(
            Long empresaId) {

        comprobarEmpresa(
                empresaId
        );

        return productoRepository
                .findAllByEmpresa_IdOrderByNombreAsc(
                        empresaId
                )
                .stream()
                .map(this::convertir)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductoResponse obtener(
            Long empresaId,
            Long productoId) {

        Producto producto =
                productoRepository
                        .findByIdAndEmpresa_Id(
                                productoId,
                                empresaId
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Producto o servicio no encontrado"
                                ));

        return convertir(producto);
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

    private void comprobarCodigoDisponible(
            Long empresaId,
            String codigoNuevo,
            String codigoActual) {

        if (codigoNuevo == null
                || Objects.equals(
                        codigoNuevo,
                        codigoActual
                )) {

            return;
        }

        if (productoRepository
                .existsByEmpresa_IdAndCodigoIgnoreCase(
                        empresaId,
                        codigoNuevo
                )) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe un producto o servicio con el código "
                            + codigoNuevo
            );
        }
    }

    private void aplicarDatos(
            Producto producto,
            ProductoRequest request,
            String codigo) {

        producto.setCodigo(codigo);

        producto.setTipo(
                request.getTipo()
        );

        producto.setNombre(
                request.getNombre().trim()
        );

        producto.setDescripcion(
                request.getDescripcion()
        );

        producto.setPrecio(
                request.getPrecio()
        );

        producto.setTipoImpuesto(
                request.getTipoImpuesto()
        );

        producto.setPorcentajeImpuesto(
                request.getPorcentajeImpuesto()
        );
    }

    private String normalizarCodigo(
            String codigo) {

        if (codigo == null
                || codigo.isBlank()) {

            return null;
        }

        return codigo.trim().toUpperCase();
    }

    private ProductoResponse convertir(
            Producto producto) {

        return new ProductoResponse(
                producto.getId(),
                producto.getEmpresa().getId(),
                producto.getCodigo(),
                producto.getTipo(),
                producto.getNombre(),
                producto.getDescripcion(),
                producto.getPrecio(),
                producto.getTipoImpuesto(),
                producto.getPorcentajeImpuesto(),
                producto.isActivo(),
                producto.getFechaCreacion(),
                producto.getFechaModificacion()
        );
    }
}