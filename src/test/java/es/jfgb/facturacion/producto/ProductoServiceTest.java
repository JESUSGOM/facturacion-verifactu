package es.jfgb.facturacion.producto;

import es.jfgb.facturacion.empresa.Empresa;
import es.jfgb.facturacion.empresa.EmpresaRepository;
import es.jfgb.facturacion.empresa.TipoImpuesto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private EmpresaRepository empresaRepository;

    private ProductoService service;

    @BeforeEach
    void setUp() {

        service =
                new ProductoService(
                        productoRepository,
                        empresaRepository
                );
    }

    @Test
    void modificarActualizaYNormalizaCodigo() {

        Empresa empresa = empresa(true);

        Producto producto =
                productoBase(empresa);

        ProductoRequest request =
                request(
                        " srv-02 ",
                        " Servicio modificado "
                );

        when(
                productoRepository
                        .findByIdAndEmpresa_Id(
                                20L,
                                1L
                        )
        ).thenReturn(
                Optional.of(producto)
        );

        when(
                productoRepository
                        .existsByEmpresa_IdAndCodigoIgnoreCase(
                                1L,
                                "SRV-02"
                        )
        ).thenReturn(false);

        when(
                productoRepository.saveAndFlush(
                        producto
                )
        ).thenReturn(producto);

        ProductoResponse response =
                service.modificar(
                        1L,
                        20L,
                        request
                );

        assertEquals(
                "SRV-02",
                producto.getCodigo()
        );

        assertEquals(
                "Servicio modificado",
                producto.getNombre()
        );

        assertEquals(
                "SRV-02",
                response.codigo()
        );

        assertTrue(
                response.activo()
        );
    }

    @Test
    void modificarRechazaCodigoDuplicado() {

        Empresa empresa = empresa(true);

        Producto producto =
                productoBase(empresa);

        ProductoRequest request =
                request(
                        "DUPLICADO",
                        "Servicio"
                );

        when(
                productoRepository
                        .findByIdAndEmpresa_Id(
                                20L,
                                1L
                        )
        ).thenReturn(
                Optional.of(producto)
        );

        when(
                productoRepository
                        .existsByEmpresa_IdAndCodigoIgnoreCase(
                                1L,
                                "DUPLICADO"
                        )
        ).thenReturn(true);

        ResponseStatusException ex =
                assertThrows(
                        ResponseStatusException.class,
                        () -> service.modificar(
                                1L,
                                20L,
                                request
                        )
                );

        assertEquals(
                HttpStatus.CONFLICT,
                ex.getStatusCode()
        );

        verify(
                productoRepository,
                never()
        ).saveAndFlush(
                any()
        );
    }

    @Test
    void cambiarActivoDesactivaProducto() {

        Empresa empresa = empresa(true);

        Producto producto =
                productoBase(empresa);

        when(
                productoRepository
                        .findByIdAndEmpresa_Id(
                                20L,
                                1L
                        )
        ).thenReturn(
                Optional.of(producto)
        );

        when(
                productoRepository.saveAndFlush(
                        producto
                )
        ).thenReturn(producto);

        ProductoResponse response =
                service.cambiarActivo(
                        1L,
                        20L,
                        false
                );

        assertFalse(
                producto.isActivo()
        );

        assertFalse(
                response.activo()
        );
    }

    @Test
    void crearRechazaEmpresaInactiva() {

        Empresa empresa =
                empresa(false);

        when(
                empresaRepository.findById(1L)
        ).thenReturn(
                Optional.of(empresa)
        );

        ResponseStatusException ex =
                assertThrows(
                        ResponseStatusException.class,
                        () -> service.crear(
                                1L,
                                request(
                                        "SRV-01",
                                        "Servicio"
                                )
                        )
                );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                ex.getStatusCode()
        );

        verify(
                productoRepository,
                never()
        ).saveAndFlush(
                any()
        );
    }

    private Empresa empresa(
            boolean activa) {

        Empresa empresa =
                new Empresa();

        empresa.setActiva(
                activa
        );

        return empresa;
    }

    private Producto productoBase(
            Empresa empresa) {

        Producto producto =
                new Producto();

        producto.setEmpresa(
                empresa
        );

        producto.setCodigo(
                "SRV-01"
        );

        producto.setTipo(
                TipoProducto.values()[0]
        );

        producto.setNombre(
                "Servicio original"
        );

        producto.setPrecio(
                new BigDecimal("10.0000")
        );

        producto.setTipoImpuesto(
                TipoImpuesto.values()[0]
        );

        producto.setPorcentajeImpuesto(
                new BigDecimal("7.00")
        );

        producto.setActivo(
                true
        );

        return producto;
    }

    private ProductoRequest request(
            String codigo,
            String nombre) {

        ProductoRequest request =
                new ProductoRequest();

        request.setCodigo(codigo);

        request.setTipo(
                TipoProducto.values()[0]
        );

        request.setNombre(nombre);

        request.setPrecio(
                new BigDecimal("20.0000")
        );

        request.setTipoImpuesto(
                TipoImpuesto.values()[0]
        );

        request.setPorcentajeImpuesto(
                new BigDecimal("7.00")
        );

        return request;
    }
}