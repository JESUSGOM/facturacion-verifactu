package es.jfgb.facturacion.verifactu.aeat.config;

import es.jfgb.facturacion.verifactu.provider.VerifactuProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("aeat-pre")
class AeatPreproduccionContextIT {

    @Autowired
    private VerifactuProvider verifactuProvider;

    @Test
    @EnabledOnOs(OS.WINDOWS)
    @EnabledIfEnvironmentVariable(
            named = "VERIFACTU_AEAT_CERT_SHA256",
            matches = ".+"
    )
    void arrancaSpringConProviderAeatPreproduccion() {

        assertNotNull(
                verifactuProvider
        );

        assertEquals(
                "AEAT-VERIFACTU-PRUEBAS",
                verifactuProvider.getNombre()
        );

        System.out.println(
                "SPRING_AEAT_PRE_OK=true"
        );

        System.out.println(
                "SPRING_AEAT_PRE_PROVIDER="
                        + verifactuProvider.getNombre()
        );
    }
}