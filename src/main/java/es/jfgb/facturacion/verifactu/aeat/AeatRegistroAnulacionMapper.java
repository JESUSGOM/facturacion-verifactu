package es.jfgb.facturacion.verifactu.aeat;

import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.suministroinformacion.EncadenamientoFacturaAnteriorType;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.suministroinformacion.IDFacturaExpedidaBajaType;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.suministroinformacion.PrimerRegistroCadenaType;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.suministroinformacion.RegistroFacturacionAnulacionType;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.suministroinformacion.SiNoType;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.suministroinformacion.SistemaInformaticoType;
import es.jfgb.facturacion.verifactu.RegistroFacturacion;
import es.jfgb.facturacion.verifactu.TipoRegistroFacturacion;
import org.springframework.stereotype.Component;

import javax.xml.datatype.DatatypeFactory;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class AeatRegistroAnulacionMapper {

    private static final DateTimeFormatter
            FORMATO_FECHA_AEAT =
            DateTimeFormatter.ofPattern(
                    "dd-MM-yyyy"
            );

    private static final DatatypeFactory
            DATATYPE_FACTORY =
            DatatypeFactory.newDefaultInstance();

    /**
     * Convierte nuestro registro local de ANULACION
     * al tipo JAXB oficial de AEAT.
     *
     * No realiza ninguna comunicacion externa.
     */
    public RegistroFacturacionAnulacionType convertir(
            RegistroFacturacion registro) {

        validarRegistro(
                registro
        );

        RegistroFacturacionAnulacionType anulacion =
                new RegistroFacturacionAnulacionType();

        anulacion.setIDVersion(
                obligatorio(
                        registro.getIdVersionAeat(),
                        "IDVersion"
                )
        );

        anulacion.setIDFactura(
                crearIdFactura(
                        registro
                )
        );

        anulacion.setEncadenamiento(
                crearEncadenamiento(
                        registro
                )
        );

        anulacion.setSistemaInformatico(
                crearSistemaInformatico(
                        registro
                )
        );

        anulacion.setFechaHoraHusoGenRegistro(
                DATATYPE_FACTORY
                        .newXMLGregorianCalendar(
                                obligatorio(
                                        registro.getFechaHoraGeneracion(),
                                        "FechaHoraHusoGenRegistro"
                                )
                        )
        );

        anulacion.setTipoHuella(
                obligatorio(
                        registro.getTipoHuella(),
                        "TipoHuella"
                )
        );

        anulacion.setHuella(
                obligatorio(
                        registro.getHuella(),
                        "Huella"
                )
        );

        return anulacion;
    }

    private void validarRegistro(
            RegistroFacturacion registro) {

        if (registro == null) {
            throw new IllegalArgumentException(
                    "El registro de facturacion "
                            + "no puede ser null"
            );
        }

        if (registro.getTipoRegistro()
                != TipoRegistroFacturacion.ANULACION) {

            throw new IllegalArgumentException(
                    "El registro debe ser de tipo ANULACION"
            );
        }
    }

    private IDFacturaExpedidaBajaType crearIdFactura(
            RegistroFacturacion registro) {

        IDFacturaExpedidaBajaType id =
                new IDFacturaExpedidaBajaType();

        id.setIDEmisorFacturaAnulada(
                obligatorio(
                        registro.getNifEmisor(),
                        "IDEmisorFacturaAnulada"
                )
        );

        id.setNumSerieFacturaAnulada(
                obligatorio(
                        registro.getNumSerieFactura(),
                        "NumSerieFacturaAnulada"
                )
        );

        id.setFechaExpedicionFacturaAnulada(
                formatearFecha(
                        registro.getFechaExpedicion()
                )
        );

        return id;
    }

    private RegistroFacturacionAnulacionType.Encadenamiento
    crearEncadenamiento(
            RegistroFacturacion registro) {

        RegistroFacturacionAnulacionType.Encadenamiento
                encadenamiento =
                new RegistroFacturacionAnulacionType
                        .Encadenamiento();

        RegistroFacturacion anterior =
                registro.getRegistroAnterior();

        /*
         * Nuestro flujo normal de anulacion siempre
         * tendra un registro anterior, porque antes
         * existe el ALTA de la factura.
         *
         * Mantenemos tambien el caso generico admitido
         * por el esquema JAXB.
         */
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
                        "IDEmisorFactura del registro anterior"
                )
        );

        registroAnterior.setNumSerieFactura(
                obligatorio(
                        anterior.getNumSerieFactura(),
                        "NumSerieFactura del registro anterior"
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

    private SistemaInformaticoType crearSistemaInformatico(
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
                        "SistemaInformatico.NombreSistemaInformatico"
                )
        );

        sistema.setIdSistemaInformatico(
                obligatorio(
                        registro.getSistemaId(),
                        "SistemaInformatico.IdSistemaInformatico"
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
                        registro.getSistemaNumeroInstalacion(),
                        "SistemaInformatico.NumeroInstalacion"
                )
        );

        sistema.setTipoUsoPosibleSoloVerifactu(
                convertirSiNo(
                        registro.getSistemaSoloVerifactu(),
                        "TipoUsoPosibleSoloVerifactu"
                )
        );

        sistema.setTipoUsoPosibleMultiOT(
                convertirSiNo(
                        registro.getSistemaMultiOt(),
                        "TipoUsoPosibleMultiOT"
                )
        );

        sistema.setIndicadorMultiplesOT(
                convertirSiNo(
                        registro.getSistemaMultiplesOt(),
                        "IndicadorMultiplesOT"
                )
        );

        return sistema;
    }

    private SiNoType convertirSiNo(
            String valor,
            String campo) {

        String normalizado =
                obligatorio(
                        valor,
                        campo
                ).toUpperCase();

        if ("S".equals(normalizado)) {
            return SiNoType.S;
        }

        if ("N".equals(normalizado)) {
            return SiNoType.N;
        }

        throw new IllegalStateException(
                campo
                        + " debe contener S o N"
        );
    }

    private String formatearFecha(
            LocalDate fecha) {

        if (fecha == null) {
            throw new IllegalStateException(
                    "La fecha de expedicion es obligatoria"
            );
        }

        return fecha.format(
                FORMATO_FECHA_AEAT
        );
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
}