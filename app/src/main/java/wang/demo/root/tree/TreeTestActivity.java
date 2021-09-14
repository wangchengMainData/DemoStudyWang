package wang.demo.root.tree;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;

import wang.demo.root.R;

public class TreeTestActivity extends Activity {

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
    }
}
