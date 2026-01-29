import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class PanelVersionHolder extends JPanel {
    JLabel label;
    JButton button;
    private Main.DownloadListener downloadListener;
    public PanelVersionHolder(Main.DownloadListener downloadListener) {}

    public PanelVersionHolder(String versionNum, String Flavor, Main.DownloadListener downloadListener) {
        this.downloadListener = downloadListener;
        this.setLayout(new GridBagLayout());
        this.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        //Empty Space
        gbc.gridx = 0;
        gbc.weightx = 0.1;
        this.add(Box.createHorizontalStrut(80), gbc);
        //Sets up the Label and Button
        label = new JLabel(versionNum + " " + humanReadizeTheFlavor(Flavor) + " (" + Flavor + ")");
        button = new JButton("Download");
        //Stuff for the Label
        label.setHorizontalAlignment(JLabel.CENTER);
        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this.add(label, gbc);
        //Stuff for the Button
        gbc.gridx = 2;
        gbc.weightx = 0.1;
        gbc.fill = GridBagConstraints.NONE;
        this.add(button, gbc);
        this.setBackground(Color.decode("#ADD8E6"));
        this.setBorder(BorderFactory.createEtchedBorder());
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                download(versionNum, Flavor);
            }
        });
    }

    /**
     * Method for unzipping files
     * @param sourceZip The location of where the Zip file was downloaded, such as Downloads
     * @param targetDir The location of where the extracted file is, in this instance, GodotPrograms
     * @throws IOException In the event that this operation fails
     */
    private void unzip(Path sourceZip, Path targetDir) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(sourceZip.toFile()))) {
            ZipEntry entry = zis.getNextEntry();
            while (entry != null) {
                Path newPath = targetDir.resolve(entry.getName());
                if (entry.isDirectory()) {
                    Files.createDirectories(newPath);
                } else {
                    Files.createDirectories(newPath.getParent());
                    Files.copy(zis, newPath, StandardCopyOption.REPLACE_EXISTING);
                }
                entry = zis.getNextEntry();
            }
            zis.closeEntry();
        }
    }

    /**
     * Makes Downloads work, and it does so far.
     * @param versionNum The version number, such as 4.5.1, or 4.6, or 1
     * @param flavor The type of Version it is, such as Stable, Release Candidate, Beta, etc.
     */
    private void download(String versionNum, String flavor) {
        new Thread(() -> {
            try {
                String userHome = System.getProperty("user.home");
                String os =  System.getProperty("os.name").toLowerCase();
                String zipName = "";
                String slug = "", platform = "";
                if (os.contains("win")) {
                    zipName = "Godot_v" + versionNum + "-" + flavor + "_win64.exe.zip";
                    slug =  "win64.exe.zip";
                    platform = "windows.64";
                } else if (os.contains("linux")){
                    int majorVersion = Integer.parseInt(versionNum.split("\\.")[0]);
                    if (majorVersion < 4){
                        slug = "x11.64.zip";
                    } else {
                        slug = "linux.x86_64.zip";
                    }
                    platform = "linux.64";
                    zipName = "Gpdot_v"  + versionNum + "-" + flavor + "_linux.x86_64.zip";
                }

                Path zipPath = Paths.get(userHome, "Downloads", zipName);
                Path extractDir = Paths.get(userHome, "GodotPrograms", zipName.replace(".zip", ""));

                String downloadUrl = "https://downloads.godotengine.org/?version=" + versionNum + "&flavor=" + flavor + "&slug="+slug+"&platform="+platform+"";

                HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.ALWAYS).build();
                HttpRequest request = HttpRequest.newBuilder().uri(URI.create(downloadUrl)).build();

                client.send(request, HttpResponse.BodyHandlers.ofFile(zipPath));
                unzip(zipPath, extractDir);
                Files.deleteIfExists(zipPath);
                SwingUtilities.invokeLater(() -> {
                    if (this.downloadListener != null) {
                        this.downloadListener.onDownloadComplete();
                    }
                    button.setText("Download");
                    button.setEnabled(true);
                    JOptionPane.showMessageDialog(null, "Download Successful");
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "Download Failed");
            }
        }).start();
    }

    /**
     * Essentially Turns the Godot Version into a more Human Readable state
     * @param flavor The type of version, such as Stable, Release Candidate, Beta, etc.
     * @return Returns a human-readable iteration of the flavor provided
     */
    private String humanReadizeTheFlavor(String flavor){
        if (flavor.equalsIgnoreCase("stable")) return "Stable";
        if (flavor.startsWith("rc")) return "Release Candidate " + flavor.substring(2);
        if (flavor.startsWith("alpha")) {
            if (flavor.contains("unofficial")){
                return "Unofficial Alpha";
            }
            return "Alpha " + flavor.substring(5);
        }
        if (flavor.startsWith("beta")) return "Beta " + flavor.substring(4);
        if (flavor.startsWith("dev")) return "Dev Build " + flavor.substring(3);
        return flavor;
    }
}
