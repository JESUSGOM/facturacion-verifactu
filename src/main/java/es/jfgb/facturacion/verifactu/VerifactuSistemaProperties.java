package es.jfgb.facturacion.verifactu;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@Validated
@ConfigurationProperties(prefix = "verifactu.sistema")
public class VerifactuSistemaProperties {

    @NotBlank
    @Size(max = 120)
    private String nombreRazon;

    @NotBlank
    @Size(max = 20)
    private String nif;

    @NotBlank
    @Size(max = 30)
    private String nombreSistema;

    @NotBlank
    @Size(max = 2)
    private String idSistema;

    @NotBlank
    @Size(max = 50)
    private String version;

    @NotBlank
    @Pattern(regexp = "[SN]")
    private String soloVerifactu;

    @NotBlank
    @Pattern(regexp = "[SN]")
    private String multiOt;

    public VerifactuSistemaProperties() {
    }

    public String getNombreRazon() {
        return nombreRazon;
    }

    public void setNombreRazon(
            String nombreRazon) {
        this.nombreRazon = nombreRazon;
    }

    public String getNif() {
        return nif;
    }

    public void setNif(
            String nif) {
        this.nif = nif;
    }

    public String getNombreSistema() {
        return nombreSistema;
    }

    public void setNombreSistema(
            String nombreSistema) {
        this.nombreSistema = nombreSistema;
    }

    public String getIdSistema() {
        return idSistema;
    }

    public void setIdSistema(
            String idSistema) {
        this.idSistema = idSistema;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(
            String version) {
        this.version = version;
    }

    public String getSoloVerifactu() {
        return soloVerifactu;
    }

    public void setSoloVerifactu(
            String soloVerifactu) {
        this.soloVerifactu = soloVerifactu;
    }

    public String getMultiOt() {
        return multiOt;
    }

    public void setMultiOt(
            String multiOt) {
        this.multiOt = multiOt;
    }
}