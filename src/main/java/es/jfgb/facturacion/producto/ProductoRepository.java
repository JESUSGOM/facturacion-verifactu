package es.jfgb.facturacion.producto;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    List<Producto> findAllByEmpresa_IdOrderByNombreAsc(Long empresaId);

    Optional<Producto> findByIdAndEmpresa_Id(
            Long id,
            Long empresaId
    );

    boolean existsByEmpresa_IdAndCodigoIgnoreCase(
            Long empresaId,
            String codigo
    );
}