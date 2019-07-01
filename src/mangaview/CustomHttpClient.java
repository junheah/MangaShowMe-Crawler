package mangaview;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CustomHttpClient {
    OkHttpClient client;
    Preference p;
    Map<String,String> cfc;

    public CustomHttpClient(Preference p){
        this.p = p;
        this.client = getUnsafeOkHttpClient().followRedirects(false).followSslRedirects(false).build();
        this.cfc = new HashMap<>();
    }

    public Response getRaw(String url, Map<String, String> cookies){
//        if(!isloaded){
//            cloudflareDns.init();
//            isloaded = true;
//        }
        Response response = null;
        try {
            String cookie = "";
            for(String key : cookies.keySet()){
                cookie += key + '=' + cookies.get(key) + "; ";
            }

            Request request = new Request.Builder()
                    .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.98 Safari/537.36")
                    .addHeader("Cookie", cookie)
                    .addHeader("Accept", "*")
                    .url(url)
                    .get()
                    .build();
            response = client.newCall(request)
                    .execute();
//            if(response != null){
//                if(response.code()>=500){
//                    System.out.println("cf");
//                }
//            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return response;
    }

    public Response get(String url, Boolean doLogin){
        Map<String, String> cookies = new HashMap<>();
//        Login login = p.getLogin();
//        if(doLogin && login!=null){
//            login.buildCookie(cookies);
//        }
        return getRaw(p.getUrl()+url,cookies);
    }
    public Response get(String url){
        return get(url,true);
    }

    public Response get(String url,Boolean doLogin, Map<String, String> customCookie){
//        Login login = p.getLogin();
//        if(doLogin && login!=null){
//            login.buildCookie(customCookie);
//        }
        return getRaw(p.getUrl()+url, customCookie);
    }

    public Response post(String url, RequestBody body){
//        if(!isloaded){
//            cloudflareDns.init();
//            isloaded = true;
//        }
        Response response = null;
        try {
            String cookie = "";
//            if(p.getLogin()!=null){
//                cookie = p.getLogin().getCookie(true);
//            }

            Request request = new Request.Builder()
                    .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.98 Safari/537.36")
                    .addHeader("Cookie", cookie)
                    .url(p.getUrl() + url)
                    .post(body)
                    .build();
            response = client.newCall(request)
                    .execute();
        }catch (Exception e){

        }
        return response;

    }
    
    public Response postRaw(String url, RequestBody body, Map<String,String> cookiem) {
    	Response response = null;
        try {
            String cookie = "";
            
            for(String key: cookiem.keySet()) {
            	cookie+= key+'='+cookiem.get(key)+';';
            }

            Request request = new Request.Builder()
                    .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.98 Safari/537.36")
                    .addHeader("Cookie", cookie)
                    .url(url)
                    .post(body)
                    .build();
            response = client.newCall(request)
                    .execute();
        }catch (Exception e){

        }
        return response;
    }

    /*
    code source : https://gist.github.com/chalup/8706740
     */

    private static OkHttpClient.Builder getUnsafeOkHttpClient() {
        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain,
                                                       String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain,
                                                       String authType) throws CertificateException {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }
                    }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            return new OkHttpClient.Builder()
                    .sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0])
                    .hostnameVerifier(new HostnameVerifier() {
                        @Override
                        public boolean verify(String hostname, SSLSession session) {
                            return true;
                        }
                    });

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }


    public void setCookie(String raw){
        this.cfc = new HashMap<>();
        String[] splitted = raw.split(";");
        for(String s : splitted){
            String[] s2 = s.split("=");
            String key = s2[0];
            String val = s2[1];
            if(key.indexOf(' ')==0){
                key = key.substring(1);
            }
            if(!key.contains("PHPSESSID")) {
                cfc.put(key,val);
            }
        }
    }
}
