package com.cmtsbsnl.cnmc;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

public class MyHttpClient {
    public static Context context;

    public MyHttpClient(Context context) {
        MyHttpClient.context = context;
    }

    public HttpsURLConnection getUrlConnection(String httpsurl){
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            InputStream caInput = new BufferedInputStream(context.getResources().getAssets().open("cmtsktk.crt"));
            Certificate ca;
            ca = cf.generateCertificate(caInput);
//            Log.i("Longer", "ca=" + ((X509Certificate) ca).getSubjectDN());
//            Log.i("Longer", "key=" + ca.getPublicKey());
            caInput.close();

            // Create a KeyStore containing our trusted CAs
            String keyStoreType = KeyStore.getDefaultType();
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);

            // Create a TrustManager that trusts the CAs in our KeyStore
            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);

            SSLContext context = SSLContext.getInstance("TLSv1","AndroidOpenSSL");
            context.init(null, tmf.getTrustManagers(), null);

            URL url = new URL(httpsurl);
            HttpsURLConnection urlConnection = (HttpsURLConnection)url.openConnection();
            urlConnection.setSSLSocketFactory(context.getSocketFactory());
            urlConnection.setHostnameVerifier((s, sslSession) -> s.equals(Constants.getServerIP()));



            return urlConnection;
        } catch (CertificateException | IOException | KeyStoreException | NoSuchAlgorithmException | NoSuchProviderException | KeyManagementException e) {
            e.printStackTrace();
        }
        return null;
    }

    public HttpsURLConnection getUrlConnectionTrustAll(String httpsurl){
        try {
            trustEveryone();
            URL url = new URL(httpsurl);
            return (HttpsURLConnection) url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;


    }

    private void trustEveryone() {
        try {
            HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new X509TrustManager[]{new X509TrustManager(){
                public void checkClientTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {}
                public void checkServerTrusted(X509Certificate[] chain,
                                               String authType) {}
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }}}, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(
                    context.getSocketFactory());
        } catch (Exception e) { // should never happen
            e.printStackTrace();
        }
    }

    public String getUrlConnection(String httpsurl, String post_data ){
        try {
            SharedPreferences sharedPreferences = new Preferences(context).getEncryptedSharedPreferences();
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            InputStream caInput = new BufferedInputStream(context.getResources().getAssets().open("cmtsktk.crt"));
            Certificate ca;
            ca = cf.generateCertificate(caInput);
//            Log.i("Longer", "ca=" + ((X509Certificate) ca).getSubjectDN());
//            Log.i("Longer", "key=" + ca.getPublicKey());
            caInput.close();

            // Create a KeyStore containing our trusted CAs
            String keyStoreType = KeyStore.getDefaultType();
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);

            // Create a TrustManager that trusts the CAs in our KeyStore
            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);

            SSLContext context = SSLContext.getInstance("TLSv1","AndroidOpenSSL");
            context.init(null, tmf.getTrustManagers(), null);

            URL url = new URL(httpsurl);
            HttpsURLConnection urlConnection = (HttpsURLConnection)url.openConnection();
            urlConnection.setSSLSocketFactory(context.getSocketFactory());
            urlConnection.setHostnameVerifier((s, sslSession) -> s.equals(Constants.getServerIP()));
            urlConnection.setRequestProperty("Context-Type","application/json; utf-8");
            urlConnection.setRequestProperty("Authorization", sharedPreferences.getString("web_token",""));
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoInput(true);
            urlConnection.setDoInput(true);
            OutputStream os = urlConnection.getOutputStream();
//            System.out.println(post_data.toString());
            os.write(post_data.getBytes());
            urlConnection.connect();
            BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
//            System.out.println(br.readLine());
            return br.readLine();


//            return urlConnection;
        } catch (IOException | GeneralSecurityException e) {
            e.printStackTrace();
        }
        return null;
    }
}
