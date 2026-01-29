import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class DownloadHandler extends JScrollPane {
    List<PanelVersionHolder> panelVersionHolders = new ArrayList<PanelVersionHolder>();
    JPanel container = new  JPanel(new GridLayout(0,1, 0, 5));
    JPanel topWrapper = new  JPanel(new BorderLayout());
    private Main.DownloadListener downloadListener;

    public DownloadHandler(Main.DownloadListener downloadListener) {
        this.downloadListener = downloadListener;
        topWrapper.add(container, BorderLayout.NORTH);
        this.setViewportView(topWrapper);
        getViewport().setBackground(Color.decode("#ADD8E6"));
        getViewport().setOpaque(false);
        setOpaque(false);
    }

    public void addRow(String versionNumber, String flavor){
        PanelVersionHolder panelVersionHolder = new PanelVersionHolder(versionNumber, flavor, downloadListener);
        panelVersionHolders.add(panelVersionHolder);
        container.add(panelVersionHolder);
        revalidate();
        repaint();
    }
    public void removeRow(int index){
        panelVersionHolders.remove(index);
    }
}
