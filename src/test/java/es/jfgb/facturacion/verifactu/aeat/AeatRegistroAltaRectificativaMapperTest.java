package es.jfgb.facturacion.verifactu.aeat;

import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.suministroinformacion.ClaveTipoRectificativaType;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.suministroinformacion.IDFacturaARType;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.suministroinformacion.RegistroFacturacionAltaType;
import es.jfgb.facturacion.factura.TipoRectificativa;
import es.jfgb.facturacion.verifactu.RegistroFacturacion;
import es.jfgb.facturacion.verifactu.RegistroFacturacionDesglose;
import es.jfgb.facturacion.verifactu.RegistroFacturacionRectificada;
import es.jfgb.facturacion.verifactu.TipoRegistroFacturacion;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class AeatRegistroAltaRectificativaMapperTest {

    private final AeatRegistroAltaMapper mapper =
            new AeatRegistroAltaMapper();

    @Test
    void mapeaRectificativaPorDiferenciasSinImporteRectificacion()
            throws Exception {

        RegistroFacturacion registro =
                crearRegistroBase(
                        TipoRectificativa.I
                );

        registro.setCuotaTotal(
                new BigDecimal("-1.40")
        );

        registro.setImporteTotal(
                new BigDecimal("-21.40")
        );

        RegistroFacturacionDesglose desglose =
                registro.getDesgloses()
                        .getFirst();

        desglose.setBaseImponibleOImporteNoSujeto(
                new BigDecimal("-20.00")
        );

        desglose.setCuotaRepercutida(
                new BigDecimal("-1.40")
        );

        RegistroFacturacionAltaType alta =
                mapper.convertir(
                        registro
                );

        assertEquals(
                ClaveTipoRectificativaType.I,
                alta.getTipoRectificativa()
        );

        assertNotNull(
                alta.getFacturasRectificadas()
        );

        assertEquals(
                1,
                alta.getFacturasRectificadas()
                        .getIDFacturaRectificada()
                        .size()
        );

        IDFacturaARType original =
                alta.getFacturasRectificadas()
                        .getIDFacturaRectificada()
                        .getFirst();

        assertEquals(
                "B12345678",
                original.getIDEmisorFactura()
        );

        assertEquals(
                "FAC-7",
                original.getNumSerieFactura()
        );

        assertEquals(
                "01-10-2026",
                original.getFechaExpedicionFactura()
        );

        assertNull(
                alta.getImporteRectificacion()
        );
    }

    @Test
    void mapeaRectificativaSustitutivaConImporteRectificacion()
            throws Exception {

        RegistroFacturacion registro =
                crearRegistroBase(
                        TipoRectificativa.S
                );

        registro.setBaseRectificada(
                new BigDecimal("100.00")
        );

        registro.setCuotaRectificada(
                new BigDecimal("7.00")
        );

        registro.setCuotaTotal(
                new BigDecimal("5.60")
        );

        registro.setImporteTotal(
                new BigDecimal("85.60")
        );

        RegistroFacturacionDesglose desglose =
                registro.getDesgloses()
                        .getFirst();

        desglose.setBaseImponibleOImporteNoSujeto(
                new BigDecimal("80.00")
        );

        desglose.setCuotaRepercutida(
                new BigDecimal("5.60")
        );

        RegistroFacturacionAltaType alta =
                mapper.convertir(
                        registro
                );

        assertEquals(
                ClaveTipoRectificativaType.S,
                alta.getTipoRectificativa()
        );

        assertNotNull(
                alta.getFacturasRectificadas()
        );

        assertEquals(
                1,
                alta.getFacturasRectificadas()
                        .getIDFacturaRectificada()
                        .size()
        );

        IDFacturaARType original =
                alta.getFacturasRectificadas()
                        .getIDFacturaRectificada()
                        .getFirst();

        assertEquals(
                "B12345678",
                original.getIDEmisorFactura()
        );

        assertEquals(
                "FAC-7",
                original.getNumSerieFactura()
        );

        assertEquals(
                "01-10-2026",
                original.getFechaExpedicionFactura()
        );

        assertNotNull(
                alta.getImporteRectificacion()
        );

        assertEquals(
                "100.00",
                alta.getImporteRectificacion()
                        .getBaseRectificada()
        );

        assertEquals(
                "7.00",
                alta.getImporteRectificacion()
                        .getCuotaRectificada()
        );

        assertNull(
                alta.getImporteRectificacion()
                        .getCuotaRecargoRectificado()
        );
    }

    private RegistroFacturacion crearRegistroBase(
            TipoRectificativa tipoRectificativa)
            throws Exception {

        RegistroFacturacion registro =
                new RegistroFacturacion();

        asignarId(
                registro,
                900000001L
        );

        registro.setTipoRegistro(
                TipoRegistroFacturacion.ALTA
        );

        registro.setNifEmisor(
                "B12345678"
        );

        registro.setNombreRazonEmisor(
                "Empresa de Prueba SL"
        );

        registro.setSerie(
                "RECT"
        );

        registro.setNumero(
                1L
        );

        registro.setNumSerieFactura(
                "RECT-1"
        );

        registro.setFechaExpedicion(
                LocalDate.of(
                        2026,
                        10,
                        5
                )
        );

        registro.setTipoFactura(
                "R1"
        );

        registro.setTipoRectificativa(
                tipoRectificativa
        );

        registro.setDescripcionOperacion(
                "RECTIFICACION FACTURA"
        );

        registro.setDestinatarioNombre(
                "Cliente de Prueba"
        );

        registro.setDestinatarioNif(
                "12345678Z"
        );

        registro.setDestinatarioCodigoPais(
                "ES"
        );

        RegistroFacturacionDesglose desglose =
                new RegistroFacturacionDesglose();

        desglose.setOrden(
                (short) 1
        );

        desglose.setImpuesto(
                "03"
        );

        desglose.setClaveRegimen(
                "01"
        );

        desglose.setCalificacionOperacion(
                "S1"
        );

        desglose.setTipoImpositivo(
                new BigDecimal("7.00")
        );

        desglose.setBaseImponibleOImporteNoSujeto(
                new BigDecimal("20.00")
        );

        desglose.setCuotaRepercutida(
                new BigDecimal("1.40")
        );

        registro.addDesglose(
                desglose
        );

        RegistroFacturacionRectificada original =
                new RegistroFacturacionRectificada();

        original.setFacturaRectificadaId(
                10L
        );

        original.setOrden(
                (short) 1
        );

        original.setNifEmisor(
                "B12345678"
        );

        original.setNumSerieFactura(
                "FAC-7"
        );

        original.setFechaExpedicion(
                LocalDate.of(
                        2026,
                        10,
                        1
                )
        );

        registro.addFacturaRectificada(
                original
        );

        registro.setIdVersionAeat(
                "1.0"
        );

        registro.setSistemaNombreRazon(
                "Gombeth Software Solutions"
        );

        registro.setSistemaNif(
                "B12345678"
        );

        registro.setSistemaNombre(
                "Gombeth Factura"
        );

        registro.setSistemaId(
                "GF"
        );

        registro.setSistemaVersion(
                "0.0.1-SNAPSHOT"
        );

        registro.setSistemaNumeroInstalacion(
                "TEST01"
        );

        registro.setSistemaSoloVerifactu(
                "S"
        );

        registro.setSistemaMultiOt(
                "S"
        );

        registro.setSistemaMultiplesOt(
                "N"
        );

        registro.setFechaHoraGeneracion(
                "2026-10-05T15:00:00+01:00"
        );

        registro.setTipoHuella(
                "01"
        );

        registro.setHuella(
                "0123456789ABCDEF0123456789ABCDEF"
                        + "0123456789ABCDEF0123456789ABCDEF"
        );

        return registro;
    }

    private void asignarId(
            RegistroFacturacion registro,
            Long id)
            throws Exception {

        Field campo =
                RegistroFacturacion.class
                        .getDeclaredField(
                                "id"
                        );

        campo.setAccessible(
                true
        );

        campo.set(
                registro,
                id
        );
    }
}