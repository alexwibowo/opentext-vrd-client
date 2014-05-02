package org.github.alexwibowo.xml.soap;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.soap.SOAPFactory;
import org.github.alexwibowo.security.SecurityHelper;
import org.github.alexwibowo.security.SecurityToken;
import org.github.alexwibowo.security.SecurityTokenGenerator;

import static com.google.common.base.Preconditions.checkArgument;
import static org.apache.commons.lang.StringUtils.isNotBlank;

/**
 * User: alexwibowo
 */
public class AxiomUsernameTokenElementBuilder {

    private SOAPFactory soapFactory;

    private String username;

    private String sharedKey;

    public AxiomUsernameTokenElementBuilder(SOAPFactory soapFactory) {
        this.soapFactory = soapFactory;
    }

    public AxiomUsernameTokenElementBuilder withUsername(String value) {
        this.username = value;
        return this;
    }

    public AxiomUsernameTokenElementBuilder withSharedKey(String value) {
        this.sharedKey = value;
        return this;
    }

    public OMElement build() {
        checkArgument(isNotBlank(username));
        checkArgument(isNotBlank(sharedKey));

        OMElement usernameToken = soapFactory.createOMElement("UsernameToken", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd", "wsse");
        OMElement usernameElement = soapFactory.createOMElement("Username", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd", "wsse");
        usernameElement.setText(username);
        usernameToken.addChild(usernameElement);

        SecurityTokenGenerator securityTokenGenerator = new SecurityTokenGenerator(new SecurityHelper(), sharedKey);
        SecurityToken generatedToken = securityTokenGenerator.generate();

        String timestamp = generatedToken.getTimestamp();

        OMElement createdElement = soapFactory.createOMElement("Created", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd", "wsse");
        createdElement.setText(timestamp);
        usernameToken.addChild(createdElement);

        OMElement nonceElement = soapFactory.createOMElement("Nonce", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd", "wsse");
        nonceElement.setText(generatedToken.getNonce());
        usernameToken.addChild(nonceElement);

        OMElement passwordElement = soapFactory.createOMElement("Password", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd", "wsse");
        passwordElement.setText(generatedToken.getNonce());

        passwordElement.addAttribute(soapFactory.createOMAttribute("Type", passwordElement.getNamespace(), "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordDigest"));
        passwordElement.addAttribute(soapFactory.createOMAttribute("EncodingType", passwordElement.getNamespace(), "SHA-1"));
        passwordElement.setText(generatedToken.getMessageDigest());
        usernameToken.addChild(passwordElement);

        return usernameToken;
    }

}
