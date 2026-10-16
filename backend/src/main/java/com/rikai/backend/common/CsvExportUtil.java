package com.rikai.backend.common;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.regex.Pattern;

public class CsvExportUtil {

    public static final String BYTE_ORDER_MARK = "\uFEFF";

    private static final Pattern PLAIN_NUMBER = Pattern.compile("^[+-]?\\d[\\d\\s().-]*$");

    public static void writeLine(Writer writer, List<String> values) throws IOException {
        boolean first = true;
        for (String value : values) {
            if (!first) {
                writer.append(',');
            }
            writer.append(escapeAndSanitize(value));
            first = false;
        }
        writer.append("\n");
    }

    private static String escapeAndSanitize(String value) {
        if (value == null) {
            return "";
        }
        String sanitized = value.trim();

        // Chống CSV Injection: chặn ô bắt đầu bằng ký tự công thức (tab/CR đầu chuỗi đã bị trim() loại bỏ)
        // nhưng không đụng tới số điện thoại/số thuần như "+84 912 345 678"
        if (!sanitized.isEmpty() && "=+-@".indexOf(sanitized.charAt(0)) >= 0
                && !PLAIN_NUMBER.matcher(sanitized).matches()) {
            sanitized = "'" + sanitized;
        }

        // Escape nháy kép và các ký tự đặc biệt
        if (sanitized.contains("\"")) {
            sanitized = sanitized.replace("\"", "\"\"");
        }
        if (sanitized.contains(",") || sanitized.contains("\n") || sanitized.contains("\r")
                || sanitized.contains("\"")) {
            sanitized = "\"" + sanitized + "\"";
        }

        return sanitized;
    }
}
