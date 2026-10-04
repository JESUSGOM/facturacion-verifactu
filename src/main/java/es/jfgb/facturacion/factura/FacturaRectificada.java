package es.jfgb.facturacion.factura;

import jakarta.persistence.*;

@Entity
@Table(
        name = "facturas_rectificadas",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_facturas_rectificadas_facturas",
                        columnNames = {
                                "factura_rectificativa_id",
                                "factura_rectificada_id"
                        }
                ),
                @UniqueConstraint(
                        name = "uk_facturas_rectificadas_orden",
                        columnNames = {
                                "factura_rectificativa_id",
                                "orden"
                        }
                )
        }
)
public class FacturaRectificada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "factura_rectificativa_id",
            nullable = false
    )
    private Factura facturaRectificativa;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "factura_rectificada_id",
            nullable = false
    )
    private Factura facturaRectificada;

    @Column(
            nullable = false
    )
    private Integer orden;

    public FacturaRectificada() {
    }

    public Long getId() {
        return id;
    }

    public Factura getFacturaRectificativa() {
        return facturaRectificativa;
    }

    public void setFacturaRectificativa(
            Factura facturaRectificativa) {
        this.facturaRectificativa =
                facturaRectificativa;
    }

    public Factura getFacturaRectificada() {
        return facturaRectificada;
    }

    public void setFacturaRectificada(
            Factura facturaRectificada) {
        this.facturaRectificada =
                facturaRectificada;
    }

    public Integer getOrden() {
        return orden;
    }

    public void setOrden(
            Integer orden) {
        this.orden = orden;
    }
}