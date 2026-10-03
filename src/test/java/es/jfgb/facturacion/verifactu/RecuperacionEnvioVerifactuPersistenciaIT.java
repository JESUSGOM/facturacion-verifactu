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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

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
                 * Seguimos bloqueando cualquier envío real.
                 */
                "verifactu.envio-habilitado=false"
        }
)
class RecuperacionEnvioVerifactuPersistenciaIT {

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

    @Autowired
    private EstadoEnvioVerifactuService estadoEnvioService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Test
    void convierteEnvioAntiguoEnInciertoSinReenviar() {

        TransactionTemplate tx =
                new TransactionTemplate(
                        transactionManager
                );

        String marca =
                Long.toString(
                        System.currentTimeMillis()
                );

        DatosPrueba datos =
                tx.execute(status -> {

                    Empresa empresa =
                            new Empresa();

                    empresa.setNif(
                            "RC"
                                    + marca.substring(
                                    Math.max(
                                            0,
                                            marca.length() - 12
                                    )
                            )
                    );

                    empresa.setRazonSocial(
                            "Empresa temporal recuperación VeriFactu"
                    );

                    empresa.setNombreComercial(
                            "Empresa recuperación test"
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
                            "CR"
                                    + marca.substring(
                                    Math.max(
                                            0,
                                            marca.length() - 12
                                    )
                            )
                    );

                    cliente.setNombre(
                            "Cliente recuperación test"
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
                            "RC"
                                    + marca.substring(
                                    Math.max(
                                            0,
                                            marca.length() - 10
                                    )
                            )
                    );

                    serie.setDescripcion(
                            "Serie recuperación test"
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
                                    serie
                            );

                    factura =
                            facturaRepository.saveAndFlush(
                                    factura
                            );

                    RegistroFacturacion registro =
                            registroService.crearAlta(
                                    factura
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

        assertNotNull(
                datos
        );

        try {

            /*
             * =====================================================
             * SIMULAMOS EL PRIMER COMMIT
             * =====================================================
             *
             * NO llamamos a ningún proveedor.
             */

            estadoEnvioService.iniciarEnvio(
                    datos.empresaId(),
                    datos.registroId()
            );

            String estadoInicial =
                    jdbcTemplate.queryForObject(
                            """
                            SELECT estado_envio
                            FROM registros_facturacion
                            WHERE id = ?
                            """,
                            String.class,
                            datos.registroId()
                    );

            assertEquals(
                    "ENVIANDO",
                    estadoInicial
            );

            /*
             * Debe existir el intento histórico.
             */

            Integer intentos =
                    jdbcTemplate.queryForObject(
                            """
                            SELECT COUNT(*)
                            FROM registro_facturacion_intentos
                            WHERE registro_facturacion_id = ?
                              AND numero_intento = 1
                              AND estado = 'ENVIANDO'
                            """,
                            Integer.class,
                            datos.registroId()
                    );

            assertEquals(
                    1,
                    intentos
            );

            /*
             * =====================================================
             * SIMULAMOS QUE LA APLICACIÓN QUEDÓ COLGADA
             * =====================================================
             *
             * Convertimos la fecha del intento en antigua.
             */

            Instant fechaAntigua =
                    Instant.now()
                            .minusSeconds(
                                    600
                            );

            jdbcTemplate.update(
                    """
                    UPDATE registros_facturacion
                    SET fecha_ultimo_intento = ?
                    WHERE id = ?
                    """,
                    fechaAntigua,
                    datos.registroId()
            );

            /*
             * El proceso de búsqueda de candidatos debe localizarlo.
             */

            Instant limite =
                    Instant.now()
                            .minusSeconds(
                                    60
                            );

            List<Long> candidatos =
                    registroRepository
                            .findIdsPorEstadoYFechaUltimoIntentoAnteriorA(
                                    EstadoEnvioRegistro.ENVIANDO,
                                    limite
                            );

            assertTrue(
                    candidatos.contains(
                            datos.registroId()
                    ),
                    "El registro ENVIANDO antiguo debe aparecer como candidato"
            );

            /*
             * =====================================================
             * RECUPERACIÓN
             * =====================================================
             */

            boolean convertido =
                    estadoEnvioService
                            .marcarComoInciertoSiSigueAntiguo(
                                    datos.registroId(),
                                    limite
                            );

            assertTrue(
                    convertido
            );

            /*
             * =====================================================
             * COMPROBACIÓN REAL EN MYSQL
             * =====================================================
             */

            String estadoRegistro =
                    jdbcTemplate.queryForObject(
                            """
                            SELECT estado_envio
                            FROM registros_facturacion
                            WHERE id = ?
                            """,
                            String.class,
                            datos.registroId()
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
                            datos.registroId()
                    );

            Instant fechaFin =
                    jdbcTemplate.queryForObject(
                            """
                            SELECT fecha_fin
                            FROM registro_facturacion_intentos
                            WHERE registro_facturacion_id = ?
                              AND numero_intento = 1
                            """,
                            Instant.class,
                            datos.registroId()
                    );

            assertEquals(
                    "INCIERTO",
                    estadoRegistro
            );

            assertEquals(
                    "INCIERTO",
                    estadoIntento
            );

            assertNotNull(
                    fechaFin
            );

            /*
             * Un INCIERTO no puede volver a iniciar envío.
             */

            assertThrows(
                    Exception.class,
                    () ->
                            estadoEnvioService.iniciarEnvio(
                                    datos.empresaId(),
                                    datos.registroId()
                            )
            );

            String estadoTrasReintento =
                    jdbcTemplate.queryForObject(
                            """
                            SELECT estado_envio
                            FROM registros_facturacion
                            WHERE id = ?
                            """,
                            String.class,
                            datos.registroId()
                    );

            Integer numeroIntentos =
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
                    "INCIERTO",
                    estadoTrasReintento
            );

            assertEquals(
                    1,
                    numeroIntentos
            );

            System.out.println(
                    "VERIFACTU_RECUPERACION_OK=true"
            );

            System.out.println(
                    "VERIFACTU_RECUPERACION_ESTADO_INICIAL=ENVIANDO"
            );

            System.out.println(
                    "VERIFACTU_RECUPERACION_CANDIDATO=true"
            );

            System.out.println(
                    "VERIFACTU_RECUPERACION_CONVERTIDO=true"
            );

            System.out.println(
                    "VERIFACTU_RECUPERACION_ESTADO_REGISTRO=INCIERTO"
            );

            System.out.println(
                    "VERIFACTU_RECUPERACION_ESTADO_INTENTO=INCIERTO"
            );

            System.out.println(
                    "VERIFACTU_RECUPERACION_REENVIO_BLOQUEADO=true"
            );

            System.out.println(
                    "VERIFACTU_RECUPERACION_INTENTOS=1"
            );

            System.out.println(
                    "VERIFACTU_RECUPERACION_HTTP=false"
            );

            System.out.println(
                    "VERIFACTU_RECUPERACION_SOAP=false"
            );

            System.out.println(
                    "VERIFACTU_RECUPERACION_CERTIFICADO=false"
            );

            System.out.println(
                    "VERIFACTU_RECUPERACION_AEAT=false"
            );

        } finally {

            /*
             * =====================================================
             * LIMPIEZA
             * =====================================================
             */

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

            System.out.println(
                    "VERIFACTU_RECUPERACION_LIMPIEZA=true"
            );
        }
    }

    private Factura crearFactura(
            Empresa empresa,
            Cliente cliente,
            SerieFacturacion serie) {

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
                new BigDecimal("40.00")
        );

        factura.setCuotaImpuesto(
                new BigDecimal("2.80")
        );

        factura.setTotal(
                new BigDecimal("42.80")
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
                "Prueba recuperación envío VeriFactu"
        );

        linea.setCantidad(
                new BigDecimal("1.0000")
        );

        linea.setPrecioUnitario(
                new BigDecimal("40.0000")
        );

        linea.setTipoImpuesto(
                TipoImpuesto.IGIC
        );

        linea.setPorcentajeImpuesto(
                new BigDecimal("7.00")
        );

        linea.setBaseImponible(
                new BigDecimal("40.00")
        );

        linea.setCuotaImpuesto(
                new BigDecimal("2.80")
        );

        linea.setTotal(
                new BigDecimal("42.80")
        );

        linea.setOrden(
                1
        );

        factura.agregarLinea(
                linea
        );

        return factura;
    }

    private record DatosPrueba(
            Long empresaId,
            Long clienteId,
            Long serieId,
            Long facturaId,
            Long registroId) {
    }
}