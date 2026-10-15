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
import es.jfgb.facturacion.verifactu.aeat.AeatSolicitudAltaXmlBuilder;
import es.jfgb.facturacion.verifactu.provider.ResultadoEnvioVerifactu;
import es.jfgb.facturacion.verifactu.provider.VerifactuProvider;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicBoolean;

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
                 * El servicio real de Spring sigue BLOQUEADO.
                 */
                "verifactu.envio-habilitado=false"
        }
)
class ServicioEnvioVerifactuTransaccionesIT {

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

    @Autowired
    private AeatSolicitudAltaXmlBuilder altaXmlBuilder;

    @Test
    void confirmaDosTransaccionesSeparadasSinAeat() {

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

                    /*
                     * ================================================
                     * EMPRESA TEMPORAL
                     * ================================================
                     */

                    Empresa empresa =
                            new Empresa();

                    empresa.setNif(
                            "TX" + marca.substring(
                                    Math.max(
                                            0,
                                            marca.length() - 12
                                    )
                            )
                    );

                    empresa.setRazonSocial(
                            "Empresa temporal transacciones VeriFactu"
                    );

                    empresa.setNombreComercial(
                            "Empresa TX Test"
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

                    /*
                     * ================================================
                     * CLIENTE TEMPORAL
                     * ================================================
                     */

                    Cliente cliente =
                            new Cliente();

                    cliente.setEmpresa(
                            empresa
                    );

                    cliente.setNif(
                            "CL" + marca.substring(
                                    Math.max(
                                            0,
                                            marca.length() - 12
                                    )
                            )
                    );

                    cliente.setNombre(
                            "Cliente temporal TX"
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

                    /*
                     * ================================================
                     * SERIE
                     * ================================================
                     */

                    SerieFacturacion serie =
                            new SerieFacturacion();

                    serie.setEmpresa(
                            empresa
                    );

                    serie.setCodigo(
                            "TX"
                                    + marca.substring(
                                    Math.max(
                                            0,
                                            marca.length() - 10
                                    )
                            )
                    );

                    serie.setDescripcion(
                            "Serie prueba transacciones"
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

                    /*
                     * ================================================
                     * FACTURA
                     * ================================================
                     */

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

                    /*
                     * ================================================
                     * REGISTRO VERIFACTU
                     * ================================================
                     */

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

        assertNotNull(
                datos
        );

        AtomicBoolean vioEnviando =
                new AtomicBoolean(false);

        AtomicBoolean vioIntento =
                new AtomicBoolean(false);

        /*
         * =========================================================
         * PROVEEDOR LOCAL/FALSO
         * =========================================================
         *
         * Cuando este método sea llamado, iniciarEnvio()
         * ya debe haber terminado su transacción.
         *
         * Consultamos directamente MySQL desde aquí.
         */

        VerifactuProvider proveedorFalso =
                new VerifactuProvider() {

                    @Override
                    public String getNombre() {
                        return "SIMULADO-TX";
                    }

                    @Override
                    public ResultadoEnvioVerifactu enviarAlta(
                            RegistroFacturacion registro) {

                        String estadoBD =
                                jdbcTemplate.queryForObject(
                                        """
                                        SELECT estado_envio
                                        FROM registros_facturacion
                                        WHERE id = ?
                                        """,
                                        String.class,
                                        datos.registroId()
                                );

                        Integer intentosBD =
                                jdbcTemplate.queryForObject(
                                        """
                                        SELECT intentos_envio
                                        FROM registros_facturacion
                                        WHERE id = ?
                                        """,
                                        Integer.class,
                                        datos.registroId()
                                );

                        Integer intentosHistoricos =
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
                                "ENVIANDO",
                                estadoBD
                        );

                        assertEquals(
                                1,
                                intentosBD
                        );

                        assertEquals(
                                1,
                                intentosHistoricos
                        );

                        vioEnviando.set(
                                true
                        );

                        vioIntento.set(
                                true
                        );

                        /*
                         * iniciarEnvio() ya ha terminado su
                         * transaccion cuando entramos aqui.
                         *
                         * El RegistroFacturacion recibido debe
                         * conservar cargadas las relaciones LAZY
                         * que necesita el mapper AEAT.
                         */
                        assertTrue(
                                Hibernate.isInitialized(
                                        registro.getDesgloses()
                                ),
                                "desgloses no esta inicializada"
                        );

                        assertTrue(
                                Hibernate.isInitialized(
                                        registro.getFacturasRectificadas()
                                ),
                                "facturasRectificadas no esta inicializada"
                        );

                        assertTrue(
                                Hibernate.isInitialized(
                                        registro.getFacturasSustituidas()
                                ),
                                "facturasSustituidas no esta inicializada"
                        );

                        RegistroFacturacion anterior =
                                registro.getRegistroAnterior();

                        assertTrue(
                                anterior == null
                                        || Hibernate.isInitialized(anterior),
                                "registroAnterior no esta inicializado"
                        );

                        /*
                         * Reproducimos localmente el punto exacto
                         * que fallo en el E2E: construir el XML
                         * una vez terminado el primer commit.
                         *
                         * generarXml() no realiza HTTP ni SOAP.
                         */
                        String xml =
                                altaXmlBuilder.generarXml(
                                        registro
                                );

                        assertNotNull(
                                xml
                        );

                        assertFalse(
                                xml.isBlank()
                        );

                        System.out.println(
                                "VERIFACTU_TX_LAZY_DETACHED_OK=true"
                        );

                        System.out.println(
                                "VERIFACTU_TX_XML_DETACHED_OK=true"
                        );

                        return new ResultadoEnvioVerifactu(
                                true,
                                "SIM-TX-001",
                                "<peticion-tx/>",
                                "<respuesta-tx/>",
                                "Aceptado por proveedor falso TX"
                        );
                    }
                };

        /*
         * Esta instancia está habilitada únicamente dentro
         * del test y solamente conoce al proveedor falso.
         */

        ServicioEnvioVerifactu servicioOffline =
                new ServicioEnvioVerifactu(
                        estadoEnvioService,
                        proveedorFalso,
                        true
                );

        try {

            /*
             * IMPORTANTE:
             * aquí NO existe una transacción exterior.
             */

            servicioOffline.enviarAlta(
                    datos.empresaId(),
                    datos.registroId()
            );

            assertTrue(
                    vioEnviando.get(),
                    "El proveedor debía ver ENVIANDO ya confirmado"
            );

            assertTrue(
                    vioIntento.get(),
                    "El intento ENVIANDO debía estar ya confirmado"
            );

            /*
             * =====================================================
             * SEGUNDO COMMIT
             * =====================================================
             */

            String estadoFinal =
                    jdbcTemplate.queryForObject(
                            """
                            SELECT estado_envio
                            FROM registros_facturacion
                            WHERE id = ?
                            """,
                            String.class,
                            datos.registroId()
                    );

            String proveedorFinal =
                    jdbcTemplate.queryForObject(
                            """
                            SELECT proveedor
                            FROM registros_facturacion
                            WHERE id = ?
                            """,
                            String.class,
                            datos.registroId()
                    );

            String identificadorFinal =
                    jdbcTemplate.queryForObject(
                            """
                            SELECT identificador_externo
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

            assertEquals(
                    "ACEPTADO",
                    estadoFinal
            );

            assertEquals(
                    "SIMULADO-TX",
                    proveedorFinal
            );

            assertEquals(
                    "SIM-TX-001",
                    identificadorFinal
            );

            assertEquals(
                    "ACEPTADO",
                    estadoIntento
            );

            System.out.println(
                    "VERIFACTU_TX_OK=true"
            );

            System.out.println(
                    "VERIFACTU_TX_PRIMER_COMMIT=true"
            );

            System.out.println(
                    "VERIFACTU_TX_ESTADO_VISIBLE_PROVEEDOR=ENVIANDO"
            );

            System.out.println(
                    "VERIFACTU_TX_INTENTO_VISIBLE_PROVEEDOR=true"
            );

            System.out.println(
                    "VERIFACTU_TX_SEGUNDO_COMMIT=true"
            );

            System.out.println(
                    "VERIFACTU_TX_ESTADO_FINAL=ACEPTADO"
            );

            System.out.println(
                    "VERIFACTU_TX_HTTP=false"
            );

            System.out.println(
                    "VERIFACTU_TX_SOAP=false"
            );

            System.out.println(
                    "VERIFACTU_TX_CERTIFICADO=false"
            );

            System.out.println(
                    "VERIFACTU_TX_AEAT=false"
            );

        } finally {

            /*
             * =====================================================
             * LIMPIEZA EXPLÍCITA
             * =====================================================
             *
             * Como necesitamos commits reales, no utilizamos
             * @Rollback.
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
                    "VERIFACTU_TX_LIMPIEZA=true"
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
                new BigDecimal("100.00")
        );

        factura.setCuotaImpuesto(
                new BigDecimal("7.00")
        );

        factura.setTotal(
                new BigDecimal("107.00")
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
                "Prueba límites transaccionales VeriFactu"
        );

        linea.setCantidad(
                new BigDecimal("1.0000")
        );

        linea.setPrecioUnitario(
                new BigDecimal("100.0000")
        );

        linea.setTipoImpuesto(
                TipoImpuesto.IGIC
        );

        linea.setPorcentajeImpuesto(
                new BigDecimal("7.00")
        );

        linea.setBaseImponible(
                new BigDecimal("100.00")
        );

        linea.setCuotaImpuesto(
                new BigDecimal("7.00")
        );

        linea.setTotal(
                new BigDecimal("107.00")
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