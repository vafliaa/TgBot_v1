package org.example.command;

public interface ExecuteCommand {
    String name();
    void execute(String chatId);
}
