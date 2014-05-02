package org.github.alexwibowo.xml.soap;


import javax.xml.soap.SOAPException;

/**
 * User: alexwibowo
 */
public interface SOAPMessageBuilder<E,F> {

    /**
     * @return HTTP content-type of the SOAP message. It is either {@link javax.xml.soap.SOAPConstants#SOAP_1_2_CONTENT_TYPE}
     *          or {@link javax.xml.soap.SOAPConstants#SOAP_1_1_CONTENT_TYPE}, depending on which SOAP version is
     *          being built
     */
    String getContentType();

    <F> F build() ;

    SOAPMessageBuilder withPayload(Object object);

    SOAPMessageBuilder withUsernameToken(String username, String sharedKey) throws SOAPException;

}