package wang.demo.root.service;


import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.Nullable;

import wang.demo.root.IMyAidlInterface;

public class MyAidlService extends Service {
    private  final String TAG = getClass().getSimpleName();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG,"start");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        Log.e(TAG,"start");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new MyAidlServiceBinder();
    }

    public class MyAidlServiceBinder extends IMyAidlInterface.Stub{
        @Override
        public int binderTest() throws RemoteException {
            Log.e(TAG,"success");
            return 1;
        }

        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

        }
    }
}
