package org.github.alexwibowo.security

import spock.lang.Specification

import static org.apache.commons.codec.binary.Base64.encodeBase64String
import static org.hamcrest.CoreMatchers.equalTo
import static org.hamcrest.MatcherAssert.assertThat

/**
 * User: alexwibowo
 */
class Base64UnitSpec extends Specification {


    def "should be able to encode empty string"() {
        when:
        String encoded = Base64.encodeToString("".getBytes(), true);

        then:
        assertThat(encoded, equalTo(""));
    }

    def "test encode"() {
        when:
        String encoded = Base64.encodeToString("voodooChicken".getBytes(), true);

        then:
        assertThat(encoded, equalTo(encodeBase64String("voodooChicken".getBytes())));
    }

    def "test encode string with newline"() {
        when:
        String encoded = Base64.encodeToString("voodooChicken\nfoo".getBytes(), true);

        then:
        assertThat(encoded, equalTo(encodeBase64String("voodooChicken\nfoo".getBytes())));
    }

    def "test encode2"() {
        when:
        String encoded = Base64.encodeToString("voodooChicken".getBytes(), false);

        then:
        assertThat(encoded, equalTo(encodeBase64String("voodooChicken".getBytes())));
    }

    def "test encode2 with newline"() {
        when:
        String encoded = Base64.encodeToString("voodooChicken\nfoo".getBytes(), false);

        then:
        assertThat(encoded, equalTo(encodeBase64String("voodooChicken\nfoo".getBytes())));
    }

    def "test decode"() {
        when:
        String encoded = Base64.encodeToString("voodooChicken\nfoo".getBytes(), true);
        byte[] decoded = Base64.decode(encoded);

        then:
        assertThat(new String(decoded), equalTo("voodooChicken\nfoo"));
    }

    def "should be able to decode empty string"() {
        when:

        byte[] decoded = Base64.decode("");
        then:
        assertThat(new String(decoded), equalTo(""));
    }
}
