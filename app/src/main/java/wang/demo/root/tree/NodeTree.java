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
                this.leftNode.add(value);//εδΈιε½
            }
        }else if(value > this.value){//right
            if(this.rightNode == null){
                setRight(new NodeTree(value));//if no exist rightchild
            }else{
                this.rightNode.add(value);//εδΈιε½
            }
        }
    }

    public static int treeDepth(NodeTree root) {
        if (root == null) {
            return 0;
        }
        // θ?‘η?ε·¦ε­ζ ηζ·±εΊ¦
        int left = treeDepth(root.leftNode);
        // θ?‘η?ε³ε­ζ ηζ·±εΊ¦
        int right = treeDepth(root.rightNode);
        // ζ rootηζ·±εΊ¦=θ·―εΎζιΏηε­ζ ζ·±εΊ¦ + 1
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
            θΏδΈͺδΈ»θ¦ζ―δ½Ώη¨arrayListζ₯ε?η°δΈδΈͺιεοΌηΆεζ―ζ¬‘θ?Ώι?δΈδΈͺε§δΈδΈͺη»ηΉε₯ιεοΌηΆεε€ζ­θΏδΈͺη»ηΉζ―ε¦ζε·¦ε³ε­η»ηΉοΌε¦ζζοΌεζθΏδΈͺε·¦ε­θηΉζΎε₯θΏδΈͺιεγ
            ε¦ζζε³ε­ζ οΌζθΏδΈͺε³ε­η»ηΉδΉζΎε₯ε°θΏδΈͺιεε½δΈ­οΌηΆεζ―ζ¬‘εεΊζ₯ηδΉζ―θΏδΈͺιεηη¬¬δΈδΈͺη»ηΉγ
        */
        ArrayList<ArrayList<NodeTree>> tree = new ArrayList<ArrayList<NodeTree>>();
        ArrayList<NodeTree> cNodelist = new ArrayList<NodeTree>();//ζ―ε±
//        ArrayList<Integer> list1 = new ArrayList<>();
        cNodelist.add(root);//ζ Ήε±
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
