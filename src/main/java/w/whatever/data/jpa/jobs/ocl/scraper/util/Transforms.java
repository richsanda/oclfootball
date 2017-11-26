package w.whatever.data.jpa.jobs.ocl.scraper.util;

import net.sf.saxon.Configuration;
import net.sf.saxon.TransformerFactoryImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

/**
 * Created by rich on 11/26/17.
 */
public class Transforms {

    private static final Class<?> thisClass = Transforms.class;

    private static final String SAXON_TRANSFORMER_FACTORY_NAME = "net.sf.saxon.TransformerFactoryImpl";

    private static InputStream getTransformAsStream(String resource) {
        return thisClass.getResourceAsStream(resource);
    }

    public static void transform(String input, Result output, String transformLocation) {
        transform(input, output, transformLocation, null);
    }

    public static void transform(Source input, Result output, String transformLocation) {
        transform(input, output, transformLocation, null);
    }

    public static void transform(String input, Result output, String transformLocation, Map<String, String> params) {
        transform(new StreamSource(new StringReader(input)), output, transformLocation, params);
    }

    public static void transform(Source input, Result output, String transformLocation, Map<String, String> params) {

        InputStream transformInputStream = getTransformAsStream(transformLocation);

        StreamSource transformSource = new StreamSource(transformInputStream);

        try {
            // pre-compile the transformer as a "template"
            TransformerFactory factory = TransformerFactory.newInstance(SAXON_TRANSFORMER_FACTORY_NAME, null);

            Configuration config = Configuration.newConfiguration();
            // config.registerExtensionFunction(new RandomDefinition());
            ((TransformerFactoryImpl)factory).setConfiguration(config);

            factory.setURIResolver(new ClasspathResourceURIResolver());
            Templates templates = factory.newTemplates(transformSource);

            Transformer transformer = templates.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

            // add params to the transformer
            if (null != params) {
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    transformer.setParameter(entry.getKey(), entry.getValue());
                }
            }

            // perform transformation
            transformer.transform(input, output);

        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }

    private static class ClasspathResourceURIResolver implements URIResolver {
        @Override
        public Source resolve(String href, String base) throws TransformerException {
            return new StreamSource(getTransformAsStream(href));
        }
    }

    private static Document document(String input) {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();
            Element root = doc.createElement("whatever");
            doc.appendChild(root);

            return doc;

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void transform(String transformFile, String inFile, String outFile) {

        try {

            File stylesheet = new File(transformFile);
            File datafile = new File(inFile);

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(datafile);
            // ...

            InputStream transformInputStream = getTransformAsStream(transformFile);
            StreamSource stylesource = new StreamSource(transformInputStream);

            // StreamSource stylesource = new StreamSource(stylesheet);
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer(stylesource);

            transformer.transform(new StreamSource(datafile), new StreamResult(outFile));

        } catch (Exception e) {

            e.printStackTrace();
        }
    }
}
