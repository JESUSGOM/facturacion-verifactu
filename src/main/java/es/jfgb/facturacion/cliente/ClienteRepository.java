package es.jfgb.facturacion.cliente;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    List<Cliente> findAllByEmpresa_IdOrderByNombreAsc(Long empresaId);

    Optional<Cliente> findByIdAndEmpresa_Id(Long id, Long empresaId);

    boolean existsByEmpresa_IdAndNifIgnoreCase(Long empresaId, String nif);
}