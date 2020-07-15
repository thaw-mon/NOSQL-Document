package mapping_QPG_to_document_collection_schemas.DocumentModel;

import Entity.QPG.Graph;
import Entity.Schemata.DocumentScalarProperty;
import Entity.Schemata.DocumentScalarPropertyExtendIndex;
import Entity.WorkLoad.*;
import mapping_QPG_to_document_collection_schemas.QPGToSchemata;

import java.util.*;


/**
 * Algorithm 4: QPGtoDocumentV
 * Input: QPG
 * Output: The corresponding Document schema
 */
public class QPGtoDocumentSchemata {

    private Graph graph;
    private Map<String, DocumentScalarPropertyExtendIndex> documentScalarPropertyMap;

    public QPGtoDocumentSchemata(Graph g) {
        graph = g;
        documentScalarPropertyMap = new HashMap<>();
    }


    /**
     * create Index
     */
    public List<String> createIndex(Node node) {
        //增加对字段的判断，即为是否当前字段在当前类中
        List<String> indexes = new ArrayList<>();
        for (int qNum : node.getQueryNumbers()) {
            List<String> equalitySearchKeysList = graph.queryKeysEntityMaps.get(qNum).getEqualitySearchKeys();
            List<String> conversionEqualitySearchKeysList = convertIndex(node, qNum, equalitySearchKeysList);
            String equalitySearchKeys = conversionEqualitySearchKeysList.toString();
            if (!equalitySearchKeys.equals("[]") && !indexes.contains(equalitySearchKeys)) {
                indexes.add(equalitySearchKeys);
            }
            List<String> inequalitySearchKeysList = graph.queryKeysEntityMaps.get(qNum).getInequalitySearchKeys();
            List<String> conversionInequalitySearchKeysList = convertIndex(node, qNum, inequalitySearchKeysList);
            String inequalitySearchKeys = conversionInequalitySearchKeysList.toString();
            if (!inequalitySearchKeys.equals("[]") && !indexes.contains(inequalitySearchKeys)) {
                indexes.add(inequalitySearchKeys);
            }
            List<String> orderingKeysList = graph.queryKeysEntityMaps.get(qNum).getOrderingKeys();
            List<String> conversionOrderingKeysList = convertIndex(node, qNum, orderingKeysList);
            String orderingKeys = conversionOrderingKeysList.toString();
            if (!orderingKeys.equals("[]") && !indexes.contains(orderingKeys)) {
                indexes.add(orderingKeys);
            }
        }
       /* System.out.println("index " + node.getNodeName());
        for (String index : indexes) {
            System.out.println(index);
        }*/
        return indexes;
    }

    public List<String> convertIndex(Node node, int qNum, List<String> stringList) {
        List<String> conversionList = new ArrayList<>();
        for (String str : stringList) {
            String[] arr = str.split("\\.");
            StringBuilder sb = new StringBuilder();
            if (!arr[0].equals(node.getNodeName())) {
                //1.根据查询实体Number 和EndPointName找到对应边
                Edge edge = graph.getEdgeByEndPointAndQNum(arr[0], qNum);
                String list = edge.getRelationShip() + edge.getEndNode() + "List";
                sb.append(list).append(".");
            }
            sb.append(arr[0]).append("_").append(arr[1]);
            conversionList.add(sb.toString());
        }
        return conversionList;
    }

    //使用DocumentScalarProperty
    public void generateSchemata() {
        documentScalarPropertyMap.clear();
        List<Node> nodes = graph.getAccessPoints();
        for (Node node : nodes) {
            //1.Get all the query entities corresponding to the Node
            List<QueryKeysEntity> queryKeysEntityList = new ArrayList<>();
            for (int qNum : node.getQueryNumbers()) {
                queryKeysEntityList.add(graph.queryKeysEntityMaps.get(qNum));
            }
            DocumentScalarProperty ST = new DocumentScalarProperty(node.getNodeName());
            DesignDocumentSchemata(queryKeysEntityList, node, null, ST);
            //Add index information
            List<String> indexes = createIndex(node);

            DocumentScalarPropertyExtendIndex documentScalarPropertyExtendIndex = new DocumentScalarPropertyExtendIndex(ST);
            documentScalarPropertyExtendIndex.setIndexes(indexes);

            documentScalarPropertyMap.put(node.getNodeName(), documentScalarPropertyExtendIndex);
        }
    }

    /**
     * Algorithm 4: designDocumentSchema
     * Input: An entity A as an access point of some queries, an entity U and its parent T in the graph of queries, as well as ST as the schema of T
     * Output: Evolving the document schema required to efficiently answer the queries, regarding the scalar properties of U
     */
    private void DesignDocumentSchemata(List<QueryKeysEntity> queryKeysEntities, Node U, Node T, DocumentScalarProperty ST) {
        DocumentScalarProperty SU = new DocumentScalarProperty(U.getNodeName());
        List<Integer> qNums = new ArrayList<>();
        if (T == null) {
            for (QueryKeysEntity entity : queryKeysEntities) {
                qNums.add(entity.getQueryNum());
            }
        } else {
            Edge edge = graph.getEdgeByNode(T, U);
            //修改如下
            List<Edge> edges = graph.getEdgesByNode(T, U);
            Set<Integer> edgeNums = new HashSet<>();
            for (Edge edge1 : edges) {
                edgeNums.addAll(edge1.getQueryNumbers());
            }
            for (QueryKeysEntity entity : queryKeysEntities) {
                if (edgeNums.contains(entity.getQueryNum()))
                    qNums.add(entity.getQueryNum());
            }

        }
        for (Property scalarProperty : U.getScalarProperties()) {
            //FOR each qNum ∈ qNums DO
            for (int qNum : qNums) {
                Set<String> pSet = new HashSet<>();
                pSet.addAll(graph.queryKeysEntityMaps.get(qNum).getEqualitySearchKeys());
                pSet.addAll(graph.queryKeysEntityMaps.get(qNum).getInequalitySearchKeys());
                pSet.addAll(graph.queryKeysEntityMaps.get(qNum).getOrderingKeys());
                pSet.addAll(graph.queryKeysEntityMaps.get(qNum).getSelectedKeys());
                if (pSet.contains(U.getNodeName() + "." + scalarProperty.getName())) {
                    //Create a field fp corresponding to p;
                    Property property = new Property(U.getNodeName() + "_" + scalarProperty.getName(), scalarProperty.getType(), scalarProperty.isPrimaryKey());
                    if (!SU.getProperty().contains(property)) {
                        SU.getProperty().add(property);
                    }
                }
            }
        }
        // FOR each QPG edge <U, X> DO
        for (Edge edge : graph.edgeMaps.getOrDefault(U.getNodeName(), new ArrayList<>())) {
            //IF queryNums(<U, X>) ∩ queryNums(A) ≠ ∅ THEN
            List<Integer> qNums2 = new ArrayList<>();
            for (QueryKeysEntity entity : queryKeysEntities) {
                if (edge.getQueryNumbers().contains(entity.getQueryNum()))
                    qNums2.add(entity.getQueryNum());
            }
            if (!qNums2.isEmpty()) {
                List<QueryKeysEntity> queryKeysEntityList = new ArrayList<>();
                for (int qNum : qNums2) {
                    queryKeysEntityList.add(graph.queryKeysEntityMaps.get(qNum));
                }
                DesignDocumentSchemata(queryKeysEntityList, graph.nodeMaps.get(edge.getEndNode()), U, SU);
            }
        }
        if (T != null && (graph.getCardinalityByNode(T, U) == '*')) {
            Set<Edge> edges = new HashSet<>();
            for (QueryKeysEntity queryKeysEntity : queryKeysEntities) {
                Edge edge = graph.getEdgeByNodeAndNum(T, U, queryKeysEntity.getQueryNum());
                edges.add(edge);
            }
            for (Edge edge : edges) {
                SU.setName(edge.getRelationShip() + SU.getName() + "List");
                ST.getDocumentPropertyList().add(SU);
            }
        } else {
            ST.getProperty().addAll(SU.getProperty());
            ST.getDocumentPropertyList().addAll(SU.getDocumentPropertyList());
        }
    }

    public List<DocumentScalarPropertyExtendIndex> getSchemataInfo() {
        List<DocumentScalarPropertyExtendIndex> documentScalarPropertyList = new ArrayList<>();
        documentScalarPropertyList.addAll(documentScalarPropertyMap.values());
        return documentScalarPropertyList;
    }

    public Map<String, DocumentScalarPropertyExtendIndex> getDocumentScalarPropertyMap() {
        return documentScalarPropertyMap;
    }

    public void setDocumentScalarPropertyMap(Map<String, DocumentScalarPropertyExtendIndex> documentScalarPropertyMap) {
        this.documentScalarPropertyMap = documentScalarPropertyMap;
    }
}
