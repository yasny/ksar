package net.atomique.ksar.export;

import java.io.File;
import javax.swing.JDialog;
import javax.swing.JProgressBar;
import net.atomique.ksar.Config;
import net.atomique.ksar.OSParser;
import net.atomique.ksar.graph.Graph;
import net.atomique.ksar.kSar;
import net.atomique.ksar.ui.SortedTreeNode;
import net.atomique.ksar.ui.TreeNodeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FilePNG implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(FilePNG.class);

	private final JProgressBar progressBar;
	private final JDialog progressDlg;
	private final String exportPath;
	private final kSar ksar;
	private int progressFileCount = 0;

	public FilePNG(String exportPath, kSar ksar) {
		this(exportPath, ksar, null, null);
	}

	public FilePNG(String exportPath, kSar ksar, JProgressBar progressBar, JDialog dlg) {
		this.exportPath = exportPath;
		this.ksar = ksar;
		this.progressBar = progressBar;
		this.progressDlg = dlg;
	}

	@Override
	public void run() {
		LOG.debug("Exporting PNGs to {}", exportPath);
		processTreeNode(ksar.graphtree);
		if (progressDlg != null)
			progressDlg.dispose();
	}

	private void processTreeNode(SortedTreeNode node) {
		int count = node.getChildCount();
		if (count > 0) {
			for (int i=0; i<count;i++) {
				SortedTreeNode n = (SortedTreeNode)node.getChildAt(i);
				processTreeNode(n);
			}
		} else {
			Object userObject = node.getUserObject();
			if (userObject instanceof TreeNodeInfo) {
				TreeNodeInfo tni = (TreeNodeInfo)userObject;
				Graph graph = tni.getNode_object();
				if (graph.isPrintSelected()) {
					saveGraphToPNG(graph);
					updateProgress(++progressFileCount);
				}
			}
		}
	}

	private void saveGraphToPNG(Graph graph) {
		String pngFileName = graph.getTitle().toLowerCase().replace(" ", "-").replaceAll("[()/]", "") + ".png";
		if (Config.getUseHostnameInGraphTitle())
			pngFileName = ((OSParser)ksar.myparser).gethostName()+"_"+pngFileName;
		String savePath = exportPath + File.separator + pngFileName;
		LOG.debug("Saving graph \"{}\" to {}", graph.getTitle(), savePath);
		File savePNG = new File(savePath);
		if (savePNG.exists())
			LOG.warn("{} already exists; will overwrite!", pngFileName);
		graph.savePNG(null, null, savePath, Config.getImageWidth(), Config.getImageHeight());
	}

	private void updateProgress(int value) {
		if (progressBar != null) {
			progressBar.setValue(value);
			progressBar.repaint();
		}
	}
	
}
