package es.jfgb.facturacion.cliente;

import es.jfgb.facturacion.empresa.Empresa;
import es.jfgb.facturacion.empresa.EmpresaRepository;
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
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private EmpresaRepository empresaRepository;

    private ClienteService service;

    @BeforeEach
    void setUp() {

        service =
                new ClienteService(
                        clienteRepository,
                        empresaRepository
                );
    }

    @Test
    void modificarActualizaDatosYNormalizaNif() {

        Empresa empresa = empresa(true);

        Cliente cliente =
                clienteBase(empresa);

        ClienteRequest request =
                request(
                        " 12345678z ",
                        " Cliente modificado "
                );

        when(
                clienteRepository
                        .findByIdAndEmpresa_Id(
                                10L,
                                1L
                        )
        ).thenReturn(
                Optional.of(cliente)
        );

        when(
                clienteRepository
                        .existsByEmpresa_IdAndNifIgnoreCase(
                                1L,
                                "12345678Z"
                        )
        ).thenReturn(false);

        when(
                clienteRepository.saveAndFlush(
                        cliente
                )
        ).thenReturn(cliente);

        ClienteResponse response =
                service.modificar(
                        1L,
                        10L,
                        request
                );

        assertEquals(
                "12345678Z",
                cliente.getNif()
        );

        assertEquals(
                "Cliente modificado",
                cliente.getNombre()
        );

        assertEquals(
                "12345678Z",
                response.nif()
        );

        assertTrue(
                response.activo()
        );
    }

    @Test
    void modificarRechazaNifDuplicado() {

        Empresa empresa = empresa(true);

        Cliente cliente =
                clienteBase(empresa);

        ClienteRequest request =
                request(
                        "99999999R",
                        "Cliente"
                );

        when(
                clienteRepository
                        .findByIdAndEmpresa_Id(
                                10L,
                                1L
                        )
        ).thenReturn(
                Optional.of(cliente)
        );

        when(
                clienteRepository
                        .existsByEmpresa_IdAndNifIgnoreCase(
                                1L,
                                "99999999R"
                        )
        ).thenReturn(true);

        ResponseStatusException ex =
                assertThrows(
                        ResponseStatusException.class,
                        () -> service.modificar(
                                1L,
                                10L,
                                request
                        )
                );

        assertEquals(
                HttpStatus.CONFLICT,
                ex.getStatusCode()
        );

        verify(
                clienteRepository,
                never()
        ).saveAndFlush(
                any()
        );
    }

    @Test
    void cambiarActivoDesactivaCliente() {

        Empresa empresa = empresa(true);

        Cliente cliente =
                clienteBase(empresa);

        when(
                clienteRepository
                        .findByIdAndEmpresa_Id(
                                10L,
                                1L
                        )
        ).thenReturn(
                Optional.of(cliente)
        );

        when(
                clienteRepository.saveAndFlush(
                        cliente
                )
        ).thenReturn(cliente);

        ClienteResponse response =
                service.cambiarActivo(
                        1L,
                        10L,
                        false
                );

        assertFalse(
                cliente.isActivo()
        );

        assertFalse(
                response.activo()
        );
    }

    @Test
    void crearRechazaEmpresaInactiva() {

        Empresa empresa =
                empresa(false);

        when(
                empresaRepository.findById(1L)
        ).thenReturn(
                Optional.of(empresa)
        );

        ResponseStatusException ex =
                assertThrows(
                        ResponseStatusException.class,
                        () -> service.crear(
                                1L,
                                request(
                                        "12345678Z",
                                        "Cliente"
                                )
                        )
                );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                ex.getStatusCode()
        );

        verify(
                clienteRepository,
                never()
        ).saveAndFlush(
                any()
        );
    }

    private Empresa empresa(
            boolean activa) {

        Empresa empresa =
                new Empresa();

        empresa.setActiva(
                activa
        );

        return empresa;
    }

    private Cliente clienteBase(
            Empresa empresa) {

        Cliente cliente =
                new Cliente();

        cliente.setEmpresa(
                empresa
        );

        cliente.setNif(
                "11111111H"
        );

        cliente.setNombre(
                "Cliente original"
        );

        cliente.setCodigoPais(
                "ES"
        );

        cliente.setActivo(
                true
        );

        return cliente;
    }

    private ClienteRequest request(
            String nif,
            String nombre) {

        ClienteRequest request =
                new ClienteRequest();

        request.setNif(nif);
        request.setNombre(nombre);
        request.setCodigoPais("es");

        return request;
    }
}