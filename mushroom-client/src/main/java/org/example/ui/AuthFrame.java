package org.example.ui;

import org.example.api.ApiClient;

import javax.swing.*;
import java.awt.*;

public class AuthFrame extends JFrame {

    private JTextField userField;
    private JPasswordField passField;
    private JButton registerButton;
    private ApiClient apiClient;

    private JButton loginButton;

    public AuthFrame() {
        apiClient = new ApiClient();
        setTitle("Вход в систему");
        setSize(400, 250);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Поля ввода
        gbc.gridx = 0; gbc.gridy = 0; add(new JLabel("Логин:"), gbc);
        userField = new JTextField(15);
        gbc.gridx = 1; add(userField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; add(new JLabel("Пароль:"), gbc);
        passField = new JPasswordField(15);
        gbc.gridx = 1; add(passField, gbc);

        // Панель для кнопок
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 0)); // 1 ряд, 2 колонки

        loginButton = new JButton("Войти");
        registerButton = new JButton("Регистрация");

        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);

        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        add(buttonPanel, gbc);

        // --- ЛОГИКА ---

        // Кнопка ВХОД
        loginButton.addActionListener(e -> authenticate(true)); // true = login

        // Кнопка РЕГИСТРАЦИЯ
        registerButton.addActionListener(e -> authenticate(false)); // false = register
    }

    private void authenticate(boolean isLogin) {
        String user = userField.getText();
        String pass = new String(passField.getPassword());

        if (user.isEmpty() || pass.isEmpty()) return;

        loginButton.setEnabled(false);
        registerButton.setEnabled(false);

        new Thread(() -> {
            try {
                if (isLogin) {
                    apiClient.login(user, pass); // Пробуем войти
                } else {
                    apiClient.register(user, pass); // Пробуем создать
                }

                SwingUtilities.invokeLater(() -> {
                    this.dispose(); // Закрываем окно входа
                    new MainFrame(user).setVisible(true); // Открываем программу
                });

            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "Ошибка: " + ex.getMessage());
                    loginButton.setEnabled(true);
                    registerButton.setEnabled(true);
                });
            }
        }).start();
    }
}