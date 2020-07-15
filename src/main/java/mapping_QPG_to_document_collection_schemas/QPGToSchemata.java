package mapping_QPG_to_document_collection_schemas;


import Entity.QPG.Graph;
import Entity.Schemata.DocumentScalarPropertyByRefExtendIndex;
import Entity.WorkLoad.Edge;
import Entity.WorkLoad.Node;
import Entity.WorkLoad.QueryKeysEntity;
import mapping_QPG_to_document_collection_schemas.DocumentModel.QPGtoDocumentSchemataWithRef2;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class QPGToSchemata {
    private Graph graph;
    private String path;
    private List<DocumentScalarPropertyByRefExtendIndex> schemata;

    public QPGToSchemata(String filePath, Graph g) {
        graph = g;
        path = filePath;
    }


    //TODO add HW HA info
    public void generateSchemata() {
        List<String> entities = graph.getAllEntities();
        Scanner scanner = new Scanner(System.in);
        System.out.println("please determine which entity types are HA/HW.");
        System.out.println("if type is HW,please input HW;if type is HA,please input HA;Neither can be entered as other characters ");
        List<String> HW = new ArrayList<>();
        List<String> HA = new ArrayList<>();
        for (String entity : entities) {
            System.out.print(entity + " : ");
            String ans = scanner.next();
            switch (ans) {
                case "HW":
                    HW.add(entity);
                    break;
                case "HA":
                    HA.add(entity);
                    break;
                default:
                    //do nothing
                    break;
            }
        }
        QPGtoDocumentSchemataWithRef2 qpGtoDocumentSchemataWithRef2 = new QPGtoDocumentSchemataWithRef2(graph);
        qpGtoDocumentSchemataWithRef2.initHighlyWrittenAndHighAccessEntities(HW,HA);
        qpGtoDocumentSchemataWithRef2.generateSchemata();
        schemata = qpGtoDocumentSchemataWithRef2.getSchemataInfoWithIndex();
    }

    public void printSchemataInfo() {

    }

    //write output to file
    public void writeOut() {
        String filepath = path + "/online_store";
        String fileName = "online_store_step2_output.info";
        File file = new File(filepath, fileName);
        try {
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write("Schemata info :\r\n");
            for (DocumentScalarPropertyByRefExtendIndex s : schemata) {
                fileWriter.write(s.toString() + "\r\n");
            }
            fileWriter.close();
            System.out.println("success write step2 out");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Graph getGraph() {
        return graph;
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
    }

    public List<DocumentScalarPropertyByRefExtendIndex> getSchemata() {
        return schemata;
    }

    public void setSchemata(List<DocumentScalarPropertyByRefExtendIndex> schemata) {
        this.schemata = schemata;
    }
}
