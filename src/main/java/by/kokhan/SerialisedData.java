package by.kokhan;

class SerialisedData {
    private String torrentFilePath;
    private String outputFilePath;

    public SerialisedData(TorrentInfo torrentInfo) {
        this.torrentFilePath = torrentInfo.torrentFilePath;
        this.outputFilePath = torrentInfo.outputFilePath;
    }

    public String getTorrentFilePath() {
        return torrentFilePath;
    }

    public String getOutputFilePath() {
        return outputFilePath;
    }
}
