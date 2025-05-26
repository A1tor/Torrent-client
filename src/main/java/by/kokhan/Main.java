package by.kokhan;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.BorderLayout;
import java.util.HashMap;

import com.formdev.flatlaf.FlatIntelliJLaf;

public class Main {
    private static HashMap<String, TorrentInfo> torrentInfoMap = new HashMap<>();
    private static JPanel torrentListPanel = new JPanel();
    public static void main(String[] args) throws Exception{
        UIManager.setLookAndFeel(new FlatIntelliJLaf());
        for (var serialisedData : TorrentSerializer.deserialize())
            addNewTorrent(serialisedData.getTorrentFilePath(), serialisedData.getOutputFilePath());
        torrentListPanel.revalidate();
        torrentListPanel.repaint();

        SwingUtilities.invokeLater(() -> {
            // Создание главного окна
            JFrame frame = new JFrame("Торрент клиент");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(700, 400);
            frame.setLayout(new BorderLayout());

            // Верхний тулбар
            JToolBar topToolBar = new JToolBar();
            topToolBar.setFloatable(false);

            JButton topButton = new JButton("Открыть файл");
            topToolBar.add(topButton);
            frame.add(topToolBar, BorderLayout.NORTH);

            topButton.addActionListener(e -> {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setAcceptAllFileFilterUsed(false);
                fileChooser.setFileFilter(new FileNameExtensionFilter("Торрент файлы (*.torrent)", "torrent"));
                int result = fileChooser.showOpenDialog(frame);

                if (result == JFileChooser.APPROVE_OPTION) {
                    var torrentFile = fileChooser.getSelectedFile();
                    fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    result = fileChooser.showOpenDialog(frame);
                    if (result == JFileChooser.APPROVE_OPTION)
                        try {
                            addNewTorrent(torrentFile.getAbsolutePath(), fileChooser.getSelectedFile().getAbsolutePath());
                            TorrentSerializer.serialize(torrentInfoMap.values());
                            torrentListPanel.revalidate();
                            torrentListPanel.repaint();
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                }
            });

            JComboBox<String> scaleUnitСomboBox = new JComboBox<>(TorrentInfo.scaleUnitArray);
            scaleUnitСomboBox.setSelectedIndex(1);
            scaleUnitСomboBox.addActionListener(e -> TorrentInfo.changeScaleUnit(scaleUnitСomboBox.getSelectedIndex()));
            topToolBar.add(scaleUnitСomboBox);

            torrentListPanel.setLayout(new BoxLayout(torrentListPanel, BoxLayout.Y_AXIS));

            JScrollPane scrollPane = new JScrollPane(torrentListPanel);
            frame.add(scrollPane, BorderLayout.CENTER);

            // Нижний тулбар
            var bottomToolBar = new JToolBar();
            bottomToolBar.setFloatable(false);
            frame.add(bottomToolBar, BorderLayout.SOUTH);

            var portLabel = new JLabel("Порт:");
            bottomToolBar.add(portLabel);

            var portField = new JTextField(Integer.toString(TorrentInfo.PORT));
            bottomToolBar.add(portField);

            var maxSpeedLabel = new JLabel("Ограничение скорости загрузки:");
            bottomToolBar.add(maxSpeedLabel);

            var maxSpeedField = new JTextField();
            bottomToolBar.add(maxSpeedField);

            JButton acceptButton = new JButton("Применить");
            acceptButton.addActionListener(event -> {
                try {
                    var value = Integer.parseInt(portField.getText());
                    if (value / 10000 > 0)
                        throw new NumberFormatException("");
                    TorrentInfo.PORT = value;
                } catch (Exception e) {
                    portField.setText(Integer.toString(TorrentInfo.PORT));
                }
                try {
                    TorrentInfo.UPLOAD_SPEED_LIMIT = maxSpeedField.getText().isEmpty() ? 0 : Integer.parseInt(maxSpeedField.getText());
                } catch (Exception e) {
                    if (TorrentInfo.UPLOAD_SPEED_LIMIT > 0)
                        portField.setText(Integer.toString(TorrentInfo.UPLOAD_SPEED_LIMIT));
                }
            });
            bottomToolBar.add(acceptButton);

            // Отображение окна
            frame.setLocationRelativeTo(null); // Центрирование окна
            frame.setVisible(true);
        });
    }

    private static int uniqueId;
    public static void addNewTorrent(String torrentFilePath, String outputFilePath) throws Exception {
        var torrentInfo = new TorrentInfo(torrentFilePath, outputFilePath);
        var torrentName = torrentInfo.torrentName;
        if (torrentName == null) {
            do {
                torrentName = Integer.toString(uniqueId);
                if (torrentInfoMap.putIfAbsent(torrentName, torrentInfo) == null) {
                    break;
                }
                uniqueId++;
            } while (true); 
            torrentInfo.torrentName = torrentName;
        } else
            torrentInfoMap.put(torrentName, torrentInfo);
        torrentListPanel.add(torrentInfo.panel);
    }

    public static void removeTorrent(TorrentInfo torrentInfo) {
        torrentInfoMap.remove(torrentInfo.torrentName);
        torrentListPanel.remove(torrentInfo.panel);
        TorrentSerializer.serialize(torrentInfoMap.values());
        torrentListPanel.revalidate();
        torrentListPanel.repaint();
    }
}