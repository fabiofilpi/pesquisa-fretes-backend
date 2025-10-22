package br.com.centralar.utils;

import java.text.Normalizer;

import br.com.centralar.exceptions.InvalidCepException;
import lombok.Getter;

public final class CepUtils {
    private CepUtils() {}

    /**
     * Converte uma string "bagunçada" em CEP formatado NNNNN-NNN.
     * @param raw qualquer string contendo (ou não) um CEP
     * @return CEP formatado NNNNN-NNN
     * @throws InvalidCepException se não for possível extrair/validar um CEP
     */
    public static String parseCep(final String raw) {
        // 0) Nulo / vazio
        if (raw == null) {
            throw InvalidCepException.build("NULL_INPUT", "Valor nulo para CEP", null, null);
        }
        final String trimmed = raw.strip();
        if (trimmed.isEmpty()) {
            throw InvalidCepException.build("EMPTY_INPUT", "Valor vazio para CEP", raw, null);
        }

        // 1) Defesa: limite de tamanho (evita abuso/memória/regex pesada)
        final String limited = trimmed.length() > 5000 ? trimmed.substring(0, 5000) : trimmed;

        // 2) Normaliza Unicode para forma compatível e simplifica
        final String normalized = Normalizer.normalize(limited, Normalizer.Form.NFKC);

        // 3) Heurística 1: “cep ...” seguido de dígitos (prioritário)
        String fromLabeled = extractLabeledCep(normalized);
        if (fromLabeled != null) {
            return validateAndFormat(fromLabeled, raw);
        }

        // 4) Heurística 2: pega a primeira sequência contígua de 8 dígitos na string
        String firstRun = extractFirstEightDigitRun(normalized);
        if (firstRun != null) {
            return validateAndFormat(firstRun, raw);
        }

        // 5) Heurística 3: se não achou, coleta todos os dígitos (unicode-aware) e tenta achar janelas de 8
        String onlyDigits = digitsOnly(normalized);
        if (onlyDigits.length() == 8) {
            return validateAndFormat(onlyDigits, raw);
        } else if (onlyDigits.length() > 8) {
            for (int i = 0; i + 8 <= onlyDigits.length(); i++) {
                String candidate = onlyDigits.substring(i, i + 8);
                if (isPlausible(candidate)) {
                    return validateAndFormat(candidate, raw);
                }
            }
        }

        throw InvalidCepException.build(
                "NOT_FOUND",
                "Não foi possível identificar um CEP na entrada",
                raw,
                null
        );
    }

    // ==== Helpers ====

    private static String validateAndFormat(String eightDigits, String originalRaw) {
        if (eightDigits == null || eightDigits.length() != 8) {
            throw InvalidCepException.build("BAD_LENGTH", "CEP precisa ter 8 dígitos", originalRaw, eightDigits);
        }
        if (!allAsciiDigits(eightDigits)) {
            throw InvalidCepException.build("NON_DIGIT", "CEP contém caracteres não numéricos", originalRaw, eightDigits);
        }
        if (!isPlausible(eightDigits)) {
            throw InvalidCepException.build("IMPLAUSIBLE", "CEP inválido (sequência não plausível)", originalRaw, eightDigits);
        }
        // formato NNNNN-NNN
        return eightDigits.substring(0, 5) + "-" + eightDigits.substring(5);
    }

    private static boolean isPlausible(String eightDigits) {
        // Rejeita todos iguais (00000000, 11111111, ...), e 00000000 especificamente
        char first = eightDigits.charAt(0);
        boolean allEqual = true;
        for (int i = 1; i < eightDigits.length(); i++) {
            if (eightDigits.charAt(i) != first) { allEqual = false; break; }
        }
        if (allEqual) return false;
        return !"00000000".equals(eightDigits);// Demais regras de faixas seriam externas (ex.: consulta a base oficial, se quiser)
    }

    private static boolean allAsciiDigits(String s) {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c < '0' || c > '9') return false;
        }
        return true;
    }

    /**
     * Procura por padrões do tipo: "(?i)\bcep\b ... 8 dígitos"
     * Aceita "CEP: 13087-500", "cep=13087500", etc.
     */
    private static String extractLabeledCep(String text) {
        // Caminha por code points e, após "cep" isolado, tenta achar a primeira run de 8 dígitos contíguos
        final String lower = text.toLowerCase();
        int idx = indexOfWholeWord(lower);
        if (idx >= 0) {
            // procura do final do token "cep" para frente
            for (int i = idx + 3; i < text.length(); i++) {
                // pega primeira run de 8 dígitos contíguos
                String run = nextEightDigitRun(text, i);
                if (run != null) return run;
            }
        }
        return null;
    }

    private static int indexOfWholeWord(String s) {
        int i = s.indexOf("cep");
        while (i >= 0) {
            boolean leftOk = (i == 0) || !Character.isLetterOrDigit(s.charAt(i - 1));
            int end = i + "cep".length();
            boolean rightOk = (end == s.length()) || !Character.isLetterOrDigit(s.charAt(end));
            if (leftOk && rightOk) return i;
            i = s.indexOf("cep", i + 1);
        }
        return -1;
    }

    private static String nextEightDigitRun(String s, int start) {
        int count = 0;
        StringBuilder sb = new StringBuilder(8);
        for (int i = start; i < s.length(); i++) {
            int cp = s.codePointAt(i);
            int charCount = Character.charCount(cp);
            if (Character.getType(cp) == Character.DECIMAL_DIGIT_NUMBER) {
                int d = Character.getNumericValue(cp);
                if (d >= 0 && d <= 9) {
                    sb.append((char) ('0' + d));
                    count++;
                    if (count == 8) return sb.toString();
                } else {
                    // número estranho, reseta run
                    count = 0; sb.setLength(0);
                }
            } else {
                // quebrou a run
                if (count > 0) { count = 0; sb.setLength(0); }
            }
            i += charCount - 1;
        }
        return null;
    }

    private static String extractFirstEightDigitRun(String s) {
        return nextEightDigitRun(s, 0);
    }

    /**
     * Extrai apenas dígitos (suporta dígitos unicode) e devolve ASCII "0-9".
     */
    private static String digitsOnly(String s) {
        StringBuilder out = new StringBuilder(16);
        for (int i = 0; i < s.length(); ) {
            int cp = s.codePointAt(i);
            int type = Character.getType(cp);
            if (type == Character.DECIMAL_DIGIT_NUMBER) {
                int d = Character.getNumericValue(cp);
                if (d >= 0 && d <= 9) out.append((char) ('0' + d));
            }
            i += Character.charCount(cp);
        }
        return out.toString();
    }

}
