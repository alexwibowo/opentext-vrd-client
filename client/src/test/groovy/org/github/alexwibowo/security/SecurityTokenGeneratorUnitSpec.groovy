package org.github.alexwibowo.security

import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.junit.Test
import org.mockito.Mockito
import org.springframework.test.util.ReflectionTestUtils
import spock.lang.Specification

import java.text.SimpleDateFormat

import static org.joda.time.DateTimeZone.forOffsetHours
import static org.junit.Assert.assertThat
import static org.junit.Assert.fail
import static org.mockito.Mockito.when
import static org.springframework.test.util.ReflectionTestUtils.setField

/**
 * User: alexwibowo
 */
class SecurityTokenGeneratorUnitSpec extends Specification {
    private String sharedKey;

    private SecurityTokenGenerator securityTokenGenerator;
    private SecurityTokenValidator securityTokenValidator;

    private SecurityHelper helper;

    private int seventyTwoHoursInSeconds;

    def setup() {
        sharedKey = "secret!";

        helper = Mockito.spy(new SecurityHelper());
        securityTokenGenerator = new SecurityTokenGenerator(helper, sharedKey);
        securityTokenValidator = new SecurityTokenValidator(helper, sharedKey);
        seventyTwoHoursInSeconds = 72 * 60 * 60;
    }

    def "should be able to verify the generated digest"() {
        given:
        SecurityToken digest = securityTokenGenerator.generate();

        expect:
        securityTokenValidator.verify(digest, seventyTwoHoursInSeconds); // should not fail
    }

    def "should fail when timestamp not given"() {
        given:
        SecurityToken digest = securityTokenGenerator.generate();
        setField(digest, "timestamp", "");

        when:
        securityTokenValidator.verify(digest, seventyTwoHoursInSeconds);

        then:
        thrown(IllegalArgumentException.class)
    }

    def "should fail when timestamp is null"() {
        given:
        SecurityToken digest = securityTokenGenerator.generate();
        setField(digest, "timestamp", null);

        when:
        securityTokenValidator.verify(digest, seventyTwoHoursInSeconds);

        then:
        thrown(IllegalArgumentException.class)
    }

    def "should fail when nonce not given"() {
        given:
        SecurityToken digest = securityTokenGenerator.generate();
        setField(digest, "nonce", "");

        when:
        securityTokenValidator.verify(digest, seventyTwoHoursInSeconds);

        then:
        thrown(IllegalArgumentException.class)
    }

    def "should fail when nonce is null"() {
        given:
        SecurityToken digest = securityTokenGenerator.generate();
        setField(digest, "nonce", null);

        when:
        securityTokenValidator.verify(digest, seventyTwoHoursInSeconds);

        then:
        thrown(IllegalArgumentException.class)
    }

    def "should fail when the message digest is tampered"() {
        given:
        SecurityToken digest = securityTokenGenerator.generate();
        setField(digest, "messageDigest", "voodooChicken");

        when:
        securityTokenValidator.verify(digest, seventyTwoHoursInSeconds);

        then:
        SecurityException e = thrown()
        e.message =~ /Message digest verification failed/
    }

    def "should fail when the timestamp is older than the ttl"() {
        given:
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(SecurityHelper.TIMESTAMP_FORMAT);
        Date oldTime = new DateTime()
                .minusSeconds(10)
                .minusMillis(1)
                .withZone(forOffsetHours(0))
                .toDate();
        when(helper.getTimestamp()).thenReturn(simpleDateFormat.format(oldTime));


        when:
        SecurityToken digest = securityTokenGenerator.generate();
        securityTokenValidator.verify(digest, 10);

        then:
        SecurityException e = thrown()
        e.message =~ /Timestamp is older than 10 seconds/
    }

    def "should fail when timestamp is in wrong format"() {
        given:
        Date date = new Date();
        SecurityToken digest = securityTokenGenerator.generate();
        setField(digest, "timestamp", new SimpleDateFormat("yyy/MM/dd").format(date));

        when:
        securityTokenValidator.verify(digest, seventyTwoHoursInSeconds);

        then:
        SecurityException e = thrown()
        e.message =~ /Unable to parse timestamp/
    }

}