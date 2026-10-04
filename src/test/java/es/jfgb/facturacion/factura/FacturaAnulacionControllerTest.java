package es.jfgb.facturacion.factura;

import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.PostMapping;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FacturaAnulacionControllerTest {

    @Test
    void endpointAnularDelegaEnFacturaService() {

        FacturaService facturaService =
                mock(
                        FacturaService.class
                );

        FacturaPdfService facturaPdfService =
                mock(
                        FacturaPdfService.class
                );

        FacturaController controller =
                new FacturaController(
                        facturaService,
                        facturaPdfService
                );

        AnulacionFacturaRequest request =
                new AnulacionFacturaRequest(
                        "Factura emitida por error; "
                                + "la operacion no debio facturarse",
                        true
                );

        FacturaResponse respuestaEsperada =
                mock(
                        FacturaResponse.class
                );

        when(
                facturaService.anular(
                        1L,
                        100L,
                        request
                )
        ).thenReturn(
                respuestaEsperada
        );

        FacturaResponse respuesta =
                controller.anular(
                        1L,
                        100L,
                        request
                );

        assertSame(
                respuestaEsperada,
                respuesta
        );

        verify(
                facturaService
        ).anular(
                1L,
                100L,
                request
        );

        verifyNoInteractions(
                facturaPdfService
        );
    }

    @Test
    void mappingAnularTieneLaRutaEsperada()
            throws Exception {

        Method metodo =
                FacturaController.class.getMethod(
                        "anular",
                        Long.class,
                        Long.class,
                        AnulacionFacturaRequest.class
                );

        PostMapping mapping =
                metodo.getAnnotation(
                        PostMapping.class
                );

        assertNotNull(
                mapping
        );

        assertArrayEquals(
                new String[]{
                        "/{facturaId}/anular"
                },
                mapping.value()
        );
    }
}