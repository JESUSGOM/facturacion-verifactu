package es.jfgb.facturacion.factura;

import jakarta.persistence.*;

@Entity
@Table(
        name = "facturas_sustituidas",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_facturas_sustituidas_facturas",
                        columnNames = {
                                "factura_sustitutiva_id",
                                "factura_sustituida_id"
                        }
                ),
                @UniqueConstraint(
                        name = "uk_facturas_sustituidas_orden",
                        columnNames = {
                                "factura_sustitutiva_id",
                                "orden"
                        }
                )
        }
)
public class FacturaSustituida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "factura_sustitutiva_id",
            nullable = false
    )
    private Factura facturaSustitutiva;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "factura_sustituida_id",
            nullable = false
    )
    private Factura facturaSustituida;

    @Column(
            nullable = false
    )
    private Integer orden;

    public FacturaSustituida() {
    }

    public Long getId() {
        return id;
    }

    public Factura getFacturaSustitutiva() {
        return facturaSustitutiva;
    }

    public void setFacturaSustitutiva(
            Factura facturaSustitutiva) {
        this.facturaSustitutiva =
                facturaSustitutiva;
    }

    public Factura getFacturaSustituida() {
        return facturaSustituida;
    }

    public void setFacturaSustituida(
            Factura facturaSustituida) {
        this.facturaSustituida =
                facturaSustituida;
    }

    public Integer getOrden() {
        return orden;
    }

    public void setOrden(
            Integer orden) {
        this.orden = orden;
    }
}
