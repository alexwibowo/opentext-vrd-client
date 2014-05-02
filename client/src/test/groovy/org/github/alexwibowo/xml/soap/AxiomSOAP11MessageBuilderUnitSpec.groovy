package org.github.alexwibowo.xml.soap

import org.apache.axiom.om.OMAbstractFactory
import org.apache.axiom.soap.SOAPEnvelope
import org.github.alexwibowo.xml.jaxb.MyHeader
import org.github.alexwibowo.xml.jaxb.ObjectFactory
import org.github.alexwibowo.xml.jaxb.SearchDocument
import spock.lang.Shared
import spock.lang.Specification

import javax.xml.bind.JAXBContext
import javax.xml.soap.SOAPConstants

/**
 * User: alexwibowo
 */
class AxiomSOAP11MessageBuilderUnitSpec extends Specification{

    @Shared
    def jaxbContext = JAXBContext.newInstance("org.github.alexwibowo.xml.jaxb")

    private AxiomSOAP11MessageBuilder builder

    def setup() {
        builder = new AxiomSOAP11MessageBuilder(jaxbContext, OMAbstractFactory.getSOAP11Factory())
    }

    def "should be able to construct SOAP 11 message without security header"(){
        given:
        def document = new SearchDocument(documentId: "12345", collectionName: "MY_COLLECTION")

        when:
        def builder = this.builder.withPayload(document)

        then: "should have the correct content type"
        assert builder.getContentType() == SOAPConstants.SOAP_1_1_CONTENT_TYPE


        and: "should create SOAP 11 envelope"
        SOAPEnvelope soapEnvelope = builder.build()
        def xmlString = soapEnvelope.toString()
        def parsed = new XmlSlurper()
                .parseText(xmlString)
                .declareNamespace(
                "soap12": "http://www.w3.org/2003/05/soap-envelope",
                "soap11": "http://schemas.xmlsoap.org/soap/envelope/",
                "wsse": "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd"
        )
        assert parsed != null
        assert parsed."soap12:Body".size() == 0

        and: "should not have header"
        assert parsed."soap11:Header".size() == 0
        assert parsed."soap12:Header".size() == 0

        and: "should have the correct body"
        assert parsed."soap11:Body" != null
        assert parsed."soap11:Body".searchDocument != null
        assert parsed."soap11:Body".searchDocument.documentId == "12345"
        assert parsed."soap11:Body".searchDocument.collectionName == "MY_COLLECTION"
    }

    def "should be able to add additional header to the soap envelope"() {
        given:
        def document = new SearchDocument(documentId: "12345", collectionName: "MY_COLLECTION")

        def header = new ObjectFactory().createMyHeader(new MyHeader(system: "Artificial"))
        SOAPEnvelope soapEnvelope = builder.withPayload(document)
                .withUsernameToken("alexwibowo", "sharedKey")
                .addHeader(header)
                .build()
        def xmlString = soapEnvelope.toString()

        when: "should create SOAP 11 envelope"
        def parsed = new XmlSlurper()
                .parseText(xmlString)
                .declareNamespace(
                "soap12": "http://www.w3.org/2003/05/soap-envelope",
                "soap11": "http://schemas.xmlsoap.org/soap/envelope/",
                "wsse": "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd",
                "art": "http://github.com/alexwibowo/opentextvrdclient/test"
        )

        then:
        assert parsed."soap11:Header".size() == 1

        and: "should have the additional header"
        assert parsed."soap11:Header"."art:MyHeader".system == "Artificial"

    }

    def "should be able to construct SOAP 11 message with security header"() {
        given:
        def document = new SearchDocument(documentId: "12345", collectionName: "MY_COLLECTION")

        when:
        SOAPEnvelope soapEnvelope = builder.withPayload(document)
                .withUsernameToken("alexwibowo", "sharedKey")
                .build()
        def xmlString = soapEnvelope.toString()

        then: "should create SOAP 11 envelope"
        def parsed = new XmlSlurper()
                .parseText(xmlString)
                .declareNamespace(
                "soap12": "http://www.w3.org/2003/05/soap-envelope",
                "soap11": "http://schemas.xmlsoap.org/soap/envelope/",
                "wsse": "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd"
        )
        assert parsed."soap11:Header".size() == 1
        assert parsed."soap11:Header".size() == 1
        assert parsed."soap12:Header".size() == 0
        assert parsed."soap12:Body".size() == 0

        and: "should have security header"
        assert parsed."soap11:Header"?."wsse:Security"?."wsse:UsernameToken"?."wsse:Username" == "alexwibowo"
        assert parsed."soap11:Header"?."wsse:Security"?."wsse:UsernameToken"?."wsse:Created" != null
        assert parsed."soap11:Header"?."wsse:Security"?."wsse:UsernameToken"?."wsse:Nonce" != null
        assert parsed."soap11:Header"?."wsse:Security"?."wsse:UsernameToken"?."wsse:Password" != null
        assert parsed."soap11:Header"?."wsse:Security"?."wsse:UsernameToken"?."wsse:Password".@"wsse:Type".text() == "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordDigest"
        assert parsed."soap11:Header"?."wsse:Security"?."wsse:UsernameToken"?."wsse:Password".@"wsse:EncodingType".text() == "SHA-1"

        and: "should have the correct body"
        assert parsed."soap11:Body" != null
        assert parsed."soap11:Body".searchDocument != null
        assert parsed."soap11:Body".searchDocument.documentId == "12345"
        assert parsed."soap11:Body".searchDocument.collectionName == "MY_COLLECTION"

    }
}
