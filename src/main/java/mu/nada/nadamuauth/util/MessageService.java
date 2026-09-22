package mu.nada.nadamuauth.util;

import mu.nada.nadamuauth.config.MessagesConfig;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.Map;

public class MessageService {

    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final MessagesConfig messagesConfig;

    public MessageService(MessagesConfig messagesConfig) {
        this.messagesConfig = messagesConfig;
    }

    public Component parse(String rawText) {
        return miniMessage.deserialize(rawText);
    }

    public Component parse(String rawText, Map<String, String> placeholders) {
        String result = rawText;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return miniMessage.deserialize(result);
    }

    public Component withPrefix(String rawText) {
        return miniMessage.deserialize(messagesConfig.prefix() + rawText);
    }

    public Component withPrefix(String rawText, Map<String, String> placeholders) {
        String result = rawText;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return miniMessage.deserialize(messagesConfig.prefix() + result);
    }

    public void sendMessage(Audience audience, String rawText) {
        audience.sendMessage(withPrefix(rawText));
    }

    public void sendMessage(Audience audience, String rawText, Map<String, String> placeholders) {
        audience.sendMessage(withPrefix(rawText, placeholders));
    }

    public void sendRawMessage(Audience audience, String rawText) {
        audience.sendMessage(parse(rawText));
    }

    public MessagesConfig config() {
        return messagesConfig;
    }
}
