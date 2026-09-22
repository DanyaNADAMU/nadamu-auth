package mu.nada.nadamuauth.service;

import com.velocitypowered.api.proxy.Player;

import java.util.concurrent.CompletableFuture;

public interface AuthService {

    CompletableFuture<AuthResult> authenticate(Player player, String password);

    CompletableFuture<RegistrationResult> register(Player player, String password, String confirmPassword);

    CompletableFuture<Boolean> changePassword(Player player, String oldPassword, String newPassword);
}
