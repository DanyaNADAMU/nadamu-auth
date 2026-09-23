package mu.nada.nadamuauth.storage;

import mu.nada.nadamuauth.config.PluginConfig;
import mu.nada.nadamuauth.model.UserAccount;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserRepositoryTest {

    @TempDir
    Path tempDir;

    private DatabaseManager databaseManager;
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        databaseManager = new DatabaseManager(tempDir, new PluginConfig.DatabaseSettings(), LoggerFactory.getLogger("TestLogger"));
        databaseManager.initialize();
        userRepository = new UserRepository(databaseManager, LoggerFactory.getLogger("TestLogger"));
    }

    @AfterEach
    void tearDown() {
        if (databaseManager != null) {
            databaseManager.shutdown();
        }
    }

    @Test
    void testCreateAndFindUser() throws Exception {
        UUID uuid = UUID.randomUUID();
        UserAccount account = new UserAccount(
                uuid,
                "TestPlayer",
                "hashedPassword",
                "127.0.0.1",
                Instant.now(),
                Instant.now(),
                false
        );

        userRepository.create(account).join();

        Optional<UserAccount> found = userRepository.findByUsername("testplayer").join();
        assertTrue(found.isPresent());
        assertEquals("TestPlayer", found.get().getUsername());
        assertEquals("127.0.0.1", found.get().getLastIp());
        assertFalse(found.get().isPremium());

        Optional<UserAccount> foundByUuid = userRepository.findByUuid(uuid).join();
        assertTrue(foundByUuid.isPresent());
        assertEquals("TestPlayer", foundByUuid.get().getUsername());
    }

    @Test
    void testSetPremiumAndLastLogin() throws Exception {
        UUID uuid = UUID.randomUUID();
        UserAccount account = new UserAccount(
                uuid,
                "Steve",
                "hash",
                "10.0.0.1",
                Instant.now(),
                Instant.now(),
                false
        );

        userRepository.create(account).join();

        userRepository.setPremium(uuid, true).join();
        userRepository.updateLastLogin(uuid, "10.0.0.2").join();

        UserAccount updated = userRepository.findByUuid(uuid).join().orElseThrow();
        assertTrue(updated.isPremium());
        assertEquals("10.0.0.2", updated.getLastIp());
    }

    @Test
    void testDirectGuestPremiumCreation() throws Exception {
        UUID mojangUuid = UUID.randomUUID();
        UserAccount premiumGuest = new UserAccount(
                mojangUuid,
                "DirectPremiumPlayer",
                "",
                "192.168.1.100",
                Instant.now(),
                Instant.now(),
                true
        );

        userRepository.create(premiumGuest).join();

        Optional<UserAccount> found = userRepository.findByUsername("directpremiumplayer").join();
        assertTrue(found.isPresent());
        assertTrue(found.get().isPremium());
        assertEquals("192.168.1.100", found.get().getLastIp());
    }
}
