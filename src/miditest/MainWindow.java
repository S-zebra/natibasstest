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
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

import jouvieje.bass.Bass;
import jouvieje.bass.BassInit;
import jouvieje.bass.defines.BASS_CONFIG;
import util.ExportWindow;
import util.MIDIStream;
import util.SoundFont;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.JSpinner;

public class MainWindow extends JPanel
    implements ActionListener, MouseMotionListener, MouseListener, ChangeListener {
  private static final long serialVersionUID = -8452899082669383649L;
  // GUI Parts
  private JPanel upperPanel, infoPanel, buttonsPanel;
  private JLabel headerSongName, headerSFName, headerSongPosition;
  private JSlider songPosSlider;
  private JLabel textSongName, textSFName, textSFLoadedSam, textSFTotalSam;

  private JButton songReplaceButton, sfReplaceButton;
  private JToggleButton playPauseButton;
  private JButton exportButton, stopButton;
  private Timer timer;

  private JFrame frame;
  private JPanel lowerPanel;
  private JPanel polyphonyPanel;
  private JLabel polyphonyHeaderLabel;
  private JSlider polyphonySlider;
  private JSpinner polyphonySpinner;

  // MIDI
  private MIDIStream myStream;
  private SoundFont soundFont;

  public MainWindow() {

    Font headerFont = new Font(".SF NS Text", Font.BOLD, 13);
    Font normalFont = headerFont.deriveFont(Font.PLAIN);

    // Dimension fullWidth = new Dimension(500, 0);

    upperPanel = new JPanel();
    upperPanel.setLayout(new BoxLayout(upperPanel, BoxLayout.Y_AXIS));

    // サウンドフォント
    infoPanel = new JPanel();

    textSFLoadedSam = new JLabel("0");
    textSFTotalSam = new JLabel("0");
    GridBagLayout gbl_infoPanel = new GridBagLayout();
    gbl_infoPanel.columnWidths = new int[] { 90, 210, 135 };
    gbl_infoPanel.rowHeights = new int[] { 20, 20, 0 };
    gbl_infoPanel.columnWeights = new double[] { 0.0, 0.0, 0.0 };
    gbl_infoPanel.rowWeights = new double[] { 0.0, 0.0, 0.0 };
    infoPanel.setLayout(gbl_infoPanel);

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
    infoPanel.add(headerSongName, gbc_headerSongName);
    headerSongName.setFont(headerFont);

    textSongName = new JLabel("Not specified");
    textSongName.setPreferredSize(new Dimension(210, 20));
    GridBagConstraints gbc_textSongName = new GridBagConstraints();
    gbc_textSongName.fill = GridBagConstraints.BOTH;
    gbc_textSongName.insets = new Insets(0, 0, 5, 5);
    gbc_textSongName.gridx = 1;
    gbc_textSongName.gridy = 0;
    infoPanel.add(textSongName, gbc_textSongName);
    textSongName.setFont(normalFont);

    songReplaceButton = new JButton("Change...");
    GridBagConstraints gbc_songReplaceButton = new GridBagConstraints();
    gbc_songReplaceButton.fill = GridBagConstraints.BOTH;
    gbc_songReplaceButton.insets = new Insets(0, 0, 5, 5);
    gbc_songReplaceButton.gridx = 2;
    gbc_songReplaceButton.gridy = 0;
    infoPanel.add(songReplaceButton, gbc_songReplaceButton);
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
    infoPanel.add(headerSFName, gbc_headerSFName);

    textSFName = new JLabel("Not specified");
    textSFName.setPreferredSize(new Dimension(210, 20));
    textSFName.setFont(normalFont);
    GridBagConstraints gbc_textSFName = new GridBagConstraints();
    gbc_textSFName.fill = GridBagConstraints.BOTH;
    gbc_textSFName.insets = new Insets(0, 0, 5, 5);
    gbc_textSFName.gridx = 1;
    gbc_textSFName.gridy = 1;
    infoPanel.add(textSFName, gbc_textSFName);
    GridBagConstraints gbc_sfReplaceButton = new GridBagConstraints();
    gbc_sfReplaceButton.insets = new Insets(0, 0, 5, 5);
    gbc_sfReplaceButton.fill = GridBagConstraints.BOTH;
    gbc_sfReplaceButton.gridx = 2;
    gbc_sfReplaceButton.gridy = 1;
    infoPanel.add(sfReplaceButton, gbc_sfReplaceButton);

    upperPanel.add(infoPanel);

    headerSongPosition = new JLabel("Position:");
    GridBagConstraints gbc_headerSongPosition = new GridBagConstraints();
    gbc_headerSongPosition.anchor = GridBagConstraints.WEST;
    gbc_headerSongPosition.insets = new Insets(0, 0, 5, 5);
    gbc_headerSongPosition.gridx = 0;
    gbc_headerSongPosition.gridy = 2;
    infoPanel.add(headerSongPosition, gbc_headerSongPosition);
    headerSongPosition.setFont(headerFont);

    songPosSlider = new JSlider();
    GridBagConstraints gbc_songPosSlider = new GridBagConstraints();
    gbc_songPosSlider.fill = GridBagConstraints.HORIZONTAL;
    gbc_songPosSlider.gridwidth = 2;
    gbc_songPosSlider.insets = new Insets(0, 0, 5, 5);
    gbc_songPosSlider.gridx = 1;
    gbc_songPosSlider.gridy = 2;
    infoPanel.add(songPosSlider, gbc_songPosSlider);
    songPosSlider.setValue(0);
    songPosSlider.addMouseMotionListener(this);
    songPosSlider.addMouseListener(this);
    songPosSlider.setEnabled(false);

    buttonsPanel = new JPanel();

    exportButton = new JButton("Export");
    exportButton.setFont(normalFont);
    exportButton.setActionCommand(String.valueOf(ActionCommand.EXPORT));
    exportButton.addActionListener(this);
    buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.X_AXIS));
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
    setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
    stopButton.setEnabled(false);
    buttonsPanel.add(stopButton);

    upperPanel.add(buttonsPanel);

    add(upperPanel);
    timer = new Timer(100, this);

    lowerPanel = new JPanel();
    FlowLayout flowLayout = (FlowLayout) lowerPanel.getLayout();
    flowLayout.setAlignment(FlowLayout.LEFT);
    add(lowerPanel);

    polyphonyPanel = new JPanel();
    lowerPanel.add(polyphonyPanel);
    polyphonyPanel.setLayout(new BoxLayout(polyphonyPanel, BoxLayout.X_AXIS));

    polyphonyHeaderLabel = new JLabel("Polyphony: ");
    polyphonyHeaderLabel.setFont(headerFont);
    polyphonyPanel.add(polyphonyHeaderLabel);

    polyphonySlider = new JSlider();
    polyphonySlider.setValue(MIDIStream.VOICES_DEFAULT);
    polyphonySlider.setMinimum(MIDIStream.VOICES_MIN);
    polyphonySlider.setMaximum(5000);
    polyphonySlider.addChangeListener(this);
    polyphonySlider.setName(ActionCommand.POLY_SLIDER);
    polyphonyPanel.add(polyphonySlider);

    polyphonySpinner = new JSpinner();
    polyphonySpinner.setFont(normalFont);
    polyphonySpinner.setModel(
        new SpinnerNumberModel(MIDIStream.VOICES_DEFAULT, MIDIStream.VOICES_MIN, MIDIStream.VOICES_MAX, 1));
    polyphonySpinner.setName(ActionCommand.POLY_SPINNER);
    polyphonySpinner.addChangeListener(this);
    polyphonyPanel.add(polyphonySpinner);
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
        File preDefinedFile = new File(System.getProperty("user.home") + "/" + myStream.getFileName() + ".wav");
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
          if (selectedFile.exists() && JOptionPane.showConfirmDialog(this,
              selectedFile.getName() + " already exists. Do you want to overwrite it?", "Overwrite confirmation",
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

  @Override
  public void stateChanged(ChangeEvent e) {
    JComponent source = ((JComponent) e.getSource());
    String sourceName = source.getName();
    if (sourceName != null && myStream != null) {

      if (sourceName.equals(ActionCommand.POLY_SLIDER)) {
        int value = polyphonySlider.getValue();
        myStream.setVoicesLimit(value);
        polyphonySpinner.setValue(value);
      } else if (sourceName.equals(ActionCommand.POLY_SPINNER)) {
        int value = (int) polyphonySpinner.getValue();
        myStream.setVoicesLimit(value);
        polyphonySlider.setValue(value);
      }
    }
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
        // -で始まらないスイッチ名
        //TODO: ファイル名
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
    System.setProperty("java.library.path", System.getProperty("java.library.path") + ":.");
    if (argsMap.containsKey("lib")) {
      System.setProperty("java.library.path", System.getProperty("java.library.path") + ":" + args[1]);
      System.out.println(args[1] + "added to java.library.path.");
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
    mainWindow.setResizable(false);
    mainWindow.setVisible(true);

    if (argsMap.containsKey("sf")) {
      mainPanel.setSFFile(new File(argsMap.get("sf")));
    }
  }
}
