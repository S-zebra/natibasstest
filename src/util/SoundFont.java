package util;

import java.io.File;
import java.nio.Buffer;

import jouvieje.bass.Bass;
import jouvieje.bass.structures.BASS_MIDI_FONT;
import jouvieje.bass.structures.BASS_MIDI_FONTINFO;
import jouvieje.bass.structures.HSOUNDFONT;
import jouvieje.bass.utils.BufferUtils;

public class SoundFont {
  private File file;
  private HSOUNDFONT handle;
  private BASS_MIDI_FONT font;
  private int flags;
  private BASS_MIDI_FONTINFO fontinfo;

  public SoundFont(File file, int flags, boolean preload) {
    this.file = file;
    this.flags = flags;
    Buffer buffer = BufferUtils.fromString(file.getAbsolutePath());
    handle = Bass.BASS_MIDI_FontInit(BufferUtils.asPointer(buffer), this.flags);
    font = BASS_MIDI_FONT.allocate();
    font.setFont(handle);
    font.setPreset(-1);
    font.setBank(0);

    fontinfo = BASS_MIDI_FONTINFO.allocate();
    updateFontInfo();

    if (preload)
      fontLoad(-1, 0);
    BassErrors.printError();
  }

  public String getName() {
    updateFontInfo();
    return fontinfo.getName();
  }

  public int getLoadedSamples() {
    updateFontInfo();
    return fontinfo.getSampleLoad();
  }

  public int getTotalSamples() {
    updateFontInfo();
    return fontinfo.getSampleSize();
  }

  public boolean fontLoad() {
    return fontLoad(-1, 0);
  }

  public boolean fontLoad(int preset, int bank) {
    return Bass.BASS_MIDI_FontLoad(handle, preset, bank);
  }

  public boolean setVolume(float volume) {
    return Bass.BASS_MIDI_FontSetVolume(handle, volume);
  }

  public BASS_MIDI_FONT toBASS_MIDI_FONT() {
    return font;
  }

  private void updateFontInfo() {
    Bass.BASS_MIDI_FontGetInfo(handle, fontinfo);
  }
}
