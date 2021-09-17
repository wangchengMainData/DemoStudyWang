package wang.demo.root.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

public class LocalService extends Service {

    private static String TAG = "LocalService";
    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG,"-------------Local Service -------------created");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG,"-------------Local Service -------------startCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    public class MyBinder extends Binder {
        public String localServiceMethod(){
            return "local success";
        }
    }
}
