package com.example.elliot.automatedorderingsystem;

import android.content.Intent;

import com.example.elliot.automatedorderingsystem.ClassLibrary.Customer;
import com.example.elliot.automatedorderingsystem.ClassLibrary.TypeOfUser;

import org.bson.Document;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
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

        // MAY NEED UNCOMMENTING - CURRENTLY WEIRD WORKAROUND TO HAL/JSON
        // String requiredString = stringJSON.substring(stringJSON.indexOf("[") , sb.indexOf("]") +1);

        request.disconnect();
        return stringJSON;

    }

    public int postAPIData(String urlToUse, JSONObject objectToInsert) throws IOException {
        // Create a string that will hold the URL connection
        final String URL = urlToUse;
        // Create new URL using string URL above
        URL url = new URL(URL);
        // Create new HTTP request
        HttpURLConnection request = (HttpURLConnection) url.openConnection();
        // Allow output over HTTP connection
        request.setDoInput(true);
        request.setDoOutput(true);
        // Set the method to POST as we're adding a new venue
        request.setRequestMethod("POST");
        // Accept JSON and set chartype
        request.setRequestProperty("Accept", "application/json");
        request.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        request.connect();

        // Set a new outputSteamWriter and write JSON, flush to ensure it wrote and close it
        OutputStreamWriter output = new OutputStreamWriter(request.getOutputStream(), "UTF-8");
        output.write(String.valueOf(objectToInsert));
        output.flush();
        output.close();

        // Get input steam
        InputStream inputStream = request.getInputStream();
        // Set bufferReader to inputStream
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        // Create String to holdnextline and a string buffer to hold entire String
        String nextLine = null;
        StringBuilder stringBuffer = new StringBuilder();
        // Loop through and add the string to the string buffer
        while ((nextLine = bufferedReader.readLine()) != null) {
            stringBuffer.append(nextLine);
        }
        // Close the input stream
        inputStream.close();

        // Get the response code to send back to determine whether the POST request was successful or not
        int responceCode = request.getResponseCode();
        request.disconnect();
        return responceCode;
    }

    public int updateAPIData() {
        int responseCode = 0;

        return responseCode;
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
