package org.example.service;

import org.example.bl.DatabaseUtils;
import org.example.TgBot;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EngTgBot {

    public void fetchEngTopics(int classNumber, String chatId, long messageId) {
        try {
            Statement statement = DatabaseUtils.getConnection().createStatement();
            String query = "SELECT \"id\", \"Название темы\", \"Класс\" FROM \"Английский_язык\"";
            ResultSet resultSet = statement.executeQuery(query);

            EditMessageText editMessageText = new EditMessageText();
            editMessageText.setChatId(chatId);
            editMessageText.setMessageId(Math.toIntExact(messageId));
            editMessageText.setText("Выберите тему для изучения в " + classNumber + " классе:");

            InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rowList = new ArrayList<>();

            while (resultSet.next()) {
                int idTopic = resultSet.getInt("id");
                int grade = resultSet.getInt("Класс");
                if (grade == classNumber) {
                    String topic = resultSet.getString("Название темы");

                    List<InlineKeyboardButton> rowInline = new ArrayList<>();
                    InlineKeyboardButton topicButton = new InlineKeyboardButton();
                    topicButton.setText(topic);
                    topicButton.setCallbackData("Topic_button_" + idTopic);
                    rowInline.add(topicButton);

                    rowList.add(rowInline);
                } else if (grade > classNumber) {
                    break;
                }
            }

            inlineKeyboardMarkup.setKeyboard(rowList);
            editMessageText.setReplyMarkup(inlineKeyboardMarkup);

            TgBot bot = new TgBot("SElabTgBot_v1", "6230815232:AAHpYALWtd3mgtVcoMrv8EEv2yFH1fMorhs");
            try {
                bot.execute(editMessageText);
            } catch (Exception e) {
                e.printStackTrace();
            }

            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            System.out.println("Failed to execute API method: " + e.getMessage());
        }
    }

}
