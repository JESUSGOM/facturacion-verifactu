package es.jfgb.facturacion.verifactu.aeat;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReconciliacionVerifactuControllerTest {

    @Test
    void delegaLaReconciliacionEnElServicio() {

        ServicioReconciliacionVerifactu servicio =
                mock(
                        ServicioReconciliacionVerifactu.class
                );

        ResultadoConsultaRegistroAeat esperado =
                ResultadoConsultaRegistroAeat.encontrado(
                        "CORRECTO",
                        null,
                        null
                );

        when(
                servicio.reconciliar(
                        1L,
                        25L
                )
        ).thenReturn(
                esperado
        );

        ReconciliacionVerifactuController controller =
                new ReconciliacionVerifactuController(
                        servicio
                );

        ResultadoConsultaRegistroAeat resultado =
                controller.reconciliar(
                        1L,
                        25L
                );

        assertSame(
                esperado,
                resultado
        );

        verify(
                servicio
        ).reconciliar(
                1L,
                25L
        );

        verifyNoMoreInteractions(
                servicio
        );
    }


    @Test
    void soloExisteEnPerfilAeatPreSinDev() {

        Profile profile =
                ReconciliacionVerifactuController.class
                        .getAnnotation(
                                Profile.class
                        );

        assertNotNull(
                profile
        );

        assertArrayEquals(
                new String[]{
                        "aeat-pre & !dev"
                },
                profile.value()
        );
    }


    @Test
    void exponeLaRutaEsperada() throws Exception {

        RequestMapping requestMapping =
                ReconciliacionVerifactuController.class
                        .getAnnotation(
                                RequestMapping.class
                        );

        assertNotNull(
                requestMapping
        );

        assertArrayEquals(
                new String[]{
                        "/api/empresas/{empresaId}/registros-facturacion"
                },
                requestMapping.value()
        );


        Method metodo =
                ReconciliacionVerifactuController.class
                        .getMethod(
                                "reconciliar",
                                Long.class,
                                Long.class
                        );

        PostMapping postMapping =
                metodo.getAnnotation(
                        PostMapping.class
                );

        assertNotNull(
                postMapping
        );

        assertArrayEquals(
                new String[]{
                        "/{registroId}/reconciliar"
                },
                postMapping.value()
        );
    }


    @Test
    void noSeRegistraConPerfilDefault() {

        try (
                AnnotationConfigApplicationContext context =
                        new AnnotationConfigApplicationContext()
        ) {

            context.getEnvironment()
                    .setActiveProfiles(
                            "default"
                    );

            context.register(
                    ReconciliacionVerifactuController.class
            );

            context.refresh();

            assertFalse(
                    context.containsBean(
                            "reconciliacionVerifactuController"
                    )
            );

            assertEquals(
                    0,
                    context.getBeansOfType(
                            ReconciliacionVerifactuController.class
                    ).size()
            );
        }
    }


    @Test
    void rechazaServicioNulo() {

        IllegalArgumentException excepcion =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                new ReconciliacionVerifactuController(
                                        null
                                )
                );

        assertTrue(
                excepcion.getMessage()
                        .contains(
                                "no puede ser nulo"
                        )
        );
    }
}