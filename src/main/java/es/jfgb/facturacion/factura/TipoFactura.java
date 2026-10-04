package es.jfgb.facturacion.factura;

public enum TipoFactura {

    F1,
    R1,
    R2,
    R3,
    R4;

    public boolean esRectificativa() {
        return this != F1;
    }
}