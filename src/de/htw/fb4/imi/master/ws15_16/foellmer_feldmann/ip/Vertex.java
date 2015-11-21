/**
 * Image Processing WiSe 2015/16
 *
 * Authors: Markus Föllmer, Sascha Feldmann
 */
package de.htw.fb4.imi.master.ws15_16.foellmer_feldmann.ip;

/**
 * Vertex that can be stored in a sequence.
 *
 * @author Sascha Feldmann <sascha.feldmann@gmx.de>
 * @since 10.11.2015
 */
public class Vertex {
	protected int x;
	protected int y;
	
	public Vertex(int x, int y) {
		super();
		
		this.x = x;
		this.y = y;
	}

	public int getX() {
		return x;
	}
	
	public void setX(int x) {
		this.x = x;
	}
	
	public int getY() {
		return y;
	}
	
	public void setY(int y) {
		this.y = y;
	}
	
	public boolean isLeftOf(Vertex right) {		
		return (this.getX() < right.getX()
				&& this.getY() == right.getY());
	}
	
	public boolean isRightOf(Vertex left) {		
		return (this.getX() > left.getX()
				&& this.getY() == left.getY());
	}
	
	public boolean isAboveOf(Vertex below) {		
		return (this.getY() < below.getY()
				&& this.getX() == below.getX());
	}
	
	public boolean isBelowOf(Vertex above) {		
		return (this.getY() > above.getY()
				&& this.getX() == above.getX());
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Vertex [getX()=");
		builder.append(getX());
		builder.append(", getY()=");
		builder.append(getY());
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + x;
		result = prime * result + y;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Vertex other = (Vertex) obj;
		if (x != other.x)
			return false;
		if (y != other.y)
			return false;
		return true;
	}
}
