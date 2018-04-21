package effects;

import java.nio.ByteBuffer;

import jouvieje.bass.Bass;
import jouvieje.bass.structures.HFX;
import jouvieje.bass.structures.HSTREAM;
import jouvieje.bass.utils.BufferUtils;
import jouvieje.bass.utils.Pointer;
import util.BassErrors;
import util.BassFxType;
import util.BufferDebugger;

public class Chorus extends EffectsBase {
  private static final int EFFECT_ID = 0;
  float wetdrymix, depth, feedback, frequency, delay;
  int waveform, phase;
  private HSTREAM streamHandle;
  private HFX fxHandle;
  private ByteBuffer buffer;

  public Chorus(HSTREAM stream) {
    this(stream, 50.0f, 10.0f, 25.0f, 1.1f, 1, 16.0f, 0);
  }

  public Chorus(HSTREAM stream, float wetDryMix, float depth, float feedback, float frequency, int waveform,
      float delay, int phase) {
    this.wetdrymix = wetDryMix;
    this.depth = depth;
    this.feedback = feedback;
    this.frequency = frequency;
    this.waveform = waveform;
    this.delay = delay;
    this.phase = phase;
    this.streamHandle = stream;
    buffer = BufferUtils.newByteBuffer((BufferUtils.SIZEOF_FLOAT * 5) + (BufferUtils.SIZEOF_INT * 2));
    this.fxHandle = Bass.BASS_ChannelSetFX(stream.asInt(), BassFxType.BASS_FX_DX8_CHORUS.getId(), 1);
    BassErrors.checkError();
  }

  public float getWetdrymix() {
    return wetdrymix;
  }

  public void setWetdrymix(float wetdrymix) {
    this.wetdrymix = wetdrymix;
    Bass.BASS_FXSetParameters(fxHandle, this.asPointer());
  }

  public float getDepth() {
    return depth;
  }

  public void setDepth(float depth) {
    this.depth = depth;
    Bass.BASS_FXSetParameters(fxHandle, this.asPointer());
  }

  public float getFeedback() {
    return feedback;
  }

  public void setFeedback(float feedback) {
    this.feedback = feedback;
    Bass.BASS_FXSetParameters(fxHandle, this.asPointer());
  }

  public float getFrequency() {
    return frequency;
  }

  public void setFrequency(float frequency) {
    this.frequency = frequency;
    Bass.BASS_FXSetParameters(fxHandle, this.asPointer());
  }

  public float getDelay() {
    return delay;
  }

  public void setDelay(float delay) {
    this.delay = delay;
    Bass.BASS_FXSetParameters(fxHandle, this.asPointer());
    System.out.println(buffer.toString());
    BassErrors.checkError();
  }

  public int getWaveform() {
    return waveform;
  }

  public void setWaveform(int waveform) {
    this.waveform = waveform;
    Bass.BASS_FXSetParameters(fxHandle, this.asPointer());
  }

  public int getPhase() {
    return phase;
  }

  public void setPhase(int phase) {
    this.phase = phase;
    Bass.BASS_FXSetParameters(fxHandle, this.asPointer());
  }

  public Pointer asPointer() {
    buffer.putFloat(wetdrymix);
    buffer.putFloat(depth);
    buffer.putFloat(feedback);
    buffer.putFloat(frequency);
    buffer.putInt(waveform);
    buffer.putFloat(delay);
    buffer.putInt(phase);
    buffer.rewind();
    BufferDebugger.dumpBuffer(buffer);
    return BufferUtils.asPointer(buffer);
  }
}
