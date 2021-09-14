package wang.demo.root.tree;

import android.util.Log;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Stack;

public class NodeTree {

    public int value;
    public NodeTree leftNode;
    public NodeTree rightNode;
    public NodeTree FatherNode;


    public NodeTree(int value){
        this.value = value;
    }

    public void setLeft(NodeTree node){
        if(this.leftNode != null)
        {
            this.leftNode = null;//release current leftNode
        }
        this.leftNode = node;
        if(this.leftNode != null)
        {
            this.leftNode.FatherNode = this;//set new child node's father to this
        }
    }

    public void setRight(NodeTree node){
        if(this.rightNode != null)
        {
            this.rightNode = null;//release current rightNode
        }
        this.rightNode = node;
        if(this.rightNode != null)
        {
            this.rightNode.FatherNode = this;//set new child node's father to this
        }
    }

    public void setChild(NodeTree node){
        if(node == null)
        return;
        if(node.value < this.value)//add left
            setLeft(node);
        else if(node.value > this.value)//add right
            setRight(node);
    }

    public void deleteChild(NodeTree node){
        if(node == null)
            return;
        if(node == this.leftNode){
            node.FatherNode = null;
            this.leftNode = null;
        }else if(node == this.rightNode){
            node.FatherNode = null;
            this.rightNode = null;
        }
    }

    public void add(int value){
        if(value < this.value) {//left
            if (this.leftNode == null) {//if no exist leftchild
                setLeft(new NodeTree(value));
            }else{
                this.leftNode.add(value);//向下递归
            }
        }else if(value > this.value){//right
            if(this.rightNode == null){
                setRight(new NodeTree(value));//if no exist rightchild
            }else{
                this.rightNode.add(value);//向下递归
            }
        }
    }

    public static int treeDepth(NodeTree root) {
        if (root == null) {
            return 0;
        }
        // 计算左子树的深度
        int left = treeDepth(root.leftNode);
        // 计算右子树的深度
        int right = treeDepth(root.rightNode);
        // 树root的深度=路径最长的子树深度 + 1
        return left >= right ? (left + 1) : (right + 1);
    }

    private boolean hasChild(NodeTree node){
        return (node.leftNode != null || node.rightNode != null);
    }

    public void showTree(NodeTree node){
        ArrayList<ArrayList<NodeTree>> a = PrintFromTopToBottom(node);
    }

    public void deleteNode(NodeTree node){
        boolean isrightChild = true;


    }

    public ArrayList<ArrayList<NodeTree>> PrintFromTopToBottom(NodeTree root) {
        /*
            这个主要是使用arrayList来实现一个队列，然后每次访问一个吧一个结点入队列，然后判断这个结点是否有左右子结点，如果有，则把这个左子节点放入这个队列。
            如果有右子树，把这个右子结点也放入到这个队列当中，然后每次取出来的也是这个队列的第一个结点。
        */
        ArrayList<ArrayList<NodeTree>> tree = new ArrayList<ArrayList<NodeTree>>();
        ArrayList<NodeTree> cNodelist = new ArrayList<NodeTree>();//每层
//        ArrayList<Integer> list1 = new ArrayList<>();
        cNodelist.add(root);//根层
        tree.add(cNodelist);
        while(cNodelist.size() > 0){
            ArrayList<NodeTree> temp = new ArrayList<>();
            for(int i = 0 ;i < cNodelist.size(); i++){
                NodeTree node = cNodelist.remove(0);
                Log.e("WC","now remove :" + node.value+"");
                if(node.leftNode != null) {
                    cNodelist.add(node.leftNode);
                    temp.add(node.leftNode);
                    Log.e("wc1","here left");
                }
                if(node.rightNode != null) {
                    cNodelist.add(node.rightNode);
                    temp.add(node.rightNode);
                    Log.e("wc1","here right");
                }
                if(cNodelist.size() == 0) {
                    Log.e("wc","123");
                    tree.add(temp);
                    temp.clear();
                }
                Log.e("wc","cNodeSize:" + cNodelist.size());
            }
        }
        return tree;
    }


    private void l(String str){
        Log.e("w2c",str);
    }
}
