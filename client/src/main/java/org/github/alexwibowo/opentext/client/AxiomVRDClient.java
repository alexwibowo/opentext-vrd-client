package org.github.alexwibowo.opentext.client;

import com.google.common.base.Function;
import com.vignette._2014._04._14.vignettequery.*;
import com.vignette.rd.webservices.record.Execute;
import org.apache.axiom.attachments.Attachments;
import org.apache.axiom.om.*;
import org.apache.axiom.om.impl.MTOMXMLStreamWriter;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.util.UIDGenerator;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.github.alexwibowo.activation.MultipartHttpResponseDataSource;
import org.github.alexwibowo.opentext.domain.DocumentMetadata;
import org.github.alexwibowo.opentext.domain.VRDOperationQName;
import org.github.alexwibowo.opentext.VRDXmlUtil;
import org.github.alexwibowo.opentext.client.cfg.VRDConfiguration;
import org.github.alexwibowo.opentext.domain.VRDDocumentVersion;
import org.github.alexwibowo.xml.soap.AxiomSOAP11MessageBuilder;
import org.github.alexwibowo.xml.soap.SOAPMessageBuilder;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.internet.WibMimeMultipart;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import java.io.*;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.transform;
import static org.apache.commons.io.IOUtils.copy;
import static org.github.alexwibowo.opentext.domain.VRDOperationQName.GetRendition;

/**
 * User: alexwibowo
 */
public class AxiomVRDClient implements VRDClient {

    private final JAXBContext jaxbContext;

    private final VRDConfiguration configuration;

    private static final OMFactory factory;

    static {
        factory = OMAbstractFactory.getOMFactory();
    }

    private final SOAPFactory soap11Factory;


    public AxiomVRDClient(JAXBContext jaxbContext, VRDConfiguration configuration, SOAPFactory soap11Factory) {
        this.jaxbContext = jaxbContext;
        this.configuration = configuration;
        this.soap11Factory = soap11Factory;
    }

    @Override
    public void getDocument(String recordID, String section, String renditionType, VRDDocumentVersion version, OutputStream outputStream)
            throws Exception {
        // create vrd namespace without prefix. This is important, as crazy VRD doesnt like prefix.
        OMNamespace vrdNamespace = factory.createOMNamespace("http://record.webservices.rd.vignette.com/", "");

        OMElement getRenditionElement = factory.createOMElement("getRendition", vrdNamespace);

        OMElement recordIdElement = factory.createOMElement("recordID", vrdNamespace);
        recordIdElement.setText(recordID);
        getRenditionElement.addChild(recordIdElement);

        OMElement sectionElement = factory.createOMElement("section", vrdNamespace);
        sectionElement.setText(section);
        getRenditionElement.addChild(sectionElement);

        OMElement subSectionElement = factory.createOMElement("subSection", vrdNamespace);
        subSectionElement.setText("0");
        getRenditionElement.addChild(subSectionElement);

        OMElement renditionTypeElement = factory.createOMElement("renditionType", vrdNamespace);
        renditionTypeElement.setText(StringUtils.isBlank(renditionType) ? "ORIGINAL" : renditionType);
        getRenditionElement.addChild(renditionTypeElement);

        OMElement versionElement = factory.createOMElement("version", vrdNamespace);
        versionElement.setText(version.name());
        getRenditionElement.addChild(versionElement);


        SOAPMessageBuilder soap11MessageBuilder = new AxiomSOAP11MessageBuilder(jaxbContext, soap11Factory)
                .withPayload(getRenditionElement)
                .withUsernameToken(
                        configuration.getUsername(),
                        configuration.getPassword()
                );

        SOAPEnvelope envelope = (SOAPEnvelope) soap11MessageBuilder.build();
        HttpResponse httpResponse = postAsMultipart(envelope, GetRendition.getQName());

        if (httpResponse.getStatusLine() != null && httpResponse.getStatusLine().getStatusCode() == 200) {
            if (httpResponse.getEntity() != null && httpResponse.getEntity().getContent() != null) {
                String contentType = httpResponse.getFirstHeader("Content-Type").getValue();
                InputStream responseAsStream = httpResponse.getEntity().getContent();

                MultipartHttpResponseDataSource dataSource = new MultipartHttpResponseDataSource(responseAsStream, contentType);
                WibMimeMultipart mp = new WibMimeMultipart(dataSource);
                BodyPart bodyPart = mp.getBodyPart(1);

                ByteArrayInputStream content = (ByteArrayInputStream) bodyPart.getContent();
                copy(content, outputStream);
            } else {
                throw new GeneralVRDException("No valid response for getting document from VRD.", null);     // need a better structure here..
            }
        } else {
            InputStream in = httpResponse.getEntity().getContent();
            SOAPEnvelope response = OMXMLBuilderFactory.createSOAPModelBuilder(in, "UTF-8").getSOAPEnvelope();
            OMElement fault = response.getBody().getFirstChildWithName(new QName("http://schemas.xmlsoap.org/soap/envelope/", "Fault"));
            OMElement detail = fault.getFirstChildWithName(new QName("detail")).getFirstElement();
            String vrdResponseXML = detail.toString();
            throw new GeneralVRDException(vrdResponseXML, null);     // need a better structure here..
        }
    }

    @Override
    public String storeDocument(String documentSource, Map<String, Object> sourceAttributes, String mimeType, File file) throws Exception {
        OMNamespace ns1 = factory.createOMNamespace("http://record.webservices.rd.vignette.com/", "");

        OMElement addRecordMappedElement = factory.createOMElement("addRecordMapped", ns1);

        OMElement licenseElement = factory.createOMElement("license", ns1);
        addRecordMappedElement.addChild(licenseElement);

        OMElement docSourceNameElement = factory.createOMElement("docSourceName", ns1);
        docSourceNameElement.setText(documentSource);
        addRecordMappedElement.addChild(docSourceNameElement);

        OMElement sourceVREFElement = factory.createOMElement("sourceVREF", ns1);
        sourceVREFElement.setText("");
        addRecordMappedElement.addChild(sourceVREFElement);

        for (Map.Entry<String, Object> attributeEntry : sourceAttributes.entrySet()) {
            addRecordMappedElement.addChild(createAttributeElement(factory, ns1, attributeEntry));
        }

        OMElement contentElement = factory.createOMElement("content", ns1);
        OMElement sectionIDElement = factory.createOMElement("sectionID", ns1);
        sectionIDElement.setText("1");
        contentElement.addChild(sectionIDElement);
        OMElement sectionDataElement = factory.createOMElement("sectionData", ns1);
        DataHandler dataHandler = new DataHandler(new FileDataSource(file));
        sectionDataElement.addChild(factory.createOMText(dataHandler, true));
        contentElement.addChild(sectionDataElement);
        OMElement renditionTypeElement = factory.createOMElement("renditionType", ns1);
        renditionTypeElement.setText(mimeType);
        contentElement.addChild(renditionTypeElement);

        addRecordMappedElement.addChild(contentElement);
        addRecordMappedElement.addChild(getOptionElement(factory, ns1));

        SOAPMessageBuilder soap11MessageBuilder = new AxiomSOAP11MessageBuilder(jaxbContext, soap11Factory)
                .withPayload(addRecordMappedElement)
                .withUsernameToken(
                        configuration.getUsername(),
                        configuration.getPassword()
                );

        SOAPEnvelope envelope = (SOAPEnvelope) soap11MessageBuilder.build();
        HttpResponse httpResponse = postAsMultipart(envelope, VRDOperationQName.AddRecordMapped.getQName());

        dataHandler.getInputStream().close();

        if (httpResponse.getEntity() != null && httpResponse.getEntity().getContent() != null) {
            InputStream in = httpResponse.getEntity().getContent();
            Attachments attachments = new Attachments(in, httpResponse.getEntity().getContentType().getValue());
            SOAPEnvelope response = OMXMLBuilderFactory.createSOAPModelBuilder(attachments).getSOAPEnvelope();

            // retrieveContentResponse is addRecordMappedResponse
            OMElement addRecordMappedResponseElement = response.getBody().getFirstElement();

            // Unfortunately, our beautiful VRD responds with 'recDesc' in a null namespace. Hence if we use JAXB to unmarshall the response, we will get
            // null recDesc inside AddRecordMappedResponse object. such is life..
            OMElement recordDescElement = addRecordMappedResponseElement.getFirstElement();

            String recordID = recordDescElement.getFirstChildWithName(new QName("recordID")).getText();
            IOUtils.closeQuietly(in);
            return recordID;
        }
        return null;
    }

    @Override
    public List<DocumentMetadata> getMetadata(List<String> recordAttributesToBeRetrieved, List<String> attributesToBeRetrieved, List<ConditionType> conditions, Long maximumRecordsToRetrieve) throws Exception {
        DataSource dataSource = new DataSource();
        QueryType queryType = new QueryType();
        queryType.setAction(ActionType.SELECT);
        queryType.setSource("#DEFAULT_FILEPLAN#");
        if (!recordAttributesToBeRetrieved.isEmpty() || !attributesToBeRetrieved.isEmpty()) {
            queryType.setAttrs(new QueryType.Attrs());
            for (String recordAttribute : recordAttributesToBeRetrieved) {
                queryType.getAttrs().getRecordAttr().add(recordAttribute);
            }
            for (String attribute : attributesToBeRetrieved) {
                queryType.getAttrs().getAttr().add(attribute);
            }
        }
        if (!conditions.isEmpty()) {
            queryType.setConditions(new QueryType.Conditions());
            for (ConditionType condition : conditions) {
                queryType.getConditions().getCondition().add(condition);
            }
        }
        dataSource.getQuery().add(queryType);

        StringWriter sw = new StringWriter();
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.marshal(dataSource, sw);

        Execute execute = new Execute();
        execute.setLicense("");
        execute.setXmlRequest(sw.toString());

        SOAPMessageBuilder soap11MessageBuilder = new AxiomSOAP11MessageBuilder(jaxbContext, soap11Factory)
                .withPayload(execute)
                .withUsernameToken(
                        configuration.getUsername(),
                        configuration.getPassword()
                );

        SOAPEnvelope envelope = (SOAPEnvelope) soap11MessageBuilder.build();
        HttpResponse httpResponse = simplePost(envelope, VRDOperationQName.Execute.getQName());

        if (httpResponse.getEntity() != null && httpResponse.getEntity().getContent() != null) {
            InputStream in = httpResponse.getEntity().getContent();
            SOAPEnvelope response = OMXMLBuilderFactory.createSOAPModelBuilder(in, "UTF-8").getSOAPEnvelope();
            OMElement retrieveContentResponse = response.getBody().getFirstElement();
            OMElement content = retrieveContentResponse.getFirstElement();
            DataSource dataSourceResult = new VRDXmlUtil(jaxbContext).parseResponse(content.getText());
            return convertToVRDMetadata(dataSourceResult.getQueryResult());
        }

        return null;
    }

    private List<DocumentMetadata> convertToVRDMetadata(List<QueryResultType> searchResults) {
        QueryResultType queryResultType = searchResults.get(0);
        List<DocumentMetadata> result = transform(queryResultType.getDataItem(), new Function<DataItemType, DocumentMetadata>() {
            public DocumentMetadata apply(DataItemType vrdRecord) {
                return new DocumentMetadata(vrdRecord.getValue());
            }
        });
        return result;
    }


    private OMNode createAttributeElement(OMFactory factory, OMNamespace ns1, Map.Entry<String, Object> attributeEntry) {
        OMElement attributesElement = factory.createOMElement("attributes", ns1);

        if (attributeEntry.getValue() instanceof Iterable) {
            Iterable iterableValues = (Iterable) attributeEntry.getValue();
            for (Object iterableValue : iterableValues) {
                OMElement nameElement = factory.createOMElement("name", ns1);
                nameElement.setText(attributeEntry.getKey());
                OMElement valueElement = factory.createOMElement("value", ns1);
                valueElement.setText((String) iterableValue);
                attributesElement.addChild(nameElement);
                attributesElement.addChild(valueElement);
            }
        } else {
            OMElement nameElement = factory.createOMElement("name", ns1);
            nameElement.setText(attributeEntry.getKey());
            OMElement valueElement = factory.createOMElement("value", ns1);
            valueElement.setText((String) attributeEntry.getValue());
            attributesElement.addChild(nameElement);
            attributesElement.addChild(valueElement);
        }
        return attributesElement;
    }

    private OMElement getOptionElement(OMFactory factory, OMNamespace ns1) {
        OMElement optionElement = factory.createOMElement("option", ns1);

        OMElement nameElement = factory.createOMElement("name", ns1);
        nameElement.setText("finalize");
        OMElement valueElement = factory.createOMElement("value", ns1);
        valueElement.setText("false");

        optionElement.addChild(nameElement);
        optionElement.addChild(valueElement);
        return optionElement;

    }


    private HttpResponse postAsMultipart(SOAPEnvelope envelope, String soapAction) throws Exception {
        DefaultHttpClient client = new DefaultHttpClient();

        HttpPost request = new HttpPost(configuration.getEndpoint());
        request.addHeader("SOAPAction", soapAction);
        request.addHeader("Pragma", "no-cache");
        request.addHeader("Cache-Control", "no-cache");
        request.addHeader("Accept", "application/soap+xml, application/dime, multipart/related, text/*");

        String boundary = UIDGenerator.generateMimeBoundary();
        String rootContentId = "0." + UIDGenerator.generateContentId();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OMOutputFormat format = new OMOutputFormat();
        format.setCharSetEncoding("UTF-8");
        format.setMimeBoundary(boundary);
        format.setRootContentId(rootContentId);
        format.setDoOptimize(true);
        String contentTypeForMTOM = format.getContentTypeForMTOM("text/xml");
        request.addHeader("Content-Type", contentTypeForMTOM);

        MTOMXMLStreamWriter mtomxmlStreamWriter = new MTOMXMLStreamWriter(baos, format);
        mtomxmlStreamWriter.setDoOptimize(true);
        envelope.serialize(mtomxmlStreamWriter);

        request.setEntity(new ByteArrayEntity(baos.toByteArray()));
        HttpResponse response = client.execute(request);
        return response;
    }

    private HttpResponse simplePost(SOAPEnvelope envelope, String soapAction) throws Exception {
        DefaultHttpClient client = new DefaultHttpClient();

        HttpPost request = new HttpPost(configuration.getEndpoint());
        request.addHeader("SOAPAction", soapAction);
        request.addHeader("Pragma", "no-cache");
        request.addHeader("Cache-Control", "no-cache");
        request.addHeader("Accept", "application/soap+xml");

        StringWriter sw = new StringWriter();
        envelope.serialize(sw);
        String string = sw.toString();
        request.setEntity(new StringEntity(string));
        HttpResponse response = client.execute(request);
        return response;
    }


}
