package org.github.alexwibowo.opentext.domain;

import com.vignette._2014._04._14.vignettequery.NullableText;
import com.vignette._2014._04._14.vignettequery.ResultValueType;

/**
 * User: alexwibowo
 */
public class VRDResultValueTypeBuilder {

    private String id;

    private String value;

    private Boolean isNull;

    public VRDResultValueTypeBuilder withId(String value) {
        this.id = value;
        return this;
    }

    public VRDResultValueTypeBuilder withValue(String value) {
        this.value = value;
        return this;
    }

    public VRDResultValueTypeBuilder setNull(Boolean value) {
        isNull = value;
        return this;
    }

    public ResultValueType build() {
        ResultValueType rt = new ResultValueType();
        rt.setID(id);

        NullableText nt = new NullableText();
        nt.setIsNull(isNull);
        nt.setValue(value);

        rt.setText(nt);

        return rt;
    }
}
