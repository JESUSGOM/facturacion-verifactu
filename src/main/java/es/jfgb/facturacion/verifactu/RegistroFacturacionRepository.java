package es.jfgb.facturacion.verifactu;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface RegistroFacturacionRepository
        extends JpaRepository<RegistroFacturacion, Long> {

    List<RegistroFacturacion>
    findAllByEmpresa_IdOrderByIdAsc(Long empresaId);

    Optional<RegistroFacturacion>
    findByIdAndEmpresa_Id(
            Long id,
            Long empresaId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<RegistroFacturacion>
    findTopByEmpresa_IdOrderByIdDesc(
            Long empresaId
    );

    boolean existsByFactura_IdAndTipoRegistro(
            Long facturaId,
            TipoRegistroFacturacion tipoRegistro
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT r
        FROM RegistroFacturacion r
        WHERE r.id = :registroId
          AND r.empresa.id = :empresaId
        """)
    Optional<RegistroFacturacion> findConBloqueo(
            @Param("empresaId") Long empresaId,
            @Param("registroId") Long registroId
    );

    @Query("""
        SELECT r.id
        FROM RegistroFacturacion r
        WHERE r.estadoEnvio = :estado
          AND r.fechaUltimoIntento IS NOT NULL
          AND r.fechaUltimoIntento < :limite
        ORDER BY r.id
        """)
    List<Long> findIdsPorEstadoYFechaUltimoIntentoAnteriorA(
            @Param("estado") EstadoEnvioRegistro estado,
            @Param("limite") Instant limite
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    SELECT r
    FROM RegistroFacturacion r
    WHERE r.id = :registroId
    """)
    Optional<RegistroFacturacion> findConBloqueoPorId(
            @Param("registroId") Long registroId
    );
}