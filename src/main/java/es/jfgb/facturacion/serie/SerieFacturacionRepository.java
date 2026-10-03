package es.jfgb.facturacion.serie;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SerieFacturacionRepository
        extends JpaRepository<SerieFacturacion, Long> {

    List<SerieFacturacion>
    findAllByEmpresa_IdOrderByCodigoAsc(Long empresaId);

    Optional<SerieFacturacion>
    findByIdAndEmpresa_Id(Long id, Long empresaId);

    boolean existsByEmpresa_IdAndCodigoIgnoreCase(
            Long empresaId,
            String codigo
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT s
            FROM SerieFacturacion s
            WHERE s.id = :serieId
              AND s.empresa.id = :empresaId
            """)
    Optional<SerieFacturacion> findConBloqueo(
            @Param("serieId") Long serieId,
            @Param("empresaId") Long empresaId
    );
}