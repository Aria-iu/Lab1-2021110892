package com.lab1;

import com.mxgraph.layout.mxOrganicLayout;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxCellRenderer;
import com.mxgraph.util.mxPoint;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxGraphView;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;

// void main(…)：主程序入口，接收用户输入文件，生成图，并允许用户选择后续各项功能
// void showDirectedGraph(type G, …)：展示有向图
// String queryBridgeWords(String word1, String word2)：查询桥接词
// String generateNewText(String inputText)：根据bridge word生成新文本
// String calcShortestPath(String word1, String word2)：计算两个单词之间的最短路径
// String randomWalk()：随机游走
public class Main {
    //boolean
    private Digraph graph;
    private Integer stopLock;

    public Main() {
        graph = null;
        stopLock = 0;
    }

    private void showDirectedGraph(Digraph G, String path ,JLabel imageLabel) throws IOException {
        mxGraph graph = G.getGraph();
        mxGraphComponent graphComponent = new mxGraphComponent(graph);
        graphComponent.setSize(100, 100);
        graphComponent.zoomAndCenter();
        mxGraphView view = graph.getView();
        int componentWidth = graphComponent.getWidth();
        int componentHeight = graphComponent.getHeight();
        mxRectangle graphBounds = view.getGraphBounds();
        double x = graphBounds.getCenterX();
        double y = graphBounds.getCenterY();
        double centerX = componentWidth / 2.0;
        double centerY = componentHeight / 2.0;
        double dx = centerX - x * view.getScale();
        double dy = centerY - y * view.getScale();
        view.setTranslate(new mxPoint(dx, dy));
        // 获取整个图形的尺寸
        graphComponent.getGraphControl().updatePreferredSize();
        // 创建图像缓冲区
        BufferedImage image = mxCellRenderer.createBufferedImage(graph, null, 1, java.awt.Color.WHITE, true, null, graphComponent.getCanvas());
        File file = new File(path);
        if (image != null) {
            // 保存图像到文件
            if (file.exists()) {
                if (!file.delete()) {
                    System.out.println("Failed to delete existing file.");
                    return;  // 如果无法删除文件，则退出方法
                }
            }
            ImageIO.write(image, "PNG", file);
        }
        ImageIcon imageIcon = new ImageIcon(path);
        imageIcon.setImage(ImageIO.read(file));
        imageLabel.setIcon(imageIcon);
    }

    private String queryBridgeWords(String word1, String word2) {
        if(!graph.map.containsKey(word1) || !graph.map.containsKey(word2)) {
            return "No " + word1 + " or " + word2 + " in the graph!";
        }
        int w1 = graph.map.get(word1);
        int w2 = graph.map.get(word2);
        Set<Integer> set1 = new HashSet<>();
        Set<Integer> set2 = new HashSet<>();
        for(int i = 0;i < graph.V;i++) {
            if(graph.adj[w1][i] > 0) {
                set1.add(i);
            }
        }
        for(int i = 0;i < graph.V;i++) {
            if(graph.adj[i][w2] > 0) {
                set2.add(i);
            }
        }
        set1.retainAll(set2);
        if(set1.isEmpty()) {
            return "No bridge words from "+ word1 +" to " + word2 + "!";
        }
        String r = "The bridge words from " + word1 + " to " + word2 + " ";
        if(set1.size() == 1) {
            return r + "is: " + graph.words.get(set1.iterator().next());
        } else {
            r += "are: ";
            int p = 0;
            for(int i : set1) {
                if(p == set1.size() - 1) {
                    r += "and " + graph.words.get(i) + ".";
                } else {
                    r += graph.words.get(i) + ", ";
                }
                p++;
            }
            return r;
        }
    }

    private String generateNewText(String newText) {
        String[] t = praseString(newText);
        ArrayList<String> u = new ArrayList<>();
        for(int k = 0;k < t.length - 1;k++) {
            u.add(t[k]);
            String word1 = t[k];
            String word2 = t[k+1];
            if(graph.map.containsKey(word1) && graph.map.containsKey(word2)) {
                int w1 = graph.map.get(word1);
                int w2 = graph.map.get(word2);
                Set<Integer> set1 = new HashSet<>();
                Set<Integer> set2 = new HashSet<>();
                for(int i = 0;i < graph.V;i++) {
                    if(graph.adj[w1][i] > 0) {
                        set1.add(i);
                    }
                }
                for(int i = 0;i < graph.V;i++) {
                    if(graph.adj[i][w2] > 0) {
                        set2.add(i);
                    }
                }
                set1.retainAll(set2);
                if(!set1.isEmpty()) {
                    List<Integer> l = new ArrayList<>(set1);
                    // 创建 Random 实例
                    Random random = new Random();
                    // 随机选择一个索引
                    int index = random.nextInt(l.size());
                    // 获取随机元素
                    int rand = l.get(index);
                    u.add(graph.words.get(rand));
                }
            }

        }
        u.add(t[t.length - 1]);
        String r = "";
        for(String s : u) {
            r += s + " ";
        }
        return r;
    }

    /* impl by zyc */
    private void getList(Vector<Integer> words, int index1, int index2, int[][] directions) {
        if (index1 == index2) {
            return;
        }
        int temp2 = directions[index1][index2];
        if (index1==temp2){
            words.add(index1);
            return;
        }
        getList(words,temp2,index2,directions);
        getList(words,index1,temp2,directions);
    }

    /* impl by zyc */
    private void calcShortestPathFromOne(String origin){
        if (!graph.map.containsKey(origin)) {
            System.out.println("No " + origin + " in the graph!");
        }
        for (int i = 0; i< graph.words.size();i++){
            if (graph.words.get(i)==origin){
                continue;
            }else{
                String ret = calcShortestPath(origin,graph.words.get(i));
                System.out.println(ret);
            }

        }
    }

    /* impl by zyc */
    private String calcShortestPath(String word1, String word2) {
        int[][] distance;
        int[][] directions;
        if (!graph.map.containsKey(word1) || !graph.map.containsKey(word2)) {
            return "No " + word1 + " or " + word2 + " in the graph!";
        }
        // index1是word1对应的index，index2是word2对应的index
        int index1 = graph.map.get(word1);
        int index2 = graph.map.get(word2);

        int v = graph.V;
        // distance代表各个节点与其他节点的距离，Integer.MAX_VALUE表示不可达
        distance = new int[v][v];
        for (int i = 0; i < v; i++) {
            for (int j = 0; j < v; j++) {
                if(graph.adj[i][j] > 0) {
                    distance[i][j] = 1;
                }else{
                    distance[i][j] = -1;
                }
            }
        }
        // 各个节点到自己的距离为0
        for (int i = 0; i < v; i++) {
            distance[i][i] = 0;
        }

        // 用来记录i到j的最短路径的上一个节点,-1表示没有,directions[i][i]存入i表示经过自己。
        directions = new int[v][v];
        // 初始化directions
        for (int i = 0; i < v; i++) {
            for (int j = 0; j < v; j++) {
                if(distance[i][j] != -1) {
                    directions[i][j] = i;
                }else{
                    directions[i][j] = -1;
                }
            }
            directions[i][i] = i;
        }

        // 根据每条边来更新距离。
        for (int k = 0; k < v; k++) {
            for (int i = 0; i < v; i++) {
                for (int j = 0; j < v; j++) {
                    if ((distance[i][k] != -1)&& (distance[k][j] != -1)) {
                        if(distance[i][j]==-1){
                            distance[i][j] = distance[i][k] + distance[k][j];
                            directions[i][j] = k;
                        }else{
                            if (distance[i][j] > distance[i][k] + distance[k][j]) {
                                distance[i][j] = distance[i][k] + distance[k][j];
                                directions[i][j] = k;
                            }
                        }
                    }
                }
            }
        }
        System.out.println("----------------------------------");
        System.out.println("---___最短路径数组，-1表示不可达___---");
        System.out.println("----------------------------------");
        for (int i = 0; i < v; i++) {
            for (int j = 0; j < v; j++) {
                System.out.print(distance[i][j] + " ");
            }
            System.out.println();
        }
        System.out.println("----------------------------------");
        System.out.println("---___最短路径方位，-1表示不可达___---");
        System.out.println("----------------------------------");
        for (int i = 0; i < v; i++) {
            for (int j = 0; j < v; j++) {
                System.out.print(directions[i][j] + " ");
            }
            System.out.println();
        }

        System.out.println("---------------------------------------");
        System.out.println("---___显示从words1到达words2的最短路径___---");
        System.out.println("---------------------------------------");
        Vector<Integer> words = new Vector<>();
        if (distance[index1][index2] == -1) {
            System.out.println(word1+" can not get to "+word2);
        }else{
            int temp = directions[index1][index2];
            getList(words,temp,index2,directions);
            getList(words,index1,temp,directions);
        }
        String ret = "";
        for (int i = 0; i < words.size(); i++) {
            int temp = words.get(words.size()-1-i);
            // System.out.print( get_string_by_id(temp)+ "->");
            ret += graph.words.get(temp);//get_string_by_id(temp);
            ret += "->";
        }
        // System.out.println(get_string_by_id(index2));
        ret += graph.words.get(index2);//get_string_by_id(index2);
        return ret;
    }

    /* impl by zyc */
    private String randomWalk() {
        int v = graph.map.size();
        System.out.println("The total nodes are: "+ v);
        Random rand = new Random();
        int random = rand.nextInt(v);
        System.out.println("Randomly Select node: "+ graph.words.get(random));
        int nodeId = random;
        String r = "";
        boolean[][] visited = new boolean[v][v];
        while(true) {
            synchronized (stopLock) {
                r += graph.words.get(nodeId) + " ";
                if(stopLock == 1) {
                    System.out.println("Interrupted by User , stop walk NOW!!");
                    break;
                } else {
                    Vector<Integer> candidates = new Vector<>();
                    for (int i = 0; i < graph.V; i++) {
                        if (graph.adj[nodeId][i] > 0) {
                            candidates.add(i);
                        }
                    }
                    if(candidates.isEmpty()){
                        System.out.println("No Out Ways!!!");
                        break;
                    }else{
                        int ran = new Random().nextInt(candidates.size());
                        int select = candidates.get(ran);
                        if(visited[nodeId][select]) {
                            System.out.println("visited! "+ graph.words.get(select));
                            System.out.println("visited!");
                            break;
                        }
                        System.out.println("Goto node: "+ graph.words.get(select));
                        visited[nodeId][select] = true;
                        nodeId = select;
                    }
                }
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                return r;
            }
        }
        return r;
    }

    private String[] praseString(String str) {
        String cleaned = str.toLowerCase().replaceAll("[@#$%^&*]+", "");
        return cleaned.split("[\\s.,?!:;\"'(){}\\[\\]—-]+");
    }

    public static void main(String[] args) {
        //创建主类
        Main main = new Main();

        // 创建 JFrame 实例
        JFrame frame = new JFrame("LAB1");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize((int)(1.618*450), 450);
        frame.setLocationRelativeTo(null);  // 窗口居中显示

        // 创建按钮
        JButton buttonReadFile = new JButton("载入文件");
        JButton buttonShowGraph = new JButton("展示图形");
        JButton buttonQueryBridgeWords = new JButton("查询桥接词");
        JButton buttonGenGenNewText = new JButton("生成新文本");
        JButton buttonCalcShortestPath = new JButton("计算最短路径");
        JButton buttonRandomWalk = new JButton("随机游走");
        JButton buttonStopWalk = new JButton("停止");
        // 创建展示图像的label
        JLabel imageLabel = new JLabel();

        // 创建一个 JPanel，并设置其布局为 BoxLayout，组件沿 Y 轴排列
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        //1
        JTextField text1 = new JTextField();
        JLabel label1 = new JLabel("载入文件路径:", JLabel.CENTER);
        JPanel panel1 = new JPanel();
        panel1.setLayout(new BoxLayout(panel1, BoxLayout.X_AXIS));
        panel1.add(buttonReadFile);
        panel1.add(label1);
        panel1.add(text1);

        //2
        JTextField text2a = new JTextField();
        JTextField text2b = new JTextField();
        JLabel label2a = new JLabel("word1:", JLabel.CENTER);
        JLabel label2b = new JLabel("word2:", JLabel.CENTER);
        JPanel panel2 = new JPanel();
        panel2.setLayout(new BoxLayout(panel2, BoxLayout.X_AXIS));
        panel2.add(buttonQueryBridgeWords);
        panel2.add(label2a);
        panel2.add(text2a);
        panel2.add(label2b);
        panel2.add(text2b);

        //3
        JTextField text3 = new JTextField();
        JLabel label3 = new JLabel("保存文件路径:", JLabel.CENTER);
        JPanel panel3 = new JPanel();
        panel3.setLayout(new BoxLayout(panel3, BoxLayout.X_AXIS));
        panel3.add(buttonShowGraph);
        panel3.add(label3);
        panel3.add(text3);

        //4
        JTextField text4 = new JTextField();
        JLabel label4 = new JLabel("text:", JLabel.CENTER);
        JPanel panel4 = new JPanel();
        panel4.setLayout(new BoxLayout(panel4, BoxLayout.X_AXIS));
        panel4.add(buttonGenGenNewText);
        panel4.add(label4);
        panel4.add(text4);

        //5
        JTextField text5a = new JTextField();
        JTextField text5b = new JTextField();
        JLabel label5a = new JLabel("word1:", JLabel.CENTER);
        JLabel label5b = new JLabel("word2:", JLabel.CENTER);
        JPanel panel5 = new JPanel();
        panel5.setLayout(new BoxLayout(panel5, BoxLayout.X_AXIS));
        panel5.add(buttonCalcShortestPath);
        panel5.add(label5a);
        panel5.add(text5a);
        panel5.add(label5b);
        panel5.add(text5b);

        //6
        JPanel panel6 = new JPanel();
        panel6.setLayout(new BoxLayout(panel6, BoxLayout.X_AXIS));
        panel6.add(buttonRandomWalk);
        panel6.add(buttonStopWalk);
        panel6.add(Box.createHorizontalGlue());

        //添加指令面板
        JLabel labelOut = new JLabel("", JLabel.CENTER);
        Font font = new Font("微软雅黑", Font.PLAIN, 18); // 这里设置字体大小为20
        labelOut.setFont(font);
        panel.add(labelOut);
        panel.add(panel1);
        panel.add(panel3);
        panel.add(panel2);
        panel.add(panel4);
        panel.add(panel5);
        panel.add(panel6);

        // 添加事件监听器
        buttonReadFile.addActionListener(_ -> {
            String pathStr = ".\\text.txt";
            if(!Objects.equals(text1.getText(), "")){
                pathStr = text1.getText();
            }
            Path path = Path.of(pathStr);
            try {
                // Java 11 或更高版本可以直接使用
                String content = Files.readString(path);
                String[] strings = main.praseString(content);
                System.out.println(Arrays.toString(strings));
                main.graph = new Digraph(strings);
            } catch (IOException error) {
                labelOut.setText("载入失败！");
            }
            labelOut.setText("载入成功！");
        });

        buttonShowGraph.addActionListener(_ -> {
            String imagePath = ".\\image.png";
            if(!Objects.equals(text3.getText(), "")) {
                imagePath = text3.getText();
            }
            try {
                main.showDirectedGraph(main.graph, imagePath, imageLabel);
            } catch (IOException ex) {
                labelOut.setText("保存失败！");
            }
            labelOut.setText("保存成功！");
        });

        buttonQueryBridgeWords.addActionListener(_ -> {
            String word1 = text2a.getText();
            String word2 = text2b.getText();
            String r = main.queryBridgeWords(word1, word2);
            labelOut.setText(r);
        });

        buttonGenGenNewText.addActionListener(_ -> {
            String newText = text4.getText();
            String r = main.generateNewText(newText);
            labelOut.setText(r);
        });

        buttonCalcShortestPath.addActionListener(_ -> {
            String word1 = text5a.getText();
            String word2 = text5b.getText();
            String r = main.calcShortestPath(word1, word2);
            labelOut.setText(r);
        });

        buttonRandomWalk.addActionListener(_ -> {
            new Thread(() -> {
                synchronized (main.stopLock) {
                    main.stopLock = 0;
                }
                String result = main.randomWalk();
                labelOut.setText(result);
            }
            ).start();
        });

        buttonStopWalk.addActionListener(_ -> {
            synchronized (main.stopLock) {
                main.stopLock = 1;
            }
        });

        // 将面板添加到框架
//        frame.add(jpanel, BorderLayout.CENTER);
        frame.add(imageLabel, BorderLayout.CENTER);
        frame.add(panel, BorderLayout.SOUTH);

        // 设置窗体可见
        frame.setVisible(true);
    }

}

class Digraph {
    public int V;
    public int E;
    public Map<String, Integer> map;
    public List<String> words;
    public int[][] adj;
    public Digraph(String[] list) {
        V = 0;
        E = 0;
        map = new HashMap<>();
        words = new ArrayList<>();
        for(String s : list) {
            if(!map.containsKey(s)) {
                map.put(s, V);
                words.add(s);
                V++;
            }
        }
        adj = new int[V][V];
        for(int i = 0;i < list.length - 1;i++) {
            int before = map.get(list[i]);
            int after = map.get(list[i + 1]);
            adj[before][after]++;
            E++;
        }
        //######
        System.out.println(map);
        for(int i = 0;i < V;i++) {
            for(int j = 0;j < V;j++) {
                System.out.print(adj[i][j] + " ");
            }
            System.out.println();
        }
        //######
    }

    public mxGraph getGraph() {
        mxGraph graph = new mxGraph();
        Object parent = graph.getDefaultParent();

        graph.getModel().beginUpdate();
        try {
            Object[] oList = new Object[V];
            for(String s : map.keySet()) {
                oList[map.get(s)] = graph.insertVertex(parent, null, s, 50, 50, 25, 20);;
            }
            for(int i = 0;i < V;i++) {
                for(int j = 0;j < V;j++) {
                    if(adj[i][j] > 0) {
                        graph.insertEdge(parent, null, String.valueOf(adj[i][j]), oList[i], oList[j]);
                    }
                }
            }
        } finally {
            graph.getModel().endUpdate();
        }

        // 应用有机布局
        mxOrganicLayout layout = new mxOrganicLayout(graph);
        layout.setUseBoundingBox(false);
        layout.execute(parent);

        return graph;
    }
}