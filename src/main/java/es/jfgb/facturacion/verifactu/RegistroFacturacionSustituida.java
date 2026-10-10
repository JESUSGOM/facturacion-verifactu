package es.jfgb.facturacion.verifactu;

import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(
        name = "registros_facturacion_sustituidas",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_reg_fact_sust_orden",
                        columnNames = {
                                "registro_facturacion_id",
                                "orden"
                        }
                ),
                @UniqueConstraint(
                        name = "uk_reg_fact_sust_factura",
                        columnNames = {
                                "registro_facturacion_id",
                                "num_serie_factura",
                                "fecha_expedicion"
                        }
                )
        }
)
public class RegistroFacturacionSustituida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "registro_facturacion_id",
            nullable = false,
            updatable = false,
            foreignKey = @ForeignKey(
                    name = "fk_reg_fact_sust_registro"
            )
    )
    private RegistroFacturacion registroFacturacion;

    @Column(
            name = "factura_sustituida_id",
            updatable = false
    )
    private Long facturaSustituidaId;

    @Column(
            name = "orden",
            nullable = false,
            updatable = false,
            columnDefinition = "SMALLINT UNSIGNED"
    )
    private Short orden;

    @Column(
            name = "nif_emisor",
            nullable = false,
            length = 20,
            updatable = false
    )
    private String nifEmisor;

    @Column(
            name = "num_serie_factura",
            nullable = false,
            length = 60,
            updatable = false
    )
    private String numSerieFactura;

    @Column(
            name = "fecha_expedicion",
            nullable = false,
            updatable = false
    )
    private LocalDate fechaExpedicion;

    @Column(
            name = "fecha_creacion",
            nullable = false,
            updatable = false
    )
    private Instant fechaCreacion;

    public RegistroFacturacionSustituida() {
    }

    @PrePersist
    public void prePersist() {

        if (fechaCreacion == null) {
            fechaCreacion = Instant.now();
        }
    }

    public Long getId() {
        return id;
    }

    public RegistroFacturacion getRegistroFacturacion() {
        return registroFacturacion;
    }

    public void setRegistroFacturacion(
            RegistroFacturacion registroFacturacion) {

        this.registroFacturacion =
                registroFacturacion;
    }

    public Long getFacturaSustituidaId() {
        return facturaSustituidaId;
    }

    public void setFacturaSustituidaId(
            Long facturaSustituidaId) {

        this.facturaSustituidaId =
                facturaSustituidaId;
    }

    public Short getOrden() {
        return orden;
    }

    public void setOrden(
            Short orden) {

        this.orden = orden;
    }

    public String getNifEmisor() {
        return nifEmisor;
    }

    public void setNifEmisor(
            String nifEmisor) {

        this.nifEmisor = nifEmisor;
    }

    public String getNumSerieFactura() {
        return numSerieFactura;
    }

    public void setNumSerieFactura(
            String numSerieFactura) {

        this.numSerieFactura =
                numSerieFactura;
    }

    public LocalDate getFechaExpedicion() {
        return fechaExpedicion;
    }

    public void setFechaExpedicion(
            LocalDate fechaExpedicion) {

        this.fechaExpedicion =
                fechaExpedicion;
    }

    public Instant getFechaCreacion() {
        return fechaCreacion;
    }
}
