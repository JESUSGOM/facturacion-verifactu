package es.jfgb.facturacion.factura;

import es.jfgb.facturacion.cliente.Cliente;
import es.jfgb.facturacion.empresa.Empresa;
import es.jfgb.facturacion.serie.SerieFacturacion;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "facturas",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_facturas_serie_numero",
                        columnNames = {"serie_id", "numero"}
                )
        }
)
public class Factura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "serie_id", nullable = false)
    private SerieFacturacion serie;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoFactura estado = EstadoFactura.BORRADOR;

    private Long numero;

    @Column(name = "fecha_expedicion")
    private LocalDate fechaExpedicion;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "tipo_factura",
            nullable = false,
            length = 2
    )
    private TipoFactura tipoFactura =
            TipoFactura.F1;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "tipo_rectificativa",
            length = 1
    )
    private TipoRectificativa tipoRectificativa;

    @Column(
            name = "motivo_rectificacion",
            length = 500
    )
    private String motivoRectificacion;

    @Column(
            name = "base_rectificada",
            precision = 15,
            scale = 2
    )
    private BigDecimal baseRectificada;

    @Column(
            name = "cuota_rectificada",
            precision = 15,
            scale = 2
    )
    private BigDecimal cuotaRectificada;
    @Column(length = 1000)
    private String observaciones;

    @Column(name = "base_imponible", nullable = false, precision = 15, scale = 2)
    private BigDecimal baseImponible = BigDecimal.ZERO;

    @Column(name = "cuota_impuesto", nullable = false, precision = 15, scale = 2)
    private BigDecimal cuotaImpuesto = BigDecimal.ZERO;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @Column(name = "cliente_nif", length = 20)
    private String clienteNif;

    @Column(name = "cliente_nombre", length = 150)
    private String clienteNombre;

    @Column(name = "cliente_direccion", length = 200)
    private String clienteDireccion;

    @Column(name = "cliente_codigo_postal", length = 10)
    private String clienteCodigoPostal;

    @Column(name = "cliente_municipio", length = 100)
    private String clienteMunicipio;

    @Column(name = "cliente_provincia", length = 100)
    private String clienteProvincia;

    @Column(name = "cliente_codigo_pais", length = 2)
    private String clienteCodigoPais;

    /*
     * Snapshot historico del emisor.
     *
     * Se rellena exclusivamente al emitir la factura.
     * De esta forma una modificacion posterior de Empresa
     * no altera la representacion de una factura ya emitida.
     */
    @Column(name = "empresa_nif", length = 20)
    private String empresaNif;

    @Column(name = "empresa_razon_social", length = 150)
    private String empresaRazonSocial;

    @Column(name = "empresa_nombre_comercial", length = 150)
    private String empresaNombreComercial;

    @Column(name = "empresa_direccion", length = 200)
    private String empresaDireccion;

    @Column(name = "empresa_codigo_postal", length = 10)
    private String empresaCodigoPostal;

    @Column(name = "empresa_municipio", length = 100)
    private String empresaMunicipio;

    @Column(name = "empresa_provincia", length = 100)
    private String empresaProvincia;

    @Column(name = "empresa_codigo_pais", length = 2)
    private String empresaCodigoPais;
    /*
     * Datos de auditoria de la anulacion.
     *
     * No sustituyen al registro VERI*FACTU de ANULACION.
     */
    @Column(name = "fecha_anulacion")
    private Instant fechaAnulacion;

    @Column(name = "motivo_anulacion", length = 500)
    private String motivoAnulacion;

    /*
     * Facturas originales afectadas por esta rectificativa.
     *
     * Para una F1 esta coleccion permanece vacia.
     */
    @OneToMany(
            mappedBy = "facturaRectificativa",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("orden ASC")
    private List<FacturaRectificada> facturasRectificadas =
            new ArrayList<>();

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private Instant fechaCreacion;

    @Column(name = "fecha_modificacion")
    private Instant fechaModificacion;

    @Column(name = "fecha_emision")
    private Instant fechaEmision;

    @OneToMany(
            mappedBy = "factura",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("orden ASC")
    private List<FacturaLinea> lineas = new ArrayList<>();

    public Factura() {
    }

    @PrePersist
    public void prePersist() {
        fechaCreacion = Instant.now();

        if (estado == null) {
            estado = EstadoFactura.BORRADOR;
        }

        if (baseImponible == null) {
            baseImponible = BigDecimal.ZERO;
        }

        if (cuotaImpuesto == null) {
            cuotaImpuesto = BigDecimal.ZERO;
        }

        if (total == null) {
            total = BigDecimal.ZERO;
        }
    }

    @PreUpdate
    public void preUpdate() {
        fechaModificacion = Instant.now();
    }

    public void agregarLinea(FacturaLinea linea) {
        lineas.add(linea);
        linea.setFactura(this);
    }

    public void eliminarLinea(FacturaLinea linea) {
        lineas.remove(linea);
        linea.setFactura(null);
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

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public SerieFacturacion getSerie() {
        return serie;
    }

    public void setSerie(SerieFacturacion serie) {
        this.serie = serie;
    }

    public EstadoFactura getEstado() {
        return estado;
    }

    public void setEstado(EstadoFactura estado) {
        this.estado = estado;
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

    public void setFechaExpedicion(LocalDate fechaExpedicion) {
        this.fechaExpedicion = fechaExpedicion;
    }

    public TipoFactura getTipoFactura() {
        return tipoFactura;
    }

    public void setTipoFactura(
            TipoFactura tipoFactura) {
        this.tipoFactura = tipoFactura;
    }

    public TipoRectificativa getTipoRectificativa() {
        return tipoRectificativa;
    }

    public void setTipoRectificativa(
            TipoRectificativa tipoRectificativa) {
        this.tipoRectificativa =
                tipoRectificativa;
    }

    public String getMotivoRectificacion() {
        return motivoRectificacion;
    }

    public void setMotivoRectificacion(
            String motivoRectificacion) {
        this.motivoRectificacion =
                motivoRectificacion;
    }

    public BigDecimal getBaseRectificada() {
        return baseRectificada;
    }

    public void setBaseRectificada(
            BigDecimal baseRectificada) {
        this.baseRectificada =
                baseRectificada;
    }

    public BigDecimal getCuotaRectificada() {
        return cuotaRectificada;
    }

    public void setCuotaRectificada(
            BigDecimal cuotaRectificada) {
        this.cuotaRectificada =
                cuotaRectificada;
    }
    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public BigDecimal getBaseImponible() {
        return baseImponible;
    }

    public void setBaseImponible(BigDecimal baseImponible) {
        this.baseImponible = baseImponible;
    }

    public BigDecimal getCuotaImpuesto() {
        return cuotaImpuesto;
    }

    public void setCuotaImpuesto(BigDecimal cuotaImpuesto) {
        this.cuotaImpuesto = cuotaImpuesto;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getClienteNif() {
        return clienteNif;
    }

    public void setClienteNif(String clienteNif) {
        this.clienteNif = clienteNif;
    }

    public String getClienteNombre() {
        return clienteNombre;
    }

    public void setClienteNombre(String clienteNombre) {
        this.clienteNombre = clienteNombre;
    }

    public String getClienteDireccion() {
        return clienteDireccion;
    }

    public void setClienteDireccion(String clienteDireccion) {
        this.clienteDireccion = clienteDireccion;
    }

    public String getClienteCodigoPostal() {
        return clienteCodigoPostal;
    }

    public void setClienteCodigoPostal(String clienteCodigoPostal) {
        this.clienteCodigoPostal = clienteCodigoPostal;
    }

    public String getClienteMunicipio() {
        return clienteMunicipio;
    }

    public void setClienteMunicipio(String clienteMunicipio) {
        this.clienteMunicipio = clienteMunicipio;
    }

    public String getClienteProvincia() {
        return clienteProvincia;
    }

    public void setClienteProvincia(String clienteProvincia) {
        this.clienteProvincia = clienteProvincia;
    }

    public String getClienteCodigoPais() {
        return clienteCodigoPais;
    }

    public void setClienteCodigoPais(String clienteCodigoPais) {
        this.clienteCodigoPais = clienteCodigoPais;
    }

    public String getEmpresaNif() {
        return empresaNif;
    }

    public void setEmpresaNif(
            String empresaNif) {
        this.empresaNif = empresaNif;
    }

    public String getEmpresaRazonSocial() {
        return empresaRazonSocial;
    }

    public void setEmpresaRazonSocial(
            String empresaRazonSocial) {
        this.empresaRazonSocial =
                empresaRazonSocial;
    }

    public String getEmpresaNombreComercial() {
        return empresaNombreComercial;
    }

    public void setEmpresaNombreComercial(
            String empresaNombreComercial) {
        this.empresaNombreComercial =
                empresaNombreComercial;
    }

    public String getEmpresaDireccion() {
        return empresaDireccion;
    }

    public void setEmpresaDireccion(
            String empresaDireccion) {
        this.empresaDireccion =
                empresaDireccion;
    }

    public String getEmpresaCodigoPostal() {
        return empresaCodigoPostal;
    }

    public void setEmpresaCodigoPostal(
            String empresaCodigoPostal) {
        this.empresaCodigoPostal =
                empresaCodigoPostal;
    }

    public String getEmpresaMunicipio() {
        return empresaMunicipio;
    }

    public void setEmpresaMunicipio(
            String empresaMunicipio) {
        this.empresaMunicipio =
                empresaMunicipio;
    }

    public String getEmpresaProvincia() {
        return empresaProvincia;
    }

    public void setEmpresaProvincia(
            String empresaProvincia) {
        this.empresaProvincia =
                empresaProvincia;
    }

    public String getEmpresaCodigoPais() {
        return empresaCodigoPais;
    }

    public void setEmpresaCodigoPais(
            String empresaCodigoPais) {
        this.empresaCodigoPais =
                empresaCodigoPais;
    }
    public Instant getFechaAnulacion() {
        return fechaAnulacion;
    }

    public void setFechaAnulacion(
            Instant fechaAnulacion) {
        this.fechaAnulacion =
                fechaAnulacion;
    }

    public String getMotivoAnulacion() {
        return motivoAnulacion;
    }

    public void setMotivoAnulacion(
            String motivoAnulacion) {
        this.motivoAnulacion =
                motivoAnulacion;
    }

    public List<FacturaRectificada> getFacturasRectificadas() {
        return facturasRectificadas;
    }

    public void agregarFacturaRectificada(
            FacturaRectificada relacion) {

        relacion.setFacturaRectificativa(
                this
        );

        facturasRectificadas.add(
                relacion
        );
    }

    public void limpiarFacturasRectificadas() {

        facturasRectificadas.clear();
    }
    public Instant getFechaCreacion() {
        return fechaCreacion;
    }

    public Instant getFechaModificacion() {
        return fechaModificacion;
    }

    public Instant getFechaEmision() {
        return fechaEmision;
    }

    public void setFechaEmision(Instant fechaEmision) {
        this.fechaEmision = fechaEmision;
    }

    public List<FacturaLinea> getLineas() {
        return lineas;
    }

    public void setLineas(List<FacturaLinea> lineas) {
        this.lineas = lineas;
    }
}