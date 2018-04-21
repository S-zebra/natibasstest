package util;

import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Formatter;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import javax.sound.sampled.AudioFileFormat.Type;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import jouvieje.bass.Bass;
import jouvieje.bass.callbacks.SYNCPROC;
import jouvieje.bass.defines.BASS_ATTRIB;
import jouvieje.bass.defines.BASS_DATA;
import jouvieje.bass.defines.BASS_MIDI;
import jouvieje.bass.defines.BASS_POS;
import jouvieje.bass.defines.BASS_SAMPLE;
import jouvieje.bass.defines.BASS_SYNC_MIDI;
import jouvieje.bass.defines.MIDI_EVENT;
import jouvieje.bass.structures.BASS_MIDI_EVENT;
import jouvieje.bass.structures.BASS_MIDI_MARK;
import jouvieje.bass.structures.HFX;
import jouvieje.bass.structures.HSTREAM;
import jouvieje.bass.structures.HSYNC;
import jouvieje.bass.utils.BufferUtils;
import jouvieje.bass.utils.Pointer;
import miditest.MainWindow;

public class MIDIStream {
  private File midiFile;
  private int flags = BASS_SAMPLE.BASS_SAMPLE_FLOAT | BASS_MIDI.BASS_MIDI_DECAYEND;// | BASS_MIDI.BASS_MIDI_DECAYSEEK;
  private int freq = 44100;
  private HSTREAM handle;
  private SoundFont soundFont;
  private boolean loaded = false;
  private boolean playing = false;
  private ActionListener acl;
  private String command;
  // Stats
  private int curNotes, totalNotes;

  public MIDIStream(File file) {
    this(file, true, null, null);
  }

  public MIDIStream(File file, boolean autoLoad, ActionListener acl, String actionCommnand) {
    this.acl = acl;
    this.command = actionCommnand;
    setFile(file, autoLoad);
  }

  public void setFile(File file, boolean autoload) {
    if (!file.exists()) {
      System.err.println(file.getAbsolutePath() + " does not exist");
      return;
    }
    if (handle != null) {
      Bass.BASS_ChannelSetPosition(handle.asInt(), 0, 0);
      Bass.BASS_StreamFree(handle);
      loaded = false;
      playing = false;
      handle = null;
    }
    midiFile = file;

    if (autoload)
      load();
  }

  private boolean load() {
    if (!loaded) {
      ByteBuffer filePointer = BufferUtils.fromString(midiFile.getAbsolutePath());
      handle = Bass.BASS_MIDI_StreamCreateFile(false, BufferUtils.asPointer(filePointer), 0, 0, flags, freq);
      if (soundFont != null) {
        setSoundFont(soundFont);
      }
      BassErrors.printError();
      totalNotes = Bass.BASS_MIDI_StreamGetEvents(handle, -1, MIDI_EVENT.MIDI_EVENT_NOTE, null) / 2;

      if (acl != null) {
        setNoteCounter();
      }
      BassErrors.printError();
      loaded = BassErrors.wasSuccess();
    }
    return loaded;
  }

  public void play() {
    ensureLoaded();
    Bass.BASS_ChannelPlay(handle.asInt(), false);
    System.out.println("play, sf: " + soundFont.toString());
    BassErrors.printError();
    playing = true;
  }

  public void pause() {
    ensureLoaded();
    Bass.BASS_ChannelPause(handle.asInt());
    System.out.println("pause");
    BassErrors.printError();
    playing = false;
  }

  public void stop() {
    ensureLoaded();
    Bass.BASS_ChannelStop(handle.asInt());
    System.out.println("stop");
    Bass.BASS_ChannelSetPosition(handle.asInt(), 0, 0);
    curNotes = 0;
    playing = false;
  }

  public void export(String path) {
    export(new File(path), null);
  }

  public void export(File dst) {
    export(dst, null);
  }

  public void export(File dst, ExportStateReceiver receiver) {
    ensureLoaded();
    if (receiver == null) {
      throw new IllegalArgumentException("receiver cannot be null.");
    }
    Bass.BASS_ChannelSetPosition(handle.asInt(), 0, BASS_POS.BASS_POS_BYTE); // 先頭に巻き戻す

    int channelLength = (int) Bass.BASS_ChannelGetLength(handle.asInt(), BASS_POS.BASS_POS_BYTE); // チャンネルの総バイト数
    int extraSecs = 6;
    System.out.println(channelLength);
    byte[] outputArray;
    try {
      outputArray = new byte[channelLength + (44100 * 4 * 2 * extraSecs)]; // 書き出し用、Byteのリスト
    } catch (OutOfMemoryError ex) { // OOME
      Runtime runtime = Runtime.getRuntime();
      receiver.onExportError(
          "Sorry, there is insufficient JVM memory. This may be happened because the song is too long.\n"
              + String.format("Technical info: Free: %,dM / Total: %,dM (Est. stream size: %,dM)", runtime.freeMemory() / 1000000, runtime.totalMemory() / 1000000, channelLength / 1000000),
          "Memory out of bounds!!");
      return;
    }
    receiver.setTotalBytes(channelLength);

    int renderedBytes = 0;
    while (BassErrors.wasSuccess()) {
      Bass.BASS_ChannelUpdate(handle.asInt(), 0);

      int nBytes = Bass.BASS_ChannelGetData(handle.asInt(), null, BASS_DATA.BASS_DATA_AVAILABLE); // レンダリング済みのバイト数

      ByteBuffer byteBuffer = BufferUtils.newByteBuffer(nBytes);
      Bass.BASS_ChannelGetData(handle.asInt(), byteBuffer, BASS_DATA.BASS_DATA_FLOAT | nBytes);

      byteBuffer.rewind();
      for (int i = 0; i < byteBuffer.limit(); i++) {
        outputArray[renderedBytes + i] = byteBuffer.get();
      }
      renderedBytes += nBytes;

      receiver.onProgressUpdate(renderedBytes);

      // 次の箇所へシークする (失敗ならbreak)
      Bass.BASS_ChannelSetPosition(handle.asInt(), renderedBytes, BASS_POS.BASS_POS_BYTE | 0x20000000);
    }

    receiver.onRenderingFinished();

    /*
     * 書き出し用PCMフォーマット 44.1 kHz, 32 bits/sample, 2ch, 8 (32bit*2ch) bytes/frame,
     * 44.1k frames/sec, little endian
     */
    AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_FLOAT, 44100, 32, 2, 8, 44100, false);
    BufferedInputStream audioSourceStream = new BufferedInputStream(new ByteArrayInputStream(outputArray));
    AudioInputStream audioInStream = new AudioInputStream(audioSourceStream, format, outputArray.length);
    try {
      AudioSystem.write(audioInStream, Type.WAVE, dst);
    } catch (IOException e) {
      receiver.onExportError("An I/O error happened.\n" + e.getLocalizedMessage(), "File save error");
    }

    receiver.onExportFinished();

  }

  public static byte[] byteBufferToArray(ByteBuffer buf) {
    byte[] res = new byte[buf.capacity()];
    buf.rewind();
    for (int i = 0; i < res.length; i++) {
      res[i] = buf.get();
    }
    return res;
  }

  public HSTREAM getHandle() {
    ensureLoaded();
    return handle;
  }

  public String getSongName() {
    ensureLoaded();
    IntBuffer buffer = BufferUtils.newIntBuffer(256);
    if (!Bass.BASS_MIDI_StreamGetMark(handle, NEW_BASS_MIDI_MARK.BASS_MIDI_MARK_TRACK, 0, buffer)) {
      return "[NO TITLE]";
    } else {
      BassErrors.printError();
      BASS_MIDI_MARK mark = BASS_MIDI_MARK.asBASS_MIDI_MARK(BufferUtils.asPointer(buffer));
      return mark.getText();
    }
  }

  public int noteCount() {
    return totalNotes;
  }

  public int currentLevel() {
    return Bass.BASS_ChannelGetLevel(handle.asInt());
  }

  public boolean isPlaying() {
    return playing;
  }

  public int getPosition() {
    ensureLoaded();
    int pos = (int) Bass.BASS_ChannelGetPosition(handle.asInt(), BASS_POS.BASS_POS_MIDI_TICK);
    BassErrors.printError();
    // System.out.println((Bass.BASS_ChannelGetPosition(handle.asInt(),
    // BASS_POS.BASS_POS_BYTE) / 1000) + " k");
    return pos;
  }

  public void setPosition(long ticks) {
    ensureLoaded();
    Bass.BASS_ChannelSetPosition(handle.asInt(), ticks, BASS_POS.BASS_POS_MIDI_TICK);
    BassErrors.printError();
  }

  public int length() {
    ensureLoaded();
    int ticks = (int) Bass.BASS_ChannelGetLength(handle.asInt(), BASS_POS.BASS_POS_MIDI_TICK);
    BassErrors.printError();
    return ticks;
  }

  public float getVoicesActive() {
    ensureLoaded();
    return 0.0f;
  }

  public void setVoicesLimit(float voices) {
    ensureLoaded();
    Bass.BASS_ChannelSetAttribute(getHandle().asInt(), BASS_ATTRIB.BASS_ATTRIB_MIDI_VOICES, voices);
  }

  public void setCPULimit(int percentage) {
    ensureLoaded();
    Bass.BASS_ChannelSetAttribute(handle.asInt(), BASS_ATTRIB.BASS_ATTRIB_MIDI_CPU, percentage);
  }

  public SoundFont getSoundFont() {
    return this.soundFont;
  }

  public void setSoundFont(SoundFont soundFont) {
    if (handle != null) {
      Bass.BASS_MIDI_StreamSetFonts(handle, soundFont.toBASS_MIDI_FONT(), 1);
    }
    BassErrors.printError();
    this.soundFont = soundFont;
  }

  // Private methods
  private void ensureLoaded() {
    if (!loaded) {
      throw new IllegalStateException("Stream is not loaded.\nCall load() before this method.");
    }
  }

  private void setNoteCounter() {
    Bass.BASS_ChannelSetSync(handle.asInt(), BASS_SYNC_MIDI.BASS_SYNC_MIDI_EVENT, MIDI_EVENT.MIDI_EVENT_NOTE, new SYNCPROC() {
      @Override
      public void SYNCPROC(HSYNC handle, int channel, int data, Pointer user) {
        int vel = data & 0x0000ffff >> 8; // Vel
        if (vel != 0)
          curNotes++;
        System.out.println(vel);
        // acl.actionPerformed(new ActionEvent(this, 0, command));
      }
    }, null);
  }

}
