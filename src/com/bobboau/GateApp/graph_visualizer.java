package com.bobboau.GateApp;

//package graph;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Font;
import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.decorators.PickableEdgePaintTransformer;
import edu.uci.ics.jung.visualization.decorators.PickableVertexPaintTransformer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.DefaultEdgeLabelRenderer;
import edu.uci.ics.jung.visualization.renderers.DefaultVertexLabelRenderer;
import edu.uci.ics.jung.visualization.renderers.EdgeLabelRenderer;

/**
 * @author Bobboau
 *
 */
public class graph_visualizer {

    /**
     * the graph
     */
    Graph<Integer,Number> graph;
    ArrayList<Vertex_people> group_people;
    ArrayList<edge_relation> group_relation;
    int Total_Link;

    /**
     * the visual component and renderer for the graph
     */
    public VisualizationViewer<Integer,Number> vv;
    
/**
 * @param VP
 * @param ER
 * @param Link_num
 */
public  graph_visualizer(ArrayList<Vertex_people> VP,ArrayList<edge_relation> ER,int Link_num) {
	Total_Link = Link_num;
	set_group_people(VP);
//	System.out.print(group_people.get(0).get_name());
	set_group_relation(ER);
//    System.out.print(group_relation.get(0).get_First()) ;    
        // create a simple graph for the demo
	graph = new DirectedSparseGraph<Integer,Number>();
	Integer[] v = createVertices(group_people.size());
	Number[] edge = createEdges(v);
        //FRlayout : Implements the Fruchterman-Reingold force-directed algorithm for node layout.
	vv =  new VisualizationViewer<Integer,Number>(new FRLayout<Integer,Number>(graph));
	
        //An instance of this abstract class can transform a source tree into a result tree.
	vv.getRenderContext().setVertexLabelTransformer(new Transformer<Integer,String>(){
   public String transform(Integer v) {
//	return group_people.get(v).get_name();
	return "";
}});
	
	
	
	vv.getRenderContext().setVertexLabelRenderer(new DefaultVertexLabelRenderer(Color.cyan));
	vv.getRenderContext().setEdgeLabelRenderer(new DefaultEdgeLabelRenderer(Color.cyan));
	
	vv.getRenderContext().setVertexFontTransformer(new Transformer<Integer, Font>(){
		public Font transform(final Integer v){
			return new Font("Verdana", Font.BOLD, 8);
		}

		
	});
	vv.getRenderContext().setVertexIconTransformer(new Transformer<Integer,Icon>() {

        	/**
        	 * Implements the Icon interface to draw an Icon with background color and
        	 * a text label
        	 */
public Icon transform(final Integer v) {
	return new Icon() {
			
		public int getIconHeight() {
			return 20;
		}
		
		public int getIconWidth() {
			return 20;
		}

		public void paintIcon(Component c, Graphics g,
				int x, int y) {
			if(vv.getPickedVertexState().isPicked(v)) {
				g.setColor(Color.blue);
				if(!group_people.get(v).get_feature().equals(null)){
					for(int i=0; i<group_people.get(v).get_feature().size();i++){
						g.drawString((String)group_people.get(v).get_feature().get(i), x-20, y+i*20+50);
					}
				}
				g.drawString(group_people.get(v).get_name(), x-20, y+30);
				
			} else {
				g.setColor(Color.red);
				if(!group_people.get(v).get_feature().equals(null)){
					for(int i=0; i<group_people.get(v).get_feature().size();i++){
					}
				}
				g.drawString(group_people.get(v).get_name(), x-20, y+30);
			}
			g.fillOval(x, y, 20,20);
			if(vv.getPickedVertexState().isPicked(v)) {
				g.setColor(Color.black);
			} else {
				g.setColor(Color.white);
			}
			
						
		}};
}});

        vv.getRenderContext().setVertexFillPaintTransformer(new PickableVertexPaintTransformer<Integer>(vv.getPickedVertexState(), Color.white,  Color.yellow));

        
		vv.getRenderContext().setEdgeLabelTransformer(new Transformer<Number,String>(){
			public String transform(final Number edge){
				if (!vv.getPickedEdgeState().isPicked(edge)){
				if (group_relation.get((Integer) edge).get_Relations().size() ==0){
					return "";}
					else{ 
//						Note just modified
//						String out = new String(" ");
//						for(int i=0;i<3;i++){	
//							
//							out = out.concat(group_relation.get((Integer) edge).get_Relations().get(i));
//							out = out.concat(" ");
//						}
					
						
						
						
				return group_relation.get((Integer) edge).get_Relations().get(0);
					}
			}else{
//				System.out.println(group_relation.get((Integer) edge).get_Relations().size());
				if (group_relation.get((Integer) edge).get_Relations().size() ==0){
					return "";}
					else{
						String out = new String("\\");
						for(int i=0;i<group_relation.get((Integer) edge).get_Relations().size();i++){	
							
							out = out.concat(group_relation.get((Integer) edge).get_Relations().get(i));
							out = out.concat("\\");
						}
//						System.out.println(group_relation.get((Integer) edge).get_Relations().get(0));
//						System.out.println(out);
				return out;
					}
				
			}
			}
				
		});
		//
		
        vv.getRenderContext().setEdgeDrawPaintTransformer(new PickableEdgePaintTransformer<Number>(vv.getPickedEdgeState(), Color.black, Color.lightGray));

        vv.setBackground(Color.white);

        // add my listener for ToolTips
        vv.setVertexToolTipTransformer(new ToStringLabeller<Integer>());

        
// gadgets
//        final GraphZoomScrollPane panel = new GraphZoomScrollPane(vv);      
//        final ModalGraphMouse gm = new DefaultModalGraphMouse<Integer,Number>();
//        vv.setGraphMouse(gm);
//        
//        final ScalingControl scaler = new CrossoverScalingControl();
//
//        JButton plus = new JButton("+");
//        plus.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                scaler.scale(vv, 1.1f, vv.getCenter());
//            }
//        });
//        JButton minus = new JButton("-");
//        minus.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                scaler.scale(vv, 1/1.1f, vv.getCenter());
//            }
//        });
//
//        
//        JPanel controls = new JPanel();
//        controls.add(plus);
//        controls.add(minus);
//        controls.add(((DefaultModalGraphMouse<Integer,Number>) gm).getModeComboBox());
//        content.add(controls, BorderLayout.SOUTH);

        
}
    
    
/**
* create some vertices
* @param count how many to create
* @return the Vertices in an array
*/
private Integer[] createVertices(int count) {
	Integer[] v = new Integer[count];
	for (int i = 0; i < count; i++) {
		v[i] = new Integer(i);
		graph.addVertex(v[i]);
        	}
        	return v;
}
/*
 * set group relations 
 */

private void set_group_people(ArrayList<Vertex_people> GP){
	group_people = new ArrayList<Vertex_people>();
	for(int i =0; i<GP.size();i++){
		Vertex_people new_people = new Vertex_people(GP.get(i).get_name());
		new_people.set_feature(GP.get(i).get_feature());
		group_people.add(new_people);
		
	}

}
/*
 * set group people 
 */
private void set_group_relation(ArrayList<edge_relation> EP){
	group_relation = new ArrayList<edge_relation>();
	for(int i = 0; i<EP.size();i++){
		edge_relation new_relation = new edge_relation(EP.get(i).get_First(),EP.get(i).get_Second());
		new_relation.set_Relations(EP.get(i).get_Relations());
		group_relation.add(new_relation);
	}
}

    /**
     * create edges for this demo graph
     * @param v an array of Vertices to connect
     * @return -
     */
    public Number[] createEdges(Integer[] v) {
    	Number[] edge_num = new Number[ Total_Link+1];
    	for (int i=0 ;i<= Total_Link ; i++){
    	edge_num[i] =i;
    	}
    	 for (int i=0; i<group_relation.size();i++){
    		 int link1 = 0;
    		 int link2 = 1;
    		 for (int j=0; j<group_people.size();j++){
    			if (group_people.get(j).get_name().equals(group_relation.get(i).get_First())){
    				link1 =j;
    				break;
    			 }
    		 }
    		 for (int j=0; j<group_people.size();j++){
     			if (group_people.get(j).get_name().equals(group_relation.get(i).get_Second())){
     				link2 =j;
     				break;
     			 }
     		 }
//    		 System.out.println(abc);
    		 
    	graph.addEdge( edge_num[i], v[link1], v[link2], EdgeType.DIRECTED);

    	 }
    	return edge_num;
    	 
    }

    /**
     * a driver for this demo
     */
    
    	
//    	
//    	final JFrame frame = new JFrame();
//   	 frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//   	 Container content = frame.getContentPane();
   	 
//    	final VisualizationViewer<Integer,Number> vv = abc.vv;
//    	final GraphZoomScrollPane panel = new GraphZoomScrollPane(vv); 
//    	content.add(panel);
//    	final ModalGraphMouse gm = new DefaultModalGraphMouse<Integer,Number>();
//    	vv.setGraphMouse(gm);
//      
//    	final ScalingControl scaler = new CrossoverScalingControl();
//
//    	JButton plus = new JButton("+");
//    	plus.addActionListener(new ActionListener() {
//    	public void actionPerformed(ActionEvent e) {
//             scaler.scale(vv, 1.1f, vv.getCenter());
//          }
//      });
//      JButton minus = new JButton("-");
//      minus.addActionListener(new ActionListener() {
//          public void actionPerformed(ActionEvent e) {
//              scaler.scale(vv, 1/1.1f, vv.getCenter());
//          }
//      });
//
//      
//      JPanel controls = new JPanel();
//      controls.add(plus);
//      controls.add(minus);
//      controls.add(((DefaultModalGraphMouse<Integer,Number>) gm).getModeComboBox());
//      content.add(controls, BorderLayout.SOUTH);
// 	 	frame.pack();
// 	 	frame.setVisible(true);
    	

//    public static void main(String[] args){
//    	graph_visualizer abc = new graph_visualizer();
//    	final JFrame frame = new JFrame();
//    	 frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//    	 frame.add(abc.generateGraph());
//    	 frame.pack();
//    	 frame.setVisible(true);
//    }
    
}
