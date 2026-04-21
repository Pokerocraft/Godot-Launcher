import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The Primary class, unsurprisingly, that handles all the Java Swing Stuff, and also all the versioning.
 */
public class Main {
    static String user = System.getProperty("user.name");
    static String fileLocation = "";
    static List<GodotVersionInfo> versions = new ArrayList<>();
    static String latestVersion = "4.6.2 (Stable)";
    static String latest3Version = "3.6.2 (Stable)";
    static String latestDevVersion = "4.7 (Dev5)";
    static String latest3DevVersion = "3.7 (Dev1)";
    static String latest2Version = "2.1.6 (Stable)";
    static String latest1Version = "1.1 (Stable)";
    static String oldestVersion = "1.0 (Stable)";
    static String slashes = System.getProperty("file.separator");
    private static JComboBox<String> comboBox;
    private static JCheckBox checkBox;
    private static JPanel panel;
    private static ItemListener comboBoxListener;
    private static ActionListener comboBoxAction;
    private static JFrame downloadFrame;
    static JButton button = new JButton("Open this instance of Godot");
    public Main() {
        boolean skip = false;
        try{
            String output = "";
            File askingFile = new File("src/main/java/dontAskAgain.txt");
            int i = 0;
            Scanner myReader = new Scanner(askingFile);
            String data;
            for (myReader = new Scanner(askingFile); myReader.hasNextLine(); output=output.concat(data+"\n")){
                data = "line output: " + myReader.nextLine();
            }
            myReader.close();
            if (output.contains("true")){
                skip = true;
            } else {
                skip = false;
            }
        } catch (FileNotFoundException e){
            System.err.println("An error occurred.");
            e.printStackTrace();
        }
        List<Boolean> hasMono = getMono();
        //Frame and Panel
        panel = new JPanel();
        JFrame frame = new JFrame("Godot Launcher");
        //Versions and If the user wants to use the .NET version
        comboBox = new JComboBox();
        checkBox = new JCheckBox(".NET Version");
        //Add all the versions. And also disables the CheckBox
        String userHome =  System.getProperty("user.home");
        String directoryPath = userHome + slashes + "GodotPrograms";
        Path programsPath = Paths.get(directoryPath);
        try{
            if(Files.exists(programsPath)){

            } else {
                Files.createDirectories(programsPath);
                JOptionPane.showMessageDialog(frame, "A Directory called GodotPrograms should be at: " + userHome + ". Go ahead and put your Godot Installations there, elsewise the program will give you an error and then quit.");
            }
        } catch (IOException e){
            System.err.println("Failed to make directory: " + e.getMessage());
        }
        if (!skip) {
            JOptionPane.showMessageDialog(frame, "<html>If you don't have any standard installations, aka, installations that don't have mono in the filename,<br>this project will think that you have no Godot Installations, and as such, you'll have to download a Standard Edition engine</html>","Info", JOptionPane.INFORMATION_MESSAGE); //Trust me, I don't want to make two more matchers to be able to use the Jenova Framework, renaming a file is easier than this stuff.
            Scanner myScanner = new  Scanner(System.in);
            System.out.println("Do you wish to skip the info box?");
            String result = myScanner.nextLine();
            if (result.equalsIgnoreCase("yes")){
                writeToFile(true);
            }
        }
        System.out.println("\n\n\nPrograms Path: " + programsPath.toString());
        String osName = System.getProperty("os.name");
        System.out.println("OS Name: " + osName);
        populateComboBox(comboBox);
        for (int i=0; i < comboBox.getItemCount(); i++){
            String itemText = comboBox.getItemAt(i);
            if (itemText != null && itemText.contains(latestVersion)){
                comboBox.setSelectedIndex(i);
                break;
            }
        }
        checkBox.setSelected(false);
        try{
            checkBox.setEnabled(hasMono.getFirst() == true);
        } catch (NoSuchElementException e){
            if (!versions.isEmpty()){
                JOptionPane.showMessageDialog(frame, "You likely only have .NET installations, which requires the Stable version in order to run..", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        if (!versions.isEmpty()){
            fileLocation = directoryPath + slashes + versions.getFirst().getOriginalFilename();
        }
        if (versions.isEmpty()) {
            JOptionPane.showMessageDialog(frame,"Could not find any Standard Installations ", "Error", JOptionPane.ERROR_MESSAGE);
            comboBox.removeAllItems();
            button.setEnabled(false);
            checkBox.setEnabled(false);
            comboBox.setEnabled(false);
        }
        JButton downloads = new  JButton("Download a Specific Godot Version");
        comboBoxListener = e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                String selected = (String) comboBox.getSelectedItem();

                if (selected == null || selected.isEmpty()) return;

                Path selectedPath = Paths.get(System.getProperty("user.home"),"GodotPrograms",  selected);
                boolean hasDotNet = Files.exists(selectedPath.resolve("GodotSharp"));
                checkBox.setEnabled(hasDotNet);
                if (!hasDotNet) {
                    checkBox.setSelected(false);
                }
            }
        };
        comboBox.addItemListener(comboBoxListener);
        comboBoxAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int index = comboBox.getSelectedIndex();
                if (index < 0|| index >= versions.size()) {
                    return;
                }
                Object selected = comboBox.getSelectedItem();
                if (selected == null) return;
                String selectedVersion =  (String) comboBox.getSelectedItem();
                checkBox.setEnabled(selectedVersion != null && hasMono.get(comboBox.getSelectedIndex()));
                for (int i = 0; i < versions.size(); i++) {
                    if (i == comboBox.getSelectedIndex()) {
                        fileLocation = directoryPath + slashes + versions.get(i).getOriginalFilename();
                        if (isMonoInstalled(comboBox.getSelectedIndex())) {
                            checkBox.setEnabled(true);
                        } else {
                            checkBox.setEnabled(false);
                            checkBox.setSelected(false);
                        }
                    }
                }
            }
        };

        comboBox.addActionListener(comboBoxAction);

        checkBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (checkBox.isSelected()) {
                    String selectedVersion =  (String) comboBox.getSelectedItem() + " .NET Version";
                    String originalVersion = directoryPath + slashes + versions.get(comboBox.getSelectedIndex()).getOriginalFilename();
                    String toInsert = "_mono";
                    int lastUnderscoreIndex = originalVersion.lastIndexOf("_");
                    if (lastUnderscoreIndex != -1) {
                        String prefix = originalVersion.substring(0, lastUnderscoreIndex);
                        int extensionIndex = originalVersion.lastIndexOf(".");
                        String suffix = originalVersion.substring(lastUnderscoreIndex, extensionIndex);
                        String newVersion = prefix + toInsert + suffix;
                        fileLocation = newVersion;
                    }
                } else {
                    String selectedVersion = (String) comboBox.getSelectedItem();
                    fileLocation = directoryPath + slashes + versions.get(comboBox.getSelectedIndex()).getOriginalFilename();
                }

            }
        });

        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int index = comboBox.getSelectedIndex();
                if (index == -1 || index >= versions.size()) return;
                try {
                    String folderName = versions.get(index).getOriginalFilename();
                    boolean wantMono = checkBox.isSelected();
                    String fullPath= findAppInFolder(folderName, wantMono);
                    if (fullPath.isEmpty()) {
                        JOptionPane.showMessageDialog(frame, "Couldn't find the Godot Executable in:\n" + folderName, "Error 404: File Not Found",  JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    ProcessBuilder processBuilder = new ProcessBuilder(fullPath);
                    File workingDir = new  File(directoryPath, folderName);
                    processBuilder.directory(workingDir);
                    processBuilder.inheritIO();
                    Process process = processBuilder.start();

                    process.waitFor();
                } catch (IOException | InterruptedException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(frame, "Error Opening Godot\n"+ ex.getMessage(),"Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        downloads.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                 if (downloadFrame == null || !downloadFrame.isVisible()){
                    downloadFrame = new JFrame("Download Godot Version");
                    downloadFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    downloadFrame.setLocationRelativeTo(null);
                    downloadFrame.setResizable(false);
                    downloadFrame.setBackground(new Color(0x357EC7));
                    downloadFrame.setSize(500, 400);
                    downloadFrame.setLayout(new BorderLayout());
                    DownloadHandler downloadHandler = new DownloadHandler(() -> refreshVersions());
                    loadVersions(downloadHandler);
                    downloadFrame.add(downloadHandler, BorderLayout.CENTER);
                    downloadFrame.setVisible(true);
                }
            }
        });

        //Alignment Stuff
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(Box.createVerticalStrut(5));
        panel.add(comboBox);
        comboBox.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(checkBox);
        panel.add(button);
        panel.add(downloads);
        checkBox.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        downloads.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.setBackground(new Color(71,140,191));
        comboBox.setBackground(new Color(16, 184, 204));


        //Frame stuff
        frame.add(panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400,400);
        frame.setResizable(false);
        frame.setVisible(true);

    }
    /**
     * Average JFrame stuff, uses {@link #populateComboBox(JComboBox)} to make the comboBox work
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {});
        Main application  = new Main();
    }

    /**
     * Fills the Combo Box with versions.
     * @param comboBox Pretty much just, a standard {@link JComboBox}
     */
    public static void populateComboBox(JComboBox<String> comboBox) {
        List<String> filenames = new ArrayList<>();
        versions.clear();
        comboBox.removeAllItems();
        File rootDirectory = new  File(System.getProperty("user.home") + slashes +"GodotPrograms");

        if (rootDirectory.exists() && rootDirectory.isDirectory()) {
            File[] files = rootDirectory.listFiles();
            if  (files != null) {
                int i = 0;
                for (File file : files) {
                    if (file.isDirectory() && !file.getName().contains("_mono")){
                        filenames.add(file.getName());
                        versions.add (new GodotVersionInfo(file.getName()));
                        if (versions.get(i).getVersionNumber().equals(latestVersion)){
                            comboBox.addItem("Godot " + versions.get(i).getVersionNumber() + " [Latest Version]");
                        } else if (versions.get(i).getVersionNumber().equals(latest3Version)){
                            comboBox.addItem("Godot " + versions.get(i).getVersionNumber() + " [Latest Godot 3 Version]");
                        } else if (versions.get(i).getVersionNumber().equals(latestDevVersion)){
                            comboBox.addItem("Godot " + versions.get(i).getVersionNumber() + " [Latest Dev Version]");
                        } else if (versions.get(i).getVersionNumber().equals(latest3DevVersion)){
                            comboBox.addItem("Godot " + versions.get(i).getVersionNumber() + " [Latest Godot 3 Dev Version]");
                        }else if (versions.get(i).getVersionNumber().equals(latest2Version)){
                            comboBox.addItem("Godot " + versions.get(i).getVersionNumber() + " [Latest Godot 2 Version]");
                        } else if (versions.get(i).getVersionNumber().equals(latest1Version)){
                            comboBox.addItem("Godot " +  versions.get(i).getVersionNumber() + " [Latest Godot 1 Version]");
                        }else if (versions.get(i).getVersionNumber().equals(oldestVersion)){
                            comboBox.addItem("Godot " + versions.get(i).getVersionNumber() + " [Blinded by Nostalgia I'm Guessing?]");
                        } else {
                            comboBox.addItem("Godot " + versions.get(i).getVersionNumber());
                        }
                        i++;
                    }
                }
            }
        } else {
            System.err.println("Whoops, apparently GodotPrograms isn't found within a specified directory. Perhaps create that folder and try again.");
        }
    }

    /**
     * Checks all versions to see if the .NET version of Godot is also installed
     * @return Returns a list of booleans regarding which version also has the .NET version installed.
     */
    public static List<Boolean> getMono(){
        List<String> filenames = new ArrayList<>();
        List<GodotVersionInfo> versions = new ArrayList<>();
        List<Boolean> hasMono = new ArrayList<>();

        File rootDirectory = new  File(System.getProperty("user.home") + slashes +"GodotPrograms");

        if (rootDirectory.exists() && rootDirectory.isDirectory()) {
            File[] files = rootDirectory.listFiles();
            if  (files != null) {
                int i = 0;
                for (File file : files) {
                    if (file.isDirectory() && !file.getName().contains("_mono")){
                        filenames.add(file.getName());
                        versions.add (new GodotVersionInfo(file.getName()));
                        if (i == 0){
                            if (files[i].getName().contains("_mono")){
                                hasMono.add(true);
                            } else {
                                hasMono.add(false);
                            }
                        }
                        else if (files[i - 1].getName().contains("_mono")){
                            hasMono.add(true);
                        }
                        else {
                            hasMono.add(false);
                        }
                        i++;
                    } else if (file.isDirectory() && file.getName().contains("_mono")){
                        i++;
                    }
                }
            }
        } else {
            System.err.println("Uh, so, this probably ran because the code didn't see GodotPrograms within " + System.getProperty("user.home") +". So just rerun it, it shouldn't give this error again");
        }
        return hasMono;
    }

    /**
     * Pretty much writes to a file called dontAskAgain.txt, it pretty much just saves if you decided to not want to see the popup about how you must download your own files
     * @param dontAsk
     */
    public static void writeToFile(boolean dontAsk){
        try{
            FileWriter myWriter = new FileWriter("src/main/java/dontAskAgain.txt");
            myWriter.write(String.valueOf(dontAsk));
            myWriter.close();
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Loads all available versions on the Godot Page
     * @param handler A {@link JScrollPane} that handles all the stuff needed for downloading things
     */
    public static void loadVersions(DownloadHandler handler){
        SwingWorker<Void, String[]> worker = new SwingWorker<>(){
            @Override
            protected Void doInBackground() throws Exception {
                Document doc = Jsoup.connect("https://godotengine.org/download/archive/").get();
                Elements versions = doc.select("h4");
                for (Element version : versions) {
                    String fullText = version.text().trim();

                    if (fullText.contains("-")){
                        String[] parts = fullText.split("-", 2);
                        String versionNum =  parts[0];
                        String flavor = parts[1];
                        publish(new String[]{versionNum, flavor});
                    }
                }
                return null;
            }
            protected void process(List<String[]> chunks) {
                for (String[] data : chunks) {
                    handler.addRow(data[0], data[1]);
                }
                handler.revalidate();
                handler.repaint();
            }
        };
        worker.execute();
    }

    /**
     * Refreshes the {@link #comboBox} so that the new downloads will work
     */
    public static void refreshVersions() {
        //Temporarily disable Listeners
        comboBox.removeItemListener(comboBoxListener);
        comboBox.removeActionListener(comboBoxAction);

        comboBox.removeAllItems();
        //Repopulate static versions list
        populateComboBox(comboBox);
       if (versions.isEmpty()) {
           button.setEnabled(false);
           checkBox.setEnabled(false);
       } else {
           button.setEnabled(true);
           comboBox.setSelectedIndex(0);
           checkBox.setSelected(isMonoInstalled(0));
       }
       comboBox.addItemListener(comboBoxListener);
       comboBox.addActionListener(comboBoxAction);
    }

    /**
     * A helper method to help with finding the file name
     * @param folderName
     * @param useMono
     * @return
     */
    private static String findAppInFolder(String folderName, boolean useMono){
        File versionDir = new File(System.getProperty("user.home") + slashes +"GodotPrograms", folderName);
        if (!versionDir.exists() || !versionDir.isDirectory()){
            return  "";
        }
        String os = System.getProperty("os.name").toLowerCase();
        File[] files = versionDir.listFiles(file -> {
            String name = file.getName().toLowerCase();
            if (os.contains("win")) {
                return name.endsWith(".exe");
            } else if (os.contains("linux")){
                return (name.contains("64") || !name.contains(".")) || name.contains("x11");
            }
            return file.canExecute();
        });
        if (files != null){
            for (File file : files){
                String fileName = file.getName().toLowerCase();
                boolean isMonoExe = fileName.contains("mono");
                if (useMono == isMonoExe){
                    if (!os.contains("win")){
                        file.setExecutable(true,false);
                    }
                    return  file.getAbsolutePath();
                }
            }
        }
        return "";
    }

    /**
     * Interface used to refresh the primary window after a download
     */
    public interface DownloadListener{
        void onDownloadComplete();
    }

    /**
     * Simplified Check to see if a .NET version is installed.
     * @param index Where you're at in the ComboBox
     * @return Returns if there is a Mono Version Installed.
     */
    public static boolean isMonoInstalled(int index) {
        if (index <0 || index >= versions.size()){
            return false;
        }
        String currentFolder = versions.get(index).getOriginalFilename();
        if (currentFolder.contains("mono")){
            return true;
        }
        String monoFolderName = currentFolder.replace("_win64", "_mono_win64").replace("_linux", "_mono_linux");
        Path monoPath = Paths.get(System.getProperty("user.home"), "GodotPrograms", monoFolderName);
        return Files.exists(monoPath);
    }
}
