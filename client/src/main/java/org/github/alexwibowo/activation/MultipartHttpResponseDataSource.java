package org.github.alexwibowo.activation;

import javax.activation.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * User: alexwibowo
 */
public class MultipartHttpResponseDataSource implements DataSource {

    private InputStream inputStream;

    private String contentType;

    public MultipartHttpResponseDataSource(InputStream inputStream, String contentType) {
        this.inputStream = inputStream;
        this.contentType = contentType;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return inputStream;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return null;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public String getName() {
        return null;
    }
}
