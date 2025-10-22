package br.com.centralar.exceptions;

import lombok.Getter;

@Getter
public class InvalidCepException extends IllegalArgumentException {
    private final String code;
    private final String inputExcerpt;
    private final String digitsExcerpt;

    private InvalidCepException(String code, String message, String inputExcerpt, String digitsExcerpt) {
        super(message);
        this.code = code;
        this.inputExcerpt = inputExcerpt;
        this.digitsExcerpt = digitsExcerpt;
    }

    public static InvalidCepException build(String code, String reason, String input, String digits) {
        String excerpt = sanitizeExcerpt(input);
        String dExcerpt = sanitizeExcerpt(digits);
        String msg = String.format(
                "CEP_INVALID(code=%s, reason=\"%s\", input=\"%s\", digits=\"%s\")",
                safe(code), safe(reason), safe(excerpt), safe(dExcerpt)
        );
        return new InvalidCepException(code, msg, excerpt, dExcerpt);
    }

    private static String sanitizeExcerpt(String s) {
        if (s == null) return null;
        String t = s.replaceAll("\\s+", " ").trim();
        if (t.length() > 120) t = t.substring(0, 120) + "…";
        return t;
    }

    private static String safe(String s) {
        return s == null ? "" : s.replace("\"", "\\\"");
    }
}
