package es.jfgb.facturacion.verifactu;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RegistroFacturacionIntentoRepository
        extends JpaRepository<RegistroFacturacionIntento, Long> {

    List<RegistroFacturacionIntento>
    findAllByRegistroFacturacion_IdOrderByNumeroIntentoAsc(Long registroFacturacionId);

    Optional<RegistroFacturacionIntento>
    findByRegistroFacturacion_IdAndNumeroIntento(
            Long registroFacturacionId,
            Integer numeroIntento
    );
}