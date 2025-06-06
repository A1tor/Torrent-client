package by.kokhan;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SpringLayout;
import javax.swing.border.Border;

import org.bitlet.wetorrent.Metafile;
import org.bitlet.wetorrent.Torrent;
import org.bitlet.wetorrent.disk.PlainFileSystemTorrentDisk;
import org.bitlet.wetorrent.disk.TorrentDisk;

public class TorrentInfo {
    private static final int HEIGHT = 20;
    private static final Border BORDER = BorderFactory.createEmptyBorder(0, 5, 0, 5);
    private static final ImageIcon PLAY_ICON = new ImageIcon(new ImageIcon(".\\src\\main\\resources\\play.png").getImage().getScaledInstance(HEIGHT, HEIGHT, Image.SCALE_SMOOTH));
    private static final ImageIcon PAUSE_ICON = new ImageIcon(new ImageIcon(".\\src\\main\\resources\\pause.png").getImage().getScaledInstance(HEIGHT, HEIGHT, Image.SCALE_SMOOTH));
    private static final ImageIcon CROSS_ICON = new ImageIcon(new ImageIcon(".\\src\\main\\resources\\cross.png").getImage().getScaledInstance(HEIGHT, HEIGHT, Image.SCALE_SMOOTH));
    public static final int SLEEP_TIME = 3000;
    public static final String[] scaleUnitArray = {"Byte", "Kb", "Mb", "Gb"};
    private static long scale = 1048576;
    private static String scaleValue = scaleUnitArray[2];
    public static int PORT = 6881;
    public static int UPLOAD_SPEED_LIMIT;
    private final TBuilder tbuilder;
    public final String torrentFilePath;
    public final String outputFilePath;
    private long lastDownloaded;
    private boolean wasStarted;
    private JLabel downloadedCountLabel;
    private JLabel downloadSpeedLabel;
    private JLabel seedToPeerLabel;
    private JLabel sizeLabel;
    private JButton playButton;
    private JProgressBar progressBar;
    public String torrentName;
    public BufferedInputStream inputStream;
    public FileInputStream fileInputStream;
    public Metafile metafile;
    public TorrentDisk tdisk;
    public JPanel panel;

    public TorrentInfo(String torrentFilePath, String outputFilePath) throws Exception{
        this.torrentFilePath = torrentFilePath;
        this.outputFilePath = outputFilePath;
        panel = new JPanel(new BorderLayout());
        var layout = new SpringLayout();
        panel.setLayout(layout);
        panel.setPreferredSize(new Dimension(0, HEIGHT));
        panel.setMinimumSize(new Dimension(0, HEIGHT));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, HEIGHT));
        
        var deleteButton = new JButton(CROSS_ICON);
        deleteButton.setBorder(BORDER);
        deleteButton.addActionListener(event -> Main.removeTorrent(this));
        deleteButton.setContentAreaFilled(false);
        deleteButton.setToolTipText("Удалить");
        panel.add(deleteButton);
        layout.putConstraint(SpringLayout.EAST, deleteButton, -10, SpringLayout.EAST, panel);

        try {
            fileInputStream = new FileInputStream(torrentFilePath);
            inputStream = new BufferedInputStream(fileInputStream);
            metafile = new Metafile(inputStream);
        } catch (Exception e) {
            var errorLabel = new JLabel(String.format("Файл '%s' не найден", torrentFilePath));
            errorLabel.setBorder(BORDER);
            errorLabel.setBackground(Color.RED);
            errorLabel.setForeground(Color.WHITE);
            errorLabel.setOpaque(true);
            panel.add(errorLabel);
            tbuilder = null;
            return;
        }
        
        tdisk = new PlainFileSystemTorrentDisk(metafile, new File(outputFilePath));
        tdisk.init();
        tbuilder = new TBuilder(this);

        torrentName = metafile.getName();

        playButton = new JButton(PLAY_ICON);
        playButton.setBorder(BORDER);
        playButton.setPreferredSize(new Dimension(HEIGHT, HEIGHT));
        playButton.setContentAreaFilled(false);  // Прозрачный фон
        playButton.setToolTipText("Запустить/остановить загрузку");
        playButton.addActionListener(event -> playButtonClick());
        panel.add(playButton);

        var nameLabel = new JLabel(torrentName.length() > 50 ? torrentName.substring(0, 50) : torrentName);
        nameLabel.setBorder(BORDER);
        panel.add(nameLabel);

        sizeLabel = new JLabel(String.format("%d %s", getMeasuredValue(metafile.getLength()), scaleValue));
        sizeLabel.setBorder(BORDER);
        panel.add(sizeLabel);

        downloadedCountLabel = new JLabel();
        downloadedCountLabel.setBorder(BORDER);
        panel.add(downloadedCountLabel);

        progressBar = new JProgressBar(0, 1000);
        progressBar.setValue(0);
        progressBar.setForeground(Color.GREEN);
        progressBar.setOpaque(false);
        progressBar.setPreferredSize(new Dimension(150, HEIGHT));
        panel.add(progressBar);

        downloadSpeedLabel = new JLabel();
        downloadSpeedLabel.setBorder(BORDER);
        panel.add(downloadSpeedLabel);

        seedToPeerLabel = new JLabel();
        seedToPeerLabel.setBorder(BORDER);
        panel.add(seedToPeerLabel);

        layout.putConstraint(SpringLayout.WEST, playButton, 10, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.WEST, nameLabel, 40, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.WEST, sizeLabel, 280, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.WEST, progressBar, 350, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, downloadedCountLabel, 0, SpringLayout.HORIZONTAL_CENTER, progressBar);
        layout.putConstraint(SpringLayout.WEST, downloadSpeedLabel, 500, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.EAST, seedToPeerLabel, -100, SpringLayout.EAST, panel);
    }

    public void updateData(Torrent torrent){
        try {
            var totalSize = getMeasuredValue(metafile.getLength());
            sizeLabel.setText(String.format("%d %s", totalSize, scaleValue));
            var downloaded = getMeasuredValue(tdisk.getCompleted());
            progressBar.setValue((int) (1000 * (downloaded / (double) totalSize)));
            downloadedCountLabel.setText(String.format("%d %s", downloaded, scaleValue));
            downloadSpeedLabel.setText(String.format("%.2f %s/s", (float) (downloaded - lastDownloaded) / (SLEEP_TIME / 1000), scaleValue));
            lastDownloaded = downloaded;
            var peersManager = torrent.getPeersManager();
            var peerCount = peersManager.getActivePeersNumber();
            var seedCount = peersManager.getSeedersNumber();
            seedToPeerLabel.setText(String.format("%d / %d", seedCount, peerCount));
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public void playButtonClick(){
       if (wasStarted) {
            playButton.setIcon(PLAY_ICON);
            tbuilder.breakDownload();
        } else {
            playButton.setIcon(PAUSE_ICON);
            new Thread(() -> {
                try {
                    tbuilder.downloadFile(metafile, tdisk);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
        wasStarted = !wasStarted; 
    }


    public static void changeScaleUnit(int scalePow){
        scale = (long) Math.pow(1024, scalePow);
        scaleValue = scaleUnitArray[scalePow];
    }

    private long getMeasuredValue(long value){
        return value / scale;
    }
}
