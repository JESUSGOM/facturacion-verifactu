package es.jfgb.facturacion.empresa;

import org.springframework.data.jpa.repository.JpaRepository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EmpresaRepository extends JpaRepository<Empresa, Long> {

    boolean existsByNif(String nif);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT e
        FROM Empresa e
        WHERE e.id = :empresaId
        """)
    Optional<Empresa> findConBloqueo(
            @Param("empresaId") Long empresaId
    );
}