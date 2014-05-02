package org.github.alexwibowo.xml;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * A SAX filter to inject namespace to a RAW XML document. Adopted from http://stackoverflow.com/questions/277502/jaxb-how-to-ignore-namespace-during-unmarshalling-xml-document
 * <p/>
 * To add namespace:
 * <code>
 * new NamespaceFilter("http://www.example.com/namespaceurl", true);
 * </code>
 * To remove namespace:
 * <code>
 * new NamespaceFilter(null, false);
 * </code>
 * User: alexwibowo
 */
public class NamespaceFilter extends XMLFilterImpl {

    private String usedNamespaceUri;

    //State variable
    private boolean addedNamespace = false;

    public NamespaceFilter(String namespaceUri) {
        super();
        this.usedNamespaceUri = namespaceUri;
    }

    /**
     * Filter a start document event.
     *
     * @throws org.xml.sax.SAXException The client may throw
     *                                  an exception during processing.
     */
    public void startDocument() throws SAXException {
        super.startDocument();
        startControlledPrefixMapping();
    }

    /**
     * Filter a start element event.
     *
     * @param uri       The element's Namespace URI, or the empty string.
     * @param localName The element's local name, or the empty string.
     * @param qName     The element's qualified (prefixed) name, or the empty
     *                  string.
     * @param attrs     The element's attributes.
     * @throws org.xml.sax.SAXException The client may throw
     *                                  an exception during processing.
     */
    public void startElement(String uri, String localName, String qName, Attributes attrs)
            throws SAXException {
        super.startElement(this.usedNamespaceUri, localName, qName, attrs);
    }

    /**
     * Filter an end element event.
     *
     * @param uri       The element's Namespace URI, or the empty string.
     * @param localName The element's local name, or the empty string.
     * @param qName     The element's qualified (prefixed) name, or the empty
     *                  string.
     * @throws org.xml.sax.SAXException The client may throw
     *                                  an exception during processing.
     */
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        super.endElement(this.usedNamespaceUri, localName, qName);
    }

    /**
     * Filter a start Namespace prefix mapping event.
     *
     * @param prefix The Namespace prefix.
     * @param uri    The Namespace URI.
     * @throws org.xml.sax.SAXException The client may throw
     *                                  an exception during processing.
     */
    public void startPrefixMapping(String prefix, String uri)
            throws SAXException {
        this.startControlledPrefixMapping();
    }

    private void startControlledPrefixMapping() throws SAXException {
        if (!this.addedNamespace) {
            //We should add namespace since it is set and has not yet been done.
            super.startPrefixMapping("", this.usedNamespaceUri);
            //Make sure we dont do it twice
            this.addedNamespace = true;
        }
    }

}