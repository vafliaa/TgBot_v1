package org.example;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Main {
    public static void main(String[] args) throws IOException {
        FileInputStream fis = new FileInputStream("C:\\Users\\Vaflia\\TgBot_v1\\src\\main\\resources\\tokenAndBdPass.properties");
        Properties prop = new Properties();
        prop.load(fis);
        final String bdPassword = prop.getProperty("bd_password");
        final String botToken = prop.getProperty("bot_token");
        String botName = "SElabTgBot_v1";

        TelegramBotsApi telegramBotsApi = null;
        try {
            telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(new TgBot(botName,botToken));

        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}