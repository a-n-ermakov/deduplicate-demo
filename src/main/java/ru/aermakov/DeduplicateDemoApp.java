package ru.aermakov;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * Users with multiple emails deduplication demo
 * Input: stdin (\n when finished)
 * Output: stdout
 * Format: user1 -> email1, email2
 */
public class DeduplicateDemoApp {

    public final static String DELIM_USER_EMAILS = " -> ";
    public final static String DELIM_EMAILS = ", ";

    public static void main(String[] args) {
        deduplicate(System.in, System.out);
    }

    /**
     * Deduplication of users by the same emails
     * Three map structures used here:
     *  - user -> emails
     *  - user -> group of users
     *  - email -> user (last arrived)
     * @param in input stream
     * @param out output stream
     */
    public static void deduplicate(InputStream in, PrintStream out) {
        Map<String, Set<String>> userEmails = new HashMap<>();
        Map<String, List<String>> userGroups = new HashMap<>();
        Map<String, String> emailUsers = new HashMap<>();
        //in
        var scanner = new Scanner(in);
        while (scanner.hasNextLine()) {
            var finished = parseLine(
                    scanner.nextLine(),
                    (user, emails) -> {
                        var existUsers = emails.stream()
                                .map(emailUsers::get)
                                .filter(Objects::nonNull)
                                .collect(Collectors.toSet());
                        //find groups
                        List<String> group;
                        if (existUsers.isEmpty()) {
                            //new
                            group = new ArrayList<>();
                        } else {
                            //merge exist groups
                            group = existUsers.stream()
                                    .flatMap(existUser -> userGroups.get(existUser).stream())
                                    .collect(Collectors.toList());
                            for (String existUser : group) {
                                userGroups.put(existUser, group);
                            }
                        }
                        group.add(user);
                        userGroups.put(user, group);
                        userEmails.put(user, emails);
                        for (String email : emails) {
                            emailUsers.put(email, user);
                        }
                    }
            );
            if (finished) {
                break;
            }
        }
        //out
        for (List<String> group : new HashSet<>(userGroups.values())) {
            //merge
            var user = group.get(0);
            var emails = userEmails.entrySet()
                    .stream()
                    .filter(e -> group.contains(e.getKey()))
                    .flatMap(e -> e.getValue().stream())
                    .collect(Collectors.toSet());
            out.println(String.join(DELIM_USER_EMAILS, user, String.join(DELIM_EMAILS, emails)));
        }
    }

    /**
     * Parse line with processing
     *
     * @param line next string
     * @param consumer processor
     * @return true if parsing is finished (\n)
     */
    public static boolean parseLine(String line, BiConsumer<String, Set<String>> consumer) {
        if (line.trim().isEmpty()) {
            return true;
        }
        var parts = line.split(DELIM_USER_EMAILS.trim());
        if (parts.length != 2) {
            return false;
        }
        var user = parts[0];
        var emails = Arrays.stream(parts[1].split(DELIM_EMAILS.trim()))
                .map(String::trim)
                .collect(Collectors.toSet());
        consumer.accept(user, emails);
        return false;
    }

}
