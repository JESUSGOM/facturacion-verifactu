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
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
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
                 * MUY IMPORTANTE:
                 * el servicio real de Spring continúa bloqueado.
                 */
                "verifactu.envio-habilitado=false"
        }
)
@Transactional
@Rollback
class ServicioEnvioVerifactuPersistenciaIT {

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
    private EntityManager entityManager;

    @Test
    void flujoCompletoConProveedorFalsoSinComunicacionExterna() {

        /*
         * =========================================================
         * EMPRESA EXISTENTE
         * =========================================================
         */

        List<Empresa> empresas =
                empresaRepository.findAll();

        assertFalse(
                empresas.isEmpty(),
                "Debe existir al menos una empresa"
        );

        Empresa empresa =
                empresas.getFirst();

        assertNotNull(
                empresa.getId()
        );

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

        cliente.setNif(
                "TST"
                        + sufijo.substring(
                        Math.max(
                                0,
                                sufijo.length() - 12
                        )
                )
        );

        cliente.setNombre(
                "Cliente envío offline"
        );

        cliente.setNombreComercial(
                "Cliente Offline"
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
                "OFF"
                        + sufijo.substring(
                        Math.max(
                                0,
                                sufijo.length() - 10
                        )
                )
        );

        serie.setDescripcion(
                "Serie test envío simulado"
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
         * =========================================================
         * FACTURA
         * =========================================================
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
         * =========================================================
         * REGISTRO VERI*FACTU
         * =========================================================
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

        assertEquals(
                0,
                registro.getIntentosEnvio()
        );

        Long registroId =
                registro.getId();

        Long empresaId =
                empresa.getId();

        /*
         * =========================================================
         * PROVEEDOR COMPLETAMENTE FALSO
         * =========================================================
         *
         * No SOAP.
         * No HTTP.
         * No certificado.
         * No AEAT.
         */

        AtomicBoolean proveedorInvocado =
                new AtomicBoolean(false);

        VerifactuProvider proveedorFalso =
                new VerifactuProvider() {

                    @Override
                    public String getNombre() {
                        return "SIMULADO-OFFLINE";
                    }

                    @Override
                    public ResultadoEnvioVerifactu enviarAlta(
                            RegistroFacturacion registroRecibido) {

                        proveedorInvocado.set(
                                true
                        );

                        assertNotNull(
                                registroRecibido
                        );

                        assertEquals(
                                registroId,
                                registroRecibido.getId()
                        );

                        /*
                         * Respuesta completamente ficticia.
                         */

                        return new ResultadoEnvioVerifactu(
                                true,
                                "SIM-EXT-001",
                                "<peticion-simulada/>",
                                "<respuesta-simulada/>",
                                "Aceptado por proveedor falso"
                        );
                    }
                };

        /*
         * =========================================================
         * SERVICIO OFFLINE
         * =========================================================
         *
         * Creamos MANUALMENTE esta instancia con true.
         *
         * El ServicioEnvioVerifactu real gestionado por Spring
         * continúa teniendo:
         *
         * verifactu.envio-habilitado=false
         *
         * Por tanto, solamente esta instancia de test puede
         * atravesar el seguro, y su proveedor es el falso anterior.
         */

        ServicioEnvioVerifactu servicioOffline =
                new ServicioEnvioVerifactu(
                        estadoEnvioService,
                        proveedorFalso,
                        true
                );

        /*
         * =========================================================
         * EJECUCIÓN DEL FLUJO COMPLETO
         * =========================================================
         */

        servicioOffline.enviarAlta(
                empresaId,
                registroId
        );

        assertTrue(
                proveedorInvocado.get(),
                "El proveedor falso debía ser invocado"
        );

        /*
         * Forzamos escritura y limpiamos el contexto JPA para
         * volver a consultar realmente el estado almacenado.
         */

        entityManager.flush();
        entityManager.clear();

        /*
         * =========================================================
         * REGISTRO FINAL EN BASE DE DATOS
         * =========================================================
         */

        RegistroFacturacion registroBD =
                registroRepository
                        .findById(
                                registroId
                        )
                        .orElseThrow();

        assertEquals(
                EstadoEnvioRegistro.ACEPTADO,
                registroBD.getEstadoEnvio()
        );

        assertEquals(
                1,
                registroBD.getIntentosEnvio()
        );

        assertEquals(
                "SIMULADO-OFFLINE",
                registroBD.getProveedor()
        );

        assertEquals(
                "SIM-EXT-001",
                registroBD.getIdentificadorExterno()
        );

        assertEquals(
                "<peticion-simulada/>",
                registroBD.getPeticion()
        );

        assertEquals(
                "<respuesta-simulada/>",
                registroBD.getRespuesta()
        );

        assertNotNull(
                registroBD.getFechaUltimoIntento()
        );

        assertNotNull(
                registroBD.getFechaEnvio()
        );

        /*
         * =========================================================
         * HISTÓRICO DE INTENTOS
         * =========================================================
         */

        List<RegistroFacturacionIntento> intentos =
                intentoRepository
                        .findAllByRegistroFacturacion_IdOrderByNumeroIntentoAsc(
                                registroId
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
                "SIMULADO-OFFLINE",
                intento.getProveedor()
        );

        assertEquals(
                "SIM-EXT-001",
                intento.getIdentificadorExterno()
        );

        assertEquals(
                "<peticion-simulada/>",
                intento.getPeticion()
        );

        assertEquals(
                "<respuesta-simulada/>",
                intento.getRespuesta()
        );

        assertNull(
                intento.getError()
        );

        assertNotNull(
                intento.getFechaInicio()
        );

        assertNotNull(
                intento.getFechaFin()
        );

        /*
         * =========================================================
         * RESULTADO
         * =========================================================
         */

        System.out.println(
                "VERIFACTU_FLUJO_OFFLINE_OK=true"
        );

        System.out.println(
                "VERIFACTU_FLUJO_ESTADO_INICIAL=PENDIENTE"
        );

        System.out.println(
                "VERIFACTU_FLUJO_INTENTO_CREADO=true"
        );

        System.out.println(
                "VERIFACTU_FLUJO_PROVEEDOR=SIMULADO-OFFLINE"
        );

        System.out.println(
                "VERIFACTU_FLUJO_ESTADO_FINAL=ACEPTADO"
        );

        System.out.println(
                "VERIFACTU_FLUJO_INTENTOS=1"
        );

        System.out.println(
                "VERIFACTU_FLUJO_RESPUESTA_PERSISTIDA=true"
        );

        System.out.println(
                "VERIFACTU_FLUJO_ROLLBACK=true"
        );

        System.out.println(
                "VERIFACTU_FLUJO_HTTP=false"
        );

        System.out.println(
                "VERIFACTU_FLUJO_SOAP=false"
        );

        System.out.println(
                "VERIFACTU_FLUJO_CERTIFICADO=false"
        );

        System.out.println(
                "VERIFACTU_FLUJO_AEAT=false"
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
                        "50.00"
                )
        );

        factura.setCuotaImpuesto(
                new BigDecimal(
                        "3.50"
                )
        );

        factura.setTotal(
                new BigDecimal(
                        "53.50"
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

        /*
         * =========================================================
         * LÍNEA IGIC 7 %
         * =========================================================
         */

        FacturaLinea linea =
                new FacturaLinea();

        linea.setDescripcion(
                "Servicio prueba envío VeriFactu offline"
        );

        linea.setCantidad(
                new BigDecimal(
                        "1.0000"
                )
        );

        linea.setPrecioUnitario(
                new BigDecimal(
                        "50.0000"
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
                        "50.00"
                )
        );

        linea.setCuotaImpuesto(
                new BigDecimal(
                        "3.50"
                )
        );

        linea.setTotal(
                new BigDecimal(
                        "53.50"
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
}