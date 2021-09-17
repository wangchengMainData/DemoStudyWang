package wang.demo.root.tree;

import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import wang.demo.root.R;
import wang.demo.root.service.LocalService;
import wang.demo.root.service.MyAidlService;

public class TreeTestActivity extends Activity {
    LocalService.MyBinder binder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);

        NodeTree tree = new NodeTree(10);
        tree.add(3);
        tree.add(15);
        tree.add(11);
        tree.add(2);
        tree.add(5);
        tree.add(11);
        tree.add(17);
        tree.showTree(tree);
        Log.e("wc","deep:" + NodeTree.treeDepth(tree));
        Intent service = new Intent(this, MyAidlService.class);
        Intent serviceLocal = new Intent(this, LocalService.class);
        startService(service);//aidl服务
        startService(serviceLocal);//本地服务
        bindService(serviceLocal,sc, Service.BIND_AUTO_CREATE);
    }
    ServiceConnection sc = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            binder = (LocalService.MyBinder)service;
            Log.e("wc",binder.localServiceMethod());
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
}
