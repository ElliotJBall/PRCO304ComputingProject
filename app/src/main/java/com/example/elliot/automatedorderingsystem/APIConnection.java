package com.example.elliot.automatedorderingsystem;

import android.content.Intent;

import com.example.elliot.automatedorderingsystem.ClassLibrary.Customer;
import com.example.elliot.automatedorderingsystem.ClassLibrary.TypeOfUser;

import org.bson.Document;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Created by Elliot on 08/03/2017.
 */

public class APIConnection implements Runnable {

    Thread thread = null;
    String connection = "";

    public String getAPIData(String urlToUse) throws IOException, JSONException, ParseException {
        connection = urlToUse;
        // Create a string that will hold the URL connection
        final String URL = urlToUse;
        // Create new URL using string URL above
        URL url = new URL(URL);
        // Create new HTTP request
        HttpURLConnection request = (HttpURLConnection) url.openConnection();
        request.addRequestProperty("Content-type" , "application/json");
        request.connect();

        // Set inputstream to the connection request
        InputStream is = request.getInputStream();
        // Set buffered reader to the inputstream
        BufferedReader br = new BufferedReader(new InputStreamReader(is));

        // Create variables to hold the data that will be pulled through the bufferedReader
        StringBuilder sb = new StringBuilder();
        String line;

        // Add the data pulled through bufferedReader to the stringBuilder
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        // Get the JSON string and cut out the uneeded stuff added by RESTHEART (Find more efficient way!)
        String stringJSON = sb.toString();
        String requiredString = stringJSON.substring(stringJSON.indexOf("[") , sb.indexOf("]") +1);

        request.disconnect();
        return requiredString;

    }

    public void onResume(){
        thread = new Thread(this);
        thread.start();
    }

    public void pause(){
        while(true){
            try{
                thread.join();
                break;
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }
        thread = null;
    }

    @Override
    public void run() {
        try {
            getAPIData(connection);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }



}
