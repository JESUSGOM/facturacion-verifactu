package es.jfgb.facturacion.verifactu;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RegistroFacturacionIntentoService {

    private final RegistroFacturacionService registroFacturacionService;
    private final RegistroFacturacionIntentoRepository intentoRepository;

    public RegistroFacturacionIntentoService(
            RegistroFacturacionService registroFacturacionService,
            RegistroFacturacionIntentoRepository intentoRepository) {

        this.registroFacturacionService = registroFacturacionService;
        this.intentoRepository = intentoRepository;
    }

    @Transactional(readOnly = true)
    public List<RegistroFacturacionIntentoResponse> listar(
            Long empresaId,
            Long registroId) {

        /*
         * Además de comprobar que el registro existe,
         * esta llamada valida que pertenece a la empresa
         * indicada en la URL.
         */
        registroFacturacionService.obtener(
                empresaId,
                registroId
        );

        return intentoRepository
                .findAllByRegistroFacturacion_IdOrderByNumeroIntentoAsc(
                        registroId
                )
                .stream()
                .map(RegistroFacturacionIntentoResponse::from)
                .toList();
    }
}