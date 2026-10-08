package es.jfgb.facturacion.verifactu.aeat;

import es.jfgb.facturacion.verifactu.RegistroFacturacion;
import es.jfgb.facturacion.verifactu.RegistroFacturacionRepository;
import es.jfgb.facturacion.verifactu.TipoRegistroFacturacion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AeatVistaPreviaServiceTest {

    @Mock
    private RegistroFacturacionRepository registroRepository;

    @Mock
    private AeatSolicitudAltaXmlBuilder altaXmlBuilder;

    @Mock
    private AeatSolicitudAnulacionXmlBuilder anulacionXmlBuilder;

    @Mock
    private RegistroFacturacion registro;

    private AeatVistaPreviaService service;

    @BeforeEach
    void setUp() {

        service =
                new AeatVistaPreviaService(
                        registroRepository,
                        altaXmlBuilder,
                        anulacionXmlBuilder
                );
    }

    @Test
    void generaXmlDeAltaConBuilderDeAlta() {

        when(
                registroRepository.findByIdAndEmpresa_Id(
                        10L,
                        1L
                )
        ).thenReturn(
                Optional.of(
                        registro
                )
        );

        when(
                registro.getTipoRegistro()
        ).thenReturn(
                TipoRegistroFacturacion.ALTA
        );

        when(
                altaXmlBuilder.generarXml(
                        registro
                )
        ).thenReturn(
                "<RegistroAlta/>"
        );

        String xml =
                service.generarXml(
                        1L,
                        10L
                );

        assertEquals(
                "<RegistroAlta/>",
                xml
        );

        verify(
                altaXmlBuilder
        ).generarXml(
                registro
        );

        verify(
                anulacionXmlBuilder,
                never()
        ).generarXml(
                registro
        );
    }

    @Test
    void generaXmlDeAnulacionConBuilderDeAnulacion() {

        when(
                registroRepository.findByIdAndEmpresa_Id(
                        37L,
                        1L
                )
        ).thenReturn(
                Optional.of(
                        registro
                )
        );

        when(
                registro.getTipoRegistro()
        ).thenReturn(
                TipoRegistroFacturacion.ANULACION
        );

        when(
                anulacionXmlBuilder.generarXml(
                        registro
                )
        ).thenReturn(
                "<RegistroAnulacion/>"
        );

        String xml =
                service.generarXml(
                        1L,
                        37L
                );

        assertEquals(
                "<RegistroAnulacion/>",
                xml
        );

        verify(
                anulacionXmlBuilder
        ).generarXml(
                registro
        );

        verify(
                altaXmlBuilder,
                never()
        ).generarXml(
                registro
        );
    }
}
