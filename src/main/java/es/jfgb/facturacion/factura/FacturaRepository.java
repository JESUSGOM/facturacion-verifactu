package es.jfgb.facturacion.factura;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FacturaRepository
        extends JpaRepository<Factura, Long> {

    List<Factura> findAllByEmpresa_IdOrderByIdDesc(Long empresaId);

    Optional<Factura> findByIdAndEmpresa_Id(
            Long id,
            Long empresaId
    );
}