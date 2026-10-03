package es.jfgb.facturacion.verifactu.aeat.soap;

import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.consultalr.ConsultaFactuSistemaFacturacionType;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.respuestaconsultalr.RespuestaConsultaFactuSistemaFacturacionType;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.suministroinformacion.CabeceraType;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.suministrolr.RegistroFacturaType;

public interface AeatSoapGateway {

    AeatSoapResultadoAlta enviarAlta(
            CabeceraType cabecera,
            RegistroFacturaType registroFactura
    );

    /**
     * Realiza una consulta de registros de facturacion.
     *
     * La construccion funcional de la consulta se realiza
     * fuera del gateway. Esta capa se limita a delegar
     * la llamada en el puerto SOAP generado por el WSDL.
     */
    RespuestaConsultaFactuSistemaFacturacionType consultar(
            ConsultaFactuSistemaFacturacionType consulta
    );
}