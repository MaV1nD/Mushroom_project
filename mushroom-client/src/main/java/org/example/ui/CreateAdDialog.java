package org.example.ui;

import org.example.api.ApiClient;
import org.example.model.Category;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.math.BigDecimal;
import java.util.List;

public class CreateAdDialog extends JDialog {
    private JTextField titleField;
    private JTextArea descArea;
    private JTextField priceField;
    private JComboBox<Category> categoryBox;
    private JLabel fileLabel;
    private File selectedFile;
    private boolean success = false;
    private final ApiClient apiClient = new ApiClient();
    private final String username;

    public CreateAdDialog(Frame owner, String username) {
        super(owner, "Новое объявление", true);
        this.username = username;
        setSize(400, 500);
        setLocationRelativeTo(owner);
        setLayout(new GridBagLayout());
        initUI();
    }

    private void initUI() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0; gbc.gridy = 0;

        add(new JLabel("Название:"), gbc);
        gbc.gridy++;
        titleField = new JTextField(20);
        add(titleField, gbc);

        gbc.gridy++;
        add(new JLabel("Категория:"), gbc);
        gbc.gridy++;
        categoryBox = new JComboBox<>();
        add(categoryBox, gbc);

        // Загрузка категорий
        new Thread(() -> {
            try {
                List<Category> cats = apiClient.getCategories();
                SwingUtilities.invokeLater(() -> {
                    for (Category c : cats) categoryBox.addItem(c);
                });
            } catch (Exception e) { e.printStackTrace(); }
        }).start();

        gbc.gridy++;
        add(new JLabel("Описание:"), gbc);
        gbc.gridy++;
        descArea = new JTextArea(5, 20);
        add(new JScrollPane(descArea), gbc);

        gbc.gridy++;
        add(new JLabel("Цена:"), gbc);
        gbc.gridy++;
        priceField = new JTextField();
        add(priceField, gbc);

        gbc.gridy++;
        JButton fileBtn = new JButton("Прикрепить фото");
        fileLabel = new JLabel("Файл не выбран");
        fileBtn.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                selectedFile = fc.getSelectedFile();
                fileLabel.setText(selectedFile.getName());
            }
        });
        add(fileBtn, gbc);
        gbc.gridy++;
        add(fileLabel, gbc);

        gbc.gridy++;
        JButton saveBtn = new JButton("Создать");
        saveBtn.addActionListener(e -> onSave());
        add(saveBtn, gbc);
    }

    private void onSave() {
        try {
            String title = titleField.getText();
            String desc = descArea.getText();
            String priceStr = priceField.getText();
            Category cat = (Category) categoryBox.getSelectedItem();

            if (title.isEmpty() || priceStr.isEmpty() || cat == null) {
                JOptionPane.showMessageDialog(this, "Заполните все поля");
                return;
            }

            BigDecimal priceVal = new BigDecimal(priceStr);

            apiClient.createAd(username, cat, title, desc, priceVal, selectedFile);

            success = true;
            setVisible(false);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Ошибка: " + e.getMessage());
        }
    }

    public boolean isSuccess() { return success; }
}