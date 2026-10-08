package es.jfgb.facturacion.verifactu;

import es.jfgb.facturacion.empresa.Empresa;
import es.jfgb.facturacion.empresa.EmpresaRepository;
import es.jfgb.facturacion.factura.EstadoFactura;
import es.jfgb.facturacion.factura.Factura;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistroFacturacionAnulacionF2R5ServiceTest {

    private static final String HUELLA_ANTERIOR =
            "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";

    private static final String HUELLA_NUEVA =
            "BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB";

    @Mock
    private EmpresaRepository empresaRepository;

    @Mock
    private RegistroFacturacionRepository registroRepository;

    @Mock
    private HuellaVerifactuService huellaVerifactuService;

    @Mock
    private VerifactuSistemaProperties sistemaProperties;

    @Mock
    private Empresa empresa;

    private RegistroFacturacionService service;

    @BeforeEach
    void setUp() {

        service =
                new RegistroFacturacionService(
                        empresaRepository,
                        registroRepository,
                        huellaVerifactuService,
                        sistemaProperties
                );
    }

    @Test
    void crearAnulacionF2ConservaAltaHistoricaYEncadenamiento() {

        probarAnulacion(
                36L,
                "SIM",
                1L,
                "SIM-1",
                LocalDate.of(2026, 10, 7),
                "F2",
                new BigDecimal("0.88"),
                new BigDecimal("13.38")
        );
    }

    @Test
    void crearAnulacionR5ConservaAltaHistoricaYEncadenamiento() {

        probarAnulacion(
                37L,
                "RECT",
                3L,
                "RECT-3",
                LocalDate.of(2026, 10, 8),
                "R5",
                new BigDecimal("-0.88"),
                new BigDecimal("-13.38")
        );
    }

    private void probarAnulacion(
            Long facturaId,
            String serie,
            Long numero,
            String numSerieFactura,
            LocalDate fechaExpedicion,
            String tipoFactura,
            BigDecimal cuotaTotal,
            BigDecimal importeTotal) {

        Factura factura =
                org.mockito.Mockito.mock(
                        Factura.class
                );

        RegistroFacturacion altaOriginal =
                new RegistroFacturacion();

        RegistroFacturacion anterior =
                new RegistroFacturacion();

        when(
                factura.getId()
        ).thenReturn(
                facturaId
        );

        when(
                factura.getEmpresa()
        ).thenReturn(
                empresa
        );

        when(
                factura.getEstado()
        ).thenReturn(
                EstadoFactura.EMITIDA
        );

        when(
                empresa.getId()
        ).thenReturn(
                1L
        );

        when(
                empresa.getVerifactuNumeroInstalacion()
        ).thenReturn(
                "TESTINST01"
        );

        when(
                empresaRepository.findConBloqueo(
                        1L
                )
        ).thenReturn(
                Optional.of(
                        empresa
                )
        );

        when(
                registroRepository
                        .existsByFactura_IdAndTipoRegistro(
                                facturaId,
                                TipoRegistroFacturacion.ANULACION
                        )
        ).thenReturn(
                false
        );

        altaOriginal.setNifEmisor(
                "B12345678"
        );

        altaOriginal.setSerie(
                serie
        );

        altaOriginal.setNumero(
                numero
        );

        altaOriginal.setNumSerieFactura(
                numSerieFactura
        );

        altaOriginal.setFechaExpedicion(
                fechaExpedicion
        );

        altaOriginal.setTipoFactura(
                tipoFactura
        );

        altaOriginal.setCuotaTotal(
                cuotaTotal
        );

        altaOriginal.setImporteTotal(
                importeTotal
        );

        altaOriginal.setNombreRazonEmisor(
                "Empresa de Prueba SL"
        );

        when(
                registroRepository
                        .findByFactura_IdAndEmpresa_IdAndTipoRegistro(
                                facturaId,
                                1L,
                                TipoRegistroFacturacion.ALTA
                        )
        ).thenReturn(
                Optional.of(
                        altaOriginal
                )
        );

        anterior.setHuella(
                HUELLA_ANTERIOR
        );

        when(
                registroRepository
                        .findTopByEmpresa_IdOrderByIdDesc(
                                1L
                        )
        ).thenReturn(
                Optional.of(
                        anterior
                )
        );

        when(
                sistemaProperties.getNombreRazon()
        ).thenReturn(
                "Gombeth Software Solutions Test"
        );

        when(
                sistemaProperties.getNif()
        ).thenReturn(
                "B12345678"
        );

        when(
                sistemaProperties.getNombreSistema()
        ).thenReturn(
                "Gombeth Factura"
        );

        when(
                sistemaProperties.getIdSistema()
        ).thenReturn(
                "GF"
        );

        when(
                sistemaProperties.getVersion()
        ).thenReturn(
                "0.0.1-SNAPSHOT"
        );

        when(
                sistemaProperties.getSoloVerifactu()
        ).thenReturn(
                "S"
        );

        when(
                sistemaProperties.getMultiOt()
        ).thenReturn(
                "S"
        );

        when(
                empresaRepository.count()
        ).thenReturn(
                1L
        );

        when(
                huellaVerifactuService.calcularAnulacion(
                        org.mockito.ArgumentMatchers.eq("B12345678"),
                        org.mockito.ArgumentMatchers.eq(numSerieFactura),
                        org.mockito.ArgumentMatchers.eq(fechaExpedicion),
                        org.mockito.ArgumentMatchers.eq(HUELLA_ANTERIOR),
                        any(OffsetDateTime.class)
                )
        ).thenReturn(
                HUELLA_NUEVA
        );

        when(
                huellaVerifactuService.formatearFechaHora(
                        any(OffsetDateTime.class)
                )
        ).thenReturn(
                "2026-10-08T11:15:00+01:00"
        );

        when(
                registroRepository.saveAndFlush(
                        any(RegistroFacturacion.class)
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(
                                0
                        )
        );

        RegistroFacturacion anulacion =
                service.crearAnulacion(
                        factura
                );

        assertEquals(
                TipoRegistroFacturacion.ANULACION,
                anulacion.getTipoRegistro()
        );

        assertSame(
                factura,
                anulacion.getFactura()
        );

        assertEquals(
                "B12345678",
                anulacion.getNifEmisor()
        );

        assertEquals(
                serie,
                anulacion.getSerie()
        );

        assertEquals(
                numero,
                anulacion.getNumero()
        );

        assertEquals(
                numSerieFactura,
                anulacion.getNumSerieFactura()
        );

        assertEquals(
                fechaExpedicion,
                anulacion.getFechaExpedicion()
        );

        assertEquals(
                tipoFactura,
                anulacion.getTipoFactura()
        );

        assertEquals(
                cuotaTotal.setScale(2),
                anulacion.getCuotaTotal()
        );

        assertEquals(
                importeTotal.setScale(2),
                anulacion.getImporteTotal()
        );

        assertSame(
                anterior,
                anulacion.getRegistroAnterior()
        );

        assertEquals(
                HUELLA_ANTERIOR,
                anulacion.getHuellaAnterior()
        );

        assertEquals(
                HUELLA_NUEVA,
                anulacion.getHuella()
        );

        assertNotNull(
                anulacion.getFechaHoraGeneracion()
        );

        assertEquals(
                EstadoEnvioRegistro.PENDIENTE,
                anulacion.getEstadoEnvio()
        );

        verify(
                huellaVerifactuService
        ).calcularAnulacion(
                org.mockito.ArgumentMatchers.eq("B12345678"),
                org.mockito.ArgumentMatchers.eq(numSerieFactura),
                org.mockito.ArgumentMatchers.eq(fechaExpedicion),
                org.mockito.ArgumentMatchers.eq(HUELLA_ANTERIOR),
                any(OffsetDateTime.class)
        );

        verify(
                registroRepository
        ).saveAndFlush(
                anulacion
        );
    }
}
