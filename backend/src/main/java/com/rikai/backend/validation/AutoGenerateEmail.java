package com.rikai.backend.validation;

import java.text.Normalizer;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class AutoGenerateEmail {
    private static final String DOMAIN = "@rikai.technology";

    private AutoGenerateEmail() {
    }


    /**
     * Create auto email unique based on full name (without suffix role)
     *
     * @param fullName
     * @param emailExistsFunc
     * @return
     */
    public static String generateUniqueEmail(String fullName, Predicate<String> emailExistsFunc) {
        return generateUniqueEmail(fullName, null, emailExistsFunc);
    }

    /**
     * Create auto email unique based on full name with suffix role
     *
     * @param fullName
     * @param roleSuffix
     * @param emailExistsFunc
     * @return
     */
    public static String generateUniqueEmail(String fullName, String roleSuffix, Predicate<String> emailExistsFunc) {
        String basePrefix = getEmailPrefixFromFullName(fullName);

        // If roleSuffix is provided, append it to the base prefix
        String suffixPart = (roleSuffix != null && !roleSuffix.isEmpty()) ? "." + roleSuffix.toLowerCase() : "";

        // Create full prefix with optional role suffix
        String fullPrefix = basePrefix + suffixPart;

        String finalEmail = fullPrefix + DOMAIN;
        int count = 1;

        // Check similar: vinh.nguyen.mentor -> vinh.nguyen.mentor1 -> vinh.nguyen.mentor2
        while (emailExistsFunc.test(finalEmail)) {
            finalEmail = fullPrefix + count + DOMAIN;
            count++;
        }

        return finalEmail;
    }

    /**
     * Get email prefix from full name
     *
     * @param fullName
     * @return
     */
    private static String getEmailPrefixFromFullName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) return "unknown";
        String normalized = removeAccent(fullName).toLowerCase().trim();
        String[] parts = normalized.split("\\s+");
        if (parts.length < 1) return "unknown";
        String firstName = parts[parts.length - 1];
        String lastName = parts[0];
        return (parts.length > 1) ? firstName + "." + lastName : firstName;
    }

    /**
     * Remove accent from string
     *
     * @param s
     * @return
     */
    private static String removeAccent(String s) {
        String temp = Normalizer.normalize(s, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(temp).replaceAll("").replace('đ', 'd').replace('Đ', 'D');
    }
}
