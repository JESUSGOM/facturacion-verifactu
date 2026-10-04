package es.jfgb.facturacion.verifactu.aeat;

import es.jfgb.facturacion.verifactu.RegistroFacturacion;
import es.jfgb.facturacion.verifactu.TipoRegistroFacturacion;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AeatSolicitudAnulacionXmlBuilderTest {

    @Test
    void generaRegistroAnulacionConEncadenamientoSinRed() {

        RegistroFacturacion anterior =
                mock(
                        RegistroFacturacion.class
                );

        RegistroFacturacion registro =
                mock(
                        RegistroFacturacion.class
                );

        String huellaAnterior =
                "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
                        + "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";

        String huellaActual =
                "BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB"
                        + "BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB";

        when(
                anterior.getNifEmisor()
        ).thenReturn(
                "B12345678"
        );

        when(
                anterior.getNumSerieFactura()
        ).thenReturn(
                "FAC-14"
        );

        when(
                anterior.getFechaExpedicion()
        ).thenReturn(
                LocalDate.of(
                        2026,
                        10,
                        1
                )
        );

        when(
                anterior.getHuella()
        ).thenReturn(
                huellaAnterior
        );

        when(
                registro.getId()
        ).thenReturn(
                99L
        );

        when(
                registro.getTipoRegistro()
        ).thenReturn(
                TipoRegistroFacturacion.ANULACION
        );

        when(
                registro.getIdVersionAeat()
        ).thenReturn(
                "1.0"
        );

        when(
                registro.getNifEmisor()
        ).thenReturn(
                "B12345678"
        );

        when(
                registro.getNombreRazonEmisor()
        ).thenReturn(
                "Empresa Historica SL"
        );

        when(
                registro.getNumSerieFactura()
        ).thenReturn(
                "FAC-15"
        );

        when(
                registro.getFechaExpedicion()
        ).thenReturn(
                LocalDate.of(
                        2026,
                        10,
                        2
                )
        );

        when(
                registro.getRegistroAnterior()
        ).thenReturn(
                anterior
        );

        when(
                registro.getSistemaNombreRazon()
        ).thenReturn(
                "Gombeth Software Solutions Test"
        );

        when(
                registro.getSistemaNif()
        ).thenReturn(
                "B12345678"
        );

        when(
                registro.getSistemaNombre()
        ).thenReturn(
                "Gombeth Factura"
        );

        when(
                registro.getSistemaId()
        ).thenReturn(
                "GF"
        );

        when(
                registro.getSistemaVersion()
        ).thenReturn(
                "0.0.1-SNAPSHOT"
        );

        when(
                registro.getSistemaNumeroInstalacion()
        ).thenReturn(
                "DEV-INST-1"
        );

        when(
                registro.getSistemaSoloVerifactu()
        ).thenReturn(
                "S"
        );

        when(
                registro.getSistemaMultiOt()
        ).thenReturn(
                "S"
        );

        when(
                registro.getSistemaMultiplesOt()
        ).thenReturn(
                "N"
        );

        when(
                registro.getFechaHoraGeneracion()
        ).thenReturn(
                "2026-10-04T10:15:30+01:00"
        );

        when(
                registro.getTipoHuella()
        ).thenReturn(
                "01"
        );

        when(
                registro.getHuella()
        ).thenReturn(
                huellaActual
        );

        AeatRegistroAnulacionMapper mapper =
                new AeatRegistroAnulacionMapper();

        var anulacion =
                mapper.convertir(
                        registro
                );

        /*
         * Identificacion exacta de la factura anulada.
         */
        assertEquals(
                "B12345678",
                anulacion
                        .getIDFactura()
                        .getIDEmisorFacturaAnulada()
        );

        assertEquals(
                "FAC-15",
                anulacion
                        .getIDFactura()
                        .getNumSerieFacturaAnulada()
        );

        assertEquals(
                "02-10-2026",
                anulacion
                        .getIDFactura()
                        .getFechaExpedicionFacturaAnulada()
        );

        /*
         * Encadenamiento.
         */
        assertNotNull(
                anulacion.getEncadenamiento()
        );

        assertNull(
                anulacion
                        .getEncadenamiento()
                        .getPrimerRegistro()
        );

        assertNotNull(
                anulacion
                        .getEncadenamiento()
                        .getRegistroAnterior()
        );

        assertEquals(
                "FAC-14",
                anulacion
                        .getEncadenamiento()
                        .getRegistroAnterior()
                        .getNumSerieFactura()
        );

        assertEquals(
                huellaAnterior,
                anulacion
                        .getEncadenamiento()
                        .getRegistroAnterior()
                        .getHuella()
        );

        /*
         * Sistema informatico.
         */
        assertEquals(
                "Gombeth Factura",
                anulacion
                        .getSistemaInformatico()
                        .getNombreSistemaInformatico()
        );

        assertEquals(
                "GF",
                anulacion
                        .getSistemaInformatico()
                        .getIdSistemaInformatico()
        );

        assertEquals(
                "DEV-INST-1",
                anulacion
                        .getSistemaInformatico()
                        .getNumeroInstalacion()
        );

        /*
         * Fecha/hora y huella del registro de anulacion.
         */
        assertEquals(
                "2026-10-04T10:15:30+01:00",
                anulacion
                        .getFechaHoraHusoGenRegistro()
                        .toXMLFormat()
        );

        assertEquals(
                "01",
                anulacion.getTipoHuella()
        );

        assertEquals(
                huellaActual,
                anulacion.getHuella()
        );

        /*
         * Generacion XML exclusivamente local.
         */
        AeatSolicitudAnulacionXmlBuilder builder =
                new AeatSolicitudAnulacionXmlBuilder(
                        mapper
                );

        String xml =
                builder.generarXml(
                        registro
                );

        assertNotNull(
                xml
        );

        assertFalse(
                xml.isBlank()
        );

        assertTrue(
                xml.contains(
                        "RegistroAnulacion"
                )
        );

        assertFalse(
                xml.contains(
                        "RegistroAlta"
                )
        );

        assertTrue(
                xml.contains(
                        "IDEmisorFacturaAnulada"
                )
        );

        assertTrue(
                xml.contains(
                        "FAC-15"
                )
        );

        assertTrue(
                xml.contains(
                        huellaAnterior
                )
        );

        assertTrue(
                xml.contains(
                        huellaActual
                )
        );

        /*
         * No existe gateway, provider ni cliente SOAP
         * en esta prueba. Por tanto no existe llamada AEAT.
         */
    }
}