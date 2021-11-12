package net.developerden.randomtp;

import me.bristermitten.mittenlib.config.Config;
import me.bristermitten.mittenlib.config.Source;
import me.bristermitten.mittenlib.config.names.NamingPattern;
import me.bristermitten.mittenlib.config.names.NamingPatterns;
import me.bristermitten.mittenlib.lang.LangMessage;

@Config
@Source("config.yml")
@NamingPattern(NamingPatterns.LOWER_KEBAB_CASE)
public final class RandomTPConfigDTO {
    int tpCost;
    int maxX;
    int minX;
    int maxZ;
    int minZ;
    MessageConfigDTO messages;

    @Config
    static final class MessageConfigDTO {
        String prefix;
        LangMessage notAPlayer;
        LangMessage unknownPlayer;
        LangMessage noPermission;
        LangMessage tpStarting;
        LangMessage tpSuccess;
        LangMessage tpFailed;
        LangMessage tpFailedMoney;
        LangMessage tpFailedAlreadyTeleporting;
    }
}
