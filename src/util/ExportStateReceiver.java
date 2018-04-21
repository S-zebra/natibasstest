package util;

public interface ExportStateReceiver {
  public void setTotalBytes(int totalBytes);
  public void onProgressUpdate(int completedBytes);
  public void onRenderingFinished();
  public void onExportFinished();
  public void onExportError(String message, String title);
}
