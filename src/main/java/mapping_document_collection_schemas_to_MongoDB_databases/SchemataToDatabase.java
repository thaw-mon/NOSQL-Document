package mapping_document_collection_schemas_to_MongoDB_databases;

import Entity.Schemata.DocumentScalarPropertyByRefExtendIndex;
import mapping_QPG_to_document_collection_schemas.DocumentModel.QPGtoDocumentSchemataWithRef2;
import org.bson.Document;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

//Generate MongoDB Table SQL
public class SchemataToDatabase {
    private List<DocumentScalarPropertyByRefExtendIndex> schemata;
    private List<Document> tableSQL;
    private List<String> indexSQL;
    private String path;


    public SchemataToDatabase(String filePath,List<DocumentScalarPropertyByRefExtendIndex> propertyList) {
        schemata = propertyList;
        path = filePath;
        tableSQL = new ArrayList<>();
    }

    public void generateDatabaseSQL() {
        System.out.println("QPGToSchemata class");
        for (DocumentScalarPropertyByRefExtendIndex property : schemata) {
            tableSQL.add(property.getDocumentScalarPropertyByRef().generateDocument(0));
        }
    }

    //write output to file
    public void writeOut() {
        String filepath = path + "/online_store";
        String fileName = "online_store_step3_output.info";
        File file = new File(filepath, fileName);
        try {
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write("Table info :\r\n");
            for (Document doc : tableSQL) {
                fileWriter.write(doc.toString() + "\r\n");
            }
            fileWriter.close();
            System.out.println("success write step3 out");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
