package ua.shpp.feniuk.validation;

public class EDDRComponents {
    String birthDate;
    String recordNumberStr;
    int checkDigit;

    EDDRComponents(String birthDate, String recordNumberStr, int checkDigit) {
        this.birthDate = birthDate;
        this.recordNumberStr = recordNumberStr;
        this.checkDigit = checkDigit;
    }
}
