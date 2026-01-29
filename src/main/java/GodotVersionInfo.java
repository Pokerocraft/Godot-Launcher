import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is meant to give {@link Main} a structure for Godot versions
 */
public class GodotVersionInfo {
    private String versionNumber;
    private boolean isDotNet;
    private String originalFilename;
    private String osName = System.getProperty("os.name");
    private String versionType;

    public static final Pattern WINDOWS_VERSION_PATTERN = Pattern.compile("Godot_v?(\\d+(?:\\.\\d+)*)[-_\\s]?((?:stable|alpha|beta|rc|dev|jenova)[\\d_mono]*)_?[_\\-]win64");
    public static final Pattern LINUX_VERSION_PATTERN = Pattern.compile("Godot_v?(\\d+(?:\\.\\d+)*)[-_\\s\\.]?((?:stable|alpha|beta|rc|dev|jenova)[\\d_mono\\.]*)_?[_\\-](linux|x11|x86_64|64)");
    /**
     * Constructor to get a specific file
     * @param filename The name of the file
     */
    public GodotVersionInfo(String filename){
        this.originalFilename = filename;
        if (filename == null){
            throw new IllegalArgumentException("Filename cannot be null");
        }

        Matcher matcher;
        if (osName.contains("Windows")) {
            matcher = WINDOWS_VERSION_PATTERN.matcher(filename);
        } else {
            matcher = LINUX_VERSION_PATTERN.matcher(filename);
        }
        if (matcher.find()) {
            this.versionNumber = matcher.group(1);
            this.versionType = matcher.group(2);
        } else {
            this.versionNumber = "Unknown";
            this.versionType = "Unknown";
        }

        this.isDotNet = filename.contains("_mono");
    }

    /**
     * Gets the version number
     * @return Returns the version number of Godot, such as 4.5.1
     */
    public String getVersionNumber() {
        if (versionNumber == null || versionType == null) {
            return "Unknown";
        }
        String displayType = versionType.substring(0,1).toUpperCase() + versionType.substring(1);
        if (displayType.equals("Jenova")){
            displayType = "Jenova Framework";
        }
        return versionNumber + " ("  + displayType + ")";
    }

    /**
     * Gets if the specific version of Godot is the .NET version, indicated by "mono" being in the filename
     * @return Returns if the specific version is the .NET version
     */
    public boolean isDotNet() {
        return isDotNet;
    }

    /**
     * Gets the original filename
     * @return Returns the original filename
     */
    public String getOriginalFilename() {
        return originalFilename;
    }

    /**
     * Sets the original filename to a new filename
     * @param originalFilename The new filename
     */
    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    /**
     * Turns the filename into a readable version, so like say Godot_v4.5.1-stable_win64 becomes Version: 4.5.1, Type: Standard
     */
    @Override
    public String toString() {
        String type = "";
        if (versionNumber.contains("dev")){
            type = "Development Version";
        } else if (versionNumber.contains("alpha")) {
            type = "Alpha Version";
        }  else if (versionNumber.contains("beta")) {
            type = "Beta Version";
        } else if (versionNumber.contains("rc")) {
            type = "Release Candidate Version";
        } else if (versionNumber.contains("stable") && !isDotNet) {
            type = "Stable Version";
        } else if (versionNumber.contains("jenova")){
            type = "Jenova Framework Edition";
        }
        if (isDotNet) {
            type += ".NET Version";
        }
        return "Version: " + versionNumber + ", Type: " + type;
    }
}
