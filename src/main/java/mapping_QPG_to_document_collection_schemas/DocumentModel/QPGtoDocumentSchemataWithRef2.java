package mapping_QPG_to_document_collection_schemas.DocumentModel;

import Entity.QPG.Graph;
import Entity.Schemata.DocRef;
import Entity.Schemata.DocumentScalarPropertyByRef;
import Entity.Schemata.DocumentScalarPropertyByRefExtendIndex;
import Entity.WorkLoad.*;

import java.util.*;

/**
 * This is our Schemata generation algorithm on Document Schemata
 */
public class QPGtoDocumentSchemataWithRef2 {

    private Graph graph;
    private Map<String, DocumentScalarPropertyByRef> documentScalarPropertyByRefMap;
    private Map<String, DocumentScalarPropertyByRefExtendIndex> documentScalarPropertyByRefExtendIndexMap;
    private List<String> highlyWrittenEntities;
    private List<String> highAccessEntities;

    public QPGtoDocumentSchemataWithRef2(Graph g) {
        graph = g;
        documentScalarPropertyByRefMap = new HashMap<>();
        documentScalarPropertyByRefExtendIndexMap = new HashMap<>();
        highlyWrittenEntities = new ArrayList<>();
        highAccessEntities = new ArrayList<>();
        initHighlyWrittenEntities();
    }

    public void initHighlyWrittenEntities() {
        highlyWrittenEntities.add("Order");
        highAccessEntities.add("Product");
    }

    //Generate Schemata with Ref
    public void generateSchemata() {
        documentScalarPropertyByRefMap.clear();
        List<Node> nodes = graph.getAccessPoints();
        //1.Initialize Schemata according to Node
        for (Node node : nodes) {
            DocumentScalarPropertyByRef ST = new DocumentScalarPropertyByRef(node.getNodeName());
            documentScalarPropertyByRefMap.put(ST.getName(), ST);
        }
        for (Node node : nodes) {
            DocumentScalarPropertyByRef ST = documentScalarPropertyByRefMap.get(node.getNodeName());
            DesignDocumentSchemataRef(node, node, null, ST);
            //Create indexes
            List<String> indexes = createIndex(node);
            DocumentScalarPropertyByRefExtendIndex documentScalarPropertyByRefExtendIndex = new DocumentScalarPropertyByRefExtendIndex(ST);
            documentScalarPropertyByRefExtendIndex.setIndexes(indexes);
            documentScalarPropertyByRefExtendIndexMap.put(node.getNodeName(), documentScalarPropertyByRefExtendIndex);
        }
    }

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
        return indexes;
    }

    public List<String> convertIndex(Node node, int qNum, List<String> stringList) {
        List<String> conversionList = new ArrayList<>();
        for (String str : stringList) {
            String[] arr = str.split("\\.");
            StringBuilder sb = new StringBuilder();
            if (arr[0].equals(node.getNodeName()))
                sb.append(arr[0]).append("_").append(arr[1]);
            conversionList.add(sb.toString());
        }
        return conversionList;
    }


    public void DesignDocumentSchemataRef(Node A, Node U, Node T, DocumentScalarPropertyByRef ST) {
        DocumentScalarPropertyByRef SU = new DocumentScalarPropertyByRef(U.getNodeName());
        List<Integer> qNums = new ArrayList<>();
        if (T == null) {
            qNums.addAll(A.getQueryNumbers());
        } else {
            List<Edge> edges = graph.getEdgesByNode(T, U);
            Set<Integer> edgeNums = new HashSet<>();
            for (Edge edge1 : edges) {
                edgeNums.addAll(edge1.getQueryNumbers());
            }

            for (int num : A.getQueryNumbers()) {
                if (edgeNums.contains(num))
                    qNums.add(num);
            }
        }

        for (Property scalarProperty : U.getScalarProperties()) {
            if (U.getPrimaryKeys().contains(scalarProperty.getName())) {
                Property property = new Property(U.getNodeName() + "_" + scalarProperty.getName(), scalarProperty.getType(), scalarProperty.isPrimaryKey());
                if (!SU.getProperty().contains(property)) {
                    SU.getProperty().add(property);
                }
            }
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
        for (String node : U.getOutNeighbors()) {
            //
            Node x = graph.nodeMaps.get(node);
            List<Edge> edges = graph.getEdgesByNode(U, x);
            List<Integer> edgeNums = new ArrayList<>();
            for (Edge edge1 : edges) {
                edgeNums.addAll(edge1.getQueryNumbers());
            }
            //Determine if edgeNums and qNums intersect
            boolean flag = false;
            for (int num : qNums) {
                if (edgeNums.contains(num)) {
                    flag = true;
                    break;
                }
            }
            if (flag) {
                DesignDocumentSchemataRef(A, x, U, SU);
            }
        }

        if (T != null && (graph.getCardinalityByNode(T, U) == '*')) {
            if (highlyWrittenEntities.contains(U.getNodeName()) || highAccessEntities.contains(U.getNodeName())) {
                if (graph.getAccessPoints().contains(U)) {
                    //add Su U.schemata
                    addSchemata(SU, U);
                    //Add Su as Array Ref to ST
                    String name = SU.getName() + "RefList";
                    DocRef docRef = new DocRef(name);
                    docRef.setArray(true);
                    docRef.setCollectionName(U.getNodeName());
                    ST.getDocRefs().add(docRef);
                } else { //ADD no contain in algorithms
                    String name = SU.getName() + "List";
                    SU.setName(name);
                    ST.getDocArray().add(SU);
                }
            } else {
                String name = SU.getName() + "List";
                SU.setName(name);
                ST.getDocArray().add(SU);
            }
        } else if (highlyWrittenEntities.contains(U.getNodeName()) || highAccessEntities.contains(U.getNodeName())) {
            if (graph.getAccessPoints().contains(U)) {
                //add Su U.schemata
                addSchemata(SU, U);
                String name = SU.getName() + "Ref";
                DocRef docRef = new DocRef(name);
                docRef.setArray(false);
                docRef.setCollectionName(U.getNodeName());

                if (!ST.getName().equals(SU.getName()))
                    ST.getDocRefs().add(docRef);
            }
        } else {
            ST.getProperty().addAll(SU.getProperty());
            ST.getDocArray().addAll(SU.getDocArray());
            ST.getDocRefs().addAll(SU.getDocRefs());
        }
    }

    //add Su into U schemata
    private void addSchemata(DocumentScalarPropertyByRef SU, Node U) {
        DocumentScalarPropertyByRef doc = documentScalarPropertyByRefMap.get(U.getNodeName());
        for (Property scalarProperty : SU.getProperty()) {
            if (!doc.getProperty().contains(scalarProperty)) {
                doc.getProperty().add(scalarProperty);
            }
        }
        for (DocumentScalarPropertyByRef documentScalarPropertyByRef : SU.getDocArray()) {
            if (!doc.getDocArray().contains(documentScalarPropertyByRef)) {
                doc.getDocArray().add(documentScalarPropertyByRef);
            }
        }
        for (DocRef docRef : SU.getDocRefs()) {
            if (!doc.getDocRefs().contains(docRef)) {
                doc.getDocRefs().add(docRef);
            }
        }
    }

    public Map<String, DocumentScalarPropertyByRef> getDocumentScalarPropertyByRefMap() {
        return documentScalarPropertyByRefMap;
    }

    public void setDocumentScalarPropertyByRefMap(Map<String, DocumentScalarPropertyByRef> documentScalarPropertyByRefMap) {
        this.documentScalarPropertyByRefMap = documentScalarPropertyByRefMap;
    }

    public Map<String, DocumentScalarPropertyByRefExtendIndex> getDocumentScalarPropertyByRefExtendIndexMap() {
        return documentScalarPropertyByRefExtendIndexMap;
    }

    public void setDocumentScalarPropertyByRefExtendIndexMap(Map<String, DocumentScalarPropertyByRefExtendIndex> documentScalarPropertyByRefExtendIndexMap) {
        this.documentScalarPropertyByRefExtendIndexMap = documentScalarPropertyByRefExtendIndexMap;
    }

    public List<DocumentScalarPropertyByRef> getSchemataInfo() {
        List<DocumentScalarPropertyByRef> documentScalarPropertyList = new ArrayList<>();
        documentScalarPropertyList.addAll(documentScalarPropertyByRefMap.values());
        return documentScalarPropertyList;
    }

    public List<DocumentScalarPropertyByRefExtendIndex> getSchemataInfoWithIndex() {
        List<DocumentScalarPropertyByRefExtendIndex> documentScalarPropertyList = new ArrayList<>();
        documentScalarPropertyList.addAll(documentScalarPropertyByRefExtendIndexMap.values());
        return documentScalarPropertyList;
    }

}
