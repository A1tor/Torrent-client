package by.kokhan;

import org.bitlet.wetorrent.Metafile;
import org.bitlet.wetorrent.Torrent;
import org.bitlet.wetorrent.disk.TorrentDisk;
import org.bitlet.wetorrent.peer.IncomingPeerListener;

public class TBuilder {
    private volatile boolean stopDownload;
    private final TorrentInfo torrentInfo;
    private final int sleepTime;

    public TBuilder (TorrentInfo torrentInfo){
        sleepTime = TorrentInfo.SLEEP_TIME;
        this.torrentInfo = torrentInfo;
    };

    public void downloadFile(Metafile metafile, TorrentDisk tdisk) throws Exception {
        tdisk.init();
        
        IncomingPeerListener peerListener = new IncomingPeerListener(TorrentInfo.PORT);
        peerListener.start();

        Torrent torrent = new Torrent(metafile, tdisk, peerListener);
        if (TorrentInfo.UPLOAD_SPEED_LIMIT > 0)
            torrent.getUploadBandwidthLimiter().setMaximumRate(TorrentInfo.UPLOAD_SPEED_LIMIT);
        torrent.startDownload();

        while (!torrent.isCompleted()) {
            if (stopDownload)
                break;

            try {
                Thread.sleep(sleepTime);
            } catch(InterruptedException ignored) {
                break;
            }

            torrent.tick();
            torrentInfo.updateData(torrent);
        }

        stopDownload = false;
        torrent.interrupt();
        peerListener.interrupt();
        torrentInfo.playButtonClick();
    }

    public boolean breakDownload() {
        if (stopDownload)
            return false;
        stopDownload = true;
        return true;
    }
}
