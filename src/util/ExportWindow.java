package util;

import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JFrame;

public class ExportWindow extends JDialog implements ExportStateReceiver {
  private static final Font systemFont = new Font(".SF NS Text", Font.PLAIN, 14);
  private int totalBytes;
  private JLabel exportingLabel, currentPercentageLabel, currentBytesLabel, totalBytesLabel;
  private JProgressBar progressBar;

  public ExportWindow(Frame frame) {
    super(frame);
    this.setModal(true);
    this.getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));

    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

    JLabel exportingLabel = new JLabel("Rendering...");
    this.exportingLabel = exportingLabel;
    panel.add(exportingLabel);

    this.getContentPane().add(panel);

    JPanel progressPanel = new JPanel();
    progressPanel.setLayout(new BoxLayout(progressPanel, BoxLayout.X_AXIS));

    JLabel label1 = new JLabel("Progress: ");
    progressPanel.add(label1);

    JLabel currentPercentageLabel = new JLabel("0");
    this.currentPercentageLabel = currentPercentageLabel;
    progressPanel.add(currentPercentageLabel);

    JLabel percentageSuffixLabel = new JLabel(" %");
    progressPanel.add(percentageSuffixLabel);

    JProgressBar progressBar = new JProgressBar();
    this.progressBar = progressBar;
    progressPanel.add(progressBar);

    //
    this.getContentPane().add(progressPanel);

    JPanel detailStatusPanel = new JPanel();
    detailStatusPanel.setLayout(new BoxLayout(detailStatusPanel, BoxLayout.X_AXIS));

    JLabel currentBytesLabel = new JLabel("0");
    this.currentBytesLabel = currentBytesLabel;
    detailStatusPanel.add(currentBytesLabel);

    JLabel detailSlashLabel = new JLabel(" / ");
    detailStatusPanel.add(detailSlashLabel);

    JLabel totalBytesLabel = new JLabel("0");
    totalBytesLabel.setText(String.format("%,d", totalBytes));
    this.totalBytesLabel = totalBytesLabel;
    detailStatusPanel.add(totalBytesLabel);

    JLabel bytesSuffixLabel = new JLabel(" bytes");
    detailStatusPanel.add(bytesSuffixLabel);

    this.getContentPane().add(detailStatusPanel);
    this.setPreferredSize(new Dimension(300, 150));
  }

  @Override
  public void onProgressUpdate(int completedBytes) {
    currentBytesLabel.setText(String.format("%,d", completedBytes));
    int percentage = (int) ((completedBytes / (totalBytes * 1.0)) * 100);
    progressBar.setValue(percentage);
    currentPercentageLabel.setText(String.valueOf(percentage));
  }

  @Override
  public void onRenderingFinished() {
    exportingLabel.setText("Rendering finished. Writing to the file...");
  }

  @Override
  public void setTotalBytes(int totalBytes) {
    this.totalBytes = totalBytes;
    totalBytesLabel.setText(String.format("%,d", totalBytes));
  }

  @Override
  public void onExportFinished() {
    this.setVisible(false);
  }

  @Override
  public void onExportError(String message, String title) {
    JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    this.setVisible(false);
  }

}
