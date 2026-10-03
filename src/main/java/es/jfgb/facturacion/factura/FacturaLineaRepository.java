package es.jfgb.facturacion.factura;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FacturaLineaRepository
        extends JpaRepository<FacturaLinea, Long> {

    List<FacturaLinea>
    findAllByFactura_IdOrderByOrdenAsc(Long facturaId);
}