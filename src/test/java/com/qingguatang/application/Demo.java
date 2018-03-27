package com.qingguatang.application;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Arrays;
import java.util.Collection;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.logging.HttpLoggingInterceptor.Level;
import retrofit2.Call;
import retrofit2.Retrofit;

public class Demo {


  private static OkHttpClient client;


  public static void run() throws Exception {
    Request request = new Request.Builder()
        .url("https://localhost:8443")
        .build();

    Response response = client.newCall(request).execute();
    System.out.println(response.body().string());
  }

  private static InputStream trustedCertificatesInputStream() {

    return Demo.class.getClassLoader().getResourceAsStream("cert/ca.pem");
  }

  private static X509TrustManager trustManagerForCertificates(InputStream in)
      throws GeneralSecurityException {
    CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
    Collection<? extends Certificate> certificates = certificateFactory.generateCertificates(in);
    if (certificates.isEmpty()) {
      throw new IllegalArgumentException("expected non-empty set of trusted certificates");
    }

    // Put the certificates a key store.
    char[] password = "unreach".toCharArray(); // Any password will work.
    KeyStore keyStore = newEmptyKeyStore(password);
    int index = 0;
    for (Certificate certificate : certificates) {
      String certificateAlias = Integer.toString(index++);
      keyStore.setCertificateEntry(certificateAlias, certificate);
    }

    // Use it to build an X509 trust manager.
    KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(
        KeyManagerFactory.getDefaultAlgorithm());
    keyManagerFactory.init(keyStore, password);
    TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
        TrustManagerFactory.getDefaultAlgorithm());
    trustManagerFactory.init(keyStore);
    TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
    if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
      throw new IllegalStateException("Unexpected default trust managers:"
          + Arrays.toString(trustManagers));
    }
    return (X509TrustManager) trustManagers[0];
  }

  private static KeyStore newEmptyKeyStore(char[] password) throws GeneralSecurityException {
    try {
      KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
      InputStream in = null; // By convention, 'null' creates an empty key store.
      keyStore.load(in, password);
      return keyStore;
    } catch (IOException e) {
      throw new AssertionError(e);
    }
  }

  protected static HostnameVerifier getHostnameVerifier(final String[] hostUrls) {

    HostnameVerifier TRUSTED_VERIFIER = new HostnameVerifier() {

      public boolean verify(String hostname, SSLSession session) {
        boolean ret = true;
//        for (String host : hostUrls) {
//          if (host.equalsIgnoreCase(hostname)) {
//            ret = true;
//          }
//        }
        return ret;
      }
    };

    return TRUSTED_VERIFIER;

  }

  public static void main(String[] args) throws Exception {

    X509TrustManager trustManager;
    SSLSocketFactory sslSocketFactory;
    try {
      trustManager = trustManagerForCertificates(trustedCertificatesInputStream());
      SSLContext sslContext = SSLContext.getInstance("TLS");
      sslContext.init(null, new TrustManager[]{trustManager}, null);
      sslSocketFactory = sslContext.getSocketFactory();
    } catch (GeneralSecurityException e) {
      throw new RuntimeException(e);
    }

    client = new OkHttpClient.Builder()
        .sslSocketFactory(sslSocketFactory, trustManager)
        .hostnameVerifier(getHostnameVerifier(null))
        .addInterceptor(new HttpLoggingInterceptor().setLevel(Level.HEADERS))
        .build();

    // run();

    Retrofit retrofit = new Retrofit.Builder()
        .baseUrl("https://localhost:8443/")
        .client(client)
        .build();

    HelloService service = retrofit.create(HelloService.class);

    Call<ResponseBody> value = service.hello();

    try {
      System.out.println(value.execute().body().string());
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

}
