package world.graph.view;

import java.awt.BorderLayout;
import java.awt.geom.Rectangle2D;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.jgraph.JGraph;
import org.jgraph.graph.AttributeMap;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.GraphConstants;
import org.jgrapht.ext.JGraphModelAdapter;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import com.vividsolutions.jts.geom.Point;

public class GraphViewer extends JFrame {

	private static final long serialVersionUID = -4853599518345563890L;

	private JPanel contentPane;
	
	private JGraphModelAdapter<Point, DefaultWeightedEdge> jgModel;
	
	private DefaultDirectedWeightedGraph<Point, DefaultWeightedEdge> graph = null;

	/**
	 * Create the frame.
	 */
	public GraphViewer() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		setVisible(true);
	}

	public void setGraph(
		DefaultDirectedWeightedGraph<Point, DefaultWeightedEdge> graph)
	{
		this.graph = graph;
	}
	
	public void showGraph() {
		jgModel = new JGraphModelAdapter<>(graph);
		
		JGraph jgraph = new JGraph(jgModel);
		contentPane.add(jgraph);
		
		for (Point v : graph.vertexSet())
			positionVertex(v);
	}

	@SuppressWarnings("unchecked")
	private void positionVertex(Point vertex) {
        DefaultGraphCell cell = jgModel.getVertexCell(vertex);
        AttributeMap attr = cell.getAttributes();
        Rectangle2D bounds = GraphConstants.getBounds(attr);

        Rectangle2D newBounds =
            new Rectangle2D.Double(
                vertex.getX(),
                vertex.getY(),
                bounds.getWidth(),
                bounds.getHeight());

        GraphConstants.setBounds(attr, newBounds);

        // TODO: Clean up generics once JGraph goes generic
        AttributeMap cellAttr = new AttributeMap();
        cellAttr.put(cell, attr);
        jgModel.edit(cellAttr, null, null, null);
    }
	
}
