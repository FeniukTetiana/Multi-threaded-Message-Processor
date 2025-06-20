package ua.shpp.feniuk.validation;

import java.util.Random;

public class EDDRValidator {
    private static final Random random = new Random();

    private static final int VALID_YEAR_START = 1924;
    private static final int VALID_YEAR_SPAN = 77;
    private static final int INVALID_YEAR_START = 2000;
    private static final int MONTH_COUNT = 12;
    private static final int MAX_DAY = 28;
    private static final int MAX_RECORD_NUMBER = 10000;
    private static final int CHECK_DIGIT_MOD = 10;
    private static final int INVALID_DIGIT_SHIFT_BOUND = 9;

    public boolean isValid(String eddr) {
        if (eddr == null || eddr.isBlank()) return false;

        String digits = eddr.replace("-", "");
        if (!digits.matches("\\d{13}")) return false;

        int sum = 0;
        for (int i = 0; i < 12; i++) {
            sum += Character.getNumericValue(digits.charAt(i));
        }
        int expected = sum % CHECK_DIGIT_MOD;
        int actual = Character.getNumericValue(digits.charAt(12));

        return expected == actual;
    }

    public String generateEDDR() {
        boolean valid = random.nextBoolean();
        return valid ? generateValidEDDR() : generateInvalidEDDR();
    }

    public String generateValidEDDR() {
        EDDRComponents eddr = generateComponents(VALID_YEAR_START);
        int checkDigit = eddr.checkDigit;
        return formatEDDR(eddr.birthDate, eddr.recordNumberStr, checkDigit);
    }

    public String generateInvalidEDDR() {
        EDDRComponents eddr = generateComponents(INVALID_YEAR_START);
        int invalidCheckDigit = (eddr.checkDigit + random.nextInt(INVALID_DIGIT_SHIFT_BOUND) + 1) % CHECK_DIGIT_MOD;
        return formatEDDR(eddr.birthDate, eddr.recordNumberStr, invalidCheckDigit);
    }

    private EDDRComponents generateComponents(int yearStart) {
        int year = random.nextInt(VALID_YEAR_SPAN) + yearStart;
        int month = random.nextInt(MONTH_COUNT) + 1;
        int day = random.nextInt(MAX_DAY) + 1;

        String birthDate = String.format("%04d%02d%02d", year, month, day);
        int recordNumber = random.nextInt(MAX_RECORD_NUMBER);
        String recordNumberStr = String.format("%04d", recordNumber);

        int sum = 0;
        for (char c : (birthDate + recordNumberStr).toCharArray()) {
            sum += Character.getNumericValue(c);
        }
        int checkDigit = sum % CHECK_DIGIT_MOD;

        return new EDDRComponents(birthDate, recordNumberStr, checkDigit);
    }

    private String formatEDDR(String birthDate, String recordNumberStr, int checkDigit) {
        boolean withDash = random.nextBoolean();
        return withDash
                ? birthDate + "-" + recordNumberStr + checkDigit
                : birthDate + recordNumberStr + checkDigit;
    }
}


