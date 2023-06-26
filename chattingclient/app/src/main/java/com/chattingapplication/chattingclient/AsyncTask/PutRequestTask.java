package com.chattingapplication.chattingclient.AsyncTask;

import android.os.AsyncTask;
import android.util.Log;

import androidx.fragment.app.Fragment;

import com.chattingapplication.chattingclient.MainActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;

public class PutRequestTask extends AsyncTask<String, Void, String> {
    private MainActivity mainActivity;
    private String functionName;
    private String className;
    private int responseCode;

    public PutRequestTask(MainActivity activity) {
        mainActivity = activity;
    }
    @Override
    protected String doInBackground(String... params) {
        try {
            URL url = new URL(MainActivity.apiUrl + params[0]);
            Log.d("debugPutURL", MainActivity.apiUrl + params[0]);
            Log.d("debugPutContent", params[1]);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("PUT");
            conn.setRequestProperty("Content-Type", "application/json");

            OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
            osw.write(params[1]);
            osw.flush();
            osw.close();

            responseCode = conn.getResponseCode();
            functionName = params[2];
            className = params[3];
            BufferedReader bufferedReader = (responseCode == HttpURLConnection.HTTP_OK ?
                    new BufferedReader(new InputStreamReader(conn.getInputStream())) :
                    new BufferedReader(new InputStreamReader(conn.getErrorStream())));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = bufferedReader.readLine()) != null) {
                response.append(inputLine);
            }
            bufferedReader.close();
            return response.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        try {
            Log.d("debugResponseCode", String.valueOf(responseCode));
            Class<?> mainClass = Class.forName(mainActivity.getPackageName() + ".MainActivity");
            Method getMethod = mainClass.getDeclaredMethod(String.format("get%s", className));
            Object getResult = getMethod.invoke(mainActivity);

            Class<?> fragmentClass = Class.forName(String.format("%s.%s", mainActivity.getPackageName(), className));
            Method responseMethod = fragmentClass.getDeclaredMethod(functionName, int.class, String.class);
            responseMethod.invoke((Fragment) getResult, responseCode, s);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}