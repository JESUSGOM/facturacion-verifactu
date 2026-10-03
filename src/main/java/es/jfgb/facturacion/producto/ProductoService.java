package es.jfgb.facturacion.producto;

import es.jfgb.facturacion.empresa.Empresa;
import es.jfgb.facturacion.empresa.EmpresaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

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

        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Empresa no encontrada"
                ));

        String codigo = normalizarCodigo(request.getCodigo());

        if (codigo != null &&
                productoRepository.existsByEmpresa_IdAndCodigoIgnoreCase(
                        empresaId,
                        codigo)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe un producto o servicio con el código " + codigo
            );
        }

        Producto producto = new Producto();

        producto.setEmpresa(empresa);
        producto.setCodigo(codigo);
        producto.setTipo(request.getTipo());
        producto.setNombre(request.getNombre().trim());
        producto.setDescripcion(request.getDescripcion());
        producto.setPrecio(request.getPrecio());
        producto.setTipoImpuesto(request.getTipoImpuesto());
        producto.setPorcentajeImpuesto(
                request.getPorcentajeImpuesto()
        );
        producto.setActivo(true);

        Producto guardado =
                productoRepository.saveAndFlush(producto);

        return convertir(guardado);
    }

    @Transactional(readOnly = true)
    public List<ProductoResponse> listar(Long empresaId) {

        comprobarEmpresa(empresaId);

        return productoRepository
                .findAllByEmpresa_IdOrderByNombreAsc(empresaId)
                .stream()
                .map(this::convertir)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductoResponse obtener(
            Long empresaId,
            Long productoId) {

        Producto producto = productoRepository
                .findByIdAndEmpresa_Id(productoId, empresaId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Producto o servicio no encontrado"
                ));

        return convertir(producto);
    }

    private void comprobarEmpresa(Long empresaId) {

        if (!empresaRepository.existsById(empresaId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Empresa no encontrada"
            );
        }
    }

    private String normalizarCodigo(String codigo) {

        if (codigo == null || codigo.isBlank()) {
            return null;
        }

        return codigo.trim().toUpperCase();
    }

    private ProductoResponse convertir(Producto producto) {

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