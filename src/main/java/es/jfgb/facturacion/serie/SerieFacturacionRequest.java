package es.jfgb.facturacion.serie;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SerieFacturacionRequest {

    @NotBlank
    @Size(max = 30)
    private String codigo;

    @NotBlank
    @Size(max = 150)
    private String descripcion;

    /*
     * Se mantiene opcional por compatibilidad con clientes
     * anteriores a V20.
     *
     * En alta:
     *   null -> ORDINARIA
     *
     * En modificacion:
     *   null -> conservar el tipo existente
     */
    private TipoSerieFacturacion tipoSerie;

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(
            String descripcion) {
        this.descripcion = descripcion;
    }

    public TipoSerieFacturacion getTipoSerie() {
        return tipoSerie;
    }

    public void setTipoSerie(
            TipoSerieFacturacion tipoSerie) {
        this.tipoSerie = tipoSerie;
    }
}