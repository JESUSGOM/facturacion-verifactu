package es.jfgb.facturacion.empresa;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "empresas")
public class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            nullable = false,
            length = 20,
            unique = true
    )
    private String nif;

    @Column(
            name = "razon_social",
            nullable = false,
            length = 150
    )
    private String razonSocial;

    @Column(
            name = "nombre_comercial",
            length = 150
    )
    private String nombreComercial;

    @Column(length = 200)
    private String direccion;

    @Column(
            name = "codigo_postal",
            length = 10
    )
    private String codigoPostal;

    @Column(length = 100)
    private String municipio;

    @Column(length = 100)
    private String provincia;

    @Column(
            name = "codigo_pais",
            nullable = false,
            length = 2
    )
    private String codigoPais = "ES";

    @Column(length = 30)
    private String telefono;

    @Column(length = 150)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "tipo_impuesto",
            nullable = false,
            length = 10
    )
    private TipoImpuesto tipoImpuesto;

    /*
     * Identificador estable de la instalación
     * VERI*FACTU correspondiente a esta empresa.
     *
     * Se permite NULL porque las empresas creadas
     * antes de V17 todavía no disponen de él.
     */
    @Column(
            name = "verifactu_numero_instalacion",
            length = 100
    )
    private String verifactuNumeroInstalacion;

    @Column(nullable = false)
    private boolean activa = true;

    @Column(
            name = "fecha_creacion",
            nullable = false,
            updatable = false
    )
    private Instant fechaCreacion;

    @Column(name = "fecha_modificacion")
    private Instant fechaModificacion;

    public Empresa() {
    }

    @PrePersist
    public void prePersist() {
        this.fechaCreacion = Instant.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.fechaModificacion = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getNif() {
        return nif;
    }

    public void setNif(String nif) {
        this.nif = nif;
    }

    public String getRazonSocial() {
        return razonSocial;
    }

    public void setRazonSocial(
            String razonSocial) {
        this.razonSocial = razonSocial;
    }

    public String getNombreComercial() {
        return nombreComercial;
    }

    public void setNombreComercial(
            String nombreComercial) {
        this.nombreComercial = nombreComercial;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(
            String direccion) {
        this.direccion = direccion;
    }

    public String getCodigoPostal() {
        return codigoPostal;
    }

    public void setCodigoPostal(
            String codigoPostal) {
        this.codigoPostal = codigoPostal;
    }

    public String getMunicipio() {
        return municipio;
    }

    public void setMunicipio(
            String municipio) {
        this.municipio = municipio;
    }

    public String getProvincia() {
        return provincia;
    }

    public void setProvincia(
            String provincia) {
        this.provincia = provincia;
    }

    public String getCodigoPais() {
        return codigoPais;
    }

    public void setCodigoPais(
            String codigoPais) {
        this.codigoPais = codigoPais;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(
            String telefono) {
        this.telefono = telefono;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(
            String email) {
        this.email = email;
    }

    public TipoImpuesto getTipoImpuesto() {
        return tipoImpuesto;
    }

    public void setTipoImpuesto(
            TipoImpuesto tipoImpuesto) {
        this.tipoImpuesto = tipoImpuesto;
    }

    public String getVerifactuNumeroInstalacion() {
        return verifactuNumeroInstalacion;
    }

    public void setVerifactuNumeroInstalacion(
            String verifactuNumeroInstalacion) {
        this.verifactuNumeroInstalacion =
                verifactuNumeroInstalacion;
    }

    public boolean isActiva() {
        return activa;
    }

    public void setActiva(
            boolean activa) {
        this.activa = activa;
    }

    public Instant getFechaCreacion() {
        return fechaCreacion;
    }

    public Instant getFechaModificacion() {
        return fechaModificacion;
    }
}