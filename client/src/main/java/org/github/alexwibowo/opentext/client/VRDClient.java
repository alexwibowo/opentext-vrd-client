package org.github.alexwibowo.opentext.client;

import com.vignette._2014._04._14.vignettequery.ConditionType;
import org.github.alexwibowo.opentext.domain.DocumentMetadata;
import org.github.alexwibowo.opentext.domain.VRDDocumentVersion;

import java.io.File;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * User: alexwibowo
 */
public interface VRDClient {

    void getDocument(String recordID, String section, String renditionType, VRDDocumentVersion version, OutputStream outputStream)
            throws Exception;

    String storeDocument(String documentSource, Map<String, Object> sourceAttributes, String mimeType, File file)
            throws Exception;

    List<DocumentMetadata> getMetadata(List<String> recordAttributesToBeRetrieved, List<String> attributesToBeRetrieved, List<ConditionType> conditions, Long maximumRecordsToRetrieve)
            throws Exception;
}
