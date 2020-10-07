package ru.aermakov;

import static java.util.Objects.isNull;

import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;

/**
 * Unit test for deduplicate
 */
public class DeduplicateDemoAppTest {

    @Test
    public void testFiles() throws IOException {
        int i = 0;
        while (true) {
            try(var in = this.getClass().getClassLoader().getResourceAsStream("in" + i);
                var expect = this.getClass().getClassLoader().getResourceAsStream("out" + i);
            ) {
                if (isNull(in) || isNull(expect)) {
                    break;
                }
                System.out.printf("--- case #%d ---%n", i);
                var fact = new ByteArrayOutputStream();
                DeduplicateDemoApp.deduplicate(in, new PrintStream(fact));
                Assert.assertEquals(parse(expect), parse(new ByteArrayInputStream(fact.toByteArray())));
                i += 1;
            }
        }
    }

    /**
     * Parse test stream into set of sets for equality comparison
     * @param in input stream
     * @return set of email sets
     * @throws IOException
     */
    private Set<Set<String>> parse(InputStream in) throws IOException {
        Set<Set<String>> result = new HashSet<>();
        try (var reader = new BufferedReader(new InputStreamReader(in))) {
            while (reader.ready()) {
                DeduplicateDemoApp.parseLine(reader.readLine(), (user, emails) -> result.add(emails));
            }
        }
        return result;
    }

}
