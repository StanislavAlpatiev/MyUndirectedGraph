//Stanislav Alpatiev

import java.util.*;

public class MyUndirectedGraph<T> implements UndirectedGraph<T> {
    private Map<T, Node<T>> graph = new HashMap<>();

    @Override
    public int getNumberOfNodes() {
        return graph.size();
    }

    @Override
    public int getNumberOfEdges() {
        int numberOfEdges = 0;
        for (Node<T> node : graph.values()) {
            //divide by 2 because two nodes share edge which makes for duplicates.
            numberOfEdges += node.numberOfEdges();
        }
        return numberOfEdges;
    }

    @Override
    public boolean add(T data) {
        if (graph.containsKey(data)) {
            return false;
        } else {
            graph.put(data, new Node<T>(data));
            return true;
        }
    }

    @Override
    public boolean connect(T startNode, T endNode, int cost) {
        if (graph.containsKey(startNode) && graph.containsKey(endNode) && cost > 0) {
            graph.get(startNode).addEdge(graph.get(endNode), cost);
            graph.get(endNode).addEdge(graph.get(startNode), cost);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean isConnected(T startNode, T endNode) {
        if (graph.containsKey(startNode) && graph.containsKey(endNode)) {
            Node<T> firstNode = graph.get(startNode);
            Node<T> secondNode = graph.get(endNode);
            return firstNode.isConnected(secondNode);
        } else {
            return false;
        }
    }

    @Override
    public int getCost(T startNode, T endNode) {
        if (isConnected(startNode, endNode)) {
            return graph.get(startNode).getWeightOfEdge(graph.get(endNode));
        }
        return -1;
    }

    @Override
    public List<T> depthFirstSearch(T start, T end) {
        Stack<Node<T>> stack = new Stack<>();
        List<T> path = new LinkedList<>();
        Set<Node<T>> traversedNodes = new HashSet<>();

        Node<T> currentNode = graph.get(start);
        Node<T> destination = graph.get(end);

        if (start.equals(end)) {
            path.add(start);
            return path;
        }

        stack.push(currentNode);
        traversedNodes.add(currentNode);

        while (!stack.isEmpty()) {
            currentNode = getNextUnvisitedEdge(traversedNodes, currentNode.edges.keySet());

            if (currentNode == null) {
                stack.pop();
                currentNode = stack.peek();
            } else {
                stack.push(currentNode);
                traversedNodes.add(currentNode);

                if (currentNode.equals(destination)) {
                    for (Node<T> node : stack) {
                        path.add(node.data);
                    }
                    return path;
                }
            }
        }
        return path;
    }

    private Node<T> getNextUnvisitedEdge(Set<Node<T>> visitedNodes, Set<Node<T>> currentNodesEdges) {
        for (Node<T> edge : currentNodesEdges) {
            if (!visitedNodes.contains(edge)) {
                return edge;
            }
        }
        return null;
    }

    @Override
    public List<T> breadthFirstSearch(T start, T end) {
        Deque<Node<T>> queue = new LinkedList<>();
        List<T> path = new LinkedList<>();
        Map<Node<T>, Node<T>> nodePairs = new HashMap<>();
        Set<Node<T>> traversedNodes = new HashSet<>();

        if (start.equals(end)) {
            path.add(start);
            return path;
        }

        queue.add(graph.get(start));
        while (!queue.isEmpty()) {
            Node<T> current = queue.poll();
            if (!traversedNodes.contains(current)) {
                traversedNodes.add(current);
                if (current.equals(graph.get(end))) {
                    while (current != null) {
                        path.add(current.data);
                        current = nodePairs.get(current); //Via föräldrarna följa vägen tillbaka till ursprunget
                    }
                    Collections.reverse(path);
                    return path;
                }

                for (Node<T> edge : current.edges.keySet()) {
                    if (!traversedNodes.contains(edge) && !nodePairs.containsKey(edge)) {
                        if (!traversedNodes.contains(edge)) {
                            queue.add(edge);
                            nodePairs.put(edge, current);
                        }
                    }
                }
            }
        }

        return path;
    }

    @Override
    public UndirectedGraph<T> minimumSpanningTree() {
        MyUndirectedGraph<T> mst = new MyUndirectedGraph<T>();
        Set<Edge<T>> usedEdges = new HashSet<>();
        Set<Node<T>> nodesInTree = new HashSet<Node<T>>();
        nodesInTree.add(graph.values().iterator().next());
        while (nodesInTree.size() != graph.size()) {
            Set<Edge<T>> edgesToRemove = new HashSet<>();
            Set<Edge<T>> possibleEdgesSet = new HashSet<>();
            PriorityQueue<Edge<T>> possibleEdgesQueue = new PriorityQueue<>();
            for (Node<T> node : nodesInTree) {
                possibleEdgesSet.addAll(node.edgesSet);
            }
            if (nodesInTree.size() != 0) {
                for (Edge<T> possibleEdge : possibleEdgesSet) {
                    for (Node<T> nodeInTree : nodesInTree) {
                        if (possibleEdge.getEndNode().data == nodeInTree.data) {
                            edgesToRemove.add(possibleEdge);
                        }
                    }
                }
            }

            possibleEdgesSet.removeAll(usedEdges);
            possibleEdgesSet.removeAll(edgesToRemove);
            possibleEdgesQueue.addAll(possibleEdgesSet);
            Edge<T> cheapestEdge = possibleEdgesQueue.poll();
            if (cheapestEdge != null) {
                usedEdges.add(cheapestEdge);
                nodesInTree.add(cheapestEdge.startNode);
                nodesInTree.add(cheapestEdge.endNode);
                mst.add(cheapestEdge.getStartNode().data);
                mst.add(cheapestEdge.getEndNode().data);
                mst.connect(cheapestEdge.getStartNode().data, cheapestEdge.getEndNode().data, cheapestEdge.getWeight());
            }
        }
        return mst;
    }

    private static class Node<T> {
        private HashMap<Node<T>, Integer> edges = new HashMap<>();
        private Set<Edge<T>> edgesSet = new HashSet<>();
        private T data;


        Node(T data) {
            this.data = data;
        }

        public int numberOfEdges() {
            return edges.size();
        }

        public void addEdge(Node<T> node, int weight) {
            edges.put(node, weight);
            edgesSet.add(new Edge<T>(this, node, weight));
        }

        public boolean isConnected(Node<T> node) {
            return edges.containsKey(node);
        }

        public int getWeightOfEdge(Node<T> node) {
            return edges.get(node);
        }
    }

    private static class Edge<T> implements Comparable<Edge<T>> {
        private Node<T> startNode;
        private Node<T> endNode;
        private int weight;

        Edge(Node<T> firstNode, Node<T> secondNode, int weight) {
            this.startNode = firstNode;
            this.endNode = secondNode;
            this.weight = weight;
        }

        public Node<T> getStartNode() {
            return startNode;
        }

        public Node<T> getEndNode() {
            return endNode;
        }

        public int getWeight() {
            return weight;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null) {
                return false;
            }
            if (this.getClass() != o.getClass()) {
                return false;
            }
            Edge otherEdge = (Edge) o;
            return startNode.equals(otherEdge.startNode) && endNode.equals(otherEdge.endNode) ||
                    startNode.equals(otherEdge.endNode) && endNode.equals(otherEdge.startNode);
        }

        @Override
        public int hashCode() {
            return startNode.data.hashCode() + endNode.data.hashCode();
        }

        @Override
        public int compareTo(Edge<T> otherEdge) {
            return weight - otherEdge.getWeight();
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("FirstNode: " + startNode.data + ", ");
            sb.append("SecondNode: " + endNode.data + ", ");
            sb.append("Weight: " + weight);
            return sb.toString();
        }
    }
}
