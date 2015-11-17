/**
 * Image Processing WiSe 2015/16
 *
 * Authors: Markus Föllmer, Sascha Feldmann
 */
package de.htw.fb4.imi.master.ws15_16.foellmer_feldmann.ip;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * [SHORT_DESCRIPTION] 
 *
 * @author Sascha Feldmann <sascha.feldmann@gmx.de>
 * @since 10.11.2015
 */
public class Outline {
	public static final int TYPE_INNER = 0;
	public static final int TYPE_OUTER = 1;
	
	protected List<Vertex> vertices = new ArrayList<>();
	protected List<Edge> edges = new ArrayList<>();
	/**
	 * Map of y values -> min and max x values to quickly get lowest and largest X value for a single line.
	 */
	protected Map<Integer, int[]> outlineLimits = new HashMap<>();
	
	protected boolean isClosed = false;
	
	protected int type = TYPE_OUTER;
	private int minY = Integer.MAX_VALUE;
	private int maxY = -1;		

	public Outline(boolean isOuter) {
		super();
		
		if (isOuter) {
			this.type = TYPE_OUTER;
		} else {
			this.type = TYPE_INNER;
		}
	}

	public boolean isClosed() {
		return isClosed;
	}
	
	public boolean isOuter() {
		return TYPE_OUTER == this.type;
	}

	private void addVertex(Vertex vertex)
	{
		if (this.vertices.size() > 0 && this.vertices.get(0).equals(vertex)) {
			// path is completed
			this.isClosed = true;			
		}
		
		this.vertices.add(vertex);
		this.determineLimit(vertex);
	}
	
	private void determineLimit(Vertex vertex) {
		if (!this.outlineLimits.containsKey(vertex.getY())) {
			// add max and min x values for the given Y to our limits map 
			int[] initialLimits = new int[2];
			initialLimits[0] = Integer.MAX_VALUE; // initial min x
			initialLimits[1] = -1; // initial max x
			
			this.outlineLimits.put(vertex.getY(), initialLimits);
		}
		
		int[] lineLimits = this.outlineLimits.get(vertex.getY());
		
		if (vertex.getX() < lineLimits[0]) {
			lineLimits[0] = vertex.getX();
		}
		
		if (vertex.getX() > lineLimits[1]) {
			lineLimits[1] = vertex.getX();
		}
		
		if (vertex.getY() < this.minY) {
			this.minY = vertex.getY();
		}
		
		if (vertex.getY() > this.maxY) {
			this.maxY = vertex.getY();
		}
	}

	public void addEdge(Edge edge)
	{
		if (this.edges.size() > 0 && this.edges.get(0).equals(edge)) {
			// path is completed
			this.isClosed = true;			
		}
		
		this.edges.add(edge);
		this.addVertex(edge.getBlack());
	}

	public Vertex[] getVertices() {
		return vertices.toArray(new Vertex[this.vertices.size()]);
	}
	
	public Edge[] getEdges() {
		return edges.toArray(new Edge[this.edges.size()]);
	}

	public boolean hasEdge(Edge e) {
		return this.edges.contains(e);
	}

	public boolean isSurroundedByAnExistingOutline(Vertex pixelVertex) {
		if (this.outlineLimits.containsKey(pixelVertex.getY())) {
			int[] lineLimits = this.outlineLimits.get(pixelVertex.getY());
			
			return lineLimits[0] < pixelVertex.getX() 
					&& lineLimits[1] > pixelVertex.getX();
		}
		
		return false;
	}

	public int getLeftLimitX(int y) {
		return this.outlineLimits.get(y)[0];
	}
	
	public int getRightLimitX(int y) {
		return this.outlineLimits.get(y)[1];
	}
	
	public int getTopLimitY() {
		return this.minY;
	}
	
	public int getBottomLimitY() {
		return this.maxY;
	}
}
