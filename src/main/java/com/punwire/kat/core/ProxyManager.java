package com.punwire.kat.core;




import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;



public final class ProxyManager
{

    private static ProxyManager instance;
    //private static Preferences corePreferences = NbPreferences.root().node("/org/netbeans/core");
    private HttpClient client=null;
    private RequestConfig requestConfig;
    private boolean isOnline;

    public static ProxyManager getDefault()
    {
        if (instance == null)
        {
            instance = new ProxyManager();
        }
        return instance;
    }

    private ProxyManager()
    {
        setOnline(true);

        System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
        System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
        System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.commons.httpclient", "error");


        client = HttpClients.createDefault();
        requestConfig = RequestConfig.custom()
                .setSocketTimeout(1000)
                .setConnectTimeout(1000)
                .build();
    }

    public HttpClient httpClient()
    {
        return client;
    }

    public String inputStringPOST(String url, NameValuePair[] query, NameValuePair[] request)
            throws IOException
    {
        String response = "";
//        PostMethod method = new PostMethod(url);
//        method.setQueryString(query);
//        method.setRequestBody(request);
//
//        int status = client.executeMethod(method);
//        if (status != HttpStatus.SC_OK)
//        {
//            throw new IOException(method.getStatusText());
//        } else
//        {
//            InputStream is = method.getResponseBodyAsStream();
//            BufferedInputStream bis = new BufferedInputStream(is);
//
//            String datastr = null;
//            StringBuilder sb = new StringBuilder();
//            byte[] bytes = new byte[8192]; // reading as chunk of 8192 bytes
//
//            int count = bis.read(bytes);
//            while (count != -1 && count <= 8192)
//            {
//                datastr = new String(bytes, 0, count);
//                sb.append(datastr);
//                count = bis.read(bytes);
//            }
//
//            bis.close();
//            response = sb.toString();
//        }
//
//        method.releaseConnection();

        return response;
    }

    public InputStream inputStreamGET(String url)
            throws IOException
    {
        InputStream stream = null;
        HttpGet httpget = new HttpGet(url);

        HttpResponse response = client.execute(httpget);
        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK)
        {
            throw new IOException("Error while URL GET");
        } else
        {
            HttpEntity entity = response.getEntity();
            stream = entity.getContent();
        }

        return stream;
    }

    public BufferedReader bufferReaderGET(String url)
            throws IOException
    {
        InputStream stream = inputStreamGET(url);
        if (stream != null)
        {
            return new BufferedReader(new InputStreamReader(stream));
        }
        return null;
    }

    public InputStream inputStreamPOST(String url, NameValuePair[] query)
            throws IOException
    {
        InputStream stream = null;
        HttpPost post = new HttpPost(url);
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        for(NameValuePair pair: query){
            nameValuePairs.add(new BasicNameValuePair(pair.getName(), pair.getValue()));
        }
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(nameValuePairs,"utf-8");

        post.setEntity(entity);


        HttpResponse response = client.execute(post);
        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK)
        {
            throw new IOException("Error during post");
        } else
        {
            stream = response.getEntity().getContent();
        }

        return stream;
    }

    public BufferedReader bufferReaderPOST(String url, NameValuePair[] query)
            throws IOException
    {
        InputStream stream = inputStreamPOST(url, query);
        if (stream != null)
        {
            return new BufferedReader(new InputStreamReader(stream));
        }
        return null;
    }

    public void setProxy()
    {

    }

    public HttpClient getHttpClient()
    {
        if( client == null ) client = HttpClients.createDefault();
        return client;
    }

    public void setOnline(boolean online)
    {
        this.isOnline = online;
    }

    public boolean isOnline()
    {
        return isOnline;
    }

    private static final String PROXY_TYPE_KEY = "proxyType";
    private static final String PROXY_HTTP_HOST_KEY = "proxyHttpHost";
    private static final String PROXY_HTTP_PORT_KEY = "proxyHttpPort";
    private static final String PROXY_USE_AUTH_KEY = "useProxyAuthentication";
    private static final String PROXY_USERNAME_KEY = "proxyAuthenticationUsername";
    private static final String PROXY_PASSWORD_KEY = "proxyAuthenticationPassword";

}
