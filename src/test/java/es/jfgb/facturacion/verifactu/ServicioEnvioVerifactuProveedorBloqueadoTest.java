package es.jfgb.facturacion.verifactu;

import es.jfgb.facturacion.verifactu.provider.VerifactuProvider;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class ServicioEnvioVerifactuProveedorBloqueadoTest {

    @Test
    void proveedorDeshabilitadoBloqueaAntesDeModificarLaBaseDeDatos() {

        EstadoEnvioVerifactuService estadoEnvioService =
                mock(
                        EstadoEnvioVerifactuService.class
                );

        VerifactuProvider provider =
                mock(
                        VerifactuProvider.class
                );

        /*
         * Simulamos el caso peligroso:
         *
         * envioHabilitado = true
         *
         * pero el perfil actual solamente dispone
         * de un proveedor deliberadamente bloqueado.
         */
        when(
                provider.bloqueaEnvio()
        ).thenReturn(
                true
        );

        ServicioEnvioVerifactu servicio =
                new ServicioEnvioVerifactu(
                        estadoEnvioService,
                        provider,
                        true
                );

        ResponseStatusException excepcion =
                assertThrows(
                        ResponseStatusException.class,
                        () ->
                                servicio.enviarAlta(
                                        123L,
                                        456L
                                )
                );

        assertEquals(
                HttpStatus.SERVICE_UNAVAILABLE,
                excepcion.getStatusCode()
        );

        assertEquals(
                503,
                excepcion.getStatusCode().value()
        );

        /*
         * Prueba fundamental:
         *
         * el registro NO llega a tocarse.
         */
        verifyNoInteractions(
                estadoEnvioService
        );

        /*
         * Solo se permite consultar el segundo cerrojo.
         */
        verify(
                provider,
                times(1)
        ).bloqueaEnvio();

        /*
         * Por tanto:
         *
         * - no se consulta el nombre del proveedor;
         * - no se ejecuta enviarAlta();
         * - no existe ninguna llamada externa.
         */
        verifyNoMoreInteractions(
                provider
        );

        System.out.println(
                "VERIFACTU_SEGUNDO_CERROJO_OK=true"
        );

        System.out.println(
                "VERIFACTU_SEGUNDO_CERROJO_HTTP_STATUS="
                        + excepcion.getStatusCode().value()
        );

        System.out.println(
                "VERIFACTU_SEGUNDO_CERROJO_BD=false"
        );

        System.out.println(
                "VERIFACTU_SEGUNDO_CERROJO_PROVEEDOR=false"
        );

        System.out.println(
                "VERIFACTU_SEGUNDO_CERROJO_AEAT=false"
        );
    }
}