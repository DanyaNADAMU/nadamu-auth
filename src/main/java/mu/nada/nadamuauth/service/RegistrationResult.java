package mu.nada.nadamuauth.service;

public enum RegistrationResult {
    SUCCESS,
    ALREADY_REGISTERED,
    PASSWORDS_DO_NOT_MATCH,
    PASSWORD_TOO_SHORT,
    PASSWORD_TOO_LONG,
    ERROR
}
