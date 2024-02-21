package org.example.command;

import org.example.bl.DatabaseUtils;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.sql.*;

public class CalculateAverageGrade implements ExecuteCommand {

    private final SendMessage message;
    private final AbsSender sender;

    public CalculateAverageGrade(SendMessage message, AbsSender sender) {
        this.message = message;
        this.sender = sender;
    }


    @Override
    public String name() {
        return "Успеваемость";
    }

    @Override
    public void execute(String chatId) {
        try {
            String tableName = "user_" + chatId;
            String selectQuery = "SELECT Название_предмета, Название_темы FROM " + tableName;
            Connection connection = DatabaseUtils.getConnection();
            Statement statement = connection.createStatement();
            ResultSet subjectResult = statement.executeQuery(selectQuery);

            int previousClassValue = -1; // Значение предыдущего класса
            int sum = 0; // Сумма оценок
            int count = 0; // Количество оценок
            String tableName2 = "";
            while (subjectResult.next()) {
                tableName2 = subjectResult.getString("Название_предмета");
                String taskName = subjectResult.getString("Название_темы");

                String selectTaskQuery = "SELECT Класс, \"Название темы\" FROM " + tableName2;
                Statement taskStatement = connection.createStatement();
                ResultSet taskResult = taskStatement.executeQuery(selectTaskQuery);

                while (taskResult.next()) {
                    int classValue = Integer.parseInt(taskResult.getString("Класс"));
                    String taskNameFromSubject = taskResult.getString("Название темы");

                    if (taskName.equals(taskNameFromSubject)) {
                        if (classValue != previousClassValue) {
                            if (count > 0) {
                                int average = sum / count;
                                StringBuilder messageToSend = new StringBuilder("Успеваемость по классам:\n\n");
                                messageToSend.append("Класс: ").append(previousClassValue).append("\n");
                                messageToSend.append("Предмет: ").append(tableName2).append("\n");
                                messageToSend.append("Средняя оценка: ").append(average).append("\n\n");
                                sendMessage(chatId, String.valueOf(messageToSend));
                            }

                            previousClassValue = classValue;
                            sum = 0;
                            count = 0;
                        }

                        String selectAverageGradeQuery = "SELECT Оценка_задачи, Оценка_тест FROM " + tableName;
                        Statement gradeStatement = connection.createStatement();
                        ResultSet gradeResult = gradeStatement.executeQuery(selectAverageGradeQuery);

                        while (gradeResult.next()) {
                            int task = gradeResult.getInt("Оценка_задачи");
                            int test = gradeResult.getInt("Оценка_тест");
                            sum += (task + test);
                            count++;
                        }

                        gradeResult.close();
                        gradeStatement.close();
                    }
                }

                taskResult.close();
                taskStatement.close();
            }

            if (count > 0) {
                int average = sum / count;
                StringBuilder messageToSend = new StringBuilder("Успеваемость по классам:\n\n");
                messageToSend.append("Класс: ").append(previousClassValue).append("\n");
                messageToSend.append("Предмет: ").append(tableName2).append("\n");
                messageToSend.append("Средняя оценка: ").append(average).append("\n\n");
                sendMessage(chatId, String.valueOf(messageToSend));
            }

            subjectResult.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage(String chatId, String textToSend) {
        message.setChatId(chatId);
        message.setText(textToSend);

        try {
            sender.execute(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
