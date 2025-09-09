package fr.arkoter.proceduraldungeons.utils;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MessageUtils {

    private static YamlConfiguration messages;
    private static final Map<String, String> messageCache = new HashMap<>();

    public static void loadMessages(File messagesFile) {
        if (!messagesFile.exists()) {
            // Créer le fichier avec des messages par défaut si il n'existe pas
            createDefaultMessagesFile(messagesFile);
        }

        messages = YamlConfiguration.loadConfiguration(messagesFile);
        messageCache.clear();

        // Pré-charger les messages dans le cache
        cacheMessages();
    }

    public static String getMessage(String key) {
        return messageCache.getOrDefault(key, "§cMessage introuvable: " + key);
    }

    public static String getMessage(String key, String... replacements) {
        String message = getMessage(key);

        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                message = message.replace(replacements[i], replacements[i + 1]);
            }
        }

        return message;
    }

    private static void cacheMessages() {
        if (messages == null) return;

        for (String key : messages.getKeys(true)) {
            if (messages.isString(key)) {
                String message = messages.getString(key);
                messageCache.put(key, ChatColor.translateAlternateColorCodes('&', message));
            }
        }
    }

    private static void createDefaultMessagesFile(File file) {
        // Cette méthode serait implémentée pour créer un fichier de messages par défaut
        // Pour la brevité, elle n'est pas complètement implémentée ici
    }
}