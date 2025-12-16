package org.example.ui;

import org.example.api.ApiClient;
import org.example.model.Ad;
import org.example.model.Category;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Random;

public class MainFrame extends JFrame {
    private final String username;
    private final ApiClient apiClient = new ApiClient();
    private JPanel adsListContainer;
    private final Random random = new Random();

    public MainFrame(String username) {
        this.username = username;
        setTitle("Грибная Лавка | " + username);
        setSize(1000, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Объявления", createAdsPanel());
        tabbedPane.addTab("Распознать гриб (AI)", createAiPanel());
        tabbedPane.addTab("Сообщения", new ChatPanel(username));
        tabbedPane.addTab("Профиль", createProfilePanel());

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createAdsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel tools = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField searchField = new JTextField(15);
        JComboBox<Category> catCombo = new JComboBox<>();
        catCombo.addItem(null);

        new Thread(() -> {
            try {
                List<Category> cats = apiClient.getCategories();
                SwingUtilities.invokeLater(() -> { for(Category c : cats) catCombo.addItem(c); });
            } catch (Exception e) {}
        }).start();

        JButton searchBtn = new JButton("Найти");
        JButton createBtn = new JButton("+ Подать");
        createBtn.setBackground(new Color(60, 179, 113));
        createBtn.setForeground(Color.BLACK);

        tools.add(new JLabel("Поиск:")); tools.add(searchField); tools.add(catCombo); tools.add(searchBtn); tools.add(createBtn);
        panel.add(tools, BorderLayout.NORTH);

        adsListContainer = new JPanel();
        adsListContainer.setLayout(new BoxLayout(adsListContainer, BoxLayout.Y_AXIS));

        JScrollPane scroll = new JScrollPane(adsListContainer);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        panel.add(scroll, BorderLayout.CENTER);

        Runnable doSearch = () -> {
            String q = searchField.getText();
            Category c = (Category) catCombo.getSelectedItem();
            Integer cid = (c != null) ? c.getId() : null;
            loadAds(q, cid);
        };

        searchBtn.addActionListener(e -> doSearch.run());
        createBtn.addActionListener(e -> {
            CreateAdDialog d = new CreateAdDialog(this, username);
            d.setVisible(true);
            if (d.isSuccess()) doSearch.run();
        });

        loadAds(null, null);
        return panel;
    }

    private void loadAds(String query, Integer catId) {
        adsListContainer.removeAll();
        adsListContainer.add(new JLabel("Загрузка..."));
        adsListContainer.revalidate(); adsListContainer.repaint();

        new Thread(() -> {
            try {
                List<Ad> ads = apiClient.getAds(query, catId);
                SwingUtilities.invokeLater(() -> {
                    adsListContainer.removeAll();
                    if (ads.isEmpty()) {
                        adsListContainer.add(new JLabel("Объявлений не найдено"));
                    } else {
                        for (Ad ad : ads) {
                            adsListContainer.add(createAdCard(ad));
                            adsListContainer.add(Box.createVerticalStrut(15));
                        }
                    }
                    adsListContainer.revalidate(); adsListContainer.repaint();
                });
            } catch (Exception e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    adsListContainer.removeAll();
                    adsListContainer.add(new JLabel("Ошибка связи с сервером"));
                    adsListContainer.revalidate(); adsListContainer.repaint();
                });
            }
        }).start();
    }

    private JPanel createAdCard(Ad ad) {
        JPanel card = new JPanel(new BorderLayout(15, 0));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        card.setBackground(getRandomPastelColor());

        // Левая часть (Картинка)
        JLabel imgLabel = new JLabel("Нет фото", SwingConstants.CENTER);
        imgLabel.setPreferredSize(new Dimension(180, 140));
        imgLabel.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        imgLabel.setOpaque(true);
        imgLabel.setBackground(Color.WHITE);

        if (ad.getImagePath() != null && !ad.getImagePath().isEmpty()) {
            new Thread(() -> {
                try {
                    URL url = new URL("http://localhost:8080/api/ads/image/" + ad.getImagePath());
                    BufferedImage img = ImageIO.read(url);
                    if (img != null) {
                        Image scaled = img.getScaledInstance(180, 140, Image.SCALE_SMOOTH);
                        ImageIcon icon = new ImageIcon(scaled);
                        SwingUtilities.invokeLater(() -> { imgLabel.setText(""); imgLabel.setIcon(icon); });
                    }
                } catch (Exception e) {}
            }).start();
        }
        card.add(imgLabel, BorderLayout.WEST);

        // Правая часть (Текст)
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel title = new JLabel(ad.getTitle());
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setForeground(Color.BLACK);
        textPanel.add(title);

        JLabel price = new JLabel(ad.getPrice() + " ₽");
        price.setFont(new Font("SansSerif", Font.BOLD, 16));
        price.setForeground(new Color(0, 100, 0));
        textPanel.add(price);

        textPanel.add(Box.createVerticalStrut(10));

        JTextArea desc = new JTextArea(ad.getDescription());
        desc.setLineWrap(true); desc.setWrapStyleWord(true); desc.setEditable(false);
        desc.setOpaque(false); desc.setFont(new Font("SansSerif", Font.PLAIN, 14));
        textPanel.add(desc);

        textPanel.add(Box.createVerticalStrut(10));
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        btnPanel.setOpaque(false);

        // --- ЛОГИКА ОТОБРАЖЕНИЯ ИМЕНИ ---
        // Если есть полное имя - показываем его, иначе логин
        String displayName = (ad.getSellerFullName() != null && !ad.getSellerFullName().isEmpty())
                ? ad.getSellerFullName()
                : ad.getUsername();

        if (ad.getUsername() != null && ad.getUsername().equals(this.username)) {
            JButton delBtn = new JButton("Удалить объявление");
            delBtn.setBackground(new Color(220, 20, 60));
            delBtn.setForeground(Color.BLACK);
            delBtn.addActionListener(e -> {
                if (JOptionPane.showConfirmDialog(this, "Удалить?", "Confirm", JOptionPane.YES_NO_OPTION) == 0) {
                    new Thread(() -> {
                        try { apiClient.deleteAd(ad.getId(), username); SwingUtilities.invokeLater(() -> loadAds(null, null)); }
                        catch (Exception ex) { ex.printStackTrace(); }
                    }).start();
                }
            });
            btnPanel.add(delBtn);
        } else {
            JButton infoBtn = new JButton("Профиль: " + displayName); // Используем Имя
            infoBtn.setBackground(Color.LIGHT_GRAY);
            infoBtn.addActionListener(e -> showSellerInfo(ad.getUsername()));
            btnPanel.add(infoBtn);

            btnPanel.add(Box.createHorizontalStrut(10));

            JButton chatBtn = new JButton("Написать");
            chatBtn.setBackground(new Color(70, 130, 180));
            chatBtn.setForeground(Color.BLACK);
            chatBtn.addActionListener(e -> {
                String msg = JOptionPane.showInputDialog(this, "Сообщение для " + displayName + ":");
                if(msg!=null && !msg.isEmpty())
                    new Thread(() -> { try { apiClient.sendMessage(username, ad.getUsername(), msg); } catch(Exception ex){} }).start();
            });
            btnPanel.add(chatBtn);
        }

        textPanel.add(btnPanel);
        card.add(textPanel, BorderLayout.CENTER);
        return card;
    }

    private Color getRandomPastelColor() {
        int r = (int)(200 + Math.random() * 55);
        int g = (int)(200 + Math.random() * 55);
        int b = (int)(200 + Math.random() * 55);
        return new Color(r, g, b);
    }

    // --- ПАНЕЛЬ AI (БЕЗ ВЕРОЯТНОСТИ) ---
    private JPanel createAiPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JLabel imageLabel = new JLabel("Выберите фото", SwingConstants.CENTER);
        imageLabel.setPreferredSize(new Dimension(300, 300));
        imageLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        JEditorPane resultArea = new JEditorPane();
        resultArea.setContentType("text/html");
        resultArea.setEditable(false);
        resultArea.setText("<html><body style='font-family:sans-serif; padding:10px;'><h3>Результат анализа появится здесь</h3></body></html>");

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, imageLabel, new JScrollPane(resultArea));
        split.setDividerLocation(350);
        panel.add(split, BorderLayout.CENTER);

        JButton uploadBtn = new JButton("Загрузить фото гриба");
        uploadBtn.setFont(new Font("SansSerif", Font.BOLD, 16));
        uploadBtn.setBackground(new Color(70, 130, 180));
        uploadBtn.setForeground(Color.BLACK);

        final File[] selectedFile = {null};

        uploadBtn.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                selectedFile[0] = fc.getSelectedFile();
                try {
                    BufferedImage img = ImageIO.read(selectedFile[0]);
                    Image scaled = img.getScaledInstance(300, 300, Image.SCALE_SMOOTH);
                    imageLabel.setIcon(new ImageIcon(scaled));
                    imageLabel.setText("");
                } catch (Exception ex) {}

                resultArea.setText("<html><body style='font-family:sans-serif; padding:10px;'><h2>Анализирую... 🧠</h2></body></html>");

                new Thread(() -> {
                    try {
                        org.example.model.RecognitionResult res = apiClient.recognizeImage(selectedFile[0]);

                        SwingUtilities.invokeLater(() -> {
                            String color = (res.getEdible() != null && res.getEdible()) ? "green" : "red";
                            String status = (res.getEdible() != null && res.getEdible()) ? "СЪЕДОБЕН ✅" : "ЯДОВИТ / НЕ СЪЕДОБЕН ☠️";
                            if (!res.isFound()) { color = "gray"; status = "Нет данных в справочнике"; }

                            String html = String.format("""
                                <html><body style='font-family:sans-serif; padding:10px;'>
                                    <h1 style='color:%s;'>%s</h1>
                                    <h3>%s</h3>
                                    <hr>
                                    <p><b>Описание:</b><br>%s</p>
                                    <p><b>Кулинария:</b><br>%s</p>
                                </body></html>
                                """,
                                    color,
                                    res.getDisplayName(),
                                    status,
                                    res.getDescription(),
                                    res.getCookingTips()
                            );
                            resultArea.setText(html);
                            resultArea.setCaretPosition(0);
                        });
                    } catch (Exception ex) {
                        SwingUtilities.invokeLater(() -> resultArea.setText("Ошибка: " + ex.getMessage()));
                        ex.printStackTrace();
                    }
                }).start();
            }
        });
        panel.add(uploadBtn, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createProfilePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("Редактирование профиля (" + username + ")");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);

        JTextField nameField = new JTextField(20);
        JTextField phoneField = new JTextField(20);
        JTextField locField = new JTextField(20);

        addFormRow(panel, gbc, 1, "Имя Фамилия:", nameField);
        addFormRow(panel, gbc, 2, "Телефон:", phoneField);
        addFormRow(panel, gbc, 3, "Город/Место:", locField);

        JButton saveBtn = new JButton("Сохранить изменения");
        saveBtn.setBackground(new Color(60, 179, 113));
        saveBtn.setForeground(Color.BLACK);
        gbc.gridy = 4; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.NONE;
        panel.add(saveBtn, gbc);

        JButton logoutBtn = new JButton("Выйти из аккаунта");
        logoutBtn.setBackground(new Color(220, 20, 60));
        logoutBtn.setForeground(Color.RED);
        gbc.gridy = 5;
        panel.add(logoutBtn, gbc);

        new Thread(() -> {
            try {
                org.example.model.User me = apiClient.getUserProfile(username);
                if (me != null) {
                    SwingUtilities.invokeLater(() -> {
                        nameField.setText(me.getFullName());
                        phoneField.setText(me.getPhoneNumber());
                        locField.setText(me.getLocation());
                    });
                }
            } catch (Exception e) {}
        }).start();

        saveBtn.addActionListener(e -> {
            org.example.model.User update = new org.example.model.User();
            update.setFullName(nameField.getText());
            update.setPhoneNumber(phoneField.getText());
            update.setLocation(locField.getText());

            new Thread(() -> {
                try {
                    apiClient.updateProfile(username, update);
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "Профиль сохранен!"));
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "Ошибка: " + ex.getMessage()));
                }
            }).start();
        });

        logoutBtn.addActionListener(e -> { dispose(); });
        return panel;
    }

    private void addFormRow(JPanel p, GridBagConstraints gbc, int row, String label, JTextField field) {
        gbc.gridy = row; gbc.gridwidth = 1; gbc.gridx = 0;
        p.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        p.add(field, gbc);
    }

    private void showSellerInfo(String sellerName) {
        new Thread(() -> {
            try {
                org.example.model.User seller = apiClient.getUserProfile(sellerName);
                SwingUtilities.invokeLater(() -> {
                    if (seller == null) return;
                    String info = String.format("""
                        Пользователь: %s
                        Имя: %s
                        Телефон: %s
                        Город: %s
                        """,
                            seller.getUsername(),
                            seller.getFullName() != null ? seller.getFullName() : "Не указано",
                            seller.getPhoneNumber() != null ? seller.getPhoneNumber() : "Не указано",
                            seller.getLocation() != null ? seller.getLocation() : "Не указано"
                    );
                    JOptionPane.showMessageDialog(this, info, "Информация о продавце", JOptionPane.INFORMATION_MESSAGE);
                });
            } catch (Exception e) {}
        }).start();
    }
}