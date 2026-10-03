package es.jfgb.facturacion.verifactu.aeat.soap;

import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.suministroinformacion.CabeceraType;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.suministrolr.RegistroFacturaType;

public interface AeatSoapGateway {

    AeatSoapResultadoAlta enviarAlta(
            CabeceraType cabecera,
            RegistroFacturaType registroFactura
    );
}