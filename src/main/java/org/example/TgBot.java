package org.example;

import org.example.command.CalculateAverageGrade;
import org.example.command.ClassSelectable;
import org.example.command.ExecuteCommand;
import org.example.command.SendMessageWithButton;
import org.example.service.EngTgBot;
import org.example.service.HisTgBot;
import org.example.service.InfTgBot;
import org.example.service.MathTgBot;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.*;
import java.util.*;

public class TgBot extends TelegramLongPollingBot {
    private final String botName;
    private final String botToken;
    private final SendMessage message = new SendMessage();
    private String messageText;
    private String chatId;
    private int classNumber;
    private int TopicId;
    private boolean waitingForAnswer = false;
    private String currentTaskText;
    private boolean flagTest = false;
    private boolean flagAnwser = false;
    private String x = ""; //Лучше назвать переменную типа nameSchoolSubject
    private final Map<String, ExecuteCommand> executeCommandMap = new HashMap<>();

    public TgBot(String botName, String botToken) {
        this.botName = botName;
        this.botToken = botToken;

        //Это как вариант дробления кода на классы
        ExecuteCommand sendMessageWithButton = new SendMessageWithButton( this);
        ExecuteCommand classSelectable = new ClassSelectable(message, this);
        ExecuteCommand calculateAverageGrade = new CalculateAverageGrade(message, this);
        executeCommandMap.put(sendMessageWithButton.name(), sendMessageWithButton);

        //Правда при разных ключах могут вызваться одни и те же методы
        //этот момент при создании API интерфейса я не продумал.
        executeCommandMap.put(classSelectable.name(), classSelectable);
        executeCommandMap.put("Back_button", classSelectable);

        executeCommandMap.put(calculateAverageGrade.name(), calculateAverageGrade);
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        // Очень много ветвлений, что плохо влияет на читаемость кода.
        // Лучше поделить функционал на отдельные классы
        if (update.hasMessage() && update.getMessage().hasText()) {
            messageText = update.getMessage().getText();
            chatId = update.getMessage().getChatId().toString();
            message.setChatId(chatId);
            //Вместо switch case метода мы используем мапу
            executeCommandMap.get(messageText).execute(chatId);
//            switch (messageText) {
//                case "/start":
//                    sendMessageWithButtons(chatId);
//                    break;
//                case "Начать обучение":
//                    classSelectable(chatId);
//                    break;
//                case "Успеваемость":
//                    calculateAverageGrade(chatId);
//                    break;
//            }
            if (!flagTest && flagAnwser && !Objects.equals(messageText, "/stop") && !Objects.equals(messageText, "/start") && !Objects.equals(messageText, "")) {
                System.out.println(x);
                processAnswer(chatId, messageText, x, TopicId);
            } else if (flagTest && !flagAnwser && !Objects.equals(messageText, "/stop") && !Objects.equals(messageText, "/start") && !Objects.equals(messageText, "")) {
                System.out.println("Test");
                processAnswerTest(chatId, messageText, x, TopicId);
            }
        } else if (update.hasCallbackQuery()) {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            String callBackQueryData = callbackQuery.getData();
            long messageId = callbackQuery.getMessage().getMessageId();
            if (callBackQueryData.endsWith("Class_button")) {
                classNumber = Integer.parseInt(String.valueOf(callBackQueryData.charAt(0)));
                itemSelectable(classNumber, chatId, messageId);
            } else if (callBackQueryData.endsWith("_button_Subject")) {
                switch (callBackQueryData) {
                    case "Math_button_Subject":
                        MathTgBot mathTgBot = new MathTgBot();
                        mathTgBot.connectToDatabase();
                        mathTgBot.fetchMathTopics(classNumber, chatId, messageId);
                        x = "Математика";
                        break;
                    case "Eng_button_Subject":
                        EngTgBot engTgBot = new EngTgBot();
                        engTgBot.fetchEngTopics(classNumber, chatId, messageId);
                        x = "Английский_язык";
                        break;
                    case "Inf_button_Subject":
                        InfTgBot infTgBot = new InfTgBot();
                        infTgBot.connectToDatabase();
                        infTgBot.fetchInfTopics(classNumber, chatId, messageId);
                        x = "Информатика";
                        break;
                    case "His_button_Subject":
                        HisTgBot hisTgBot = new HisTgBot();
                        hisTgBot.connectToDatabase();
                        hisTgBot.fetchHisTopics(classNumber, chatId, messageId);
                        x = "История";
                        break;
                }
            } else if (callBackQueryData.contains("Topic_button_")) {
                TopicId = Integer.parseInt(callBackQueryData.substring(callBackQueryData.length() - 2));
                fetchTopicOptions(chatId, messageId);
            } else if (callBackQueryData.equals("Theory_button")) {
                fetchTheoryData(chatId, messageId, TopicId);
            } else if (callBackQueryData.equals("Tasks_button")) {
                sendTask(chatId, messageId, TopicId);
            } else if (callBackQueryData.equals("Test_button")) {
                sendTest(chatId, messageId, TopicId);
            } else if (callBackQueryData.equals("Back_button")) {
                System.out.println("Use Back_button");
                executeCommandMap.get("Back_button").execute(chatId);
            } else if (callBackQueryData.equals("Back_button2") || callBackQueryData.equals("Back_button3") || callBackQueryData.equals("Back_button4")) {
                System.out.println("Use Back_button2");
                fetchTopicOptions(chatId, messageId);
            } else if (callBackQueryData.equals("Back_button5")) {
                System.out.println("Use Back_button5");
                itemSelectable(classNumber, chatId, messageId);
            }
        }
    }


    //Создание кнопок выбора предмета и кнопки назад
    public void itemSelectable(int classNumber, String chatId, long messageId) {
        if (chatId == null) {
            System.out.println("ChatId is null");
            return;
        }
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(chatId);
        editMessageText.setMessageId(Math.toIntExact(messageId));
        editMessageText.setText("Выберите предмет для изучения в " + classNumber + " классе:");

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline2 = new ArrayList<>();
        List<InlineKeyboardButton> rowInline3 = new ArrayList<>();

        InlineKeyboardButton backButton = new InlineKeyboardButton();
        backButton.setText("Назад");
        backButton.setCallbackData("Back_button");
        rowInline3.add(backButton);

        if (classNumber < 5) {
            InlineKeyboardButton mathButton = new InlineKeyboardButton();
            mathButton.setText("Математика");
            mathButton.setCallbackData("Math_button_Subject");
            rowInline.add(mathButton);

            InlineKeyboardButton engButton = new InlineKeyboardButton();
            engButton.setText("Английский язык");
            engButton.setCallbackData("Eng_button_Subject");
            rowInline.add(engButton);

        } else if (classNumber < 8) {
            InlineKeyboardButton mathButton = new InlineKeyboardButton();
            mathButton.setText("Математика");
            mathButton.setCallbackData("Math_button_Subject");
            rowInline.add(mathButton);

            InlineKeyboardButton engButton = new InlineKeyboardButton();
            engButton.setText("Английский язык");
            engButton.setCallbackData("Inf_button_Subject");
            rowInline.add(engButton);

            InlineKeyboardButton infButton = new InlineKeyboardButton();
            infButton.setText("Информатика");
            infButton.setCallbackData("Inf_button_Subject");
            rowInline2.add(infButton);

            InlineKeyboardButton hisButton = new InlineKeyboardButton();
            hisButton.setText("История");
            hisButton.setCallbackData("His_button_Subject");
            rowInline2.add(hisButton);
        } else if (classNumber < 10) {
            InlineKeyboardButton engButton = new InlineKeyboardButton();
            engButton.setText("Английский язык");
            engButton.setCallbackData("Eng_button_Subject");
            rowInline.add(engButton);

            InlineKeyboardButton infButton = new InlineKeyboardButton();
            infButton.setText("Информатика");
            infButton.setCallbackData("Inf_button_Subject");
            rowInline.add(infButton);
            InlineKeyboardButton hisButton = new InlineKeyboardButton();

            hisButton.setText("История");
            hisButton.setCallbackData("His_button_Subject");
            rowInline2.add(hisButton);
        }

        rowList.add(rowInline);
        rowList.add(rowInline2);
        rowList.add(rowInline3);

        inlineKeyboardMarkup.setKeyboard(rowList);
        editMessageText.setReplyMarkup(inlineKeyboardMarkup);

        try {
            execute(editMessageText);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void fetchTopicOptions(String chatId, long messageId) {
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(chatId);
        editMessageText.setMessageId(Math.toIntExact(messageId));
        editMessageText.setText("Выбор обучения: ");

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();

        InlineKeyboardButton theoryButton = new InlineKeyboardButton();
        theoryButton.setText("Теория");
        theoryButton.setCallbackData("Theory_button");

        InlineKeyboardButton tasksButton = new InlineKeyboardButton();
        tasksButton.setText("Задания");
        tasksButton.setCallbackData("Tasks_button");

        InlineKeyboardButton testButton = new InlineKeyboardButton();
        testButton.setText("Тест");
        testButton.setCallbackData("Test_button");

        InlineKeyboardButton backButton5 = new InlineKeyboardButton();
        backButton5.setText("Назад");
        backButton5.setCallbackData("Back_button5");

        rowInline.add(theoryButton);
        rowInline.add(tasksButton);
        rowInline.add(testButton);

        List<InlineKeyboardButton> rowInline2 = new ArrayList<>();
        rowInline2.add(backButton5);

        rowList.add(rowInline);
        rowList.add(rowInline2);
        inlineKeyboardMarkup.setKeyboard(rowList);
        editMessageText.setReplyMarkup(inlineKeyboardMarkup);

        try {
            execute(editMessageText);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // Отправка сообщения
    public void sendMessage(String chatId, String textToSend) {
        message.setChatId(chatId);
        message.setText(textToSend);

        try {
            execute(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Изменение сообщения
    public void editMessage(String textToEdit, String chatId, long messageId) {
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(chatId);
        editMessageText.setText(textToEdit);
        editMessageText.setMessageId((int) messageId);

        try {
            execute(editMessageText);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void fetchTheoryData(String chatId, long messageId, int TopicId) {
        try {
            String dbUrl = "jdbc:postgresql://localhost:5432/TgBot";
            String dbUser = "postgres";
            String dbPassword = "1793";
            Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);

            Statement statement = connection.createStatement();
            String query = "SELECT id, текст FROM Теория";
            ResultSet resultSet = statement.executeQuery(query);
            try {
                connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
                System.out.println("Connected to the database!");
            } catch (SQLException e) {
                System.out.println("Failed to connect to the database table Теория.");
                e.printStackTrace();
            }
            String theoryText = "";
            boolean topicFound = false;

            while (resultSet.next()) {
                int theoryId = resultSet.getInt("id");
                if (theoryId == TopicId) {
                    theoryText = resultSet.getString("текст");
                    topicFound = true;
                    break;
                }
            }

            EditMessageText editMessageText = new EditMessageText();
            editMessageText.setChatId(chatId);
            editMessageText.setMessageId(Math.toIntExact(messageId));

            if (topicFound) {
                editMessageText.setText(theoryText);

                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                List<List<InlineKeyboardButton>> rowList = new ArrayList<>();

                List<InlineKeyboardButton> rowInline = new ArrayList<>();
                InlineKeyboardButton backButton2 = new InlineKeyboardButton();
                backButton2.setText("Назад");
                backButton2.setCallbackData("Back_button2");
                rowInline.add(backButton2);

                rowList.add(rowInline);
                inlineKeyboardMarkup.setKeyboard(rowList);
                editMessageText.setReplyMarkup(inlineKeyboardMarkup);
            } else {
                editMessageText.setText("Теория не найдена");
            }

            resultSet.close();
            statement.close();
            connection.close();

            execute(editMessageText);
        } catch (SQLException | TelegramApiException e) {
            System.out.println("Failed to execute API method: " + e.getMessage());
        }
    }

    public void sendTask(String chatId, long messageId, int TopicId) {
        try {
            flagAnwser = true;
            String dbUrl = "jdbc:postgresql://localhost:5432/TgBot";
            String dbUser = "postgres";
            String dbPassword = "1793";
            Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);

            String selectQueryTask = "SELECT id, задание FROM \"Задачи\"";
            Statement statementTask = connection.createStatement();
            ResultSet resultSetTask = statementTask.executeQuery(selectQueryTask);
            boolean taskFound = false;
            while (resultSetTask.next()) {
                int currentTaskId = resultSetTask.getInt("id");
                if (currentTaskId == TopicId) {
                    currentTaskText = resultSetTask.getString("задание");
                    taskFound = true;
                    break;
                }
            }
            EditMessageText editMessageText = new EditMessageText();
            editMessageText.setChatId(chatId);
            editMessageText.setMessageId(Math.toIntExact(messageId));

            if (taskFound) {
                editMessageText.setText(currentTaskText);
            } else editMessageText.setText("Задача не найдена");

            resultSetTask.close();
            statementTask.close();
            connection.close();
            execute(editMessageText);

        } catch (SQLException | TelegramApiException e) {
            e.printStackTrace();
        }
    }


    private void processAnswer(String chatId, String answer, String tableName2, int TopicId) {
        try {
            System.out.println(tableName2);
            System.out.println("Медот работает 2");
            String dbUrl = "jdbc:postgresql://localhost:5432/TgBot";
            String dbUser = "postgres";
            String dbPassword = "1793";
            Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
            Statement statement = connection.createStatement();
            String selectQueryAnswer = "SELECT id, ответ FROM Задачи";
            PreparedStatement statementAnswer = connection.prepareStatement(selectQueryAnswer);
            ResultSet resultSetAnswer = statementAnswer.executeQuery();
            boolean answerFound = false;
            String expectedAnswer = "";
            while (resultSetAnswer.next()) {
                int currentTaskId = resultSetAnswer.getInt("id");
                if (currentTaskId == TopicId) {
                    expectedAnswer = resultSetAnswer.getString("ответ");
                    answerFound = true;
                    break;
                }
            }
            if (answerFound) {
                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                List<List<InlineKeyboardButton>> rowList1 = new ArrayList<>();

                List<InlineKeyboardButton> rowInline1 = new ArrayList<>();
                InlineKeyboardButton backButton3 = new InlineKeyboardButton();
                backButton3.setText("Назад");
                backButton3.setCallbackData("Back_button3");
                rowInline1.add(backButton3);

                rowList1.add(rowInline1);
                inlineKeyboardMarkup.setKeyboard(rowList1);
                message.setReplyMarkup(inlineKeyboardMarkup);

                String tableName = "user_" + chatId;
                String checkTableQuery = "SELECT to_regclass('" + tableName + "')";
                ResultSet resultSet = statement.executeQuery(checkTableQuery);
                resultSet.next();
                boolean tableExists = (resultSet.getString(1) != null);

                if (tableExists) {
                    System.out.println("Таблица уже существует: " + tableName);
                } else {
                    // Создание таблицы
                    String createTableQuery = "CREATE TABLE " + tableName + " (" +
                            "Название_предмета VARCHAR(255), " +
                            "Название_темы VARCHAR(255), " +
                            "Оценка_задачи INT," +
                            "Оценка_тест INT" +
                            ")";

                    statement.executeUpdate(createTableQuery);
                    System.out.println("Таблица создана: " + tableName);
                }

                String checkQuery = "SELECT COUNT(*) FROM " + tableName + " WHERE Название_темы = ?";
                PreparedStatement checkStatement = connection.prepareStatement(checkQuery);
                checkStatement.setString(1, String.valueOf(TopicId));
                ResultSet checkResult = checkStatement.executeQuery();
                checkResult.next();
                int count = checkResult.getInt(1);
                checkResult.close();
                checkStatement.close();

                if (count > 0) {
                    // Запись существует, обновление балла
                    String updateQuery = "UPDATE " + tableName + " SET Оценка_задачи = ? WHERE Название_темы = ?";
                    PreparedStatement updateStatement = connection.prepareStatement(updateQuery);
                    if (Objects.equals(String.valueOf(expectedAnswer.charAt(0)), answer)) {
                        sendMessage(chatId, "Ваш ответ: " + answer + "\n Верно!");
                        updateStatement.setInt(1, 5);
                    } else {
                        sendMessage(chatId, "Ваш ответ: " + answer + "\nНе верно!\n Решение: " + expectedAnswer.substring(1));
                        updateStatement.setInt(1, 2);
                    }
                    updateStatement.setString(2, String.valueOf(TopicId));
                    updateStatement.executeUpdate();
                    updateStatement.close();
                } else {
                    // Получение названия темы
                    String topicQuery = "SELECT \"Название темы\" FROM " + tableName2 + " WHERE id = ?";
                    PreparedStatement topicStatement = connection.prepareStatement(topicQuery);
                    topicStatement.setInt(1, TopicId);
                    ResultSet topicResult = topicStatement.executeQuery();
                    topicResult.next();
                    String topicName = topicResult.getString("Название темы");
                    topicResult.close();
                    topicStatement.close();

                    // Проверка наличия записи с таким же названием темы
                    String checkTopicQuery = "SELECT COUNT(*) FROM " + tableName + " WHERE Название_темы = ?";
                    PreparedStatement checkTopicStatement = connection.prepareStatement(checkTopicQuery);
                    checkTopicStatement.setString(1, topicName);
                    ResultSet checkTopicResult = checkTopicStatement.executeQuery();
                    checkTopicResult.next();
                    int topicCount = checkTopicResult.getInt(1);
                    checkTopicResult.close();
                    checkTopicStatement.close();

                    if (topicCount > 0) {
                        // Запись существует, обновление балла
                        String updateQuery = "UPDATE " + tableName + " SET Оценка_задачи = ? WHERE Название_темы = ?";
                        PreparedStatement updateStatement = connection.prepareStatement(updateQuery);
                        if (Objects.equals(String.valueOf(expectedAnswer.charAt(0)), answer)) {
                            sendMessage(chatId, "Ваш ответ: " + answer + "\n Верно!");
                            updateStatement.setInt(1, 5);
                        } else {
                            sendMessage(chatId, "Ваш ответ: " + answer + "\nНе верно!\n Решение: " + expectedAnswer.substring(1));
                            updateStatement.setInt(1, 2);
                        }
                        updateStatement.setString(2, topicName);
                        updateStatement.executeUpdate();
                        updateStatement.close();
                    } else {
                        // Добавление данных в таблицу пользователя
                        String insertQuery = "INSERT INTO " + tableName + " (Название_предмета, Название_темы, Оценка_задачи) VALUES (?, ?, ?)";
                        PreparedStatement insertStatement = connection.prepareStatement(insertQuery);
                        insertStatement.setString(1, tableName2);
                        insertStatement.setString(2, topicName);
                        if (Objects.equals(String.valueOf(expectedAnswer.charAt(0)), answer)) {
                            sendMessage(chatId, "Ваш ответ: " + answer + "\n Верно!");
                            insertStatement.setInt(3, 5);
                        } else {
                            sendMessage(chatId, "Ваш ответ: " + answer + "\nНе верно!\n Решение: " + expectedAnswer.substring(1));
                            insertStatement.setInt(3, 2);
                        }
                        insertStatement.executeUpdate();
                        insertStatement.close();
                    }
                }

            }

            resultSetAnswer.close();
            statementAnswer.close();
            connection.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        flagAnwser = false;
    }


    public void sendTest(String chatId, long messageId, int TopicId) {
        try {
            flagTest = true;
            String dbUrl = "jdbc:postgresql://localhost:5432/TgBot";
            String dbUser = "postgres";
            String dbPassword = "1793";
            Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);

            String selectQueryTask = "SELECT id, вопрос FROM \"Тест\"";
            Statement statementTask = connection.createStatement();
            ResultSet resultSetTask = statementTask.executeQuery(selectQueryTask);
            boolean taskFound = false;
            while (resultSetTask.next()) {
                int currentTaskId = resultSetTask.getInt("id");
                if (currentTaskId == TopicId) {
                    currentTaskText = resultSetTask.getString("вопрос");
                    taskFound = true;
                    break;
                }
            }
            EditMessageText editMessageText = new EditMessageText();
            editMessageText.setChatId(chatId);
            editMessageText.setMessageId(Math.toIntExact(messageId));

            if (taskFound) {
                editMessageText.setText("Тест(вводите ответы в строчку через пробел, ответ буква) \n" + currentTaskText);
            } else editMessageText.setText("Тест не найден");

            resultSetTask.close();
            statementTask.close();
            connection.close();
            execute(editMessageText);

        } catch (SQLException | TelegramApiException e) {
            e.printStackTrace();
        }
    }

    // Будте вызываться тест по первой теме по трем темам те за весь класс. Чел вводит через пробел ответы и выводится результат.
    private void processAnswerTest(String chatId, String answer, String x, int TopicId) {
        try {
            System.out.println("Метод работает 2");
            String dbUrl = "jdbc:postgresql://localhost:5432/TgBot";
            String dbUser = "postgres";
            String dbPassword = "1793";
            Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
            Statement statement = connection.createStatement();
            String selectQueryAnswer = "SELECT id, ответ FROM Тест";
            PreparedStatement statementAnswer = connection.prepareStatement(selectQueryAnswer);
            ResultSet resultSetAnswer = statementAnswer.executeQuery();
            boolean answerFound = false;
            String expectedAnswer = "";
            while (resultSetAnswer.next()) {
                int currentTaskId = resultSetAnswer.getInt("id");
                if (currentTaskId == TopicId) {
                    expectedAnswer = resultSetAnswer.getString("ответ");
                    answerFound = true;
                    break;
                }
            }
            if (answerFound) {
                int correctCount = 0;
                String[] userAnswers = answer.split(" ");
                String[] expectedAnswers = expectedAnswer.split(" ");
                StringBuilder resultMessage = new StringBuilder();

                for (int i = 0; i < userAnswers.length; i++) {
                    if (i < expectedAnswers.length && Objects.equals(expectedAnswers[i], userAnswers[i])) {
                        correctCount++;
                        resultMessage.append("Вопрос ").append(i + 1).append(": Верно!\n");
                    } else {
                        resultMessage.append("Вопрос ").append(i + 1).append(": Не верно!\n");
                    }
                }

                resultMessage.append("Количество верных ответов: ").append(correctCount);

                String tableName = "user_" + chatId;
                String checkTableQuery = "SELECT to_regclass('" + tableName + "')";
                ResultSet resultSet = statement.executeQuery(checkTableQuery);
                resultSet.next();
                boolean tableExists = (resultSet.getString(1) != null);

                if (tableExists) {
                    System.out.println("Таблица уже существует: " + tableName);
                } else {
                    // Создание таблицы
                    String createTableQuery = "CREATE TABLE " + tableName + " (" +
                            "Название_предмета VARCHAR(255), " +
                            "Название_темы VARCHAR(255), " +
                            "Оценка_задачи INT, " +
                            "Оценка_тест INT" +
                            ")";
                    statement.executeUpdate(createTableQuery);
                    System.out.println("Таблица создана: " + tableName);
                }

                String checkQuery = "SELECT COUNT(*) FROM " + tableName + " WHERE Название_темы = ?";
                PreparedStatement checkStatement = connection.prepareStatement(checkQuery);
                checkStatement.setString(1, String.valueOf(TopicId));
                ResultSet checkResult = checkStatement.executeQuery();
                checkResult.next();
                int count = checkResult.getInt(1);
                checkResult.close();
                checkStatement.close();

                if (count > 0) {
                    // Запись существует, обновление балла
                    String updateQuery = "UPDATE " + tableName + " SET \"Оценка_тест\" = ? WHERE Название_темы = ?";
                    PreparedStatement updateStatement = connection.prepareStatement(updateQuery);
                    updateStatement.setInt(1, correctCount);
                    updateStatement.setString(2, String.valueOf(TopicId));
                    updateStatement.executeUpdate();
                    updateStatement.close();
                } else {
                    // Получение названия темы
                    String topicQuery = "SELECT \"Название темы\" FROM " + x + " WHERE id = ?";
                    PreparedStatement topicStatement = connection.prepareStatement(topicQuery);
                    topicStatement.setInt(1, TopicId);
                    ResultSet topicResult = topicStatement.executeQuery();
                    topicResult.next();
                    String topicName = topicResult.getString("Название темы");
                    topicResult.close();
                    topicStatement.close();

                    // Проверка наличия записи с таким же названием темы
                    String checkTopicQuery = "SELECT COUNT(*) FROM " + tableName + " WHERE Название_темы = ?";
                    PreparedStatement checkTopicStatement = connection.prepareStatement(checkTopicQuery);
                    checkTopicStatement.setString(1, topicName);
                    ResultSet checkTopicResult = checkTopicStatement.executeQuery();
                    checkTopicResult.next();
                    int topicCount = checkTopicResult.getInt(1);
                    checkTopicResult.close();
                    checkTopicStatement.close();

                    if (topicCount > 0) {
                        // Запись существует, обновление балла
                        String updateQuery = "UPDATE " + tableName + " SET \"Оценка_тест\" = ? WHERE Название_темы = ?";
                        PreparedStatement updateStatement = connection.prepareStatement(updateQuery);
                        updateStatement.setInt(1, correctCount);
                        updateStatement.setString(2, topicName);
                        updateStatement.executeUpdate();
                        updateStatement.close();
                    } else {
                        // Добавление данных в таблицу пользователя
                        String insertQuery = "INSERT INTO " + tableName + " (Название_предмета, Название_темы, \"Оценка_тест\") VALUES (?, ?, ?)";
                        PreparedStatement insertStatement = connection.prepareStatement(insertQuery);
                        insertStatement.setString(1, x);
                        insertStatement.setString(2, topicName);
                        insertStatement.setInt(3, correctCount);
                        insertStatement.executeUpdate();
                        insertStatement.close();
                    }
                }

                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                List<List<InlineKeyboardButton>> rowList2 = new ArrayList<>();

                List<InlineKeyboardButton> rowInline2 = new ArrayList<>();
                InlineKeyboardButton backButton4 = new InlineKeyboardButton();
                backButton4.setText("Назад");
                backButton4.setCallbackData("Back_button4");
                rowInline2.add(backButton4);

                rowList2.add(rowInline2);
                inlineKeyboardMarkup.setKeyboard(rowList2);
                message.setReplyMarkup(inlineKeyboardMarkup);

                sendMessage(chatId, resultMessage.toString());
            }

            resultSetAnswer.close();
            statementAnswer.close();
            connection.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        flagTest = false;
    }

}