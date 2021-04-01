import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.*;
import java.net.URL;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.net.URLDecoder;

public class ShallowKnowledge {
    private static BufferedReader br  = null;
    private static InputStreamReader isr = null;
    private static InputStream stream = null;

  public static void main ( String[] args ) throws UnsupportedEncodingException{
    isr = new InputStreamReader(System.in);
    br = new BufferedReader(isr);

    System.out.println("キーボードから調べたい単語を入力してください");

    String str = null;
    try {
        str = br.readLine();
        br.close();
        isr.close();
    } catch (IOException e) {
        e.printStackTrace();
    }
    String format = "https://ja.wikipedia.org/w/api.php?format=xml&action=query&prop=extracts&exintro&explaintext&redirects=1&titles=%s";
    String encodedStr = URLEncoder.encode(str, "UTF-8");
    String apiUrl = String.format(format, encodedStr);
    try {
      System.out.println( connectGetJson(apiUrl) );
    } catch ( Exception e ) {
      e.printStackTrace();
    }
  }

  public static String connectGetJson(String apiUrl)throws Exception{
    String strResult; 
    String getUrl = apiUrl;
    URL url = new URL(getUrl);
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setConnectTimeout(100000);
    connection.setReadTimeout(100000);
    connection.setRequestMethod("GET");
    connection.connect();

    int responseCode = connection.getResponseCode();
    if(responseCode == HttpURLConnection.HTTP_OK){
      InputStream in = connection.getInputStream();
      String encoding = connection.getContentEncoding();
      if(null == encoding){
          encoding = "UTF-8";
      }
      StringBuffer result = new StringBuffer();
      final InputStreamReader inReader = new InputStreamReader(in, encoding);
      final BufferedReader bufReader = new BufferedReader(inReader);

      String line = null;
      while((line = bufReader.readLine()) != null) {
          result.append(line);
      }
      bufReader.close();
      inReader.close();

      strResult = result.toString();
      if(strResult.contains("<extract xml:space=\"preserve\">")){
        String resultUtf8 = URLDecoder.decode(strResult, "UTF-8");
        byte[] byteResult = resultUtf8.getBytes("UTF-8");
        String newResult = new String(byteResult, "UTF-8");
        int startIndex = newResult.indexOf("<extract xml:space=\"preserve\">");
        startIndex += "<extract xml:space=\"preserve\">".length();
        int endIndex = newResult.indexOf("</extract>");
        String extract = newResult.substring(startIndex, endIndex);
        return extract;
      }else {
        String msg = "申し訳ございません。入力していただいた単語は、wikipedia常には存在しないタイトル名です。";
        return msg;
      }
      
    }else {
      try {
        stream = connection.getInputStream();
      }
      catch (IOException e) {
        stream = connection.getErrorStream();
      }
      return "申し訳ございません。アクセスエラーが発生しました\n"+ apiUrl + responseCode + stream;
    }
  }
}