import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class DownloadHandler extends JScrollPane {
    List<PanelVersionHolder> panelVersionHolders = new ArrayList<PanelVersionHolder>();

    public void addRow(String versionNumber, String flavor){
        PanelVersionHolder panelVersionHolder = new PanelVersionHolder(versionNumber, flavor);
        panelVersionHolders.add(panelVersionHolder);
    }
    public void removeRow(int index){
        panelVersionHolders.remove(index);
    }
}
