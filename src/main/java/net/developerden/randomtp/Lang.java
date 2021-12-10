package net.developerden.randomtp;

import me.bristermitten.mittenlib.lang.LangMessage;
import me.bristermitten.mittenlib.lang.LangService;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Map;
import java.util.function.Function;

public class Lang {
    private final LangService langService;
    private final Provider<RandomTPConfig> config;

    @Inject
    public Lang(LangService langService, Provider<RandomTPConfig> config) {
        this.langService = langService;
        this.config = config;
    }

    public void send(@NotNull CommandSender receiver, @NotNull Function<RandomTPConfig.MessageConfig, LangMessage> langMessage) {
        final var t = config.get().messages();
        langService.send(receiver, langMessage.apply(t), t.prefix());
    }

    public void send(@NotNull CommandSender receiver, @NotNull Function<RandomTPConfig.MessageConfig, LangMessage> langMessage, @NotNull Map<String, Object> placeholders) {
        final var t = config.get().messages();
        langService.send(receiver, langMessage.apply(t), placeholders, t.prefix());
    }
}
