package org.techtown.SmartCushion;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class FetchData extends AsyncTask<String, Void, ArrayList<Integer>> {
    String data = "";
    String dataParsed = "";
    String singleParsed = "";
    String filteredData = "";
    ArrayList<Integer> testdata = new ArrayList<>();
    @Override
    protected ArrayList<Integer> doInBackground(String... strings) {
        try {
            //서버 접속. USERID, date 인자로 리퀘스트
            URL url = new URL("http://169.56.84.167:3000/data/"+strings[0]+"/"+strings[1]);
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
            if (data.length() == 2) {
                Log.e("Connect_success", "Null Data Fetched");
                return null;
            }
            Log.e("Connect_success",data);
            //JSONArray JA = new JSONArray(data);
            //JSONObject JO = (JSONObject) JA.get(0);
            /*JSONObject JO = new JSONObject(data);
            singleParsed = "user:"+JO.get("user")+"\n"+
                    "date:"+JO.get("date")+"\n"+
                    "position:"+JO.get("position")+"\n";
            dataParsed = dataParsed + singleParsed + "\n";
            filteredData = JO.get("position").toString();
            */

            /////////JSON객체를 받아서 시간대 별로 자세상태 값 파싱하여 리턴
            JSONArray JA = new JSONArray(data);
            JSONObject JO = (JSONObject) JA.get(0);

            //JSONObject JO = new JSONObject(data);
            for(int i=0;i<24;i++){
                if(JO.has(String.valueOf(i))){
                    testdata.add(JO.getInt(Integer.toString(i)));
                }
                else{
                    testdata.add(-1);
                }
            }


        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }


        //return filteredData;
        Log.e("Connect_success","- FetchData Success -");
        return testdata;
    }

    //@Override
    protected void onPostExecute(ArrayList<Integer> s) {
        super.onPostExecute(s);
    }

}
