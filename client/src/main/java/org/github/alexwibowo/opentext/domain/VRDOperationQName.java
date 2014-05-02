package org.github.alexwibowo.opentext.domain;

/**
 * User: alexwibowo
 */
public enum VRDOperationQName {
    AddVersion("http://record.webservices.rd.vignette.com/addVersion"),
    AddRendition("http://record.webservices.rd.vignette.com/addRendition"),
    AddMultiSectionRecordMapped("http://record.webservices.rd.vignette.com/addMultiSectionRecordMapped"),
    AddRecordMapped("http://record.webservices.rd.vignette.com/addRecordMapped"),
    AddCaseMapped("http://record.webservices.rd.vignette.com/addCaseMapped"),
    IsUserInGroup("http://record.webservices.rd.vignette.com/isUserInGroup"),
    IsValid("http://record.webservices.rd.vignette.com/isValid"),
    Login("http://record.webservices.rd.vignette.com/login"),
    Logout("http://record.webservices.rd.vignette.com/logout"),
    Execute("http://record.webservices.rd.vignette.com/execute"),
    GetUITemplate("http://record.webservices.rd.vignette.com/getUITemplate"),
    GetRendition("http://record.webservices.rd.vignette.com/getRendition"),
    TriggerRecordContainerDisposition("http://record.webservices.rd.vignette.com/triggerRecordContainerDisposition"),
    TriggerContainerDisposition("http://record.webservices.rd.vignette.com/triggerContainerDisposition"),
    TriggerRecordDisposition("http://record.webservices.rd.vignette.com/triggerRecordDisposition");

    private final String qName;

    VRDOperationQName(String qName) {
        this.qName = qName;
    }

    public String getQName() {
        return qName;
    }
}
