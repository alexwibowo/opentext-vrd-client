package org.github.alexwibowo.security

import org.joda.time.DateTime
import org.joda.time.Period
import org.junit.Before
import org.junit.Test
import spock.lang.Specification

import java.text.SimpleDateFormat
import java.util.concurrent.Callable
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

import static org.junit.Assert.assertThat
import static org.junit.Assert.assertThat


/**
 * User: alexwibowo
 */
class SecurityHelperUnitSpec extends Specification {


    private SecurityHelper helper;

    def setup() {
        helper = new SecurityHelper();
    }

    def "should always generate unique string"(){
        given:
        int numOfGenerator = 10;
        int numOfKeyToGeneratePerGenerator = 1000;

        when:
        List<String> nonces = generateNoncesInParallel(numOfGenerator, numOfKeyToGeneratePerGenerator);

        then:
        assert nonces.unique().size() ==  numOfGenerator * numOfKeyToGeneratePerGenerator
    }

    private List<String> generateNoncesInParallel(int numOfGenerator, int numOfKeyToGeneratePerGenerator)
            throws InterruptedException, ExecutionException {
        ExecutorService executorService = Executors.newFixedThreadPool(numOfGenerator);

        List<Callable<List<String>>> callables = createCallables(numOfGenerator, numOfKeyToGeneratePerGenerator);

        List<Future<List<String>>> futures = executorService.invokeAll(callables);
        executorService.shutdown();
        executorService.awaitTermination(30, TimeUnit.SECONDS);

        futures.collect { Future future -> future.get()}.flatten()
    }

    private List<Callable<List<String>>> createCallables(int numOfGenerator, int numOfKeyToGeneratePerGenerator) {
        // create callable tasks that will be executed in parallel
        List<Callable<List<String>>> callables = new ArrayList<Callable<List<String>>>();
        for (int i = 0; i < numOfGenerator; i++) {
            callables.add(createCallable(numOfKeyToGeneratePerGenerator));
        }
        return callables;
    }

    private Callable<List<String>> createCallable(final int numNoncesToGenerate) {
        return new Callable<List<String>>(){
            public List<String> call() throws Exception {
                // do the actual work.. generate nonces
                List<String> nonces = new ArrayList<String>();
                for (int i = 0; i < numNoncesToGenerate; i++) {
                    nonces.add(helper.getNonce());
                }
                return nonces;
            }
        };
    }

    def "getTimestamp should generate currentTime"(){
        given:
        String timestamp = helper.getTimestamp();
        Date date = new SimpleDateFormat(SecurityHelper.TIMESTAMP_FORMAT).parse(timestamp);

        when:
        int seconds = new Period(new DateTime(date), new DateTime()).getSeconds();

        then:
        assert seconds < 5
    }

}