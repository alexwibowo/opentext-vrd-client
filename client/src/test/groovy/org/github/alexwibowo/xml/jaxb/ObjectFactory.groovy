//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.04.09 at 07:57:54 AM EST 
//


package org.github.alexwibowo.xml.jaxb

import javax.xml.bind.JAXBElement
import javax.xml.bind.annotation.XmlElementDecl
import javax.xml.bind.annotation.XmlRegistry
import javax.xml.namespace.QName

@XmlRegistry
public class ObjectFactory {

    private final static QName _SearchDocument_QNAME = new QName("http://github.com/alexwibowo/opentextvrdclient/test", "SearchDocument");
    private final static QName _EnterpriseHeader_QNAME = new QName("http://github.com/alexwibowo/opentextvrdclient/test", "MyHeader");

    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link SearchDocument }
     *
     */
    public SearchDocument createSearchDocument() {
        return new SearchDocument();
    }

    public MyHeader createMyHeader() {
        return new MyHeader();
    }

    @XmlElementDecl(namespace = "http://github.com/alexwibowo/opentextvrdclient/test", name = "SearchDocument")
    public JAXBElement<SearchDocument> createSearchDocument(SearchDocument value) {
        return new JAXBElement<SearchDocument>(_SearchDocument_QNAME, SearchDocument.class, null, value);
    }

    /**
     * Create an instance of {@link javax.xml.bind.JAXBElement }{@code <}{@link MyHeader }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://github.com/alexwibowo/opentextvrdclient/test", name = "myHeader")
    public JAXBElement<MyHeader> createMyHeader(MyHeader value) {
        return new JAXBElement<MyHeader>(_EnterpriseHeader_QNAME, MyHeader.class, null, value);
    }
}