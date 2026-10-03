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
import es.jfgb.facturacion.verifactu.provider.EnvioVerifactuInciertoException;
import es.jfgb.facturacion.verifactu.provider.ResultadoEnvioVerifactu;
import es.jfgb.facturacion.verifactu.provider.VerifactuProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        properties = {
                "verifactu.sistema.nombre-razon=Gombeth Software Solutions Test",
                "verifactu.sistema.nif=TESTSIF001",
                "verifactu.sistema.nombre-sistema=Gombeth Factura",
                "verifactu.sistema.id-sistema=GF",
                "verifactu.sistema.version=0.0.1-SNAPSHOT",
                "verifactu.sistema.solo-verifactu=S",
                "verifactu.sistema.multi-ot=S",

                /*
                 * El servicio real de Spring sigue bloqueado.
                 */
                "verifactu.envio-habilitado=false"
        }
)
class ErroresEnvioVerifactuPersistenciaIT {

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private SerieFacturacionRepository serieRepository;

    @Autowired
    private FacturaRepository facturaRepository;

    @Autowired
    private RegistroFacturacionService registroService;

    @Autowired
    private EstadoEnvioVerifactuService estadoEnvioService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Test
    void persisteErrorSeguroEInciertoCorrectamente() {

        comprobarErrorSeguro();

        comprobarResultadoIncierto();

        System.out.println(
                "VERIFACTU_ERRORES_OK=true"
        );

        System.out.println(
                "VERIFACTU_ERROR_SEGURO=ERROR"
        );

        System.out.println(
                "VERIFACTU_ERROR_INCIERTO=INCIERTO"
        );

        System.out.println(
                "VERIFACTU_ERRORES_HTTP=false"
        );

        System.out.println(
                "VERIFACTU_ERRORES_SOAP=false"
        );

        System.out.println(
                "VERIFACTU_ERRORES_CERTIFICADO=false"
        );

        System.out.println(
                "VERIFACTU_ERRORES_AEAT=false"
        );
    }

    private void comprobarErrorSeguro() {

        DatosPrueba datos =
                crearDatosPrueba(
                        "ERR"
                );

        try {

            VerifactuProvider proveedorFalso =
                    new VerifactuProvider() {

                        @Override
                        public String getNombre() {
                            return "SIMULADO-ERROR";
                        }

                        @Override
                        public ResultadoEnvioVerifactu enviarAlta(
                                RegistroFacturacion registro) {

                            throw new IllegalStateException(
                                    "Fallo seguro simulado"
                            );
                        }
                    };

            ServicioEnvioVerifactu servicio =
                    new ServicioEnvioVerifactu(
                            estadoEnvioService,
                            proveedorFalso,
                            true
                    );

            ResponseStatusException excepcion =
                    assertThrows(
                            ResponseStatusException.class,
                            () ->
                                    servicio.enviarAlta(
                                            datos.empresaId(),
                                            datos.registroId()
                                    )
                    );

            assertEquals(
                    502,
                    excepcion.getStatusCode().value()
            );

            comprobarEstadoPersistido(
                    datos.registroId(),
                    "ERROR",
                    "SIMULADO-ERROR",
                    "IllegalStateException"
            );

            System.out.println(
                    "VERIFACTU_ERROR_SEGURO_PERSISTIDO=true"
            );

        } finally {

            limpiar(
                    datos
            );

            System.out.println(
                    "VERIFACTU_LIMPIEZA_ERR=true"
            );
        }
    }

    private void comprobarResultadoIncierto() {

        DatosPrueba datos =
                crearDatosPrueba(
                        "INC"
                );

        try {

            VerifactuProvider proveedorFalso =
                    new VerifactuProvider() {

                        @Override
                        public String getNombre() {
                            return "SIMULADO-INCIERTO";
                        }

                        @Override
                        public ResultadoEnvioVerifactu enviarAlta(
                                RegistroFacturacion registro) {

                            throw new EnvioVerifactuInciertoException(
                                    "Resultado incierto simulado"
                            );
                        }
                    };

            ServicioEnvioVerifactu servicio =
                    new ServicioEnvioVerifactu(
                            estadoEnvioService,
                            proveedorFalso,
                            true
                    );

            ResponseStatusException excepcion =
                    assertThrows(
                            ResponseStatusException.class,
                            () ->
                                    servicio.enviarAlta(
                                            datos.empresaId(),
                                            datos.registroId()
                                    )
                    );

            assertEquals(
                    502,
                    excepcion.getStatusCode().value()
            );

            comprobarEstadoPersistido(
                    datos.registroId(),
                    "INCIERTO",
                    "SIMULADO-INCIERTO",
                    "EnvioVerifactuInciertoException"
            );

            /*
             * INCIERTO no debe poder reenviarse.
             */

            assertThrows(
                    ResponseStatusException.class,
                    () ->
                            estadoEnvioService.iniciarEnvio(
                                    datos.empresaId(),
                                    datos.registroId()
                            )
            );

            Integer intentos =
                    jdbcTemplate.queryForObject(
                            """
                            SELECT intentos_envio
                            FROM registros_facturacion
                            WHERE id = ?
                            """,
                            Integer.class,
                            datos.registroId()
                    );

            assertEquals(
                    1,
                    intentos
            );

            System.out.println(
                    "VERIFACTU_ERROR_INCIERTO_PERSISTIDO=true"
            );

            System.out.println(
                    "VERIFACTU_ERROR_INCIERTO_REENVIO_BLOQUEADO=true"
            );

        } finally {

            limpiar(
                    datos
            );

            System.out.println(
                    "VERIFACTU_LIMPIEZA_INC=true"
            );
        }
    }

    private void comprobarEstadoPersistido(
            Long registroId,
            String estadoEsperado,
            String proveedorEsperado,
            String textoErrorEsperado) {

        String estadoRegistro =
                jdbcTemplate.queryForObject(
                        """
                        SELECT estado_envio
                        FROM registros_facturacion
                        WHERE id = ?
                        """,
                        String.class,
                        registroId
                );

        String proveedorRegistro =
                jdbcTemplate.queryForObject(
                        """
                        SELECT proveedor
                        FROM registros_facturacion
                        WHERE id = ?
                        """,
                        String.class,
                        registroId
                );

        String respuestaRegistro =
                jdbcTemplate.queryForObject(
                        """
                        SELECT respuesta
                        FROM registros_facturacion
                        WHERE id = ?
                        """,
                        String.class,
                        registroId
                );

        Integer intentosRegistro =
                jdbcTemplate.queryForObject(
                        """
                        SELECT intentos_envio
                        FROM registros_facturacion
                        WHERE id = ?
                        """,
                        Integer.class,
                        registroId
                );

        String estadoIntento =
                jdbcTemplate.queryForObject(
                        """
                        SELECT estado
                        FROM registro_facturacion_intentos
                        WHERE registro_facturacion_id = ?
                          AND numero_intento = 1
                        """,
                        String.class,
                        registroId
                );

        String proveedorIntento =
                jdbcTemplate.queryForObject(
                        """
                        SELECT proveedor
                        FROM registro_facturacion_intentos
                        WHERE registro_facturacion_id = ?
                          AND numero_intento = 1
                        """,
                        String.class,
                        registroId
                );

        String errorIntento =
                jdbcTemplate.queryForObject(
                        """
                        SELECT error
                        FROM registro_facturacion_intentos
                        WHERE registro_facturacion_id = ?
                          AND numero_intento = 1
                        """,
                        String.class,
                        registroId
                );

        Integer historicos =
                jdbcTemplate.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM registro_facturacion_intentos
                        WHERE registro_facturacion_id = ?
                        """,
                        Integer.class,
                        registroId
                );

        assertEquals(
                estadoEsperado,
                estadoRegistro
        );

        assertEquals(
                estadoEsperado,
                estadoIntento
        );

        assertEquals(
                proveedorEsperado,
                proveedorRegistro
        );

        assertEquals(
                proveedorEsperado,
                proveedorIntento
        );

        assertEquals(
                1,
                intentosRegistro
        );

        assertEquals(
                1,
                historicos
        );

        assertNotNull(
                respuestaRegistro
        );

        assertNotNull(
                errorIntento
        );

        assertTrue(
                respuestaRegistro.contains(
                        textoErrorEsperado
                )
        );

        assertTrue(
                errorIntento.contains(
                        textoErrorEsperado
                )
        );
    }

    private DatosPrueba crearDatosPrueba(
            String codigo) {

        TransactionTemplate tx =
                new TransactionTemplate(
                        transactionManager
                );

        return tx.execute(status -> {

            String marca =
                    Long.toString(
                            System.currentTimeMillis()
                    );

            String sufijo =
                    marca.substring(
                            Math.max(
                                    0,
                                    marca.length() - 10
                            )
                    );

            Empresa empresa =
                    new Empresa();

            empresa.setNif(
                    "E" + codigo + sufijo
            );

            empresa.setRazonSocial(
                    "Empresa temporal error " + codigo
            );

            empresa.setNombreComercial(
                    "Empresa error " + codigo
            );

            empresa.setCodigoPais(
                    "ES"
            );

            empresa.setTipoImpuesto(
                    TipoImpuesto.IGIC
            );

            empresa.setActiva(
                    true
            );

            empresa =
                    empresaRepository.saveAndFlush(
                            empresa
                    );

            Cliente cliente =
                    new Cliente();

            cliente.setEmpresa(
                    empresa
            );

            cliente.setNif(
                    "C" + codigo + sufijo
            );

            cliente.setNombre(
                    "Cliente error " + codigo
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

            SerieFacturacion serie =
                    new SerieFacturacion();

            serie.setEmpresa(
                    empresa
            );

            serie.setCodigo(
                    codigo + sufijo
            );

            serie.setDescripcion(
                    "Serie error " + codigo
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

            Factura factura =
                    crearFactura(
                            empresa,
                            cliente,
                            serie,
                            codigo
                    );

            factura =
                    facturaRepository.saveAndFlush(
                            factura
                    );

            RegistroFacturacion registro =
                    registroService.crearAlta(
                            factura
                    );

            assertNotNull(
                    registro.getId()
            );

            assertEquals(
                    EstadoEnvioRegistro.PENDIENTE,
                    registro.getEstadoEnvio()
            );

            return new DatosPrueba(
                    empresa.getId(),
                    cliente.getId(),
                    serie.getId(),
                    factura.getId(),
                    registro.getId()
            );
        });
    }

    private Factura crearFactura(
            Empresa empresa,
            Cliente cliente,
            SerieFacturacion serie,
            String codigo) {

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
                1L
        );

        factura.setFechaExpedicion(
                LocalDate.now()
        );

        factura.setBaseImponible(
                new BigDecimal("30.00")
        );

        factura.setCuotaImpuesto(
                new BigDecimal("2.10")
        );

        factura.setTotal(
                new BigDecimal("32.10")
        );

        factura.setClienteNif(
                cliente.getNif()
        );

        factura.setClienteNombre(
                cliente.getNombre()
        );

        factura.setClienteCodigoPais(
                "ES"
        );

        FacturaLinea linea =
                new FacturaLinea();

        linea.setDescripcion(
                "Prueba error " + codigo
        );

        linea.setCantidad(
                new BigDecimal("1.0000")
        );

        linea.setPrecioUnitario(
                new BigDecimal("30.0000")
        );

        linea.setTipoImpuesto(
                TipoImpuesto.IGIC
        );

        linea.setPorcentajeImpuesto(
                new BigDecimal("7.00")
        );

        linea.setBaseImponible(
                new BigDecimal("30.00")
        );

        linea.setCuotaImpuesto(
                new BigDecimal("2.10")
        );

        linea.setTotal(
                new BigDecimal("32.10")
        );

        linea.setOrden(
                1
        );

        factura.agregarLinea(
                linea
        );

        return factura;
    }

    private void limpiar(
            DatosPrueba datos) {

        TransactionTemplate tx =
                new TransactionTemplate(
                        transactionManager
                );

        tx.executeWithoutResult(status -> {

            jdbcTemplate.update(
                    """
                    DELETE FROM registro_facturacion_intentos
                    WHERE registro_facturacion_id = ?
                    """,
                    datos.registroId()
            );

            jdbcTemplate.update(
                    """
                    DELETE FROM registros_facturacion_desgloses
                    WHERE registro_facturacion_id = ?
                    """,
                    datos.registroId()
            );

            jdbcTemplate.update(
                    """
                    DELETE FROM registros_facturacion
                    WHERE id = ?
                    """,
                    datos.registroId()
            );

            jdbcTemplate.update(
                    """
                    DELETE FROM factura_lineas
                    WHERE factura_id = ?
                    """,
                    datos.facturaId()
            );

            jdbcTemplate.update(
                    """
                    DELETE FROM facturas
                    WHERE id = ?
                    """,
                    datos.facturaId()
            );

            jdbcTemplate.update(
                    """
                    DELETE FROM series_facturacion
                    WHERE id = ?
                    """,
                    datos.serieId()
            );

            jdbcTemplate.update(
                    """
                    DELETE FROM clientes
                    WHERE id = ?
                    """,
                    datos.clienteId()
            );

            jdbcTemplate.update(
                    """
                    DELETE FROM empresas
                    WHERE id = ?
                    """,
                    datos.empresaId()
            );
        });
    }

    private record DatosPrueba(
            Long empresaId,
            Long clienteId,
            Long serieId,
            Long facturaId,
            Long registroId) {
    }
}