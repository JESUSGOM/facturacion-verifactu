package es.jfgb.facturacion.verifactu;

import es.jfgb.facturacion.empresa.Empresa;
import es.jfgb.facturacion.empresa.EmpresaRepository;
import es.jfgb.facturacion.factura.EstadoFactura;
import es.jfgb.facturacion.factura.Factura;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RegistroFacturacionAnulacionServiceTest {

    @Test
    void creaAnulacionEncadenadaSinModificarAltaOriginal() {

        EmpresaRepository empresaRepository =
                mock(
                        EmpresaRepository.class
                );

        RegistroFacturacionRepository registroRepository =
                mock(
                        RegistroFacturacionRepository.class
                );

        VerifactuSistemaProperties sistemaProperties =
                mock(
                        VerifactuSistemaProperties.class
                );

        Empresa empresa =
                mock(
                        Empresa.class
                );

        Factura factura =
                mock(
                        Factura.class
                );

        RegistroFacturacion alta =
                mock(
                        RegistroFacturacion.class
                );

        when(
                empresa.getId()
        ).thenReturn(
                1L
        );

        when(
                empresa.getVerifactuNumeroInstalacion()
        ).thenReturn(
                "DEV-INST-1"
        );

        when(
                factura.getId()
        ).thenReturn(
                15L
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
                alta.getTipoRegistro()
        ).thenReturn(
                TipoRegistroFacturacion.ALTA
        );

        when(
                alta.getNifEmisor()
        ).thenReturn(
                "B12345678"
        );

        when(
                alta.getSerie()
        ).thenReturn(
                "FAC"
        );

        when(
                alta.getNumero()
        ).thenReturn(
                15L
        );

        when(
                alta.getNumSerieFactura()
        ).thenReturn(
                "FAC-15"
        );

        when(
                alta.getFechaExpedicion()
        ).thenReturn(
                LocalDate.of(
                        2026,
                        10,
                        2
                )
        );

        when(
                alta.getTipoFactura()
        ).thenReturn(
                "F1"
        );

        when(
                alta.getCuotaTotal()
        ).thenReturn(
                new BigDecimal(
                        "4.20"
                )
        );

        when(
                alta.getImporteTotal()
        ).thenReturn(
                new BigDecimal(
                        "64.20"
                )
        );

        when(
                alta.getNombreRazonEmisor()
        ).thenReturn(
                "Empresa Historica SL"
        );

        when(
                alta.getHuella()
        ).thenReturn(
                "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
                        + "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
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
                                15L,
                                TipoRegistroFacturacion.ANULACION
                        )
        ).thenReturn(
                false
        );

        when(
                registroRepository
                        .findByFactura_IdAndEmpresa_IdAndTipoRegistro(
                                15L,
                                1L,
                                TipoRegistroFacturacion.ALTA
                        )
        ).thenReturn(
                Optional.of(
                        alta
                )
        );

        when(
                registroRepository
                        .findTopByEmpresa_IdOrderByIdDesc(
                                1L
                        )
        ).thenReturn(
                Optional.of(
                        alta
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
                "TESTSIF001"
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
                registroRepository.saveAndFlush(
                        any(
                                RegistroFacturacion.class
                        )
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(
                                0
                        )
        );

        RegistroFacturacionService service =
                new RegistroFacturacionService(
                        empresaRepository,
                        registroRepository,
                        new HuellaVerifactuService(),
                        sistemaProperties
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
                "FAC",
                anulacion.getSerie()
        );

        assertEquals(
                15L,
                anulacion.getNumero()
        );

        assertEquals(
                "FAC-15",
                anulacion.getNumSerieFactura()
        );

        assertEquals(
                LocalDate.of(
                        2026,
                        10,
                        2
                ),
                anulacion.getFechaExpedicion()
        );

        assertEquals(
                "F1",
                anulacion.getTipoFactura()
        );

        assertEquals(
                new BigDecimal(
                        "4.20"
                ),
                anulacion.getCuotaTotal()
        );

        assertEquals(
                new BigDecimal(
                        "64.20"
                ),
                anulacion.getImporteTotal()
        );

        assertSame(
                alta,
                anulacion.getRegistroAnterior()
        );

        assertEquals(
                alta.getHuella(),
                anulacion.getHuellaAnterior()
        );

        assertNotNull(
                anulacion.getHuella()
        );

        assertEquals(
                64,
                anulacion.getHuella().length()
        );

        assertEquals(
                EstadoEnvioRegistro.PENDIENTE,
                anulacion.getEstadoEnvio()
        );

        assertEquals(
                "Empresa Historica SL",
                anulacion.getNombreRazonEmisor()
        );

        assertEquals(
                "Gombeth Factura",
                anulacion.getSistemaNombre()
        );

        /*
         * Comprobamos que el ALTA se usa como fuente
         * historica, no como objeto a modificar.
         */
        verify(
                alta,
                never()
        ).setTipoRegistro(
                any()
        );

        verify(
                registroRepository
        ).saveAndFlush(
                anulacion
        );
    }
}