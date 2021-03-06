package net.dankito.appdownloader.app.model;

/**
 * Created by ganymed on 18/11/16.
 */

public enum HashAlgorithm {

  // for supported algorithms see: https://developer.android.com/reference/java/security/MessageDigest.html

  MD5("MD5"),
  SHA1("SHA-1"),
  SHA256("SHA256");


  private String algorithmName;


  HashAlgorithm(String algorithmName) {
    this.algorithmName = algorithmName;
  }


  public String getAlgorithmName() {
    return algorithmName;
  }

}
