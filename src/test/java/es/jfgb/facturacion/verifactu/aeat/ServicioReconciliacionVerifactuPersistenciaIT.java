package es.jfgb.facturacion.verifactu.aeat;

import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.consultalr.ConsultaFactuSistemaFacturacionType;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.respuestaconsultalr.EstadoRegFactuType;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.respuestaconsultalr.EstadoRegistroType;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.respuestaconsultalr.IndicadorPaginacionType;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.respuestaconsultalr.RegistroRespuestaConsultaRegFacturacionType;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.respuestaconsultalr.RespuestaConsultaFactuSistemaFacturacionType;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.respuestaconsultalr.ResultadoConsultaType;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.suministroinformacion.IDFacturaExpedidaType;
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
import es.jfgb.facturacion.verifactu.EstadoEnvioRegistro;
import es.jfgb.facturacion.verifactu.EstadoEnvioVerifactuService;
import es.jfgb.facturacion.verifactu.EstadoReconciliacionVerifactuService;
import es.jfgb.facturacion.verifactu.RegistroFacturacion;
import es.jfgb.facturacion.verifactu.RegistroFacturacionIntento;
import es.jfgb.facturacion.verifactu.RegistroFacturacionIntentoRepository;
import es.jfgb.facturacion.verifactu.RegistroFacturacionRepository;
import es.jfgb.facturacion.verifactu.RegistroFacturacionService;
import es.jfgb.facturacion.verifactu.aeat.soap.AeatSoapGateway;
import es.jfgb.facturacion.verifactu.provider.EnvioVerifactuInciertoException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
                 * CRITICO:
                 *
                 * El contexto Spring real continua teniendo
                 * bloqueados tanto envio como consulta.
                 */
                "verifactu.envio-habilitado=false",
                "verifactu.consulta-habilitada=false"
        }
)
class ServicioReconciliacionVerifactuPersistenciaIT {

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
    private RegistroFacturacionIntentoRepository intentoRepository;

    @Autowired
    private RegistroFacturacionService registroService;

    @Autowired
    private EstadoEnvioVerifactuService estadoEnvioService;

    @Autowired
    private EstadoReconciliacionVerifactuService
            estadoReconciliacionService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private Environment environment;

    @Test
    void reconciliaInciertoComoAceptadoConGatewayFalso()
            throws Exception {

        /*
         * =========================================================
         * VERIFICACION PREVIA DE LOS CERROJOS REALES
         * =========================================================
         */

        assertEquals(
                "false",
                environment.getProperty(
                        "verifactu.envio-habilitado"
                )
        );

        assertEquals(
                "false",
                environment.getProperty(
                        "verifactu.consulta-habilitada"
                )
        );

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
                     * =================================================
                     * EMPRESA TEMPORAL
                     * =================================================
                     */

                    Empresa empresa =
                            new Empresa();

                    empresa.setNif(
                            "RQ"
                                    + marca.substring(
                                    Math.max(
                                            0,
                                            marca.length() - 12
                                    )
                            )
                    );

                    empresa.setRazonSocial(
                            "Empresa temporal reconciliacion VeriFactu"
                    );

                    empresa.setNombreComercial(
                            "Empresa reconciliacion test"
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
                     * =================================================
                     * CLIENTE TEMPORAL
                     * =================================================
                     */

                    Cliente cliente =
                            new Cliente();

                    cliente.setEmpresa(
                            empresa
                    );

                    cliente.setNif(
                            "CQ"
                                    + marca.substring(
                                    Math.max(
                                            0,
                                            marca.length() - 12
                                    )
                            )
                    );

                    cliente.setNombre(
                            "Cliente reconciliacion test"
                    );

                    cliente.setNombreComercial(
                            "Cliente reconciliacion"
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
                     * =================================================
                     * SERIE TEMPORAL
                     * =================================================
                     */

                    SerieFacturacion serie =
                            new SerieFacturacion();

                    serie.setEmpresa(
                            empresa
                    );

                    serie.setCodigo(
                            "REC"
                                    + marca.substring(
                                    Math.max(
                                            0,
                                            marca.length() - 10
                                    )
                            )
                    );

                    serie.setDescripcion(
                            "Serie reconciliacion offline"
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
                     * =================================================
                     * FACTURA TEMPORAL
                     * =================================================
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
                     * =================================================
                     * REGISTRO VERI*FACTU REAL EN BD
                     * =================================================
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
                            registro.getId(),
                            registro.getNifEmisor(),
                            registro.getNumSerieFactura(),
                            registro.getFechaExpedicion()
                    );
                });

        assertNotNull(
                datos
        );

        try {

            /*
             * =========================================================
             * PENDIENTE -> ENVIANDO
             * =========================================================
             *
             * Esto solamente cambia estado local.
             * NO existe proveedor ni comunicacion externa.
             */

            estadoEnvioService.iniciarEnvio(
                    datos.empresaId(),
                    datos.registroId()
            );

            String estadoEnviando =
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
                    estadoEnviando
            );

            /*
             * =========================================================
             * ENVIANDO -> INCIERTO
             * =========================================================
             *
             * Simulamos que un envio anterior tuvo resultado
             * desconocido. No se realiza ese envio.
             */

            estadoEnvioService.registrarIncierto(
                    datos.empresaId(),
                    datos.registroId(),
                    "SIMULADO-INCIERTO",
                    new EnvioVerifactuInciertoException(
                            "Resultado incierto simulado "
                                    + "para prueba de reconciliacion"
                    )
            );

            String estadoIncierto =
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
                    "INCIERTO",
                    estadoIncierto
            );

            /*
             * =========================================================
             * RESPUESTA AEAT COMPLETAMENTE FICTICIA
             * =========================================================
             */

            RespuestaConsultaFactuSistemaFacturacionType
                    respuestaFalsa =
                    crearRespuestaCorrecta(
                            datos
                    );

            /*
             * =========================================================
             * GATEWAY FALSO
             * =========================================================
             *
             * Es un mock de Mockito.
             *
             * NO crea:
             * - SfVerifactu
             * - SfPortTypeVerifactu
             * - cliente HTTP
             * - SSLContext
             * - certificado
             * - conexion AEAT
             */

            AeatSoapGateway gatewayFalso =
                    mock(
                            AeatSoapGateway.class
                    );

            when(
                    gatewayFalso.consultar(
                            any(
                                    ConsultaFactuSistemaFacturacionType.class
                            )
                    )
            ).thenReturn(
                    respuestaFalsa
            );

            /*
             * =========================================================
             * SERVICIO MANUAL EXCLUSIVAMENTE PARA ESTE TEST
             * =========================================================
             *
             * true habilita SOLO esta instancia Java.
             *
             * El bean/configuracion real sigue teniendo
             * verifactu.consulta-habilitada=false.
             *
             * Y esta instancia solamente conoce el gateway Mockito.
             */

            ServicioReconciliacionVerifactu
                    servicioOffline =
                    new ServicioReconciliacionVerifactu(
                            estadoReconciliacionService,
                            new AeatConsultaRegistroBuilder(),
                            gatewayFalso,
                            new AeatConsultaRegistroInterpreter(),
                            true
                    );

            /*
             * =========================================================
             * RECONCILIACION
             * =========================================================
             */

            ResultadoConsultaRegistroAeat resultado =
                    servicioOffline.reconciliar(
                            datos.empresaId(),
                            datos.registroId()
                    );

            assertNotNull(
                    resultado
            );

            assertEquals(
                    EstadoConsultaRegistroAeat.ENCONTRADO,
                    resultado.estado()
            );

            assertEquals(
                    1,
                    resultado.numeroCoincidencias()
            );

            assertTrue(
                    resultado.esConcluyente()
            );

            verify(
                    gatewayFalso
            ).consultar(
                    any(
                            ConsultaFactuSistemaFacturacionType.class
                    )
            );

            /*
             * =========================================================
             * COMPROBAR REGISTRO EN MYSQL
             * =========================================================
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

            String proveedorRegistro =
                    jdbcTemplate.queryForObject(
                            """
                            SELECT proveedor
                            FROM registros_facturacion
                            WHERE id = ?
                            """,
                            String.class,
                            datos.registroId()
                    );

            assertEquals(
                    "ACEPTADO",
                    estadoFinal
            );

            assertEquals(
                    1,
                    numeroIntentos
            );

            /*
             * La reconciliacion no debe borrar ni reemplazar
             * la informacion del intento incierto original.
             */
            assertEquals(
                    "SIMULADO-INCIERTO",
                    proveedorRegistro
            );

            /*
             * =========================================================
             * COMPROBAR HISTORICO DE INTENTOS
             * =========================================================
             */

            List<RegistroFacturacionIntento> intentos =
                    intentoRepository
                            .findAllByRegistroFacturacion_IdOrderByNumeroIntentoAsc(
                                    datos.registroId()
                            );

            assertEquals(
                    1,
                    intentos.size()
            );

            RegistroFacturacionIntento intento =
                    intentos.getFirst();

            assertEquals(
                    1,
                    intento.getNumeroIntento()
            );

            assertEquals(
                    EstadoEnvioRegistro.ACEPTADO,
                    intento.getEstado()
            );

            assertEquals(
                    "SIMULADO-INCIERTO",
                    intento.getProveedor()
            );

            assertNotNull(
                    intento.getError()
            );

            assertTrue(
                    intento.getError()
                            .contains(
                                    "EnvioVerifactuInciertoException"
                            )
            );

            assertNotNull(
                    intento.getFechaInicio()
            );

            assertNotNull(
                    intento.getFechaFin()
            );

            /*
             * =========================================================
             * RESULTADO VISIBLE
             * =========================================================
             */

            System.out.println(
                    "VERIFACTU_RECONCILIACION_BD_OK=true"
            );

            System.out.println(
                    "VERIFACTU_RECONCILIACION_ESTADO_INICIAL=PENDIENTE"
            );

            System.out.println(
                    "VERIFACTU_RECONCILIACION_ESTADO_INTERMEDIO=ENVIANDO"
            );

            System.out.println(
                    "VERIFACTU_RECONCILIACION_ESTADO_INCIERTO=INCIERTO"
            );

            System.out.println(
                    "VERIFACTU_RECONCILIACION_RESPUESTA_SIMULADA=CORRECTO"
            );

            System.out.println(
                    "VERIFACTU_RECONCILIACION_ESTADO_FINAL=ACEPTADO"
            );

            System.out.println(
                    "VERIFACTU_RECONCILIACION_INTENTOS=1"
            );

            System.out.println(
                    "VERIFACTU_RECONCILIACION_GATEWAY=MOCKITO"
            );

            System.out.println(
                    "VERIFACTU_RECONCILIACION_HTTP=false"
            );

            System.out.println(
                    "VERIFACTU_RECONCILIACION_SOAP_REAL=false"
            );

            System.out.println(
                    "VERIFACTU_RECONCILIACION_CERTIFICADO=false"
            );

            System.out.println(
                    "VERIFACTU_RECONCILIACION_AEAT=false"
            );

        } finally {

            /*
             * =========================================================
             * LIMPIEZA COMPLETA
             * =========================================================
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
                    "VERIFACTU_RECONCILIACION_LIMPIEZA=true"
            );
        }
    }

    private RespuestaConsultaFactuSistemaFacturacionType
    crearRespuestaCorrecta(
            DatosPrueba datos) {

        RespuestaConsultaFactuSistemaFacturacionType respuesta =
                new RespuestaConsultaFactuSistemaFacturacionType();

        respuesta.setResultadoConsulta(
                ResultadoConsultaType.CON_DATOS
        );

        respuesta.setIndicadorPaginacion(
                IndicadorPaginacionType.N
        );

        IDFacturaExpedidaType idFactura =
                new IDFacturaExpedidaType();

        idFactura.setIDEmisorFactura(
                datos.nifEmisor()
        );

        idFactura.setNumSerieFactura(
                datos.numSerieFactura()
        );

        idFactura.setFechaExpedicionFactura(
                datos.fechaExpedicion()
                        .format(
                                java.time.format.DateTimeFormatter
                                        .ofPattern(
                                                "dd-MM-yyyy"
                                        )
                        )
        );

        EstadoRegistroType correcta =
                EstadoRegistroType.CORRECTO;

        EstadoRegFactuType estadoRegistro =
                new EstadoRegFactuType();

        estadoRegistro.setEstadoRegistro(
                correcta
        );

        RegistroRespuestaConsultaRegFacturacionType registroRespuesta =
                new RegistroRespuestaConsultaRegFacturacionType();

        registroRespuesta.setIDFactura(
                idFactura
        );

        registroRespuesta.setEstadoRegistro(
                estadoRegistro
        );

        respuesta
                .getRegistroRespuestaConsultaFactuSistemaFacturacion()
                .add(
                        registroRespuesta
                );

        return respuesta;
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
                new BigDecimal(
                        "80.00"
                )
        );

        factura.setCuotaImpuesto(
                new BigDecimal(
                        "5.60"
                )
        );

        factura.setTotal(
                new BigDecimal(
                        "85.60"
                )
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
                "Prueba reconciliacion VeriFactu offline"
        );

        linea.setCantidad(
                new BigDecimal(
                        "1.0000"
                )
        );

        linea.setPrecioUnitario(
                new BigDecimal(
                        "80.0000"
                )
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
                new BigDecimal(
                        "80.00"
                )
        );

        linea.setCuotaImpuesto(
                new BigDecimal(
                        "5.60"
                )
        );

        linea.setTotal(
                new BigDecimal(
                        "85.60"
                )
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
            Long registroId,
            String nifEmisor,
            String numSerieFactura,
            LocalDate fechaExpedicion) {
    }
}