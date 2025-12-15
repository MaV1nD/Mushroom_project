package org.example.ui;

import org.example.api.ApiClient;
import org.example.model.MessageDTO;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.util.List;

public class ChatPanel extends JPanel {

    private final String myUsername;
    private final ApiClient apiClient = new ApiClient();

    private JList<String> partnersList;
    private DefaultListModel<String> partnersModel;
    private JTextArea chatArea;
    private JTextField inputField;
    private JButton sendButton;

    private String currentPartner = null;

    public ChatPanel(String username) {
        this.myUsername = username;
        setLayout(new BorderLayout());

        // 1. Левая панель (Список контактов)
        partnersModel = new DefaultListModel<>();
        partnersList = new JList<>(partnersModel);
        partnersList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        partnersList.setPreferredSize(new Dimension(150, 0));
        partnersList.addListSelectionListener(this::onPartnerSelected);

        // Кнопка обновления контактов
        JButton refreshPartnersBtn = new JButton("Обновить");
        refreshPartnersBtn.addActionListener(e -> loadPartners());

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(new JScrollPane(partnersList), BorderLayout.CENTER);
        leftPanel.add(refreshPartnersBtn, BorderLayout.SOUTH);
        leftPanel.setBorder(BorderFactory.createTitledBorder("Контакты"));

        add(leftPanel, BorderLayout.WEST);

        // 2. Центральная панель (Чат)
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setFont(new Font("SansSerif", Font.PLAIN, 14));

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(new JScrollPane(chatArea), BorderLayout.CENTER);

        // Панель ввода
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputField = new JTextField();
        inputField.addActionListener(e -> sendMessage()); // Enter отправляет
        sendButton = new JButton("Отправить");
        sendButton.addActionListener(e -> sendMessage());

        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        rightPanel.add(inputPanel, BorderLayout.SOUTH);
        add(rightPanel, BorderLayout.CENTER);

        // Первая загрузка
        loadPartners();
    }

    private void loadPartners() {
        new Thread(() -> {
            try {
                java.util.List<String> partners = apiClient.getChatPartners(myUsername);
                SwingUtilities.invokeLater(() -> {
                    partnersModel.clear();
                    for (String p : partners) partnersModel.addElement(p);
                });
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    private void onPartnerSelected(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            currentPartner = partnersList.getSelectedValue();
            loadChatHistory();
        }
    }

    private void loadChatHistory() {
        if (currentPartner == null) return;

        chatArea.setText("Загрузка переписки с " + currentPartner + "...\n");
        inputField.setEnabled(false);
        sendButton.setEnabled(false);

        new Thread(() -> {
            try {
                List<MessageDTO> history = apiClient.getChatHistory(myUsername, currentPartner);

                SwingUtilities.invokeLater(() -> {
                    chatArea.setText("");
                    for (MessageDTO msg : history) {
                        // --- ГЛАВНОЕ ИЗМЕНЕНИЕ: ВЫБОР ИМЕНИ ---
                        String displayName = msg.getSenderName(); // По дефолту логин

                        // Если есть полное имя - берем его
                        if (msg.getSenderFullName() != null && !msg.getSenderFullName().isEmpty()) {
                            displayName = msg.getSenderFullName();
                        }

                        // Если это я - пишем "Я"
                        if (msg.getSenderName().equals(myUsername)) {
                            displayName = "Я";
                        }
                        // --------------------------------------

                        chatArea.append("[" + displayName + "]: " + msg.getText() + "\n");
                    }
                    inputField.setEnabled(true);
                    sendButton.setEnabled(true);
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> chatArea.setText("Ошибка: " + ex.getMessage()));
            }
        }).start();
    }

    private void sendMessage() {
        String text = inputField.getText().trim();
        if (text.isEmpty() || currentPartner == null) return;

        inputField.setText(""); // Сразу очистим

        new Thread(() -> {
            try {
                apiClient.sendMessage(myUsername, currentPartner, text);
                // После отправки обновляем чат
                loadChatHistory();
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "Ошибка отправки: " + ex.getMessage()));
            }
        }).start();
    }
}