package org.github.alexwibowo.xml.jaxb

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlRootElement
import javax.xml.bind.annotation.XmlType

/**
 * User: alexwibowo
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = [
    "documentId",
    "collectionName"
])
@XmlRootElement(name = "searchDocument")
class SearchDocument {

    String documentId
    String collectionName
}
