package es.jfgb.facturacion.verifactu.aeat;

import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.suministroinformacion.CalificacionOperacionType;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.suministroinformacion.ClaveTipoFacturaType;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.suministroinformacion.DesgloseType;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.suministroinformacion.DetalleType;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.suministroinformacion.EncadenamientoFacturaAnteriorType;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.suministroinformacion.IDFacturaExpedidaType;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.suministroinformacion.PersonaFisicaJuridicaType;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.suministroinformacion.PrimerRegistroCadenaType;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.suministroinformacion.RegistroFacturacionAltaType;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.suministroinformacion.SiNoType;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.suministroinformacion.SistemaInformaticoType;
import es.jfgb.facturacion.verifactu.RegistroFacturacion;
import es.jfgb.facturacion.verifactu.RegistroFacturacionDesglose;
import es.jfgb.facturacion.verifactu.TipoRegistroFacturacion;
import org.springframework.stereotype.Component;

import javax.xml.datatype.DatatypeFactory;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class AeatRegistroAltaMapper {

    private static final DateTimeFormatter
            FORMATO_FECHA_AEAT =
            DateTimeFormatter.ofPattern(
                    "dd-MM-yyyy"
            );

    private static final DatatypeFactory
            DATATYPE_FACTORY =
            DatatypeFactory.newDefaultInstance();

    public RegistroFacturacionAltaType convertir(
            RegistroFacturacion registro) {

        validarRegistro(
                registro
        );

        RegistroFacturacionAltaType alta =
                new RegistroFacturacionAltaType();

        /*
         * =====================================================
         * VERSION
         * =====================================================
         */

        alta.setIDVersion(
                obligatorio(
                        registro.getIdVersionAeat(),
                        "IDVersion"
                )
        );

        /*
         * =====================================================
         * IDENTIFICACION DE LA FACTURA
         * =====================================================
         */

        alta.setIDFactura(
                crearIdFactura(
                        registro
                )
        );

        /*
         * =====================================================
         * EMISOR Y TIPO DE FACTURA
         * =====================================================
         */

        alta.setNombreRazonEmisor(
                obligatorio(
                        registro.getNombreRazonEmisor(),
                        "NombreRazonEmisor"
                )
        );

        alta.setTipoFactura(
                convertirTipoFactura(
                        registro.getTipoFactura()
                )
        );

        alta.setDescripcionOperacion(
                obligatorio(
                        registro.getDescripcionOperacion(),
                        "DescripcionOperacion"
                )
        );

        /*
         * =====================================================
         * DESTINATARIO
         * =====================================================
         *
         * Primera versión de Gombeth Factura:
         * destinatario español identificado mediante NIF.
         * =====================================================
         */

        alta.setDestinatarios(
                crearDestinatarios(
                        registro
                )
        );

        /*
         * =====================================================
         * DESGLOSE FISCAL
         * =====================================================
         */

        alta.setDesglose(
                crearDesglose(
                        registro
                )
        );

        /*
         * =====================================================
         * TOTALES
         * =====================================================
         */

        alta.setCuotaTotal(
                importeObligatorio(
                        registro.getCuotaTotal(),
                        "CuotaTotal"
                )
        );

        alta.setImporteTotal(
                importeObligatorio(
                        registro.getImporteTotal(),
                        "ImporteTotal"
                )
        );

        /*
         * =====================================================
         * ENCADENAMIENTO
         * =====================================================
         */

        alta.setEncadenamiento(
                crearEncadenamiento(
                        registro
                )
        );

        /*
         * =====================================================
         * SISTEMA INFORMATICO
         * =====================================================
         */

        alta.setSistemaInformatico(
                crearSistemaInformatico(
                        registro
                )
        );

        /*
         * =====================================================
         * FECHA/HORA DE GENERACION
         * =====================================================
         */

        alta.setFechaHoraHusoGenRegistro(
                convertirFechaHora(
                        registro.getFechaHoraGeneracion()
                )
        );

        /*
         * =====================================================
         * HUELLA
         * =====================================================
         */

        alta.setTipoHuella(
                obligatorio(
                        registro.getTipoHuella(),
                        "TipoHuella"
                )
        );

        alta.setHuella(
                obligatorio(
                        registro.getHuella(),
                        "Huella"
                )
        );

        return alta;
    }

    private IDFacturaExpedidaType crearIdFactura(
            RegistroFacturacion registro) {

        IDFacturaExpedidaType idFactura =
                new IDFacturaExpedidaType();

        idFactura.setIDEmisorFactura(
                obligatorio(
                        registro.getNifEmisor(),
                        "IDEmisorFactura"
                )
        );

        idFactura.setNumSerieFactura(
                obligatorio(
                        registro.getNumSerieFactura(),
                        "NumSerieFactura"
                )
        );

        idFactura.setFechaExpedicionFactura(
                formatearFecha(
                        registro.getFechaExpedicion()
                )
        );

        return idFactura;
    }

    private RegistroFacturacionAltaType.Destinatarios
    crearDestinatarios(
            RegistroFacturacion registro) {

        String pais =
                obligatorio(
                        registro.getDestinatarioCodigoPais(),
                        "CodigoPais destinatario"
                );

        /*
         * Esta primera implementación únicamente
         * genera destinatarios españoles mediante NIF.
         *
         * El soporte de IDOtro se incorporará
         * posteriormente de forma explícita.
         */
        if (!"ES".equalsIgnoreCase(pais)) {

            throw new IllegalStateException(
                    "El mapper AEAT actual solamente admite "
                            + "destinatarios españoles. País: "
                            + pais
            );
        }

        PersonaFisicaJuridicaType destinatario =
                new PersonaFisicaJuridicaType();

        destinatario.setNombreRazon(
                obligatorio(
                        registro.getDestinatarioNombre(),
                        "NombreRazon destinatario"
                )
        );

        destinatario.setNIF(
                obligatorio(
                        registro.getDestinatarioNif(),
                        "NIF destinatario"
                )
        );

        RegistroFacturacionAltaType.Destinatarios
                destinatarios =
                new RegistroFacturacionAltaType
                        .Destinatarios();

        destinatarios
                .getIDDestinatario()
                .add(
                        destinatario
                );

        return destinatarios;
    }

    private DesgloseType crearDesglose(
            RegistroFacturacion registro) {

        List<RegistroFacturacionDesglose>
                origenes =
                registro.getDesgloses();

        if (origenes == null
                || origenes.isEmpty()) {

            throw new IllegalStateException(
                    "El registro no contiene "
                            + "desgloses fiscales"
            );
        }

        if (origenes.size() > 12) {

            throw new IllegalStateException(
                    "El registro contiene más de "
                            + "12 desgloses fiscales"
            );
        }

        DesgloseType desglose =
                new DesgloseType();

        for (RegistroFacturacionDesglose origen
                : origenes) {

            desglose
                    .getDetalleDesglose()
                    .add(
                            crearDetalle(
                                    origen
                            )
                    );
        }

        return desglose;
    }

    private DetalleType crearDetalle(
            RegistroFacturacionDesglose origen) {

        if (origen == null) {

            throw new IllegalStateException(
                    "Se ha encontrado un "
                            + "desglose fiscal nulo"
            );
        }

        /*
         * Primera versión:
         * soportamos las operaciones que Gombeth Factura
         * está generando actualmente:
         *
         * operación sujeta y no exenta,
         * identificada mediante CalificacionOperacion.
         *
         * Las operaciones exentas se implementarán
         * posteriormente de forma expresa.
         */
        if (origen.getOperacionExenta() != null
                && !origen.getOperacionExenta()
                .isBlank()) {

            throw new IllegalStateException(
                    "El mapper AEAT actual todavía no "
                            + "admite operaciones exentas"
            );
        }

        DetalleType detalle =
                new DetalleType();

        if (tieneValor(
                origen.getImpuesto())) {

            detalle.setImpuesto(
                    origen.getImpuesto()
                            .trim()
            );
        }

        if (tieneValor(
                origen.getClaveRegimen())) {

            detalle.setClaveRegimen(
                    origen.getClaveRegimen()
                            .trim()
            );
        }

        detalle.setCalificacionOperacion(
                convertirCalificacionOperacion(
                        origen.getCalificacionOperacion()
                )
        );

        if (origen.getTipoImpositivo()
                != null) {

            detalle.setTipoImpositivo(
                    numero(
                            origen.getTipoImpositivo()
                    )
            );
        }

        detalle.setBaseImponibleOimporteNoSujeto(
                importeObligatorio(
                        origen
                                .getBaseImponibleOImporteNoSujeto(),
                        "BaseImponibleOimporteNoSujeto"
                )
        );

        if (origen.getBaseImponibleACoste()
                != null) {

            detalle.setBaseImponibleACoste(
                    numero(
                            origen.getBaseImponibleACoste()
                    )
            );
        }

        if (origen.getCuotaRepercutida()
                != null) {

            detalle.setCuotaRepercutida(
                    numero(
                            origen.getCuotaRepercutida()
                    )
            );
        }

        if (origen.getTipoRecargoEquivalencia()
                != null) {

            detalle.setTipoRecargoEquivalencia(
                    numero(
                            origen.getTipoRecargoEquivalencia()
                    )
            );
        }

        if (origen.getCuotaRecargoEquivalencia()
                != null) {

            detalle.setCuotaRecargoEquivalencia(
                    numero(
                            origen.getCuotaRecargoEquivalencia()
                    )
            );
        }

        return detalle;
    }

    private RegistroFacturacionAltaType.Encadenamiento
    crearEncadenamiento(
            RegistroFacturacion registro) {

        RegistroFacturacionAltaType.Encadenamiento
                encadenamiento =
                new RegistroFacturacionAltaType
                        .Encadenamiento();

        RegistroFacturacion anterior =
                registro.getRegistroAnterior();

        if (anterior == null) {

            encadenamiento.setPrimerRegistro(
                    PrimerRegistroCadenaType.S
            );

            return encadenamiento;
        }

        EncadenamientoFacturaAnteriorType
                registroAnterior =
                new EncadenamientoFacturaAnteriorType();

        registroAnterior.setIDEmisorFactura(
                obligatorio(
                        anterior.getNifEmisor(),
                        "IDEmisorFactura "
                                + "del registro anterior"
                )
        );

        registroAnterior.setNumSerieFactura(
                obligatorio(
                        anterior.getNumSerieFactura(),
                        "NumSerieFactura "
                                + "del registro anterior"
                )
        );

        registroAnterior.setFechaExpedicionFactura(
                formatearFecha(
                        anterior.getFechaExpedicion()
                )
        );

        registroAnterior.setHuella(
                obligatorio(
                        anterior.getHuella(),
                        "Huella del registro anterior"
                )
        );

        encadenamiento.setRegistroAnterior(
                registroAnterior
        );

        return encadenamiento;
    }

    private SistemaInformaticoType
    crearSistemaInformatico(
            RegistroFacturacion registro) {

        SistemaInformaticoType sistema =
                new SistemaInformaticoType();

        sistema.setNombreRazon(
                obligatorio(
                        registro.getSistemaNombreRazon(),
                        "SistemaInformatico.NombreRazon"
                )
        );

        sistema.setNIF(
                obligatorio(
                        registro.getSistemaNif(),
                        "SistemaInformatico.NIF"
                )
        );

        sistema.setNombreSistemaInformatico(
                obligatorio(
                        registro.getSistemaNombre(),
                        "SistemaInformatico."
                                + "NombreSistemaInformatico"
                )
        );

        sistema.setIdSistemaInformatico(
                obligatorio(
                        registro.getSistemaId(),
                        "SistemaInformatico."
                                + "IdSistemaInformatico"
                )
        );

        sistema.setVersion(
                obligatorio(
                        registro.getSistemaVersion(),
                        "SistemaInformatico.Version"
                )
        );

        sistema.setNumeroInstalacion(
                obligatorio(
                        registro
                                .getSistemaNumeroInstalacion(),
                        "SistemaInformatico."
                                + "NumeroInstalacion"
                )
        );

        sistema.setTipoUsoPosibleSoloVerifactu(
                convertirSiNo(
                        registro
                                .getSistemaSoloVerifactu(),
                        "TipoUsoPosibleSoloVerifactu"
                )
        );

        sistema.setTipoUsoPosibleMultiOT(
                convertirSiNo(
                        registro
                                .getSistemaMultiOt(),
                        "TipoUsoPosibleMultiOT"
                )
        );

        sistema.setIndicadorMultiplesOT(
                convertirSiNo(
                        registro
                                .getSistemaMultiplesOt(),
                        "IndicadorMultiplesOT"
                )
        );

        return sistema;
    }

    private ClaveTipoFacturaType
    convertirTipoFactura(
            String valor) {

        String texto =
                obligatorio(
                        valor,
                        "TipoFactura"
                );

        try {

            return ClaveTipoFacturaType
                    .fromValue(
                            texto
                    );

        } catch (IllegalArgumentException e) {

            throw new IllegalStateException(
                    "TipoFactura AEAT no válido: "
                            + texto,
                    e
            );
        }
    }

    private CalificacionOperacionType
    convertirCalificacionOperacion(
            String valor) {

        String texto =
                obligatorio(
                        valor,
                        "CalificacionOperacion"
                );

        try {

            return CalificacionOperacionType
                    .fromValue(
                            texto
                    );

        } catch (IllegalArgumentException e) {

            throw new IllegalStateException(
                    "CalificacionOperacion AEAT "
                            + "no válida: "
                            + texto,
                    e
            );
        }
    }

    private SiNoType convertirSiNo(
            String valor,
            String campo) {

        String texto =
                obligatorio(
                        valor,
                        campo
                ).toUpperCase();

        try {

            return SiNoType
                    .fromValue(
                            texto
                    );

        } catch (IllegalArgumentException e) {

            throw new IllegalStateException(
                    campo
                            + " debe contener S o N",
                    e
            );
        }
    }

    private javax.xml.datatype.XMLGregorianCalendar
    convertirFechaHora(
            String valor) {

        String texto =
                obligatorio(
                        valor,
                        "FechaHoraHusoGenRegistro"
                );

        try {

            return DATATYPE_FACTORY
                    .newXMLGregorianCalendar(
                            texto
                    );

        } catch (IllegalArgumentException e) {

            throw new IllegalStateException(
                    "FechaHoraHusoGenRegistro "
                            + "no tiene formato válido: "
                            + texto,
                    e
            );
        }
    }

    private String formatearFecha(
            LocalDate fecha) {

        if (fecha == null) {

            throw new IllegalStateException(
                    "FechaExpedicionFactura "
                            + "es obligatoria"
            );
        }

        return fecha.format(
                FORMATO_FECHA_AEAT
        );
    }

    private String importeObligatorio(
            BigDecimal valor,
            String campo) {

        if (valor == null) {

            throw new IllegalStateException(
                    campo
                            + " es obligatorio"
            );
        }

        return numero(
                valor
        );
    }

    private String numero(
            BigDecimal valor) {

        if (valor == null) {
            return null;
        }

        return valor.toPlainString();
    }

    private String obligatorio(
            String valor,
            String campo) {

        if (valor == null
                || valor.isBlank()) {

            throw new IllegalStateException(
                    campo
                            + " es obligatorio"
            );
        }

        return valor.trim();
    }

    private boolean tieneValor(
            String valor) {

        return valor != null
                && !valor.isBlank();
    }

    private void validarRegistro(
            RegistroFacturacion registro) {

        if (registro == null) {

            throw new IllegalArgumentException(
                    "El registro de facturación "
                            + "no puede ser null"
            );
        }

        if (registro.getTipoRegistro()
                != TipoRegistroFacturacion.ALTA) {

            throw new IllegalStateException(
                    "El mapper solamente admite "
                            + "registros de tipo ALTA"
            );
        }

        if (registro.getId() == null) {

            throw new IllegalStateException(
                    "El registro de facturación "
                            + "debe estar persistido"
            );
        }
    }
}