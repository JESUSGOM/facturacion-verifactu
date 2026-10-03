package es.jfgb.facturacion.verifactu;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(
        name = "registros_facturacion_desgloses",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_reg_fact_desglose_orden",
                        columnNames = {
                                "registro_facturacion_id",
                                "orden"
                        }
                )
        }
)
public class RegistroFacturacionDesglose {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "registro_facturacion_id",
            nullable = false,
            updatable = false,
            foreignKey = @ForeignKey(
                    name = "fk_reg_fact_desglose_registro"
            )
    )
    private RegistroFacturacion registroFacturacion;

    @Column(
            name = "orden",
            nullable = false,
            updatable = false,
            columnDefinition = "SMALLINT UNSIGNED"
    )
    private Short orden;

    @Column(
            name = "impuesto",
            length = 2,
            updatable = false
    )
    private String impuesto;

    @Column(
            name = "clave_regimen",
            length = 2,
            updatable = false
    )
    private String claveRegimen;

    @Column(
            name = "calificacion_operacion",
            length = 2,
            updatable = false
    )
    private String calificacionOperacion;

    @Column(
            name = "operacion_exenta",
            length = 2,
            updatable = false
    )
    private String operacionExenta;

    @Column(
            name = "tipo_impositivo",
            precision = 5,
            scale = 2,
            updatable = false
    )
    private BigDecimal tipoImpositivo;

    @Column(
            name = "base_imponible_o_importe_no_sujeto",
            nullable = false,
            precision = 15,
            scale = 2,
            updatable = false
    )
    private BigDecimal baseImponibleOImporteNoSujeto;

    @Column(
            name = "base_imponible_a_coste",
            precision = 15,
            scale = 2,
            updatable = false
    )
    private BigDecimal baseImponibleACoste;

    @Column(
            name = "cuota_repercutida",
            precision = 15,
            scale = 2,
            updatable = false
    )
    private BigDecimal cuotaRepercutida;

    @Column(
            name = "tipo_recargo_equivalencia",
            precision = 5,
            scale = 2,
            updatable = false
    )
    private BigDecimal tipoRecargoEquivalencia;

    @Column(
            name = "cuota_recargo_equivalencia",
            precision = 15,
            scale = 2,
            updatable = false
    )
    private BigDecimal cuotaRecargoEquivalencia;

    @Column(
            name = "fecha_creacion",
            nullable = false,
            updatable = false
    )
    private Instant fechaCreacion;

    public RegistroFacturacionDesglose() {
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
        this.registroFacturacion = registroFacturacion;
    }

    public Short getOrden() {
        return orden;
    }

    public void setOrden(Short orden) {
        this.orden = orden;
    }

    public String getImpuesto() {
        return impuesto;
    }

    public void setImpuesto(String impuesto) {
        this.impuesto = impuesto;
    }

    public String getClaveRegimen() {
        return claveRegimen;
    }

    public void setClaveRegimen(String claveRegimen) {
        this.claveRegimen = claveRegimen;
    }

    public String getCalificacionOperacion() {
        return calificacionOperacion;
    }

    public void setCalificacionOperacion(
            String calificacionOperacion) {
        this.calificacionOperacion = calificacionOperacion;
    }

    public String getOperacionExenta() {
        return operacionExenta;
    }

    public void setOperacionExenta(String operacionExenta) {
        this.operacionExenta = operacionExenta;
    }

    public BigDecimal getTipoImpositivo() {
        return tipoImpositivo;
    }

    public void setTipoImpositivo(
            BigDecimal tipoImpositivo) {
        this.tipoImpositivo = tipoImpositivo;
    }

    public BigDecimal getBaseImponibleOImporteNoSujeto() {
        return baseImponibleOImporteNoSujeto;
    }

    public void setBaseImponibleOImporteNoSujeto(
            BigDecimal baseImponibleOImporteNoSujeto) {
        this.baseImponibleOImporteNoSujeto =
                baseImponibleOImporteNoSujeto;
    }

    public BigDecimal getBaseImponibleACoste() {
        return baseImponibleACoste;
    }

    public void setBaseImponibleACoste(
            BigDecimal baseImponibleACoste) {
        this.baseImponibleACoste = baseImponibleACoste;
    }

    public BigDecimal getCuotaRepercutida() {
        return cuotaRepercutida;
    }

    public void setCuotaRepercutida(
            BigDecimal cuotaRepercutida) {
        this.cuotaRepercutida = cuotaRepercutida;
    }

    public BigDecimal getTipoRecargoEquivalencia() {
        return tipoRecargoEquivalencia;
    }

    public void setTipoRecargoEquivalencia(
            BigDecimal tipoRecargoEquivalencia) {
        this.tipoRecargoEquivalencia =
                tipoRecargoEquivalencia;
    }

    public BigDecimal getCuotaRecargoEquivalencia() {
        return cuotaRecargoEquivalencia;
    }

    public void setCuotaRecargoEquivalencia(
            BigDecimal cuotaRecargoEquivalencia) {
        this.cuotaRecargoEquivalencia =
                cuotaRecargoEquivalencia;
    }

    public Instant getFechaCreacion() {
        return fechaCreacion;
    }
}