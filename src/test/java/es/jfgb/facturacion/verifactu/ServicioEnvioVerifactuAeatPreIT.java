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
import es.jfgb.facturacion.verifactu.provider.ResultadoEnvioVerifactu;
import es.jfgb.facturacion.verifactu.provider.VerifactuProvider;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        properties = {
                /*
                 * El ServicioEnvioVerifactu administrado por Spring
                 * permanece SIEMPRE bloqueado durante esta prueba.
                 *
                 * La única instancia habilitada se crea manualmente
                 * dentro del método de test.
                 */
                "verifactu.envio-habilitado=false",
                "verifactu.consulta-habilitada=false",

                /*
                 * Evitamos cualquier actuación del recuperador
                 * mientras realizamos esta prueba controlada.
                 */
                "verifactu.envios.recuperacion-initial-delay-ms=86400000",
                "verifactu.envios.recuperacion-intervalo-ms=86400000"
        }
)
@ActiveProfiles("aeat-pre")
@EnabledOnOs(OS.WINDOWS)
@EnabledIfEnvironmentVariable(
        named = "VERIFACTU_AEAT_CERT_SHA256",
        matches = ".+"
)
@EnabledIfEnvironmentVariable(
        named = "AEAT_PRE_SERVICIO_REAL_HABILITADO",
        matches = "SI"
)
class ServicioEnvioVerifactuAeatPreIT {

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
    private VerifactuProvider verifactuProvider;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PlatformTransactionManager transactionManager;


    @Test
    void enviaAltaCompletaServicioProviderPersistencia() {

        String nombreObligado =
                obligatorio(
                        "AEAT_TEST_OBLIGADO_NOMBRE"
                );

        String nifObligado =
                obligatorioNif(
                        "AEAT_TEST_OBLIGADO_NIF"
                );

        /*
         * =========================================================
         * PROTECCION DE LA BD EXISTENTE
         * =========================================================
         *
         * Este E2E no reutiliza ni modifica una empresa existente.
         *
         * Si el NIF real ya está en la BD, abortamos ANTES de
         * crear datos y, por supuesto, ANTES de contactar con AEAT.
         */
        if (empresaRepository.existsByNif(
                nifObligado
        )) {

            throw new IllegalStateException(
                    "Ya existe en la BD una empresa con el NIF "
                            + "del obligado. E2E abortado de forma segura."
            );
        }

        assertEquals(
                "AEAT-VERIFACTU-PRUEBAS",
                verifactuProvider.getNombre()
        );

        TransactionTemplate tx =
                new TransactionTemplate(
                        transactionManager
                );

        String marca =
                Long.toString(
                        System.currentTimeMillis()
                );

        /*
         * =========================================================
         * PRIMER COMMIT
         *
         * Creamos un conjunto completamente independiente:
         *
         * empresa
         * cliente
         * serie
         * factura
         * registro VERI*FACTU PENDIENTE
         *
         * La empresa utiliza la identidad real del obligado
         * únicamente para esta prueba de PRE.
         * =========================================================
         */
        DatosPrueba datos =
                tx.execute(status -> {

                    Empresa empresa =
                            new Empresa();

                    empresa.setNif(
                            nifObligado
                    );

                    empresa.setRazonSocial(
                            nombreObligado
                    );

                    empresa.setNombreComercial(
                            "PRE E2E Gombeth Factura"
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

                    /*
                     * En esta prueba técnica utilizamos al propio
                     * obligado como destinatario, igual que en los
                     * IT directos ya validados.
                     */
                    cliente.setNif(
                            nifObligado
                    );

                    cliente.setNombre(
                            nombreObligado
                    );

                    cliente.setNombreComercial(
                            "Destinatario técnico PRE"
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

                    String codigoSerie =
                            "PREE2E"
                                    + marca.substring(
                                    Math.max(
                                            0,
                                            marca.length() - 8
                                    )
                            );

                    serie.setCodigo(
                            codigoSerie
                    );

                    serie.setDescripcion(
                            "Serie técnica E2E AEAT PRE"
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

                    assertNotNull(
                            registro.getId()
                    );

                    assertEquals(
                            EstadoEnvioRegistro.PENDIENTE,
                            registro.getEstadoEnvio()
                    );

                    assertEquals(
                            0,
                            registro.getIntentosEnvio()
                    );

                    return new DatosPrueba(
                            empresa.getId(),
                            cliente.getId(),
                            serie.getId(),
                            factura.getId(),
                            registro.getId(),
                            registro.getNumSerieFactura(),
                            registro.getFechaExpedicion(),
                            registro.getSistemaNumeroInstalacion()
                    );
                });


        assertNotNull(
                datos
        );


        /*
         * =========================================================
         * IDENTIFICADORES NO SENSIBLES ANTES DEL SOAP
         * =========================================================
         *
         * No imprimimos:
         * - NIF
         * - nombre
         * - XML
         * - huella
         * - certificado
         * - CSV
         */
        System.out.println(
                "E2E_AEAT_PRE_EMPRESA_ID="
                        + datos.empresaId()
        );

        System.out.println(
                "E2E_AEAT_PRE_CLIENTE_ID="
                        + datos.clienteId()
        );

        System.out.println(
                "E2E_AEAT_PRE_SERIE_ID="
                        + datos.serieId()
        );

        System.out.println(
                "E2E_AEAT_PRE_FACTURA_ID="
                        + datos.facturaId()
        );

        System.out.println(
                "E2E_AEAT_PRE_REGISTRO_ID="
                        + datos.registroId()
        );

        System.out.println(
                "E2E_AEAT_PRE_NUM_SERIE_FACTURA="
                        + datos.numSerieFactura()
        );

        System.out.println(
                "E2E_AEAT_PRE_FECHA_EXPEDICION="
                        + datos.fechaExpedicion()
        );

        System.out.println(
                "E2E_AEAT_PRE_NUMERO_INSTALACION="
                        + datos.numeroInstalacion()
        );

        System.out.println(
                "E2E_AEAT_PRE_ESTADO_INICIAL=PENDIENTE"
        );


        /*
         * =========================================================
         * ENVOLTORIO DEL PROVIDER REAL
         * =========================================================
         *
         * Justo antes del SOAP comprobamos directamente en MySQL
         * que el primer commit ya es visible:
         *
         * registro = ENVIANDO
         * intentos_envio = 1
         * intento histórico = ENVIANDO
         */
        VerifactuProvider providerObservado =
                new VerifactuProvider() {

                    @Override
                    public String getNombre() {

                        return verifactuProvider
                                .getNombre();
                    }

                    @Override
                    public boolean bloqueaEnvio() {

                        return verifactuProvider
                                .bloqueaEnvio();
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

                        System.out.println(
                                "E2E_AEAT_PRE_PRIMER_COMMIT=true"
                        );

                        System.out.println(
                                "E2E_AEAT_PRE_ESTADO_ANTES_SOAP=ENVIANDO"
                        );

                        System.out.println(
                                "E2E_AEAT_PRE_INTENTO_ANTES_SOAP=true"
                        );

                        /*
                         * ÚNICO punto de comunicación real con AEAT PRE.
                         */
                        return verifactuProvider
                                .enviarAlta(
                                        registro
                                );
                    }
                };


        /*
         * =========================================================
         * SERVICIO HABILITADO ÚNICAMENTE EN MEMORIA PARA ESTE TEST
         * =========================================================
         *
         * El bean ServicioEnvioVerifactu de Spring sigue teniendo
         * verifactu.envio-habilitado=false.
         */
        ServicioEnvioVerifactu servicioE2E =
                new ServicioEnvioVerifactu(
                        estadoEnvioService,
                        providerObservado,
                        true
                );


        try {

            servicioE2E.enviarAlta(
                    datos.empresaId(),
                    datos.registroId()
            );

        } catch (RuntimeException e) {

            String estadoActual =
                    jdbcTemplate.queryForObject(
                            """
                            SELECT estado_envio
                            FROM registros_facturacion
                            WHERE id = ?
                            """,
                            String.class,
                            datos.registroId()
                    );

            System.out.println(
                    "E2E_AEAT_PRE_RESULTADO=ERROR"
            );

            System.out.println(
                    "E2E_AEAT_PRE_ESTADO_TRAS_ERROR="
                            + estadoActual
            );

            System.out.println(
                    "E2E_AEAT_PRE_DATOS_CONSERVADOS=true"
            );

            throw e;
        }


        /*
         * =========================================================
         * SEGUNDO COMMIT
         * =========================================================
         */
        RegistroFacturacion registroFinal =
                registroRepository
                        .findById(
                                datos.registroId()
                        )
                        .orElseThrow();


        assertEquals(
                EstadoEnvioRegistro.ACEPTADO,
                registroFinal.getEstadoEnvio()
        );

        assertEquals(
                1,
                registroFinal.getIntentosEnvio()
        );

        assertEquals(
                "AEAT-VERIFACTU-PRUEBAS",
                registroFinal.getProveedor()
        );

        assertNotNull(
                registroFinal.getIdentificadorExterno()
        );

        assertFalse(
                registroFinal
                        .getIdentificadorExterno()
                        .isBlank()
        );

        assertNotNull(
                registroFinal.getPeticion()
        );

        assertFalse(
                registroFinal
                        .getPeticion()
                        .isBlank()
        );

        assertNotNull(
                registroFinal.getRespuesta()
        );

        assertFalse(
                registroFinal
                        .getRespuesta()
                        .isBlank()
        );

        assertNotNull(
                registroFinal.getFechaUltimoIntento()
        );

        assertNotNull(
                registroFinal.getFechaEnvio()
        );


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
                "AEAT-VERIFACTU-PRUEBAS",
                intento.getProveedor()
        );

        assertNotNull(
                intento.getIdentificadorExterno()
        );

        assertFalse(
                intento
                        .getIdentificadorExterno()
                        .isBlank()
        );

        assertNotNull(
                intento.getPeticion()
        );

        assertFalse(
                intento.getPeticion()
                        .isBlank()
        );

        assertNotNull(
                intento.getRespuesta()
        );

        assertFalse(
                intento.getRespuesta()
                        .isBlank()
        );

        assertNull(
                intento.getError()
        );


        System.out.println(
                "E2E_AEAT_PRE_OK=true"
        );

        System.out.println(
                "E2E_AEAT_PRE_SEGUNDO_COMMIT=true"
        );

        System.out.println(
                "E2E_AEAT_PRE_ESTADO_FINAL=ACEPTADO"
        );

        System.out.println(
                "E2E_AEAT_PRE_INTENTOS=1"
        );

        System.out.println(
                "E2E_AEAT_PRE_CSV_PRESENTE=true"
        );

        System.out.println(
                "E2E_AEAT_PRE_PETICION_PERSISTIDA=true"
        );

        System.out.println(
                "E2E_AEAT_PRE_RESPUESTA_PERSISTIDA=true"
        );

        /*
         * NO se elimina nada automáticamente después de una
         * comunicación real.
         *
         * Primero revisaremos juntos el resultado.
         */
        System.out.println(
                "E2E_AEAT_PRE_DATOS_CONSERVADOS=true"
        );
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
                        "30.00"
                )
        );

        factura.setCuotaImpuesto(
                new BigDecimal(
                        "2.10"
                )
        );

        factura.setTotal(
                new BigDecimal(
                        "32.10"
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
                "PRUEBA E2E GOMBETH FACTURA AEAT PRE"
        );

        linea.setCantidad(
                new BigDecimal(
                        "1.0000"
                )
        );

        linea.setPrecioUnitario(
                new BigDecimal(
                        "30.0000"
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
                        "30.00"
                )
        );

        linea.setCuotaImpuesto(
                new BigDecimal(
                        "2.10"
                )
        );

        linea.setTotal(
                new BigDecimal(
                        "32.10"
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


    private static String obligatorioNif(
            String nombreVariable) {

        String valor =
                obligatorio(
                        nombreVariable
                ).toUpperCase();

        if (!valor.matches(
                "[0-9A-Z]{9}"
        )) {

            throw new IllegalStateException(
                    nombreVariable
                            + " debe contener exactamente "
                            + "9 caracteres alfanumericos"
            );
        }

        return valor;
    }


    private static String obligatorio(
            String nombreVariable) {

        String valor =
                System.getenv(
                        nombreVariable
                );

        if (valor == null
                || valor.isBlank()) {

            throw new IllegalStateException(
                    "Falta la variable de entorno "
                            + nombreVariable
            );
        }

        return valor.trim();
    }


    private record DatosPrueba(
            Long empresaId,
            Long clienteId,
            Long serieId,
            Long facturaId,
            Long registroId,
            String numSerieFactura,
            LocalDate fechaExpedicion,
            String numeroInstalacion) {
    }
}