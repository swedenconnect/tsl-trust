/*
 * Copyright 2012 Swedish Agency for Economic and Regional Growth - Tillväxtverket 
 *  		 
 * Licensed under the EUPL, Version 1.1 or ñ as soon they will be approved by the 
 * European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence. 
 * You may obtain a copy of the Licence at:
 *
 * http://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed 
 * under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and limitations 
 * under the Licence.
 */
package se.tillvaxtverket.tsltrust.common.utils.general;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

/**
 * Tree object functions
 */
public class ObjectTree {

    private DefaultMutableTreeNode nameNode = null;
    private DefaultMutableTreeNode objectNode = null;
    private DefaultMutableTreeNode keyNode = null;

    public ObjectTree(String rootName, String rootIdentifier, Object rootObject) {
        nameNode = new DefaultMutableTreeNode(rootName);
        objectNode = new DefaultMutableTreeNode(rootObject);
        keyNode = new DefaultMutableTreeNode(rootIdentifier);
    }

    public DefaultMutableTreeNode getNameTree() {
        return nameNode;
    }

    public DefaultMutableTreeNode getkeyTree() {
        return keyNode;
    }

    public DefaultMutableTreeNode getObjectTree() {
        return objectNode;
    }

    public void addChildNode(String parentKey, String childKey, String childName, Object childObject) {
        if (getkeyNode(childKey) == null) {
            DefaultMutableTreeNode targetKeyNode = getkeyNode(parentKey);
            if (targetKeyNode != null) {
                DefaultMutableTreeNode targetNameNode = getRelatedNode(targetKeyNode, nameNode);
                DefaultMutableTreeNode targetObjectNode = getRelatedNode(targetKeyNode, objectNode);
                targetKeyNode.add(new DefaultMutableTreeNode(childKey));
                targetNameNode.add(new DefaultMutableTreeNode(childName));
                targetObjectNode.add(new DefaultMutableTreeNode(childObject));
            }
        }
    }

    public void updateNode(String currentKey, String newKey, String newName, Object newObject) {
        DefaultMutableTreeNode targetKeyNode = getkeyNode(currentKey);
        if (targetKeyNode != null) {
            DefaultMutableTreeNode targetNameNode = getRelatedNode(targetKeyNode, nameNode);
            DefaultMutableTreeNode targetObjectNode = getRelatedNode(targetKeyNode, objectNode);
            targetKeyNode.setUserObject(newKey);
            targetNameNode.setUserObject(newName);
            targetObjectNode.setUserObject(newObject);
        }
    }
    
    public void updateNodeObject(String key, Object newObject){
        DefaultMutableTreeNode targetKeyNode = getkeyNode(key);
        if (targetKeyNode != null) {
            DefaultMutableTreeNode targetObjectNode = getRelatedNode(targetKeyNode, objectNode);
            targetObjectNode.setUserObject(newObject);            
        }        
    }

    public Object getObjectFromJTreeSelection(JTree jTree) {
        TreePath tp = jTree.getSelectionPath();
        if (tp != null) {
            DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tp.getLastPathComponent();
            return selectNode(getPath(selectedNode), objectNode).getUserObject();
        }
        return null;
    }

    /**
     * Returns the next child of the provided node
     * @param selectNode the parent node of the wanted child node
     * @return The first available child node. If no child node is available, then the select node is returned.
     */
    private static DefaultMutableTreeNode getNextChild(DefaultMutableTreeNode selectNode) {
        Enumeration e = selectNode.preorderEnumeration();

        while (e.hasMoreElements()) {
            if (((DefaultMutableTreeNode) e.nextElement()).equals(selectNode)) {
                if (e.hasMoreElements()) {
                    return (DefaultMutableTreeNode) e.nextElement();
                }
            }
        }
        return selectNode;
    }

    /**
     * Attempts to return the next node on the same depth in the tree (breadth first enumeration). If no node at the same depth is available
     * then the first child node is selected.
    
     * @param selectNode The node from which the next node is searched
     * @param rootNode The root node of the tree within which the search is done. This may be the parent of the select node or the root of the whole tree.
     * @return The next node in breadth first enumeration. if no next node is available, the selectNode is returned.
     */
    private static DefaultMutableTreeNode getNextNode(DefaultMutableTreeNode selectNode, DefaultMutableTreeNode rootNode) {
        Enumeration e = rootNode.breadthFirstEnumeration();
        while (e.hasMoreElements()) {
            if (((DefaultMutableTreeNode) e.nextElement()).equals(selectNode)) {
                if (e.hasMoreElements()) {
                    return (DefaultMutableTreeNode) e.nextElement();
                }
            }
        }
        return selectNode;
    }

    private DefaultMutableTreeNode getkeyNode(String searchKey) {
        DefaultMutableTreeNode targetNode = null;
        DefaultMutableTreeNode resultNode = null;
        Enumeration e = keyNode.breadthFirstEnumeration();
        while (e.hasMoreElements()) {
            targetNode = (DefaultMutableTreeNode) e.nextElement();
            if (((String) targetNode.getUserObject()).equals(searchKey)) {
                resultNode = targetNode;
                break;
            }
        }
        return resultNode;
    }

    private static List<Integer> getPath(DefaultMutableTreeNode target) {
        TreeNode[] nodePath = target.getPath();
        TreeNode parent = nodePath[0];
        List<Integer> nodeIndex = new ArrayList<Integer>();
        for (TreeNode node : nodePath) {
            nodeIndex.add(parent.getIndex(node));
            parent = node;
        }
        for (int i : nodeIndex) {
        }
        return nodeIndex;
    }

    private static DefaultMutableTreeNode selectNode(List<Integer> path, DefaultMutableTreeNode parent) {
        DefaultMutableTreeNode selectNode = parent;
        for (int index : path) {
            if (index == -1) {
                selectNode = parent;
                continue;
            }
            selectNode = getNextChild(selectNode);
            int i = 0;
            while (i < index) {
                selectNode = getNextNode(selectNode, parent);
                i++;
            }
        }
        return selectNode;
    }

    public static DefaultMutableTreeNode getRelatedNode(DefaultMutableTreeNode keyNode, DefaultMutableTreeNode relatedNode) {
        List<Integer> path = getPath(keyNode);
        DefaultMutableTreeNode related = selectNode(path, relatedNode);
        return related;
    }
}