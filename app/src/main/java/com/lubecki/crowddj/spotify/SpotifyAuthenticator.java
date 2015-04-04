package com.lubecki.crowddj.spotify;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.util.Log;

import com.lubecki.crowddj.R;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import timber.log.Timber;

/**
 * Created by JoshBeridon on 4/3/15.
 */
public class SpotifyAuthenticator {
    public static void authenticate(Activity context, int requestCode)  {
        String clientID = context.getResources().getString(R.string.spotify_client_id);
        String redirect = context.getResources().getString(R.string.spotify_redirect_uri);

        String [] scopes = {
            "streaming"
        };

        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(clientID, AuthenticationResponse.Type.TOKEN, redirect);
        builder.setScopes(scopes);
        AuthenticationRequest request = builder.build();
        AuthenticationClient.openLoginActivity(context, requestCode, request);

    }
    public static void handleResponse(Activity context, int resultCode, Intent intent){

        Resources res = context.getResources();
        String prefsName = res.getString(R.string.shared_prefs_name);
        String spotifyTokenKey = res.getString(R.string.spotify_token_key);

        AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
        if(response.getType() == AuthenticationResponse.Type.TOKEN){
            SharedPreferences.Editor editor = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE).edit();
            editor.putString(spotifyTokenKey,response.getAccessToken());
            editor.apply();

        }
        else {
            Log.i("message","Error getting spotify token");
            Timber.i(response.getType().name() + response.getError()+"This is the response type recieved");

        }
    }

}
