package org.techtown.SmartCushion;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class FetchData extends AsyncTask<String, Void, String> {
    String data = "";
    String dataParsed = "";
    String singleParsed = "";
    String filteredData = "";
    @Override
    protected String doInBackground(String... strings) {
        try {
            URL url = new URL("http://192.168.1.112:3000/data/"+strings[0]+"/"+strings[1]);
            //strings[0] is USERID and strings[1] is DATE
            Log.d("testtest",strings[1]);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("GET");
            InputStream inputStream = httpURLConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line = "";
            StringBuilder body = new StringBuilder();
            while((line = bufferedReader.readLine()) != null){
                body.append(line);
                //data = data + line;
            }
            data = body.toString();
            Log.d("ABC",data);

            /*
            JSONArray JA = new JSONArray(data);
            for(int i = 0; i<JA.length(); i++){
                JSONObject JO = (JSONObject) JA.get(i);
            }
            */

            //JSONArray JA = new JSONArray(data);
            //JSONObject JO = (JSONObject) JA.get(0);
            JSONObject JO = new JSONObject(data);
            singleParsed = "user:"+JO.get("user")+"\n"+
                    "date:"+JO.get("date")+"\n"+
                    "position:"+JO.get("position")+"\n";
            dataParsed = dataParsed + singleParsed + "\n";
            filteredData = JO.get("position").toString();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }


        return filteredData;
    }

    //@Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
    }

}
