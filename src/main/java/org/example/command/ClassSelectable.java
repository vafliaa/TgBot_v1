package org.example.command;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.util.ArrayList;
import java.util.List;

public class ClassSelectable implements ExecuteCommand {

    private final SendMessage message;
    private final AbsSender sender;

    public ClassSelectable(SendMessage message, AbsSender sender) {
        this.message = message;
        this.sender = sender;
    }


    @Override
    public String name() {
        return "Начать обучение";
    }

    @Override
    public void execute(String chatId) {
        if (chatId == null) {
            System.out.println("ChatId is null");
            return;
        }
        message.setChatId(chatId);
        message.setText("Выберите класс для изучения:");

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();

        for (int i = 1; i <= 3; i++) {
            InlineKeyboardButton classButton = new InlineKeyboardButton();
            classButton.setText(i + " класс");
            classButton.setCallbackData(i + "Class_button");
            rowInline.add(classButton);
        }

        List<InlineKeyboardButton> rowInline2 = new ArrayList<>();

        for (int i = 4; i <= 6; i++) {
            InlineKeyboardButton classButton = new InlineKeyboardButton();
            classButton.setText(i + " класс");
            classButton.setCallbackData(i + "Class_button");
            rowInline2.add(classButton);
        }

        List<InlineKeyboardButton> rowInline3 = new ArrayList<>();

        for (int i = 7; i <= 9; i++) {
            InlineKeyboardButton classButton = new InlineKeyboardButton();
            classButton.setText(i + " класс");
            classButton.setCallbackData(i + "Class_button");
            rowInline3.add(classButton);
        }

        rowList.add(rowInline);
        rowList.add(rowInline2);
        rowList.add(rowInline3);

        inlineKeyboardMarkup.setKeyboard(rowList);
        message.setReplyMarkup(inlineKeyboardMarkup);
        try {
            sender.execute(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
