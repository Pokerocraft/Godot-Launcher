import javax.swing.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadManager extends SwingWorker<File, Integer> {
    private final String downloadUrl;
    private final String destinationPath;
    private final JProgressBar progressBar;

    public DownloadManager(String url, String dest, JProgressBar bar) {
        this.downloadUrl = url;
        this.destinationPath = dest;
        this.progressBar = bar;
    }
    protected File doInBackground() throws Exception {
        URL url = new URL(downloadUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        int fileSize = connection.getContentLength();
        try (InputStream input = new BufferedInputStream(connection.getInputStream());
        FileOutputStream output = new FileOutputStream(destinationPath)) {
            byte[] data = new byte[1024];
            int count;
            long totalDownloaded = 0;
            while ((count = input.read(data)) != -1) {
                totalDownloaded += count;
                output.write(data, 0, count);
                int progress = (int) ((totalDownloaded * 100) / fileSize);
                publish(progress);
            }
        }
        return new  File(destinationPath);
    }
    @Override
    protected void process(java.util.List<Integer> chunks) {
        for (int progress : chunks) {
            progressBar.setValue(progress);
        }
    }

    @Override
    protected void done() {
        try{
            File downloadedFile = get();
            System.out.println("Download's Done: " + downloadedFile.getName());
            unzip(downloadedFile);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void unzip(File downloadedFile) throws IOException {
        String destDir = null;
    }
}
