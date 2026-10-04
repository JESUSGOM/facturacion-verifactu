package es.jfgb.facturacion.serie;

import es.jfgb.facturacion.empresa.Empresa;
import es.jfgb.facturacion.empresa.EmpresaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SerieFacturacionServiceTest {

    @Mock
    private SerieFacturacionRepository serieRepository;

    @Mock
    private EmpresaRepository empresaRepository;

    private SerieFacturacionService service;

    @BeforeEach
    void setUp() {

        service =
                new SerieFacturacionService(
                        serieRepository,
                        empresaRepository
                );
    }

    @Test
    void modificarSerieSinUsoPermiteCambiarCodigo() {

        Empresa empresa = empresa(true);

        SerieFacturacion serie =
                serieBase(
                        empresa,
                        0L
                );

        SerieFacturacionRequest request =
                request(
                        " f2027 ",
                        " Serie modificada "
                );

        when(
                serieRepository.findConBloqueo(
                        30L,
                        1L
                )
        ).thenReturn(
                Optional.of(serie)
        );

        when(
                serieRepository
                        .existsByEmpresa_IdAndCodigoIgnoreCase(
                                1L,
                                "F2027"
                        )
        ).thenReturn(false);

        when(
                serieRepository.saveAndFlush(
                        serie
                )
        ).thenReturn(serie);

        SerieFacturacionResponse response =
                service.modificar(
                        1L,
                        30L,
                        request
                );

        assertEquals(
                "F2027",
                serie.getCodigo()
        );

        assertEquals(
                "Serie modificada",
                serie.getDescripcion()
        );

        assertEquals(
                "F2027",
                response.codigo()
        );
    }

    @Test
    void modificarSerieUsadaNoPermiteCambiarCodigo() {

        Empresa empresa = empresa(true);

        SerieFacturacion serie =
                serieBase(
                        empresa,
                        10L
                );

        when(
                serieRepository.findConBloqueo(
                        30L,
                        1L
                )
        ).thenReturn(
                Optional.of(serie)
        );

        ResponseStatusException ex =
                assertThrows(
                        ResponseStatusException.class,
                        () -> service.modificar(
                                1L,
                                30L,
                                request(
                                        "OTRA",
                                        "Otra serie"
                                )
                        )
                );

        assertEquals(
                HttpStatus.CONFLICT,
                ex.getStatusCode()
        );

        verify(
                serieRepository,
                never()
        ).saveAndFlush(
                any()
        );
    }

    @Test
    void modificarRechazaCodigoDuplicado() {

        Empresa empresa = empresa(true);

        SerieFacturacion serie =
                serieBase(
                        empresa,
                        0L
                );

        when(
                serieRepository.findConBloqueo(
                        30L,
                        1L
                )
        ).thenReturn(
                Optional.of(serie)
        );

        when(
                serieRepository
                        .existsByEmpresa_IdAndCodigoIgnoreCase(
                                1L,
                                "DUPLICADA"
                        )
        ).thenReturn(true);

        ResponseStatusException ex =
                assertThrows(
                        ResponseStatusException.class,
                        () -> service.modificar(
                                1L,
                                30L,
                                request(
                                        "DUPLICADA",
                                        "Serie"
                                )
                        )
                );

        assertEquals(
                HttpStatus.CONFLICT,
                ex.getStatusCode()
        );
    }

    @Test
    void cambiarActivaDesactivaSerie() {

        Empresa empresa = empresa(true);

        SerieFacturacion serie =
                serieBase(
                        empresa,
                        4L
                );

        when(
                serieRepository.findConBloqueo(
                        30L,
                        1L
                )
        ).thenReturn(
                Optional.of(serie)
        );

        when(
                serieRepository.saveAndFlush(
                        serie
                )
        ).thenReturn(serie);

        SerieFacturacionResponse response =
                service.cambiarActiva(
                        1L,
                        30L,
                        false
                );

        assertFalse(
                serie.isActiva()
        );

        assertFalse(
                response.activa()
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
                                        "F2026",
                                        "Facturas"
                                )
                        )
                );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                ex.getStatusCode()
        );

        verify(
                serieRepository,
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

    @Test
    void modificarSerieSinUsoPermiteCambiarTipo() {

        Empresa empresa =
                empresa(true);

        SerieFacturacion serie =
                serieBase(
                        empresa,
                        0L
                );

        assertEquals(
                TipoSerieFacturacion.ORDINARIA,
                serie.getTipoSerie()
        );

        SerieFacturacionRequest request =
                request(
                        "F2026",
                        "Facturas 2026"
                );

        request.setTipoSerie(
                TipoSerieFacturacion.RECTIFICATIVA
        );

        when(
                serieRepository.findConBloqueo(
                        30L,
                        1L
                )
        ).thenReturn(
                Optional.of(serie)
        );

        when(
                serieRepository.saveAndFlush(
                        serie
                )
        ).thenReturn(
                serie
        );

        SerieFacturacionResponse response =
                service.modificar(
                        1L,
                        30L,
                        request
                );

        assertEquals(
                TipoSerieFacturacion.RECTIFICATIVA,
                serie.getTipoSerie()
        );

        assertEquals(
                TipoSerieFacturacion.RECTIFICATIVA,
                response.tipoSerie()
        );
    }

    @Test
    void modificarSerieUsadaNoPermiteCambiarTipo() {

        Empresa empresa =
                empresa(true);

        SerieFacturacion serie =
                serieBase(
                        empresa,
                        10L
                );

        SerieFacturacionRequest request =
                request(
                        "F2026",
                        "Facturas 2026"
                );

        request.setTipoSerie(
                TipoSerieFacturacion.RECTIFICATIVA
        );

        when(
                serieRepository.findConBloqueo(
                        30L,
                        1L
                )
        ).thenReturn(
                Optional.of(serie)
        );

        ResponseStatusException ex =
                assertThrows(
                        ResponseStatusException.class,
                        () ->
                                service.modificar(
                                        1L,
                                        30L,
                                        request
                                )
                );

        assertEquals(
                HttpStatus.CONFLICT,
                ex.getStatusCode()
        );

        assertEquals(
                TipoSerieFacturacion.ORDINARIA,
                serie.getTipoSerie()
        );

        verify(
                serieRepository,
                never()
        ).saveAndFlush(
                any()
        );
    }
    private SerieFacturacion serieBase(
            Empresa empresa,
            Long ultimoNumero) {

        SerieFacturacion serie =
                new SerieFacturacion();

        serie.setEmpresa(
                empresa
        );

        serie.setCodigo(
                "F2026"
        );

        serie.setDescripcion(
                "Facturas 2026"
        );

        serie.setUltimoNumero(
                ultimoNumero
        );

        serie.setActiva(
                true
        );

        return serie;
    }

    private SerieFacturacionRequest request(
            String codigo,
            String descripcion) {

        SerieFacturacionRequest request =
                new SerieFacturacionRequest();

        request.setCodigo(
                codigo
        );

        request.setDescripcion(
                descripcion
        );

        return request;
    }
}