package mu.nada.nadamuauth.security;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class PasswordService {

    private static final int BCRYPT_COST = 12;

    public String hash(String rawPassword) {
        return BCrypt.withDefaults().hashToString(BCRYPT_COST, rawPassword.toCharArray());
    }

    public boolean verify(String rawPassword, String storedHash) {
        if (rawPassword == null || storedHash == null) {
            return false;
        }
        return BCrypt.verifyer().verify(rawPassword.toCharArray(), storedHash).verified;
    }

    public ValidationResult validate(String password, int minLength, int maxLength) {
        if (password == null || password.length() < minLength) {
            return ValidationResult.TOO_SHORT;
        }
        if (password.length() > maxLength) {
            return ValidationResult.TOO_LONG;
        }
        return ValidationResult.VALID;
    }

    public enum ValidationResult {
        VALID,
        TOO_SHORT,
        TOO_LONG
    }
}
