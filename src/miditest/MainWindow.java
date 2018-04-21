package miditest;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.filechooser.FileFilter;

import jouvieje.bass.Bass;
import jouvieje.bass.BassInit;
import jouvieje.bass.defines.BASS_CONFIG;
import util.ExportWindow;
import util.MIDIStream;
import util.SoundFont;
import java.awt.Component;
import java.awt.BorderLayout;
import javax.swing.SwingConstants;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;

public class MainWindow extends JPanel implements ActionListener, MouseMotionListener, MouseListener {
  private static final long serialVersionUID = -8452899082669383649L;
  // GUI Parts
  private JPanel upperPanel, songInfoPanel, sfInfoPanel, buttonsPanel, controlPanel;
  private JPanel songNamePanel;
  private JLabel headerSongName, headerSFName, headerSongPosition, headerEffects;
  private JSlider songPosSlider;
  private JLabel textSongName, textSFName, textSFLoadedSam, textSFTotalSam;

  private JButton songReplaceButton, sfReplaceButton;
  private JToggleButton playPauseButton;
  private JButton exportButton, stopButton;
  private Timer timer;
  // MIDI
  private MIDIStream myStream;
  private SoundFont soundFont;
  private boolean pendingSoundFont;
  private JFrame frame;

  public MainWindow() {
    this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

    Font headerFont = new Font(".SF NS Text", Font.BOLD, 13);
    Font normalFont = headerFont.deriveFont(Font.PLAIN);

    // Dimension fullWidth = new Dimension(500, 0);

    upperPanel = new JPanel();
    upperPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

    // 曲名
    songInfoPanel = new JPanel();
    songInfoPanel.setLayout(new BoxLayout(songInfoPanel, BoxLayout.X_AXIS));
    upperPanel.add(songInfoPanel);

    // サウンドフォント
    sfInfoPanel = new JPanel();

    textSFLoadedSam = new JLabel("0");
    textSFTotalSam = new JLabel("0");
    GridBagLayout gbl_sfInfoPanel = new GridBagLayout();
    gbl_sfInfoPanel.columnWidths = new int[] { 90, 210, 135 };
    gbl_sfInfoPanel.rowHeights = new int[] { 20, 20, 0 };
    gbl_sfInfoPanel.columnWeights = new double[] { 0.0, 0.0, 0.0 };
    gbl_sfInfoPanel.rowWeights = new double[] { 0.0, 0.0, 0.0 };
    sfInfoPanel.setLayout(gbl_sfInfoPanel);

    sfReplaceButton = new JButton("Replace...");
    sfReplaceButton.setFont(normalFont);
    sfReplaceButton.setActionCommand(String.valueOf(ActionCommand.SF_CHANGE));
    sfReplaceButton.addActionListener(this);

    headerSongName = new JLabel("Song: ");
    GridBagConstraints gbc_headerSongName = new GridBagConstraints();
    gbc_headerSongName.fill = GridBagConstraints.BOTH;
    gbc_headerSongName.insets = new Insets(0, 0, 5, 5);
    gbc_headerSongName.gridx = 0;
    gbc_headerSongName.gridy = 0;
    sfInfoPanel.add(headerSongName, gbc_headerSongName);
    headerSongName.setFont(headerFont);

    textSongName = new JLabel("Not specified");
    GridBagConstraints gbc_textSongName = new GridBagConstraints();
    gbc_textSongName.fill = GridBagConstraints.BOTH;
    gbc_textSongName.insets = new Insets(0, 0, 5, 5);
    gbc_textSongName.gridx = 1;
    gbc_textSongName.gridy = 0;
    sfInfoPanel.add(textSongName, gbc_textSongName);
    textSongName.setFont(normalFont);

    songReplaceButton = new JButton("Change...");
    GridBagConstraints gbc_songReplaceButton = new GridBagConstraints();
    gbc_songReplaceButton.fill = GridBagConstraints.BOTH;
    gbc_songReplaceButton.insets = new Insets(0, 0, 5, 5);
    gbc_songReplaceButton.gridx = 2;
    gbc_songReplaceButton.gridy = 0;
    sfInfoPanel.add(songReplaceButton, gbc_songReplaceButton);
    songReplaceButton.setFont(normalFont);
    songReplaceButton.setActionCommand(String.valueOf(ActionCommand.SONG_CHANGE));
    songReplaceButton.addActionListener(this);

    headerSFName = new JLabel("SoundFont: ");
    headerSFName.setFont(headerFont);
    GridBagConstraints gbc_headerSFName = new GridBagConstraints();
    gbc_headerSFName.fill = GridBagConstraints.BOTH;
    gbc_headerSFName.insets = new Insets(0, 0, 5, 5);
    gbc_headerSFName.gridx = 0;
    gbc_headerSFName.gridy = 1;
    sfInfoPanel.add(headerSFName, gbc_headerSFName);

    textSFName = new JLabel("Not specified");
    textSFName.setFont(normalFont);
    GridBagConstraints gbc_textSFName = new GridBagConstraints();
    gbc_textSFName.fill = GridBagConstraints.BOTH;
    gbc_textSFName.insets = new Insets(0, 0, 5, 5);
    gbc_textSFName.gridx = 1;
    gbc_textSFName.gridy = 1;
    sfInfoPanel.add(textSFName, gbc_textSFName);
    GridBagConstraints gbc_sfReplaceButton = new GridBagConstraints();
    gbc_sfReplaceButton.insets = new Insets(0, 0, 5, 5);
    gbc_sfReplaceButton.fill = GridBagConstraints.BOTH;
    gbc_sfReplaceButton.gridx = 2;
    gbc_sfReplaceButton.gridy = 1;
    sfInfoPanel.add(sfReplaceButton, gbc_sfReplaceButton);

    upperPanel.add(sfInfoPanel);

    headerSongPosition = new JLabel("Position:");
    GridBagConstraints gbc_headerSongPosition = new GridBagConstraints();
    gbc_headerSongPosition.anchor = GridBagConstraints.WEST;
    gbc_headerSongPosition.insets = new Insets(0, 0, 5, 5);
    gbc_headerSongPosition.gridx = 0;
    gbc_headerSongPosition.gridy = 2;
    sfInfoPanel.add(headerSongPosition, gbc_headerSongPosition);
    headerSongPosition.setFont(headerFont);

    songPosSlider = new JSlider();
    GridBagConstraints gbc_songPosSlider = new GridBagConstraints();
    gbc_songPosSlider.fill = GridBagConstraints.HORIZONTAL;
    gbc_songPosSlider.gridwidth = 2;
    gbc_songPosSlider.insets = new Insets(0, 0, 5, 5);
    gbc_songPosSlider.gridx = 1;
    gbc_songPosSlider.gridy = 2;
    sfInfoPanel.add(songPosSlider, gbc_songPosSlider);
    songPosSlider.setValue(0);
    songPosSlider.addMouseMotionListener(this);
    songPosSlider.addMouseListener(this);
    songPosSlider.setEnabled(false);

    buttonsPanel = new JPanel();

    exportButton = new JButton("Export");
    exportButton.setFont(normalFont);
    exportButton.setActionCommand(String.valueOf(ActionCommand.EXPORT));
    exportButton.addActionListener(this);
    buttonsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
    exportButton.setEnabled(false);
    buttonsPanel.add(exportButton);

    playPauseButton = new JToggleButton("Play / Pause");
    playPauseButton.setFont(normalFont);
    playPauseButton.setActionCommand(String.valueOf(ActionCommand.PLAY_PAUSE));
    playPauseButton.addActionListener(this);
    playPauseButton.setEnabled(false);
    buttonsPanel.add(playPauseButton);

    stopButton = new JButton("Stop");
    stopButton.setFont(normalFont);
    stopButton.setActionCommand(String.valueOf(ActionCommand.STOP));
    stopButton.addActionListener(this);
    stopButton.setEnabled(false);
    buttonsPanel.add(stopButton);

    upperPanel.add(buttonsPanel);

    add(upperPanel);
    timer = new Timer(100, this);
    timer.setActionCommand(String.valueOf(ActionCommand.TIMER));
    timer.setRepeats(true);
  }

  public void setFrame(Frame frame) {
    this.frame = (JFrame) frame;
  }

  @Override
  public void actionPerformed(ActionEvent event) {
    char command = event.getActionCommand().charAt(0);
    switch (command) {
      case ActionCommand.SONG_CHANGE:
        loadMIDIFile();
      break;
      case ActionCommand.SF_CHANGE:
        loadSFFile();
      break;
      case ActionCommand.EXPORT:
        File preDefinedFile = new File(System.getProperty("user.home") + "/" + myStream.getSongName() + ".wav");
        JFileChooser selectDialog = new JFileChooser();
        ExportWindow exportWindow = new ExportWindow(this.frame);

        selectDialog.setFileFilter(new FileFilter() {
          @Override
          public String getDescription() {
            return "WAV File";
          }

          @Override
          public boolean accept(File f) {
            return f.getName().endsWith(".wav");
          }
        });
        selectDialog.setSelectedFile(preDefinedFile);
        if (selectDialog.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
          File selectedFile = selectDialog.getSelectedFile();
          if (selectedFile.exists() && JOptionPane.showConfirmDialog(this, selectedFile.getName() + " already exists. Do you want to overwrite it?", "Overwrite confirmation",
              JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.NO_OPTION) {
            return;
          }
          new Thread(new Runnable() {

            @Override
            public void run() {
              myStream.export(selectDialog.getSelectedFile(), exportWindow);
            }
          }).start();
          exportWindow.pack();
          exportWindow.setLocationRelativeTo(null);
          exportWindow.setVisible(true);
        }
      break;
      case ActionCommand.PLAY_PAUSE:
        timer.start();
        if (myStream.isPlaying()) {
          myStream.pause();
          playPauseButton.setSelected(false);
          timer.stop();
        } else {
          myStream.play();
          playPauseButton.setSelected(true);
          timer.start();
        }
      break;
      case ActionCommand.STOP:
        myStream.stop();
        playPauseButton.setSelected(false);
        timer.stop();
        songPosSlider.setValue(0);
      case ActionCommand.TIMER:
        SwingUtilities.invokeLater(new Runnable() {
          @Override
          public void run() {
            songPosSlider.setValue(myStream.getPosition() / 120);
          }
        });
      break;
      default:
      break;
    }
  }

  private void loadMIDIFile() {
    File midiFile = selectAndGetFile("MIDI File", new String[] { "mid", "midi" });
    if (midiFile != null) {
      textSongName.setText("Now loading...");
      new Thread(new Runnable() {

        @Override
        public void run() {
          if (myStream != null) {
            myStream.setFile(midiFile, true);
          } else {
            myStream = new MIDIStream(midiFile);
            myStream.setVoicesLimit(1200);
            if (soundFont != null) {
              myStream.setSoundFont(soundFont);
            }
          }
          SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
              textSongName.setText(myStream.getSongName() + " - " + midiFile.getName());
              songPosSlider.setMaximum(myStream.length() / 120);
              exportButton.setEnabled(true);
              playPauseButton.setEnabled(true);
              stopButton.setEnabled(true);
              songPosSlider.setEnabled(true);
            }
          });
        }

      }).start();
    }
  }

  private void loadSFFile() {
    File SFFile = selectAndGetFile("SoundFont File", new String[] { "sf2", "SF2", "sfz", "SFZ" });
    setSFFile(SFFile);
  }

  public void setSFFile(File SFFile) {
    if (SFFile != null) {
      SoundFont mySoundFont = new SoundFont(SFFile, 0, false);
      textSFName.setText("Now loading...");
      new Thread(new Runnable() {
        @Override
        public void run() {
          mySoundFont.fontLoad();
          if (myStream != null) {
            myStream.setSoundFont(mySoundFont);
          }
          soundFont = mySoundFont;
          SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
              textSFName.setText(soundFont.getName());
              textSFLoadedSam.setText(String.valueOf(soundFont.getLoadedSamples()));
              textSFTotalSam.setText(String.valueOf(soundFont.getTotalSamples()));
            }
          });
        }
      }).start();

    }
  }

  private File selectAndGetFile(String description, String[] acceptableExts) {
    JFileChooser chooser = new JFileChooser();
    FileFilter filter = new FileFilter() {

      @Override
      public String getDescription() {
        return description;
      }

      @Override
      public boolean accept(File file) {
        if (file.exists() && file.isFile() && file.canRead()) {
          String[] separators = file.getName().split("\\.");
          String extension = separators[separators.length - 1];
          List<String> searchList = Arrays.asList(acceptableExts);
          return searchList.contains(extension);
        } else {
          return false;
        }
      }
    };
    chooser.setCurrentDirectory(new File("/Users/kazu/Win8_Shared/MIDIs/"));
    chooser.setFileFilter(filter);
    chooser.showOpenDialog(this);
    return chooser.getSelectedFile();
  }

  public JFrame GetFrame() {
    return this.frame;
  }

  @Override
  public void mouseDragged(MouseEvent e) {
    if (e.getSource().equals(songPosSlider)) {
      myStream.setPosition(songPosSlider.getValue() * 120);
    }
  }

  @Override
  public void mouseMoved(MouseEvent e) {
  }

  @Override
  public void mouseClicked(MouseEvent e) {
    if (e.getSource().equals(songPosSlider)) {
      myStream.setPosition(songPosSlider.getValue() * 120);
    }
  }

  @Override
  public void mousePressed(MouseEvent e) {
  }

  @Override
  public void mouseReleased(MouseEvent e) {

  }

  @Override
  public void mouseEntered(MouseEvent e) {

  }

  @Override
  public void mouseExited(MouseEvent e) {

  }

}

class Loader {
  public static HashMap<String, String> analyzeArgs(String[] args) {
    HashMap<String, String> hashMap = new HashMap<>();
    for (int i = 0; i < args.length; i += 2) {
      String switchName = args[i];
      if (switchName.startsWith("-")) {
        if (i + 1 < args.length) {
          String value = args[i + 1];
          hashMap.put(switchName.substring(1), value);
        } else {
          // -xx で終わる形式
          System.out.println("Unknown switch: " + switchName);
        }
      } else {
        // -で始まらないスイッチ名 / ファイル名(未実装)
        System.out.println("Unknown argument: " + switchName);
      }
    }
    return hashMap;
  }

  public static void main(String[] args) throws Exception {
    String appName = "BASSMIDI test";
    System.setProperty("apple.laf.useScreenMenuBar", "true");
    System.setProperty("apple.awt.application.name", appName);
    HashMap<String, String> argsMap = analyzeArgs(args);

    if (argsMap.containsKey("lib")) {
      System.setProperty("java.library.path", System.getProperty("java.library.path") + ":" + args[0]);
      System.out.println(args[0] + "added to java.library.path.");
    }
    BassInit.DEBUG = false;
    BassInit.loadLibraries();
    Bass.BASS_Init(-1, 44100, 0, null, null);
    Bass.BASS_SetConfig(BASS_CONFIG.BASS_CONFIG_UPDATETHREADS, Runtime.getRuntime().availableProcessors());

    JFrame mainWindow = new JFrame(appName);
    MainWindow mainPanel = new MainWindow();
    mainPanel.setFrame(mainWindow);
    mainWindow.getContentPane().add(mainPanel);
    mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    mainWindow.setSize(new Dimension(500, 300));
    mainWindow.setVisible(true);

    if (argsMap.containsKey("sf")) {
      mainPanel.setSFFile(new File(argsMap.get("sf")));
    }
  }
}
