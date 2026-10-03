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
import es.jfgb.facturacion.verifactu.provider.EstadoResultadoEnvioVerifactu;
import es.jfgb.facturacion.verifactu.provider.ResultadoEnvioVerifactu;
import es.jfgb.facturacion.verifactu.provider.VerifactuProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
                 * El servicio real de Spring continúa BLOQUEADO.
                 */
                "verifactu.envio-habilitado=false"
        }
)
class EstadosResultadoVerifactuPersistenciaIT {

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
    void persisteCorrectamenteTodosLosEstadosDelProveedor() {

        comprobarEstado(
                "ACE",
                EstadoResultadoEnvioVerifactu.ACEPTADO,
                "ACEPTADO"
        );

        comprobarEstado(
                "AER",
                EstadoResultadoEnvioVerifactu.ACEPTADO_CON_ERRORES,
                "ACEPTADO"
        );

        comprobarEstado(
                "REC",
                EstadoResultadoEnvioVerifactu.RECHAZADO,
                "RECHAZADO"
        );

        comprobarEstado(
                "INC",
                EstadoResultadoEnvioVerifactu.REQUIERE_RECONCILIACION,
                "INCIERTO"
        );

        System.out.println(
                "VERIFACTU_ESTADOS_OK=true"
        );

        System.out.println(
                "VERIFACTU_ESTADO_ACEPTADO=ACEPTADO"
        );

        System.out.println(
                "VERIFACTU_ESTADO_ACEPTADO_CON_ERRORES=ACEPTADO"
        );

        System.out.println(
                "VERIFACTU_ESTADO_RECHAZADO=RECHAZADO"
        );

        System.out.println(
                "VERIFACTU_ESTADO_RECONCILIACION=INCIERTO"
        );

        System.out.println(
                "VERIFACTU_ESTADOS_HTTP=false"
        );

        System.out.println(
                "VERIFACTU_ESTADOS_SOAP=false"
        );

        System.out.println(
                "VERIFACTU_ESTADOS_CERTIFICADO=false"
        );

        System.out.println(
                "VERIFACTU_ESTADOS_AEAT=false"
        );
    }

    private void comprobarEstado(
            String codigo,
            EstadoResultadoEnvioVerifactu resultadoProveedor,
            String estadoEsperadoBD) {

        TransactionTemplate tx =
                new TransactionTemplate(
                        transactionManager
                );

        DatosPrueba datos =
                tx.execute(status ->
                        crearDatosPrueba(
                                codigo
                        )
                );

        assertNotNull(
                datos
        );

        try {

            VerifactuProvider proveedorFalso =
                    new VerifactuProvider() {

                        @Override
                        public String getNombre() {
                            return "SIMULADO-ESTADOS";
                        }

                        @Override
                        public ResultadoEnvioVerifactu enviarAlta(
                                RegistroFacturacion registro) {

                            return new ResultadoEnvioVerifactu(
                                    resultadoProveedor,
                                    "SIM-" + codigo,
                                    "<peticion-" + codigo + "/>",
                                    "<respuesta-" + codigo + "/>",
                                    "Resultado simulado " + resultadoProveedor
                            );
                        }
                    };

            /*
             * Esta instancia se habilita SOLAMENTE dentro del test
             * y solamente conoce al proveedor falso anterior.
             */

            ServicioEnvioVerifactu servicioOffline =
                    new ServicioEnvioVerifactu(
                            estadoEnvioService,
                            proveedorFalso,
                            true
                    );

            servicioOffline.enviarAlta(
                    datos.empresaId(),
                    datos.registroId()
            );

            /*
             * =============================================
             * REGISTRO PRINCIPAL
             * =============================================
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

            Integer intentosRegistro =
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

            String identificadorRegistro =
                    jdbcTemplate.queryForObject(
                            """
                            SELECT identificador_externo
                            FROM registros_facturacion
                            WHERE id = ?
                            """,
                            String.class,
                            datos.registroId()
                    );

            assertEquals(
                    estadoEsperadoBD,
                    estadoRegistro
            );

            assertEquals(
                    1,
                    intentosRegistro
            );

            assertEquals(
                    "SIMULADO-ESTADOS",
                    proveedorRegistro
            );

            assertEquals(
                    "SIM-" + codigo,
                    identificadorRegistro
            );

            /*
             * =============================================
             * HISTÓRICO DEL INTENTO
             * =============================================
             */

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

            Integer intentosHistoricos =
                    jdbcTemplate.queryForObject(
                            """
                            SELECT COUNT(*)
                            FROM registro_facturacion_intentos
                            WHERE registro_facturacion_id = ?
                            """,
                            Integer.class,
                            datos.registroId()
                    );

            assertEquals(
                    estadoEsperadoBD,
                    estadoIntento
            );

            assertEquals(
                    1,
                    intentosHistoricos
            );

            System.out.println(
                    "VERIFACTU_ESTADO_"
                            + resultadoProveedor
                            + "="
                            + estadoRegistro
            );

        } finally {

            /*
             * =============================================
             * LIMPIEZA DE DATOS TEMPORALES
             * =============================================
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
                    "VERIFACTU_LIMPIEZA_"
                            + codigo
                            + "=true"
            );
        }
    }

    private DatosPrueba crearDatosPrueba(
            String codigo) {

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
                "Empresa temporal estado " + codigo
        );

        empresa.setNombreComercial(
                "Empresa test " + codigo
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
                "Cliente temporal " + codigo
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
                "Serie prueba estado " + codigo
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
                new BigDecimal("25.00")
        );

        factura.setCuotaImpuesto(
                new BigDecimal("1.75")
        );

        factura.setTotal(
                new BigDecimal("26.75")
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
                "Prueba estado " + codigo
        );

        linea.setCantidad(
                new BigDecimal("1.0000")
        );

        linea.setPrecioUnitario(
                new BigDecimal("25.0000")
        );

        linea.setTipoImpuesto(
                TipoImpuesto.IGIC
        );

        linea.setPorcentajeImpuesto(
                new BigDecimal("7.00")
        );

        linea.setBaseImponible(
                new BigDecimal("25.00")
        );

        linea.setCuotaImpuesto(
                new BigDecimal("1.75")
        );

        linea.setTotal(
                new BigDecimal("26.75")
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