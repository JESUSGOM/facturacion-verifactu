package es.jfgb.facturacion.verifactu;

import es.jfgb.facturacion.empresa.Empresa;
import es.jfgb.facturacion.factura.Factura;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "registros_facturacion")
public class RegistroFacturacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "empresa_id",
            nullable = false,
            updatable = false
    )
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "factura_id",
            nullable = false,
            updatable = false
    )
    private Factura factura;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "tipo_registro",
            nullable = false,
            length = 20,
            updatable = false
    )
    private TipoRegistroFacturacion tipoRegistro;

    @Column(
            name = "nif_emisor",
            nullable = false,
            length = 20,
            updatable = false
    )
    private String nifEmisor;

    @Column(
            nullable = false,
            length = 30,
            updatable = false
    )
    private String serie;

    @Column(
            nullable = false,
            updatable = false
    )
    private Long numero;

    @Column(
            name = "fecha_expedicion",
            nullable = false,
            updatable = false
    )
    private LocalDate fechaExpedicion;

    @Column(
            name = "tipo_factura",
            nullable = false,
            length = 10,
            updatable = false
    )
    private String tipoFactura;

    @Column(
            name = "cuota_total",
            nullable = false,
            precision = 15,
            scale = 2,
            updatable = false
    )
    private BigDecimal cuotaTotal;

    @Column(
            name = "importe_total",
            nullable = false,
            precision = 15,
            scale = 2,
            updatable = false
    )
    private BigDecimal importeTotal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "registro_anterior_id",
            updatable = false
    )
    private RegistroFacturacion registroAnterior;

    @Column(
            name = "huella_anterior",
            length = 64,
            updatable = false
    )
    private String huellaAnterior;

    @Column(
            name = "fecha_hora_generacion",
            nullable = false,
            length = 35,
            updatable = false
    )
    private String fechaHoraGeneracion;

    @Column(
            nullable = false,
            length = 64,
            updatable = false
    )
    private String huella;

    /*
     * =========================================================
     * Snapshot fiscal AEAT / VERI*FACTU
     *
     * Estas columnas son nullable en base de datos porque los
     * registros históricos anteriores a V16 no las tienen.
     *
     * Para los registros nuevos las rellenaremos desde
     * RegistroFacturacionService.
     * =========================================================
     */

    @Column(
            name = "id_version_aeat",
            length = 10,
            updatable = false
    )
    private String idVersionAeat;

    @Column(
            name = "nombre_razon_emisor",
            length = 120,
            updatable = false
    )
    private String nombreRazonEmisor;

    @Column(
            name = "descripcion_operacion",
            length = 500,
            updatable = false
    )
    private String descripcionOperacion;

    @Column(
            name = "destinatario_nombre",
            length = 120,
            updatable = false
    )
    private String destinatarioNombre;

    @Column(
            name = "destinatario_nif",
            length = 20,
            updatable = false
    )
    private String destinatarioNif;

    @Column(
            name = "destinatario_codigo_pais",
            length = 2,
            updatable = false
    )
    private String destinatarioCodigoPais;

    @Column(
            name = "destinatario_tipo_identificacion",
            length = 10,
            updatable = false
    )
    private String destinatarioTipoIdentificacion;

    @Column(
            name = "destinatario_id_otro",
            length = 100,
            updatable = false
    )
    private String destinatarioIdOtro;

    @Column(
            name = "tipo_huella",
            length = 10,
            updatable = false
    )
    private String tipoHuella;

    @Column(
            name = "sistema_nombre_razon",
            length = 120,
            updatable = false
    )
    private String sistemaNombreRazon;

    @Column(
            name = "sistema_nif",
            length = 20,
            updatable = false
    )
    private String sistemaNif;

    @Column(
            name = "sistema_nombre",
            length = 30,
            updatable = false
    )
    private String sistemaNombre;

    @Column(
            name = "sistema_id",
            length = 2,
            updatable = false
    )
    private String sistemaId;

    @Column(
            name = "sistema_version",
            length = 50,
            updatable = false
    )
    private String sistemaVersion;

    @Column(
            name = "sistema_numero_instalacion",
            length = 100,
            updatable = false
    )
    private String sistemaNumeroInstalacion;

    @Column(
            name = "sistema_solo_verifactu",
            length = 1,
            updatable = false
    )
    private String sistemaSoloVerifactu;

    @Column(
            name = "sistema_multi_ot",
            length = 1,
            updatable = false
    )
    private String sistemaMultiOt;

    @Column(
            name = "sistema_multiples_ot",
            length = 1,
            updatable = false
    )
    private String sistemaMultiplesOt;

    @OneToMany(
            mappedBy = "registroFacturacion",
            cascade = CascadeType.PERSIST,
            fetch = FetchType.LAZY
    )
    @OrderBy("orden ASC")
    private List<RegistroFacturacionDesglose> desgloses =
            new ArrayList<>();

    /*
     * =========================================================
     * Estado de envío
     * =========================================================
     */

    @Enumerated(EnumType.STRING)
    @Column(
            name = "estado_envio",
            nullable = false,
            length = 20
    )
    private EstadoEnvioRegistro estadoEnvio =
            EstadoEnvioRegistro.PENDIENTE;

    @Column(
            name = "intentos_envio",
            nullable = false
    )
    private Integer intentosEnvio = 0;

    @Column(name = "fecha_ultimo_intento")
    private Instant fechaUltimoIntento;

    @Column(length = 50)
    private String proveedor;

    @Column(
            name = "identificador_externo",
            length = 255
    )
    private String identificadorExterno;

    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(
            name = "peticion",
            columnDefinition = "LONGTEXT"
    )
    private String peticion;

    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(
            name = "respuesta",
            columnDefinition = "LONGTEXT"
    )
    private String respuesta;

    @Column(name = "fecha_envio")
    private Instant fechaEnvio;

    @Column(
            name = "fecha_creacion",
            nullable = false,
            updatable = false
    )
    private Instant fechaCreacion;

    @Column(
            name = "num_serie_factura",
            nullable = false,
            length = 60,
            updatable = false
    )
    private String numSerieFactura;

    public RegistroFacturacion() {
    }

    @PrePersist
    public void prePersist() {

        if (fechaCreacion == null) {
            fechaCreacion = Instant.now();
        }

        if (estadoEnvio == null) {
            estadoEnvio = EstadoEnvioRegistro.PENDIENTE;
        }

        if (intentosEnvio == null) {
            intentosEnvio = 0;
        }
    }

    /*
     * =========================================================
     * Relaciones auxiliares
     * =========================================================
     */

    public void addDesglose(
            RegistroFacturacionDesglose desglose) {

        if (desglose == null) {
            return;
        }

        desglose.setRegistroFacturacion(this);
        desgloses.add(desglose);
    }

    /*
     * =========================================================
     * Getters / Setters
     * =========================================================
     */

    public Long getId() {
        return id;
    }

    public Empresa getEmpresa() {
        return empresa;
    }

    public void setEmpresa(Empresa empresa) {
        this.empresa = empresa;
    }

    public Factura getFactura() {
        return factura;
    }

    public void setFactura(Factura factura) {
        this.factura = factura;
    }

    public TipoRegistroFacturacion getTipoRegistro() {
        return tipoRegistro;
    }

    public void setTipoRegistro(
            TipoRegistroFacturacion tipoRegistro) {
        this.tipoRegistro = tipoRegistro;
    }

    public String getNifEmisor() {
        return nifEmisor;
    }

    public void setNifEmisor(String nifEmisor) {
        this.nifEmisor = nifEmisor;
    }

    public String getSerie() {
        return serie;
    }

    public void setSerie(String serie) {
        this.serie = serie;
    }

    public Long getNumero() {
        return numero;
    }

    public void setNumero(Long numero) {
        this.numero = numero;
    }

    public LocalDate getFechaExpedicion() {
        return fechaExpedicion;
    }

    public void setFechaExpedicion(
            LocalDate fechaExpedicion) {
        this.fechaExpedicion = fechaExpedicion;
    }

    public String getTipoFactura() {
        return tipoFactura;
    }

    public void setTipoFactura(String tipoFactura) {
        this.tipoFactura = tipoFactura;
    }

    public BigDecimal getCuotaTotal() {
        return cuotaTotal;
    }

    public void setCuotaTotal(BigDecimal cuotaTotal) {
        this.cuotaTotal = cuotaTotal;
    }

    public BigDecimal getImporteTotal() {
        return importeTotal;
    }

    public void setImporteTotal(BigDecimal importeTotal) {
        this.importeTotal = importeTotal;
    }

    public RegistroFacturacion getRegistroAnterior() {
        return registroAnterior;
    }

    public void setRegistroAnterior(
            RegistroFacturacion registroAnterior) {
        this.registroAnterior = registroAnterior;
    }

    public String getHuellaAnterior() {
        return huellaAnterior;
    }

    public void setHuellaAnterior(
            String huellaAnterior) {
        this.huellaAnterior = huellaAnterior;
    }

    public String getFechaHoraGeneracion() {
        return fechaHoraGeneracion;
    }

    public void setFechaHoraGeneracion(
            String fechaHoraGeneracion) {
        this.fechaHoraGeneracion = fechaHoraGeneracion;
    }

    public String getHuella() {
        return huella;
    }

    public void setHuella(String huella) {
        this.huella = huella;
    }

    public String getIdVersionAeat() {
        return idVersionAeat;
    }

    public void setIdVersionAeat(String idVersionAeat) {
        this.idVersionAeat = idVersionAeat;
    }

    public String getNombreRazonEmisor() {
        return nombreRazonEmisor;
    }

    public void setNombreRazonEmisor(
            String nombreRazonEmisor) {
        this.nombreRazonEmisor = nombreRazonEmisor;
    }

    public String getDescripcionOperacion() {
        return descripcionOperacion;
    }

    public void setDescripcionOperacion(
            String descripcionOperacion) {
        this.descripcionOperacion = descripcionOperacion;
    }

    public String getDestinatarioNombre() {
        return destinatarioNombre;
    }

    public void setDestinatarioNombre(
            String destinatarioNombre) {
        this.destinatarioNombre = destinatarioNombre;
    }

    public String getDestinatarioNif() {
        return destinatarioNif;
    }

    public void setDestinatarioNif(
            String destinatarioNif) {
        this.destinatarioNif = destinatarioNif;
    }

    public String getDestinatarioCodigoPais() {
        return destinatarioCodigoPais;
    }

    public void setDestinatarioCodigoPais(
            String destinatarioCodigoPais) {
        this.destinatarioCodigoPais =
                destinatarioCodigoPais;
    }

    public String getDestinatarioTipoIdentificacion() {
        return destinatarioTipoIdentificacion;
    }

    public void setDestinatarioTipoIdentificacion(
            String destinatarioTipoIdentificacion) {
        this.destinatarioTipoIdentificacion =
                destinatarioTipoIdentificacion;
    }

    public String getDestinatarioIdOtro() {
        return destinatarioIdOtro;
    }

    public void setDestinatarioIdOtro(
            String destinatarioIdOtro) {
        this.destinatarioIdOtro = destinatarioIdOtro;
    }

    public String getTipoHuella() {
        return tipoHuella;
    }

    public void setTipoHuella(String tipoHuella) {
        this.tipoHuella = tipoHuella;
    }

    public String getSistemaNombreRazon() {
        return sistemaNombreRazon;
    }

    public void setSistemaNombreRazon(
            String sistemaNombreRazon) {
        this.sistemaNombreRazon = sistemaNombreRazon;
    }

    public String getSistemaNif() {
        return sistemaNif;
    }

    public void setSistemaNif(String sistemaNif) {
        this.sistemaNif = sistemaNif;
    }

    public String getSistemaNombre() {
        return sistemaNombre;
    }

    public void setSistemaNombre(
            String sistemaNombre) {
        this.sistemaNombre = sistemaNombre;
    }

    public String getSistemaId() {
        return sistemaId;
    }

    public void setSistemaId(String sistemaId) {
        this.sistemaId = sistemaId;
    }

    public String getSistemaVersion() {
        return sistemaVersion;
    }

    public void setSistemaVersion(
            String sistemaVersion) {
        this.sistemaVersion = sistemaVersion;
    }

    public String getSistemaNumeroInstalacion() {
        return sistemaNumeroInstalacion;
    }

    public void setSistemaNumeroInstalacion(
            String sistemaNumeroInstalacion) {
        this.sistemaNumeroInstalacion =
                sistemaNumeroInstalacion;
    }

    public String getSistemaSoloVerifactu() {
        return sistemaSoloVerifactu;
    }

    public void setSistemaSoloVerifactu(
            String sistemaSoloVerifactu) {
        this.sistemaSoloVerifactu =
                sistemaSoloVerifactu;
    }

    public String getSistemaMultiOt() {
        return sistemaMultiOt;
    }

    public void setSistemaMultiOt(
            String sistemaMultiOt) {
        this.sistemaMultiOt = sistemaMultiOt;
    }

    public String getSistemaMultiplesOt() {
        return sistemaMultiplesOt;
    }

    public void setSistemaMultiplesOt(
            String sistemaMultiplesOt) {
        this.sistemaMultiplesOt =
                sistemaMultiplesOt;
    }

    public List<RegistroFacturacionDesglose> getDesgloses() {
        return desgloses;
    }

    public EstadoEnvioRegistro getEstadoEnvio() {
        return estadoEnvio;
    }

    public void setEstadoEnvio(
            EstadoEnvioRegistro estadoEnvio) {
        this.estadoEnvio = estadoEnvio;
    }

    public Integer getIntentosEnvio() {
        return intentosEnvio;
    }

    public void setIntentosEnvio(
            Integer intentosEnvio) {
        this.intentosEnvio = intentosEnvio;
    }

    public Instant getFechaUltimoIntento() {
        return fechaUltimoIntento;
    }

    public void setFechaUltimoIntento(
            Instant fechaUltimoIntento) {
        this.fechaUltimoIntento =
                fechaUltimoIntento;
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

    public void setIdentificadorExterno(
            String identificadorExterno) {
        this.identificadorExterno =
                identificadorExterno;
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

    public Instant getFechaEnvio() {
        return fechaEnvio;
    }

    public void setFechaEnvio(Instant fechaEnvio) {
        this.fechaEnvio = fechaEnvio;
    }

    public Instant getFechaCreacion() {
        return fechaCreacion;
    }

    public String getNumSerieFactura() {
        return numSerieFactura;
    }

    public void setNumSerieFactura(
            String numSerieFactura) {
        this.numSerieFactura = numSerieFactura;
    }
}