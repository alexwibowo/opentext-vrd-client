package org.github.alexwibowo.opentext;

import com.vignette._2014._04._14.vignettequery.DataSource;
import org.github.alexwibowo.xml.NamespaceFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.sax.SAXSource;
import java.io.StringReader;
import java.io.StringWriter;

import static com.google.common.base.Preconditions.checkArgument;
import static org.apache.commons.lang.StringUtils.isNotBlank;

/**
 * User: alexwibowo
 */
public class VRDXmlUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(VRDXmlUtil.class.getName());

    private JAXBContext jaxbContext;

    public VRDXmlUtil(JAXBContext jaxbContext) {
        this.jaxbContext = jaxbContext;
    }

    public String createRequest(DataSource ds)
            throws JAXBException {
        StringWriter writer = new StringWriter();

        Marshaller marshaller = jaxbContext.createMarshaller();
        if ("true".equals(System.getProperty(Marshaller.JAXB_FORMATTED_OUTPUT))) {
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        }
        /*try {
            marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", new DefaultToVrdNamespacePrefixMapper());
        } catch(PropertyException e) {
            // In case another JAXB implementation is used
        }*/

        marshaller.marshal(ds, writer);
        String vrdRequestXML = writer.toString();
        LOGGER.trace("About to perform the following request", vrdRequestXML);
        return vrdRequestXML;
    }

    public DataSource parseResponse(String vrdResponseXML)
            throws SAXException, JAXBException {
        try {
            checkArgument(isNotBlank(vrdResponseXML), "Response must not be blank");
            XMLReader reader = XMLReaderFactory.createXMLReader();

            //Create the filter (to add namespace) and set the xmlReader as its parent.
            NamespaceFilter inFilter = new NamespaceFilter("http://www.vignette.com/2011/09/13/VignetteQuery.xsd");
            inFilter.setParent(reader);

            //Prepare the input, in this case a java.io.File (output)
            InputSource is = new InputSource(new StringReader(vrdResponseXML));

            //Create a SAXSource specifying the filter
            SAXSource source = new SAXSource(inFilter, is);

            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            return (DataSource) unmarshaller.unmarshal(source);
        } catch (SAXException|JAXBException e) {
            LOGGER.error(String.format("Failed to parse response from VRD. Response is :\n%s", vrdResponseXML), e);
            throw e;
        }
    }
}