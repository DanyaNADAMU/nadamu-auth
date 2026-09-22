package mu.nada.nadamuauth.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordServiceTest {

    private PasswordService passwordService;

    @BeforeEach
    void setUp() {
        passwordService = new PasswordService();
    }

    @Test
    void testHashAndVerify() {
        String rawPassword = "SuperSecretPassword123";
        String hash = passwordService.hash(rawPassword);

        assertNotNull(hash);
        assertTrue(passwordService.verify(rawPassword, hash));
        assertFalse(passwordService.verify("WrongPassword", hash));
    }

    @Test
    void testValidation() {
        assertEquals(PasswordService.ValidationResult.TOO_SHORT, passwordService.validate("12345", 6, 64));
        assertEquals(PasswordService.ValidationResult.TOO_LONG, passwordService.validate("a".repeat(65), 6, 64));
        assertEquals(PasswordService.ValidationResult.VALID, passwordService.validate("validPass", 6, 64));
    }
}
