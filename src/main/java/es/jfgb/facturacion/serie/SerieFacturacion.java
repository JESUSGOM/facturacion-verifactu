package es.jfgb.facturacion.serie;

import es.jfgb.facturacion.empresa.Empresa;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(
        name = "series_facturacion",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_series_facturacion_empresa_codigo",
                        columnNames = {"empresa_id", "codigo"}
                )
        }
)
public class SerieFacturacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @Column(nullable = false, length = 30)
    private String codigo;

    @Column(nullable = false, length = 150)
    private String descripcion;

    @Column(name = "ultimo_numero", nullable = false)
    private Long ultimoNumero = 0L;

    @Column(nullable = false)
    private boolean activa = true;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private Instant fechaCreacion;

    @Column(name = "fecha_modificacion")
    private Instant fechaModificacion;

    public SerieFacturacion() {
    }

    @PrePersist
    public void prePersist() {
        fechaCreacion = Instant.now();

        if (ultimoNumero == null) {
            ultimoNumero = 0L;
        }
    }

    @PreUpdate
    public void preUpdate() {
        fechaModificacion = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Empresa getEmpresa() {
        return empresa;
    }

    public void setEmpresa(Empresa empresa) {
        this.empresa = empresa;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Long getUltimoNumero() {
        return ultimoNumero;
    }

    public void setUltimoNumero(Long ultimoNumero) {
        this.ultimoNumero = ultimoNumero;
    }

    public boolean isActiva() {
        return activa;
    }

    public void setActiva(boolean activa) {
        this.activa = activa;
    }

    public Instant getFechaCreacion() {
        return fechaCreacion;
    }

    public Instant getFechaModificacion() {
        return fechaModificacion;
    }
}