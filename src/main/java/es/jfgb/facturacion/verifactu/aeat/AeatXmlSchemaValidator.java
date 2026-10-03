package es.jfgb.facturacion.verifactu.aeat;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;

@Component
public class AeatXmlSchemaValidator {

    private static final String XSD_PRINCIPAL =
            "aeat/verifactu/wsdl/SuministroLR.xsd";

    private static final String XSD_XMLDSIG =
            "aeat/verifactu/wsdl/xmldsig-core-schema.xsd";

    private static final String NAMESPACE_XMLDSIG =
            "http://www.w3.org/2000/09/xmldsig#";

    private final Schema schema;

    public AeatXmlSchemaValidator() {

        this.schema =
                cargarSchema();
    }

    /**
     * Valida el XML contra SuministroLR.xsd
     * y todos sus esquemas relacionados.
     *
     * No realiza ninguna comunicación con AEAT
     * ni con otros servidores externos.
     */
    public void validar(
            String xml) {

        if (xml == null
                || xml.isBlank()) {

            throw new IllegalArgumentException(
                    "El XML VERI*FACTU "
                            + "no puede estar vacío"
            );
        }

        try {

            Validator validator =
                    schema.newValidator();

            /*
             * Bloqueamos cualquier DTD externo.
             */
            validator.setProperty(
                    XMLConstants.ACCESS_EXTERNAL_DTD,
                    ""
            );

            /*
             * No permitimos que la validación
             * descargue esquemas por HTTP/HTTPS.
             *
             * Los esquemas relacionados deben
             * resolverse localmente.
             */
            validator.setProperty(
                    XMLConstants.ACCESS_EXTERNAL_SCHEMA,
                    "file,jar"
            );

            validator.validate(
                    new StreamSource(
                            new StringReader(
                                    xml
                            )
                    )
            );

        } catch (SAXException e) {

            throw new IllegalStateException(
                    "El XML VERI*FACTU "
                            + "no cumple los XSD: "
                            + e.getMessage(),
                    e
            );

        } catch (IOException e) {

            throw new IllegalStateException(
                    "No se ha podido validar "
                            + "el XML VERI*FACTU",
                    e
            );
        }
    }

    private Schema cargarSchema() {

        try {

            ClassPathResource recurso =
                    new ClassPathResource(
                            XSD_PRINCIPAL
                    );

            URL url =
                    recurso.getURL();

            SchemaFactory factory =
                    SchemaFactory.newInstance(
                            XMLConstants
                                    .W3C_XML_SCHEMA_NS_URI
                    );

            /*
             * Seguridad:
             *
             * no se permite descargar DTD.
             */
            factory.setProperty(
                    XMLConstants.ACCESS_EXTERNAL_DTD,
                    ""
            );

            /*
             * Tampoco se permite cargar esquemas
             * remotos por HTTP/HTTPS.
             *
             * Sí permitimos file y jar porque los
             * XSD de AEAT están empaquetados
             * dentro de nuestra aplicación.
             */
            factory.setProperty(
                    XMLConstants.ACCESS_EXTERNAL_SCHEMA,
                    "file,jar"
            );

            /*
             * SuministroInformacion.xsd contiene
             * originalmente una referencia HTTP a:
             *
             * xmldsig-core-schema.xsd
             *
             * La interceptamos y la resolvemos
             * mediante nuestra copia local.
             */
            factory.setResourceResolver(
                    new AeatSchemaResourceResolver()
            );

            return factory.newSchema(
                    url
            );

        } catch (SAXException | IOException e) {

            throw new IllegalStateException(
                    "No se ha podido cargar "
                            + XSD_PRINCIPAL,
                    e
            );
        }
    }

    private static class AeatSchemaResourceResolver
            implements LSResourceResolver {

        @Override
        public LSInput resolveResource(
                String type,
                String namespaceURI,
                String publicId,
                String systemId,
                String baseURI) {

            boolean esXmlDsig =
                    NAMESPACE_XMLDSIG.equals(
                            namespaceURI
                    )
                            ||
                            (
                                    systemId != null
                                            &&
                                            systemId.endsWith(
                                                    "xmldsig-core-schema.xsd"
                                            )
                            );

            if (!esXmlDsig) {

                /*
                 * Dejamos que el parser resuelva
                 * normalmente los demás XSD locales.
                 */
                return null;
            }

            try {

                ClassPathResource recurso =
                        new ClassPathResource(
                                XSD_XMLDSIG
                        );

                return new AeatLsInput(
                        publicId,
                        recurso.getURL()
                                .toExternalForm(),
                        recurso.getInputStream()
                );

            } catch (IOException e) {

                throw new IllegalStateException(
                        "No se ha podido cargar "
                                + "el esquema XMLDSIG local",
                        e
                );
            }
        }
    }

    private static class AeatLsInput
            implements LSInput {

        private String publicId;
        private String systemId;
        private InputStream byteStream;
        private Reader characterStream;
        private String stringData;
        private String baseURI;
        private String encoding;
        private boolean certifiedText;

        AeatLsInput(
                String publicId,
                String systemId,
                InputStream byteStream) {

            this.publicId =
                    publicId;

            this.systemId =
                    systemId;

            this.byteStream =
                    byteStream;
        }

        @Override
        public Reader getCharacterStream() {
            return characterStream;
        }

        @Override
        public void setCharacterStream(
                Reader characterStream) {

            this.characterStream =
                    characterStream;
        }

        @Override
        public InputStream getByteStream() {
            return byteStream;
        }

        @Override
        public void setByteStream(
                InputStream byteStream) {

            this.byteStream =
                    byteStream;
        }

        @Override
        public String getStringData() {
            return stringData;
        }

        @Override
        public void setStringData(
                String stringData) {

            this.stringData =
                    stringData;
        }

        @Override
        public String getSystemId() {
            return systemId;
        }

        @Override
        public void setSystemId(
                String systemId) {

            this.systemId =
                    systemId;
        }

        @Override
        public String getPublicId() {
            return publicId;
        }

        @Override
        public void setPublicId(
                String publicId) {

            this.publicId =
                    publicId;
        }

        @Override
        public String getBaseURI() {
            return baseURI;
        }

        @Override
        public void setBaseURI(
                String baseURI) {

            this.baseURI =
                    baseURI;
        }

        @Override
        public String getEncoding() {
            return encoding;
        }

        @Override
        public void setEncoding(
                String encoding) {

            this.encoding =
                    encoding;
        }

        @Override
        public boolean getCertifiedText() {
            return certifiedText;
        }

        @Override
        public void setCertifiedText(
                boolean certifiedText) {

            this.certifiedText =
                    certifiedText;
        }
    }
}