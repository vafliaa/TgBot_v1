package org.example;

import org.example.bl.DatabaseUtils;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

public class Main {
    public static void main(String[] args) throws IOException, SQLException {
        /*
            Так как сам проект может переместиться, то лучше всего будет
            не писать полный путь до файла, а использовать функцию getResources
            Main.class.getResource("tokenAndBdPass.properties")
         */
        FileInputStream fis = new FileInputStream("C:\\Users\\Vaflia\\TgBot_v1\\src\\main\\resources\\tokenAndBdPass.properties");
        Properties prop = new Properties();
        prop.load(fis);
        final String bdPassword = prop.getProperty("bd_password");
        final String botToken = prop.getProperty("bot_token");
        String botName = "SElabTgBot_v1"; // имя для бота тоже лучше вынести в проперти файл

        /*
            Раз telegramBotsApi нигде дальше не используется,
            то лучше её внести в try {} блок.
         */
        TelegramBotsApi telegramBotsApi = null;
        try {
            telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);

            // bdPassword нигде не используется, надо создать дополнительные
            // параметры для класса TgBot, чтобы при создании подключения к бд использовать эти параметры.
            // Да и вообще лучше создать один статический класс, где будет происходить подключение к бд
            // и дальше возвращать само подключение
            String dbUrl = "jdbc:postgresql://localhost:5432/TgBot";
            String dbUser = "postgres";
            String dbPassword = "1793";
            DatabaseUtils.connectToDatabase(dbUrl, dbUser, dbPassword);

            telegramBotsApi.registerBot(new TgBot(botName,botToken));

        } catch (TelegramApiException e) {
            /*
                Тут лучше выводить в логи приложения информацию об ошибке
                и завершить процесс System.exit(1)
             */
            e.printStackTrace();
        } finally {
            DatabaseUtils.shutdown();
        }
    }
}