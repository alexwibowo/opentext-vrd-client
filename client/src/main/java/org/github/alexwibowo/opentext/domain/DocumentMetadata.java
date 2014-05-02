package org.github.alexwibowo.opentext.domain;

import com.vignette._2014._04._14.vignettequery.ResultValueType;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.util.*;

/**
 * An encapsulation of VRD document metadata
 * <p/>
 * User: alexwibowo
 */
public class DocumentMetadata implements Serializable {

    private Map<String, Object> metadata;

    public DocumentMetadata() {
        metadata = new HashMap<String, Object>();
    }

    public DocumentMetadata(List<ResultValueType> jaxbMetadata) {
        this();
        for (ResultValueType attribute : jaxbMetadata) {
            set(attribute.getID(), attribute.getText().getValue());
        }
    }

    /**
     * @return unique metadata name contained within this document.
     */
    public Set<String> getAvailableAttributeNames() {
        return metadata.keySet();
    }

    /**
     * @param key attribute key
     * @return attribute value(s) for the key. Note that this method might return a {@link Set} class,
     * if {@link #isMultiValueAttribute(String)} return true
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) metadata.get(key);
    }

    /**
     * @param attributeName attribute name
     * @return <code>true</code> if the given attribute key only have one value, <code>false</code> otherwise
     */
    public boolean isMultiValueAttribute(String attributeName) {
        return metadata.get(attributeName) instanceof Set;
    }

    @SuppressWarnings("unchecked")
    private void set(String attributeName, Object attributeValue) {
        Object existingValue = get(attributeName);
        if (existingValue != null) {
            if (existingValue instanceof Set) {
                ((Set) existingValue).add(attributeValue);
            } else {
                handleCreationOfNewAttributeContainer(attributeName, attributeValue, existingValue);
            }
        } else {
            metadata.put(attributeName, attributeValue);
        }
    }

    private void handleCreationOfNewAttributeContainer(String attributeKey,
                                                       Object newAttributeValue,
                                                       Object existingAttributeValue) {
        // we dont want to eagerly create a container, if the attribute being added is equal to the existing value,
        // in which case we will end up with a Set containing just one value
        if (!existingAttributeValue.equals(newAttributeValue)) {
            Set<Object> container = new HashSet<Object>();
            container.add(existingAttributeValue);
            container.add(newAttributeValue);
            metadata.put(attributeKey, container);
        }
    }

    /**
     * @return the number of attributes contained within this document
     */
    public int getNumberOfMetadata() {
        return metadata.size();
    }

    /**
     * @param attributeName attribute name
     * @return <code>true</code> if the attribute exists, <code>false</code> otherwise
     */
    public boolean hasAttribute(String attributeName) {
        return metadata.containsKey(attributeName);
    }

    /**
     * @param metadata
     * @return new instance of {@link org.github.alexwibowo.opentext.domain.DocumentMetadata}
     */
    public static DocumentMetadata createFrom(Map<String, String> metadata) {
        List<ResultValueType> jaxbMetadata = new ArrayList<ResultValueType>();
        for (Map.Entry<String, String> metadataEntry : metadata.entrySet()) {
            if (StringUtils.isNotBlank(metadataEntry.getValue()) && metadataEntry.getValue().contains(";")) {
                for (String currentValue : metadataEntry.getValue().split(";")) {
                    jaxbMetadata.add(new VRDResultValueTypeBuilder()
                            .withId(metadataEntry.getKey())
                            .withValue(currentValue)
                            .build());
                }
            } else {
                jaxbMetadata.add(new VRDResultValueTypeBuilder()
                        .withId(metadataEntry.getKey())
                        .withValue(metadataEntry.getValue())
                        .build());
            }

        }
        return new DocumentMetadata(jaxbMetadata);
    }
}
