package es.jfgb.facturacion.verifactu;

import es.jfgb.facturacion.cliente.Cliente;
import es.jfgb.facturacion.cliente.ClienteRepository;
import es.jfgb.facturacion.empresa.Empresa;
import es.jfgb.facturacion.empresa.EmpresaRepository;
import es.jfgb.facturacion.empresa.TipoImpuesto;
import es.jfgb.facturacion.factura.Factura;
import es.jfgb.facturacion.factura.FacturaLinea;
import es.jfgb.facturacion.factura.FacturaRepository;
import es.jfgb.facturacion.serie.SerieFacturacion;
import es.jfgb.facturacion.serie.SerieFacturacionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(
        properties = {
                /*
                 * Configuración SIF exclusiva del contexto de test.
                 *
                 * No habilita ningún envío.
                 */
                "verifactu.sistema.nombre-razon=Gombeth Software Solutions Test",
                "verifactu.sistema.nif=TESTSIF001",
                "verifactu.sistema.nombre-sistema=Gombeth Factura",
                "verifactu.sistema.id-sistema=GF",
                "verifactu.sistema.version=0.0.1-SNAPSHOT",
                "verifactu.sistema.solo-verifactu=S",
                "verifactu.sistema.multi-ot=S",

                /*
                 * Cinturón adicional de seguridad.
                 */
                "verifactu.envio-habilitado=false"
        }
)
@Transactional
@Rollback
class RegistroFacturacionPersistenciaIT {

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private SerieFacturacionRepository serieRepository;

    @Autowired
    private FacturaRepository facturaRepository;

    @Autowired
    private RegistroFacturacionRepository registroRepository;

    @Autowired
    private RegistroFacturacionService registroService;

    @Test
    void persisteDosAltasEncadenadasYRevierteLaTransaccion() {

        /*
         * =========================================================
         * EMPRESA EXISTENTE
         * =========================================================
         *
         * No creamos una nueva empresa para no alterar
         * artificialmente el número de obligados tributarios.
         */

        List<Empresa> empresas =
                empresaRepository.findAll();

        assertFalse(
                empresas.isEmpty(),
                "Debe existir al menos una empresa para ejecutar el test"
        );

        Empresa empresa =
                empresas.getFirst();

        assertNotNull(
                empresa.getId()
        );

        /*
         * Sufijo para evitar colisiones con pruebas anteriores.
         */

        String sufijo =
                Long.toString(
                        System.currentTimeMillis()
                );

        /*
         * =========================================================
         * CLIENTE TEMPORAL
         * =========================================================
         */

        Cliente cliente =
                new Cliente();

        cliente.setEmpresa(
                empresa
        );

        /*
         * No es un NIF real.
         * Es exclusivamente un identificador técnico de prueba.
         */

        cliente.setNif(
                "TST" + sufijo.substring(
                        Math.max(
                                0,
                                sufijo.length() - 12
                        )
                )
        );

        cliente.setNombre(
                "Cliente VeriFactu Test"
        );

        cliente.setNombreComercial(
                "Cliente Test"
        );

        cliente.setCodigoPais(
                "ES"
        );

        cliente.setActivo(
                true
        );

        cliente =
                clienteRepository.saveAndFlush(
                        cliente
                );

        assertNotNull(
                cliente.getId()
        );

        /*
         * =========================================================
         * SERIE TEMPORAL
         * =========================================================
         */

        SerieFacturacion serie =
                new SerieFacturacion();

        serie.setEmpresa(
                empresa
        );

        serie.setCodigo(
                "TST"
                        + sufijo.substring(
                        Math.max(
                                0,
                                sufijo.length() - 10
                        )
                )
        );

        serie.setDescripcion(
                "Serie temporal prueba VeriFactu"
        );

        serie.setUltimoNumero(
                0L
        );

        serie.setActiva(
                true
        );

        serie =
                serieRepository.saveAndFlush(
                        serie
                );

        assertNotNull(
                serie.getId()
        );

        /*
         * =========================================================
         * FACTURA 1
         * =========================================================
         */

        Factura factura1 =
                crearFactura(
                        empresa,
                        cliente,
                        serie,
                        1L,
                        "Primera operación prueba encadenamiento",
                        new BigDecimal("20.00"),
                        new BigDecimal("1.40"),
                        new BigDecimal("21.40")
                );

        factura1 =
                facturaRepository.saveAndFlush(
                        factura1
                );

        assertNotNull(
                factura1.getId()
        );

        /*
         * =========================================================
         * ALTA VERI*FACTU 1
         * =========================================================
         */

        RegistroFacturacion registro1 =
                registroService.crearAlta(
                        factura1
                );

        assertNotNull(
                registro1
        );

        assertNotNull(
                registro1.getId()
        );

        assertNotNull(
                registro1.getHuella()
        );

        assertTrue(
                registro1.getHuella()
                        .matches(
                                "[0-9A-F]{64}"
                        )
        );

        assertEquals(
                EstadoEnvioRegistro.PENDIENTE,
                registro1.getEstadoEnvio()
        );

        /*
         * =========================================================
         * FACTURA 2
         * =========================================================
         */

        Factura factura2 =
                crearFactura(
                        empresa,
                        cliente,
                        serie,
                        2L,
                        "Segunda operación prueba encadenamiento",
                        new BigDecimal("30.00"),
                        new BigDecimal("2.10"),
                        new BigDecimal("32.10")
                );

        factura2 =
                facturaRepository.saveAndFlush(
                        factura2
                );

        assertNotNull(
                factura2.getId()
        );

        /*
         * =========================================================
         * ALTA VERI*FACTU 2
         * =========================================================
         */

        RegistroFacturacion registro2 =
                registroService.crearAlta(
                        factura2
                );

        assertNotNull(
                registro2
        );

        assertNotNull(
                registro2.getId()
        );

        /*
         * =========================================================
         * COMPROBACIÓN DEL ENCADENAMIENTO PERSISTIDO
         * =========================================================
         */

        assertNotNull(
                registro2.getRegistroAnterior(),
                "El segundo registro debe enlazar con el primero"
        );

        assertEquals(
                registro1.getId(),
                registro2
                        .getRegistroAnterior()
                        .getId()
        );

        assertEquals(
                registro1.getHuella(),
                registro2.getHuellaAnterior()
        );

        assertNotNull(
                registro2.getHuella()
        );

        assertTrue(
                registro2.getHuella()
                        .matches(
                                "[0-9A-F]{64}"
                        )
        );

        assertNotEquals(
                registro1.getHuella(),
                registro2.getHuella()
        );

        assertEquals(
                EstadoEnvioRegistro.PENDIENTE,
                registro2.getEstadoEnvio()
        );

        /*
         * =========================================================
         * COMPROBACIÓN DIRECTA CONTRA REPOSITORY
         * =========================================================
         *
         * No nos conformamos con los objetos en memoria:
         * volvemos a consultar los registros persistidos.
         */

        registroRepository.flush();

        RegistroFacturacion registro1BD =
                registroRepository
                        .findById(
                                registro1.getId()
                        )
                        .orElseThrow();

        RegistroFacturacion registro2BD =
                registroRepository
                        .findById(
                                registro2.getId()
                        )
                        .orElseThrow();

        assertEquals(
                registro1.getHuella(),
                registro1BD.getHuella()
        );

        assertNotNull(
                registro2BD.getRegistroAnterior()
        );

        assertEquals(
                registro1BD.getId(),
                registro2BD
                        .getRegistroAnterior()
                        .getId()
        );

        assertEquals(
                registro1BD.getHuella(),
                registro2BD.getHuellaAnterior()
        );

        assertEquals(
                registro2.getHuella(),
                registro2BD.getHuella()
        );

        /*
         * =========================================================
         * SNAPSHOT FISCAL
         * =========================================================
         */

        assertEquals(
                "F1",
                registro2BD.getTipoFactura()
        );

        assertEquals(
                "01",
                registro2BD.getTipoHuella()
        );

        assertEquals(
                "Gombeth Factura",
                registro2BD.getSistemaNombre()
        );

        assertEquals(
                "GF",
                registro2BD.getSistemaId()
        );

        assertEquals(
                empresa.getNif(),
                registro2BD.getNifEmisor()
        );

        assertEquals(
                cliente.getNif(),
                registro2BD.getDestinatarioNif()
        );

        assertEquals(
                "ES",
                registro2BD.getDestinatarioCodigoPais()
        );

        /*
         * Debe haberse generado un desglose IGIC.
         */

        assertNotNull(
                registro2BD.getDesgloses()
        );

        assertEquals(
                1,
                registro2BD
                        .getDesgloses()
                        .size()
        );

        RegistroFacturacionDesglose desglose =
                registro2BD
                        .getDesgloses()
                        .getFirst();

        assertEquals(
                "03",
                desglose.getImpuesto()
        );

        assertEquals(
                "01",
                desglose.getClaveRegimen()
        );

        assertEquals(
                "S1",
                desglose.getCalificacionOperacion()
        );

        assertEquals(
                new BigDecimal("7.00"),
                desglose.getTipoImpositivo()
        );

        assertEquals(
                new BigDecimal("30.00"),
                desglose
                        .getBaseImponibleOImporteNoSujeto()
        );

        assertEquals(
                new BigDecimal("2.10"),
                desglose.getCuotaRepercutida()
        );

        /*
         * =========================================================
         * RESULTADO
         * =========================================================
         *
         * La anotación @Rollback hará que la transacción
         * completa se deshaga al finalizar el test.
         */

        System.out.println(
                "VERIFACTU_BD_OK=true"
        );

        System.out.println(
                "VERIFACTU_BD_REGISTRO_1_PERSISTIDO=true"
        );

        System.out.println(
                "VERIFACTU_BD_REGISTRO_2_PERSISTIDO=true"
        );

        System.out.println(
                "VERIFACTU_BD_CADENA_CORRECTA=true"
        );

        System.out.println(
                "VERIFACTU_BD_HUELLA_ANTERIOR_CORRECTA=true"
        );

        System.out.println(
                "VERIFACTU_BD_DESGLOSE_IGIC=true"
        );

        System.out.println(
                "VERIFACTU_BD_ESTADO_PENDIENTE=true"
        );

        System.out.println(
                "VERIFACTU_BD_ROLLBACK=true"
        );

        System.out.println(
                "VERIFACTU_BD_ENVIO_AEAT=false"
        );
    }

    private Factura crearFactura(
            Empresa empresa,
            Cliente cliente,
            SerieFacturacion serie,
            Long numero,
            String descripcion,
            BigDecimal base,
            BigDecimal cuota,
            BigDecimal total) {

        Factura factura =
                new Factura();

        factura.setEmpresa(
                empresa
        );

        factura.setCliente(
                cliente
        );

        factura.setSerie(
                serie
        );

        factura.setNumero(
                numero
        );

        factura.setFechaExpedicion(
                LocalDate.now()
        );

        factura.setBaseImponible(
                base
        );

        factura.setCuotaImpuesto(
                cuota
        );

        factura.setTotal(
                total
        );

        /*
         * Snapshot del destinatario utilizado por
         * RegistroFacturacionService.
         */

        factura.setClienteNif(
                cliente.getNif()
        );

        factura.setClienteNombre(
                cliente.getNombre()
        );

        factura.setClienteCodigoPais(
                "ES"
        );

        /*
         * =========================================================
         * LÍNEA IGIC 7 %
         * =========================================================
         */

        FacturaLinea linea =
                new FacturaLinea();

        linea.setDescripcion(
                descripcion
        );

        linea.setCantidad(
                new BigDecimal(
                        "1.0000"
                )
        );

        linea.setPrecioUnitario(
                base.setScale(4)
        );

        linea.setTipoImpuesto(
                TipoImpuesto.IGIC
        );

        linea.setPorcentajeImpuesto(
                new BigDecimal(
                        "7.00"
                )
        );

        linea.setBaseImponible(
                base
        );

        linea.setCuotaImpuesto(
                cuota
        );

        linea.setTotal(
                total
        );

        linea.setOrden(
                1
        );

        factura.agregarLinea(
                linea
        );

        return factura;
    }
}