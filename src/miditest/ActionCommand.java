package miditest;

public interface ActionCommand {
  public static final char SONG_CHANGE = 'a';
  public static final char SF_CHANGE = 'b';
  public static final char PLAY_PAUSE = 'c';
  public static final char STOP = 'd';
  public static final char TIMER = 'e';
  public static final char NOTE = 'f';
  public static final char EXPORT = 'g';
  public static final String POLY_SPINNER = "polyphonySpinner";
  public static final String POLY_SLIDER = "polyphonySlider";
  public static final String SONG_POS_SLIDER = "songPosSlider";
}
