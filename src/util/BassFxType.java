package util;

public enum BassFxType {
  BASS_FX_DX8_CHORUS(0),
  BASS_FX_DX8_COMPRESSOR(1),
  BASS_FX_DX8_DISTORTION(2),
  BASS_FX_DX8_ECHO(3),
  BASS_FX_DX8_FLANGER(4),
  BASS_FX_DX8_GARGLE(5),
  BASS_FX_DX8_I3DL2REVERB(6),
  BASS_FX_DX8_PARAMEQ(6),
  BASS_FX_DX8_REVERB(7);
  
  private final int id;
  
  private BassFxType(final int id) {
    this.id=id;
  }
  
  public int getId() {
    return id;
  }
  
}
