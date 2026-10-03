package es.jfgb.facturacion.empresa;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmpresaServiceTest {

    @Mock
    private EmpresaRepository empresaRepository;

    private EmpresaService service;

    @BeforeEach
    void setUp() {
        service = new EmpresaService(
                empresaRepository
        );
    }

    @Test
    void modificarActualizaDatosYNormalizaNif() {

        Empresa empresa = empresaBase();

        EmpresaRequest request = request(
                " b87654321 ",
                " Empresa modificada "
        );

        when(
                empresaRepository.findById(1L)
        ).thenReturn(
                Optional.of(empresa)
        );

        when(
                empresaRepository.existsByNif(
                        "B87654321"
                )
        ).thenReturn(false);

        when(
                empresaRepository.saveAndFlush(
                        empresa
                )
        ).thenReturn(empresa);

        EmpresaResponse response =
                service.modificar(
                        1L,
                        request
                );

        assertEquals(
                "B87654321",
                empresa.getNif()
        );

        assertEquals(
                "Empresa modificada",
                empresa.getRazonSocial()
        );

        assertEquals(
                "B87654321",
                response.nif()
        );

        assertTrue(
                response.activa()
        );
    }

    @Test
    void modificarRechazaNifDuplicado() {

        Empresa empresa = empresaBase();

        EmpresaRequest request = request(
                "B99999999",
                "Empresa duplicada"
        );

        when(
                empresaRepository.findById(1L)
        ).thenReturn(
                Optional.of(empresa)
        );

        when(
                empresaRepository.existsByNif(
                        "B99999999"
                )
        ).thenReturn(true);

        ResponseStatusException ex =
                assertThrows(
                        ResponseStatusException.class,
                        () -> service.modificar(
                                1L,
                                request
                        )
                );

        assertEquals(
                HttpStatus.CONFLICT,
                ex.getStatusCode()
        );

        verify(
                empresaRepository,
                never()
        ).saveAndFlush(
                any()
        );
    }

    @Test
    void cambiarActivaDesactivaEmpresa() {

        Empresa empresa = empresaBase();

        when(
                empresaRepository.findConBloqueo(1L)
        ).thenReturn(
                Optional.of(empresa)
        );

        when(
                empresaRepository.saveAndFlush(
                        empresa
                )
        ).thenReturn(empresa);

        EmpresaResponse response =
                service.cambiarActiva(
                        1L,
                        false
                );

        assertFalse(
                empresa.isActiva()
        );

        assertFalse(
                response.activa()
        );

        verify(
                empresaRepository
        ).findConBloqueo(1L);
    }

    private Empresa empresaBase() {

        Empresa empresa =
                new Empresa();

        empresa.setNif(
                "B12345678"
        );

        empresa.setRazonSocial(
                "Empresa original"
        );

        empresa.setCodigoPais(
                "ES"
        );

        empresa.setTipoImpuesto(
                TipoImpuesto.values()[0]
        );

        empresa.setActiva(true);

        return empresa;
    }

    private EmpresaRequest request(
            String nif,
            String razonSocial) {

        EmpresaRequest request =
                new EmpresaRequest();

        request.setNif(nif);
        request.setRazonSocial(razonSocial);
        request.setCodigoPais("es");
        request.setTipoImpuesto(
                TipoImpuesto.values()[0]
        );

        return request;
    }
}