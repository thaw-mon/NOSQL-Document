import mapping_QPG_to_document_collection_schemas.QPGToSchemata;
import mapping_document_collection_schemas_to_MongoDB_databases.SchemataToDatabase;
import mapping_infoFiles_to_QPG.GenerateQPG;
import mapping_infoFiles_to_QPG.Input;

import java.io.File;

public class Main {


    public static void main(String[] args) {
        //this is just for run here
        String path = "src/main/resources/experiments";
        //this path for jar package
        String path2 = "../resources/experiments";
        //add Determine if the file exists
        String fileName = "onlineStore.info";
        File file = new File(path, fileName);
        if (!file.exists()) {
            path = path2;
        }
        Input input = new Input(path, fileName);
        input.transferFileToClass();
        GenerateQPG qpg = new GenerateQPG(input);
        qpg.init();
        qpg.writeOut(); //write step1 answer
        QPGToSchemata schemata = new QPGToSchemata(path, qpg.getGraph());
        schemata.generateSchemata();
        schemata.writeOut(); //write step2 answer
        SchemataToDatabase database = new SchemataToDatabase(path, schemata.getSchemata());
        database.generateDatabaseSQL();
        database.writeOut();

    }
}
