package org.github.alexwibowo.xml.soap;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMSourcedElement;
import org.apache.axiom.om.ds.jaxb.JAXBOMDataSource;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.http.annotation.NotThreadSafe;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

/**
 * User: alexwibowo
 */
@NotThreadSafe
public class AxiomSOAP11MessageBuilder implements SOAPMessageBuilder<SOAPHeader,SOAPEnvelope> {

    private Object payloadDocument;

    private final JAXBContext jaxbContext;

    private final SOAPEnvelope envelope;
    private final SOAPHeader soapHeader;

    private final SOAPFactory soap11Factory;

    private OMElement usernameToken;

    private List<OMElement> additionalHeaders;
    private List<OMElement> additionalSecurityHeaders;

    public AxiomSOAP11MessageBuilder(JAXBContext jaxbContext, SOAPFactory soap11Factory) {
        this.jaxbContext = jaxbContext;
        this.soap11Factory = soap11Factory;
        this.additionalHeaders = newArrayList();
        this.additionalSecurityHeaders = newArrayList();
        envelope = soap11Factory.getDefaultEnvelope();
        soapHeader = envelope.getHeader();
    }

    public AxiomSOAP11MessageBuilder withPayload(Object object) {
        checkNotNull(object);
        payloadDocument = object;
        return this;
    }

    public AxiomSOAP11MessageBuilder addHeader(JAXBElement header) {
        JAXBOMDataSource jaxbomDataSource = new JAXBOMDataSource(jaxbContext, header);
        OMSourcedElement omElement = soap11Factory.createOMElement(jaxbomDataSource);
        addHeader(omElement);
        return this;
    }

    public AxiomSOAP11MessageBuilder addHeader(OMElement omElement) {
        additionalHeaders.add(omElement);
        return this;
    }

    public AxiomSOAP11MessageBuilder withAdditionalSecurityHeader(OMElement omElement) {
        additionalSecurityHeaders.add(omElement);
        return this;
    }

    public AxiomSOAP11MessageBuilder withUsernameToken(String username, String sharedKey) throws SOAPException {
        this.usernameToken = new AxiomUsernameTokenElementBuilder(soap11Factory)
                .withUsername(username)
                .withSharedKey(sharedKey)
                .build();
        return this;
    }

    public SOAPEnvelope build() {
        org.apache.axiom.soap.SOAPBody body = envelope.getBody();
        createSecurityHeadersIfNecessary();
        for (OMElement additionalHeader : additionalHeaders) {
            soapHeader.addChild(additionalHeader);
        }

        XmlRootElement annotation = payloadDocument.getClass().getAnnotation(XmlRootElement.class);
        if (annotation != null) {
            JAXBOMDataSource jaxbomDataSource = new JAXBOMDataSource(jaxbContext, payloadDocument);
            OMSourcedElement omElement = soap11Factory.createOMElement(jaxbomDataSource);
            body.addChild(omElement);
        }else if (payloadDocument instanceof  OMElement) {
            OMElement omElementPayload = (OMElement) payloadDocument;
            body.addChild(omElementPayload);
        }
        return envelope;
    }

    private void createSecurityHeadersIfNecessary() {
        if (usernameToken != null || !additionalSecurityHeaders.isEmpty()) {
            OMElement securityElement = soap11Factory.createOMElement("Security", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd", "wsse");
            for (OMElement additionalSecurityHeader : additionalSecurityHeaders) {
                securityElement.addChild(additionalSecurityHeader);
            }
            if (usernameToken != null) {
                securityElement.addChild(usernameToken);
            }
            soapHeader.addChild(securityElement);
        }
    }

    public String getContentType() {
        return SOAPConstants.SOAP_1_1_CONTENT_TYPE;
    }
}