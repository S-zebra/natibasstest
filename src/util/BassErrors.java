package util;

import java.util.HashMap;

import jouvieje.bass.Bass;
import jouvieje.bass.exceptions.BassException;

public final class BassErrors {
  private static BassErrors instance = new BassErrors();
  private HashMap<Integer, String> errorMap = new HashMap<>();

  private BassErrors() {
    errorMap.put(0, "BASS_OK");
    errorMap.put(1, "BASS_ERROR_MEM");
    errorMap.put(2, "BASS_ERROR_FILEOPEN");
    errorMap.put(3, "BASS_ERROR_DRIVER");
    errorMap.put(4, "BASS_ERROR_BUFLOST");
    errorMap.put(5, "BASS_ERROR_HANDLE");
    errorMap.put(6, "BASS_ERROR_FORMAT");
    errorMap.put(7, "BASS_ERROR_POSITION");
    errorMap.put(8, "BASS_ERROR_INIT");
    errorMap.put(9, "BASS_ERROR_START");
    errorMap.put(10, "BASS_ERROR_SSL");
    errorMap.put(14, "BASS_ERROR_ALREADY");
    errorMap.put(18, "BASS_ERROR_NOCHAN");
    errorMap.put(19, "BASS_ERROR_ILLTYPE");
    errorMap.put(20, "BASS_ERROR_ILLPARAM");
    errorMap.put(21, "BASS_ERROR_NO3D");
    errorMap.put(22, "BASS_ERROR_NOEAX");
    errorMap.put(23, "BASS_ERROR_DEVICE");
    errorMap.put(24, "BASS_ERROR_NOPLAY");
    errorMap.put(25, "BASS_ERROR_FREQ");
    errorMap.put(27, "BASS_ERROR_NOTFILE");
    errorMap.put(29, "BASS_ERROR_NOHW");
    errorMap.put(31, "BASS_ERROR_EMPTY");
    errorMap.put(32, "BASS_ERROR_NONET");
    errorMap.put(33, "BASS_ERROR_CREATE");
    errorMap.put(34, "BASS_ERROR_NOFX");
    errorMap.put(37, "BASS_ERROR_NOTAVAIL");
    errorMap.put(38, "BASS_ERROR_DECODE");
    errorMap.put(39, "BASS_ERROR_DX");
    errorMap.put(40, "BASS_ERROR_TIMEOUT");
    errorMap.put(41, "BASS_ERROR_FILEFORM");
    errorMap.put(42, "BASS_ERROR_SPEAKER");
    errorMap.put(43, "BASS_ERROR_VERSION");
    errorMap.put(44, "BASS_ERROR_CODEC");
    errorMap.put(45, "BASS_ERROR_ENDED");
    errorMap.put(46, "BASS_ERROR_BUSY");
    errorMap.put(-1, "BASS_ERROR_UNKNOWN");
  }

  public static String getErrorString(int ec) {
    return instance.gerErr(ec);
  }

  public static void printError() {
    int error=Bass.BASS_ErrorGetCode();
    if (error!=0) System.err.println(error+": "+getErrorString(error));
  }

  private String gerErr(int code) {
    return errorMap.containsKey(code) ? errorMap.get(code) : "undefined";
  }

  public static boolean wasSuccess() {
    return Bass.BASS_ErrorGetCode() == 0;
  }

  public static void checkError() {
    if(!wasSuccess()) {
      throw new BassException(instance.gerErr(Bass.BASS_ErrorGetCode()));
    }
  }
}
