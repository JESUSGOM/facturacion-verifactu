package es.jfgb.facturacion.factura;

public enum TipoFactura {

    F1,

    R1,
    R2,
    R3,
    R4,
    R5;

    public boolean esRectificativa() {
        return this == R1
                || this == R2
                || this == R3
                || this == R4
                || this == R5;
    }
}