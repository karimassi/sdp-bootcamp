package ch.epfl.balelecbud.util.http;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.gson.JsonElement;

import java.util.concurrent.CompletableFuture;

import ch.epfl.balelecbud.BalelecbudApplication;


public class VolleyHttpClient implements HttpClient {

    private static final HttpClient instance = new VolleyHttpClient();

    RequestQueue queue;

    private VolleyHttpClient() {
        queue = Volley.newRequestQueue(BalelecbudApplication.getAppContext());
    }

    @Override
    public CompletableFuture<JsonElement> get(String url) {
        HttpGetRequest request = new HttpGetRequest(url);
        queue.add(request.getGetRequest());
        return request;
    }

    public static HttpClient getInstance() {
        return instance;
    }



}