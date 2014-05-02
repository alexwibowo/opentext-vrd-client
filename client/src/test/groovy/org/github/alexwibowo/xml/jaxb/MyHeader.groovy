package org.github.alexwibowo.xml.jaxb

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlType

/**
 * User: s74627
 * Date: 9/04/14
 * Time: 12:56 PM
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MyHeader", propOrder = [
    "system"
])
class MyHeader {
    String system
}
