package com.examples.chatapp.InternetCheck;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class CheckInternetConnection
{
    public boolean NetworkState(Context context)
    {
        final ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if(networkInfo != null)
        {
            return true;
        }
        return false;
    }
}
