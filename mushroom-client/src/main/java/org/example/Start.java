package org.example;

import com.formdev.flatlaf.FlatDarkLaf;
import org.example.ui.AuthFrame;

import javax.swing.*;

public class Start {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (Exception ex) {
            System.err.println("Не удалось установить тему FlatLaf. Будет использован стандартный вид.");
            ex.printStackTrace();
        }

        // 2. Запускаем интерфейс в специальном потоке Swing (Event Dispatch Thread)
        // Это обязательно для любых приложений на Swing, чтобы интерфейс не зависал
        SwingUtilities.invokeLater(() -> {
            // Создаем окно авторизации
            AuthFrame authFrame = new AuthFrame();

            // Делаем его видимым
            authFrame.setVisible(true);
        });
    }
}