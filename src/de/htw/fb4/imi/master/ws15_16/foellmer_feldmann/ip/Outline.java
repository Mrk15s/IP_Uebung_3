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
import java.util.SortedSet;
import java.util.TreeSet;

import de.htw.fb4.imi.master.ws15_16.foellmer_feldmann.ip.util.ImageUtil;

/**
 * [SHORT_DESCRIPTION] 
 *
 * @author Sascha Feldmann <sascha.feldmann@gmx.de>
 * @since 10.11.2015
 */
public class Outline {
	public static final int TYPE_INNER = 0;
	public static final int TYPE_OUTER = 1;
	
	protected List<Vertex> whiteVertices = new ArrayList<>();
	protected List<Vertex> blackVertices = new ArrayList<>();
	protected List<Edge> edges = new ArrayList<>();
	/**
	 * Map of y values -> x values, starting from the minimum, going to top.
	 */
	protected Map<Integer, SortedSet<Integer>> outlineLimits = new HashMap<>();
	
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

	private void addWhiteVertex(Vertex vertex)
	{
		if (this.whiteVertices.size() > 0 && this.whiteVertices.get(0).equals(vertex)) {
			// path is completed
			this.isClosed = true;			
		}
		
		this.whiteVertices.add(vertex);
	}
	
	private void determineLimit(Vertex vertex) {
		if (!this.outlineLimits.containsKey(vertex.getY())) {
			// add max and min x values for the given Y to our limits map 
			SortedSet<Integer> initialLimits = new TreeSet<>();
			
			this.outlineLimits.put(vertex.getY(), initialLimits);
		}
		
		SortedSet<Integer> lineLimits = this.outlineLimits.get(vertex.getY());		
		lineLimits.add(vertex.getX());
		
		if (vertex.getY() < this.minY) {
			this.minY = vertex.getY();
		}
		
		if (vertex.getY() > this.maxY) {
			this.maxY = vertex.getY();
		}
	}
	
	private void addBlackVertex(Vertex black) {
		if (this.blackVertices.size() > 0 && this.blackVertices.get(0).equals(black)) {
			// path is completed
			this.isClosed = true;			
		}
		
		this.blackVertices.add(black);
		this.determineLimit(black);
	}

	public void addEdge(Edge edge)
	{
		if (this.edges.size() > 0 && this.edges.get(0).equals(edge)) {
			// path is completed
			this.isClosed = true;			
		}
		
		this.edges.add(edge);
		this.addWhiteVertex(edge.getWhite());
		this.addBlackVertex(edge.getBlack());
	}	

	public Vertex[] getVertices() {
		return whiteVertices.toArray(new Vertex[this.whiteVertices.size()]);
	}
	
	public Edge[] getEdges() {
		return edges.toArray(new Edge[this.edges.size()]);
	}

	public boolean hasEdge(Edge e) {
		return this.edges.contains(e);
	}

	public boolean isSurroundedByAnExistingOutline(Vertex pixelVertex) {
		if (this.outlineLimits.containsKey(pixelVertex.getY())) {
			Integer[] lineLimits = this.outlineLimits.get(pixelVertex.getY()).toArray(new Integer[0]);
			
			for (int i = 0; i < lineLimits.length - 1; i++) {
				int xValue = lineLimits[i];
				int nextXValue = lineLimits[i + 1];
				
				if (
					xValue < pixelVertex.getX()
					&& nextXValue > pixelVertex.getX()) {
					return true;
				}				
			}
		}
		
		return false;
	}

	public int getLeftLimitX(int y) {
		return this.outlineLimits.get(y).first();
	}
	
	public int getRightLimitX(int y) {
		return this.outlineLimits.get(y).last();
	}
	
	public int getTopLimitY() {
		return this.minY;
	}
	
	public int getBottomLimitY() {
		return this.maxY;
	}

	public int getRightLimitX(int x, int y) {
		if (this.outlineLimits.containsKey(y)) {
			Integer[] lineLimits = this.outlineLimits.get(y).toArray(new Integer[0]);
			
			for (int i = 0; i < lineLimits.length - 1; i++) {
				int xValue = lineLimits[i];
				int nextXValue = lineLimits[i + 1];
				
				if (ImageUtil.isEven(i) && xValue == x) {
					return nextXValue;
				}
			}
		}
		
		return -1;		
	}
	
	public Integer[] getXValues(int y)
	{
		return this.outlineLimits.get(y).toArray(new Integer[0]);
	}

	public boolean containsWhiteVertex(Vertex vertex) {
		return this.whiteVertices.contains(vertex);
	}
	
	public boolean containsBlackVertex(Vertex vertex) {
		return this.blackVertices.contains(vertex);
	}

	public boolean containsVertex(Vertex vertex) {
		return this.containsWhiteVertex(vertex) || this.containsBlackVertex(vertex);
	}
	
	
}
