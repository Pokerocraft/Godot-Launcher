import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
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
    static String latestVersion = "4.5.1 (Stable)";
    static String latest3Version = "3.6.2 (Stable)";
    static String latestDevVersion = "4.6 (Beta3)";
    static String latest3DevVersion = "3.7 (Dev1)";
    static String slashes = System.getProperty("file.separator");
    /**
     * Average JFrame stuff, uses {@link #populateComboBox(JComboBox)} to make the comboBox work
     */
    public static void main(String[] args) {
        boolean skip = false;
        try{
            String output = "";
            File askingFile = new File("src/main/java/donutAskAgain.txt");
            int i = 0;
            Scanner myReader = new Scanner(askingFile);
            String data;
            for (myReader = new Scanner(askingFile); myReader.hasNextLine(); output=output.concat(data+"\n")){
                data = "line output: " + myReader.nextLine();
                System.out.println(data);
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
        JPanel panel = new JPanel();
        JFrame frame = new JFrame("Godot Launcher");
        //Versions and If the user wants to use the .NET version
        JComboBox<String> comboBox = new JComboBox();
        JCheckBox checkBox = new JCheckBox(".NET Version");
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
            JOptionPane.showMessageDialog(frame, "<html>Having no Stable Versions (Any file that doesn't have _mono in the filename) will crash the engine.<br>Also if you are using a custom engine, such as the Jenova Framework, please change the engine name to fit with the other Godot Versions, so that way my job is easier.<br>Also, in the event that you didn't read the repository description or have no idea what it means: <font color=red> YOU MUST DOWNLOAD YOUR OWN GODOT VERSIONS</font></html>","Info", JOptionPane.INFORMATION_MESSAGE); //Trust me, I don't want to make two more matchers to be able to use the Jenova Framework, renaming a file is easier than this stuff.
            Scanner myScanner = new  Scanner(System.in);
            System.out.println("Do you wish to skip the info box?");
            String result = myScanner.nextLine();
            if (result.equalsIgnoreCase("yes")){
                writeToFile(true);
            }
        }
        System.out.println("Programs Path: " + programsPath.toString());
        String osName = System.getProperty("os.name");
        System.out.println("OS Name: " + osName);
        populateComboBox(comboBox);
        checkBox.setSelected(false);
        try{
            checkBox.setEnabled(hasMono.getFirst() == true);
        } catch (NoSuchElementException e){
            JOptionPane.showMessageDialog(frame, "You likely only have .NET installations, which requires the Stable version in order to run..", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            System.exit(1);
        }
        if (versions.isEmpty()) {
            JOptionPane.showMessageDialog(frame,"Ah, you don't have any installations of Godot.\nThis ain't the Minecraft launcher.\nSo go and install Godot, then put it in " + programsPath +".\nIf you have only .NET versions, that's because the launcher uses a Check Box to run the mono version, so it's best to add the Standard version as well", "Error", JOptionPane.ERROR_MESSAGE);
//            System.out.println("Uhh, you don't have any installations of Godot, please install one before using this.\n" +
//                    "This isn't the Minecraft Launcher after all");
            System.exit(0);
        }
        JButton button = new JButton("Open this instance of Godot");
        fileLocation = directoryPath + slashes + versions.getFirst().getOriginalFilename();


        comboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
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
        });

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

                System.out.println(checkBox.isSelected());
            }
        });

        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try{
                    String fullPath = "";
                    if (versions.get(comboBox.getSelectedIndex()).getVersionNumber().contains("Jenova Framework")) {
                        if (osName.contains("Windows")) {
                            fullPath = fileLocation + slashes + "godot.windows.editor.x86_64.exe";
                        }
                    }
                    else if (osName.contains("Linux")) {
                        fullPath = fileLocation + slashes + versions.getFirst().getOriginalFilename() + ".x86_64";
                    } else {
                        if (isMonoInstalled(comboBox.getSelectedIndex()) && checkBox.isSelected()) {
                            String originalVersion = versions.get(comboBox.getSelectedIndex()).getOriginalFilename();
                            String toInsert = "_mono";
                            int lastUnderscoreIndex = originalVersion.lastIndexOf("_");
                            String prefix = originalVersion.substring(0, lastUnderscoreIndex);
                            int extensionIndex = originalVersion.lastIndexOf(".");
                            String suffix = originalVersion.substring(lastUnderscoreIndex, extensionIndex);
                            String newVersion = prefix + toInsert + suffix + ".exe";
                            fullPath = fileLocation + slashes + newVersion;
                        } else {
                            fullPath = fileLocation + slashes + versions.get(comboBox.getSelectedIndex()).getOriginalFilename();
                        }
                    }
                    ProcessBuilder processBuilder = new ProcessBuilder(fullPath);

                    processBuilder.directory(new File(fileLocation));
                    processBuilder.inheritIO();

                    Process process = processBuilder.start();

                    int exitCode = process.waitFor();
                    System.out.println("Godot Engine "+versions.get(comboBox.getSelectedIndex()).getVersionNumber()+" closed with code " + exitCode);
                } catch (IOException | InterruptedException ex){
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(frame, "Whoops, there was an error opening Godot ;-;... \n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
        panel.setBackground(new Color(71,140,191));
        comboBox.setBackground(new Color(16, 184, 204));


        //Frame stuff
        frame.add(panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400,400);
        frame.setVisible(true);

    }

    /**
     * Fills the Combo Box with versions.
     * @param comboBox Pretty much just, a standard {@link JComboBox}
     */
    public static void populateComboBox(JComboBox<String> comboBox) {
        List<String> filenames = new ArrayList<>();
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
     * Checks to see if the .NET version is installed for a version at a specific index
     * @param index Because isMonoInstalled runs {@link #getMono() getMono().get(index)}, it uses a specified index to check if that specific version of Godot has the .NET version installed.
     * @return Returns true if a specific index of {@link #getMono()} is true.
     */
    public static boolean isMonoInstalled(int index){
        return getMono().get(index);
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

    public static void writeToFile(boolean dontAsk){
        try{
            FileWriter myWriter = new FileWriter("src/main/java/donutAskAgain.txt");
            myWriter.write(String.valueOf(dontAsk));
            myWriter.close();
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
