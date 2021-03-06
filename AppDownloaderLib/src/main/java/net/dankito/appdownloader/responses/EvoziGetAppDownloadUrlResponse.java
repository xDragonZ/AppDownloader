package net.dankito.appdownloader.responses;

import net.dankito.appdownloader.util.StringUtils;

/**
 * Created by ganymed on 02/11/16.
 */

public class EvoziGetAppDownloadUrlResponse {


  protected String url;

  protected boolean isSuccessful = false;

  protected String status;

  protected String packagename;

  protected String filesize;

  protected String md5;

  protected String version;

  protected int version_code;

  protected String fetched_at;

  protected boolean cache;

  protected String state;


  // in error cases

  protected String data;

  protected String http_code;



  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;

    this.isSuccessful = "success".equals(status);
  }

  public String getPackagename() {
    return packagename;
  }

  public void setPackagename(String packagename) {
    this.packagename = packagename;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;

    if(StringUtils.isNotNullOrEmpty(url) && url.startsWith("http") == false) {
      this.url = "https:" + url;
    }
  }

  public String getFilesize() {
    return filesize;
  }

  public void setFilesize(String filesize) {
    this.filesize = filesize;
  }

  public String getMd5() {
    return md5;
  }

  public void setMd5(String md5) {
    this.md5 = md5;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public int getVersion_code() {
    return version_code;
  }

  public void setVersion_code(int version_code) {
    this.version_code = version_code;
  }

  public String getFetched_at() {
    return fetched_at;
  }

  public void setFetched_at(String fetched_at) {
    this.fetched_at = fetched_at;
  }

  public boolean isCache() {
    return cache;
  }

  public void setCache(boolean cache) {
    this.cache = cache;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }


  public boolean hasErrorOccurred() {
    return "error".equals(getStatus()) || StringUtils.isNotNullOrEmpty(getData());
  }

  public String getData() {
    return data;
  }

  public void setData(String data) {
    this.data = data;
  }

  public String getHttp_code() {
    return http_code;
  }

  public void setHttp_code(String http_code) {
    this.http_code = http_code;
  }

}
