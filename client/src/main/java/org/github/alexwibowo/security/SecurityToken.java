package org.github.alexwibowo.security;

/**
 * Result of producing a digest, which consists of:
 * <ul>
 * <li>Timestamp {@link #getTimestamp()}</li>
 * <li>Nonce key {@link #getNonce()}</li>
 * <li>Message digest {@link #getMessageDigest()}</li>
 * </ul>
 * User: alexwibowo
 */
public class SecurityToken {

    private String timestamp;

    private String nonce;

    private String messageDigest;

    public SecurityToken(String timestamp, String nonce, String messageDigest) {
        this.timestamp = timestamp;
        this.nonce = nonce;
        this.messageDigest = messageDigest;
    }

    /**
     * @return timestamp used when producing the message digest
     */
    public String getTimestamp() {
        return timestamp;
    }

    /**
     * @return nonce key used when producing the message digest
     */
    public String getNonce() {
        return nonce;
    }

    /**
     * @return the message digest
     */
    public String getMessageDigest() {
        return messageDigest;
    }

}