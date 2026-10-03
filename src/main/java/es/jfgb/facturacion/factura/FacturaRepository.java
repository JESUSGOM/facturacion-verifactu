package es.jfgb.facturacion.factura;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FacturaRepository
        extends JpaRepository<Factura, Long> {

    List<Factura> findAllByEmpresa_IdOrderByIdDesc(
            Long empresaId
    );

    Optional<Factura> findByIdAndEmpresa_Id(
            Long id,
            Long empresaId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT f
            FROM Factura f
            WHERE f.id = :facturaId
              AND f.empresa.id = :empresaId
            """)
    Optional<Factura> findConBloqueo(
            @Param("empresaId") Long empresaId,
            @Param("facturaId") Long facturaId
    );
}