package es.jfgb.facturacion.verifactu;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

@Entity
@Table(name = "registro_facturacion_intentos")
public class RegistroFacturacionIntento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "registro_facturacion_id",
            nullable = false,
            updatable = false
    )
    private RegistroFacturacion registroFacturacion;

    @Column(name = "numero_intento", nullable = false, updatable = false)
    private Integer numeroIntento;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoEnvioRegistro estado;

    @Column(name = "proveedor", length = 50)
    private String proveedor;

    @Column(name = "identificador_externo", length = 255)
    private String identificadorExterno;

    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "peticion", columnDefinition = "LONGTEXT")
    private String peticion;

    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "respuesta", columnDefinition = "LONGTEXT")
    private String respuesta;

    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "error", columnDefinition = "LONGTEXT")
    private String error;

    @Column(name = "fecha_inicio", nullable = false, updatable = false)
    private Instant fechaInicio;

    @Column(name = "fecha_fin")
    private Instant fechaFin;

    public Long getId() {
        return id;
    }

    public RegistroFacturacion getRegistroFacturacion() {
        return registroFacturacion;
    }

    public void setRegistroFacturacion(RegistroFacturacion registroFacturacion) {
        this.registroFacturacion = registroFacturacion;
    }

    public Integer getNumeroIntento() {
        return numeroIntento;
    }

    public void setNumeroIntento(Integer numeroIntento) {
        this.numeroIntento = numeroIntento;
    }

    public EstadoEnvioRegistro getEstado() {
        return estado;
    }

    public void setEstado(EstadoEnvioRegistro estado) {
        this.estado = estado;
    }

    public String getProveedor() {
        return proveedor;
    }

    public void setProveedor(String proveedor) {
        this.proveedor = proveedor;
    }

    public String getIdentificadorExterno() {
        return identificadorExterno;
    }

    public void setIdentificadorExterno(String identificadorExterno) {
        this.identificadorExterno = identificadorExterno;
    }

    public String getPeticion() {
        return peticion;
    }

    public void setPeticion(String peticion) {
        this.peticion = peticion;
    }

    public String getRespuesta() {
        return respuesta;
    }

    public void setRespuesta(String respuesta) {
        this.respuesta = respuesta;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Instant getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(Instant fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public Instant getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(Instant fechaFin) {
        this.fechaFin = fechaFin;
    }
}