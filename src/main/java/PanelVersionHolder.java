import javax.swing.*;

public class PanelVersionHolder extends JPanel {
    JLabel label;
    JButton button;
    public PanelVersionHolder(String versionNum, String Flavor) {
        label = new JLabel(versionNum + "-" + Flavor);
        button = new JButton("Download");
        button.addActionListener(e -> {
        });
    }
}
