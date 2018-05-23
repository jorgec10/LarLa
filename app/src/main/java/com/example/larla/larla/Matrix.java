package com.example.larla.larla;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.matrix.androidsdk.HomeServerConnectionConfig;
import org.matrix.androidsdk.MXDataHandler;
import org.matrix.androidsdk.MXSession;
import org.matrix.androidsdk.data.store.MXMemoryStore;
import org.matrix.androidsdk.rest.model.login.Credentials;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class Matrix {

    public static final String HTTPS_MATRIX_SERVER = "https://matrix.org";
    public static final String MATRIX_REST_LOGIN_ENDPOINT = "https://matrix.org/_matrix/client/r0/login";
    public static final String MATRIX_REST_REGISTER_ENDPOINT = "https://matrix.org/_matrix/client/r0/register";

    private HomeServerConnectionConfig hsConfig;
    private MXSession session;

    private final Context appContext;

    private static Matrix ourInstance;

    public static Matrix getInstance(Context appContext) {
        if ((ourInstance == null) && (appContext != null)) {
            ourInstance = new Matrix(appContext);
        }
        return ourInstance;
    }

    private Matrix(Context appContext) {
        this.appContext = appContext;
        this.hsConfig = new HomeServerConnectionConfig(Uri.parse(HTTPS_MATRIX_SERVER));
    }

    public boolean login (String username, String password) {


        try {
            URL url = new URL(MATRIX_REST_LOGIN_ENDPOINT);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            // Starts the query
            conn.connect();

            OutputStream os = conn.getOutputStream();

            String body = "{\n" +
                    "  \"initial_device_display_name\": \"LarLa\",\n" +
                    "  \"password\": \"" + password + "\",\n" +
                    "  \"type\": \"m.login.password\",\n" +
                    "  \"user\": \"" + username + "\"\n" +
                    "}";
            os.write(body.getBytes());

            int response = conn.getResponseCode();

            if (response == 200) {

                InputStream is = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                StringBuilder responseStringBuilder = new StringBuilder();

                String inputStr;
                while((inputStr = reader.readLine()) != null) {
                    responseStringBuilder.append(inputStr);
                }

                JSONObject jsonResponse = new JSONObject(responseStringBuilder.toString());

                Credentials cred = new Credentials();
                cred.homeServer = jsonResponse.getString("home_server");
                cred.userId = jsonResponse.getString("user_id");
                cred.accessToken = jsonResponse.getString("access_token");
                cred.deviceId = jsonResponse.getString("device_id");


                this.hsConfig.setCredentials(cred);
                this.session = new MXSession(hsConfig, new MXDataHandler(new MXMemoryStore(cred, appContext), cred), appContext);

                session.startEventStream(session.getCurrentSyncToken());


                return true;

            } else {
                return false;
            }

        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            //e.printStackTrace();
        }

        return false;

    }

    public boolean register (String username, String password) {


        try {
            URL url = new URL(MATRIX_REST_REGISTER_ENDPOINT);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            // Starts the query
            conn.connect();

            OutputStream os = conn.getOutputStream();

            String body = "{\n" +
                    "  \"user\": \"" + username + "\",\n" +
                    "  \"password\": \"" + password + "\"\n" +
                    "}";
            os.write(body.getBytes());

            int response = conn.getResponseCode();

            if (response == 200) {

                InputStream is = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                StringBuilder responseStringBuilder = new StringBuilder();

                String inputStr;
                while((inputStr = reader.readLine()) != null) {
                    responseStringBuilder.append(inputStr);
                }

                JSONObject jsonResponse = new JSONObject(responseStringBuilder.toString());

                Credentials cred = new Credentials();
                cred.homeServer = jsonResponse.getString("home_server");
                cred.userId = jsonResponse.getString("user_id");
                cred.accessToken = jsonResponse.getString("access_token");
                cred.deviceId = jsonResponse.getString("device_id");

                return true;

            } else {
                InputStream is = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                StringBuilder responseStringBuilder = new StringBuilder();

                String inputStr;
                while((inputStr = reader.readLine()) != null) {
                    responseStringBuilder.append(inputStr);
                }

                Log.d("registermatrix", responseStringBuilder.toString());

                return false;
            }

        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            //e.printStackTrace();
        }

        return false;

    }

    public MXSession getSession() {
        return session;
    }
}
