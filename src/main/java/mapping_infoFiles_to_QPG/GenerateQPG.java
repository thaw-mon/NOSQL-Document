package mapping_infoFiles_to_QPG;

import Entity.QPG.Graph;
import Entity.QPG.QueryPathInfo;
import Entity.WorkLoad.Edge;
import Entity.WorkLoad.Node;
import Entity.WorkLoad.QueryKeysEntity;

import java.io.File;
import java.io.FileWriter;

/**
 * convert Input class To QPG
 */
public class GenerateQPG {
    private Input input;
    private QueryPathInfo queryPathInfo;
    private Graph graph;

    public GenerateQPG(Input in) {
        input = in;
        queryPathInfo = new QueryPathInfo();
        graph = new Graph();
    }

    public void init() {
        queryPathInfo.initQPG(input);
        graph.init(queryPathInfo);
        System.out.println("success build Query Path Graph");
    }

    //write output to file
    public void writeOut() {
        String path = input.getPath() + "/online_store";
        String fileName = "online_store_step1_output.info";
        File file = new File(path, fileName);
        try {
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write("Node info :\r\n");
            for (Node node : queryPathInfo.getNodeList()) {
                fileWriter.write(node.toString() + "\r\n");
            }
            fileWriter.write("Edge info :\r\n");
            for (Edge edge : queryPathInfo.getEdgeList()) {
                fileWriter.write(edge.toString() + "\r\n");
            }
            fileWriter.write("QueryKeysEntity info :\r\n");
            for (QueryKeysEntity queryKeysEntity : queryPathInfo.getQueryKeysEntityList()) {
                fileWriter.write(queryKeysEntity.toString() + "\r\n");
            }
            fileWriter.close();
            System.out.println("success write step1 out");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Input getInput() {
        return input;
    }

    public void setInput(Input input) {
        this.input = input;
    }

    public QueryPathInfo getQueryPathInfo() {
        return queryPathInfo;
    }

    public void setQueryPathInfo(QueryPathInfo queryPathInfo) {
        this.queryPathInfo = queryPathInfo;
    }

    public Graph getGraph() {
        return graph;
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
    }
}
