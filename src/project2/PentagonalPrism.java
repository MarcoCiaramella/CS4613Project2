package project2;

import graphicslib3D.Shape3D;
import graphicslib3D.Vertex3D;

/**
 * Auxiliary class for quickly generating a pentagonal prism.
 * <p>
 * Based on Program 6.1.1 - Sphere (Coded Version) from Gordon & Clevenger.
 *
 * @author Eric Peterson
 */
public class PentagonalPrism extends Shape3D
{
	/* ********* *
	 * Constants *
	 * ********* */
	private static final int NUM_VERTICES = 10;
	private static final int NUM_INDICES = 144;
	private static final double GOLDEN_RATIO = (1 + Math.sqrt(5)) / 2;
	
	/* **************** *
	 * Member Variables *
	 * **************** */
	private int[] m_indices;
	private Vertex3D[] m_vertices;
	
	public PentagonalPrism()
	{
		m_vertices = new Vertex3D[NUM_VERTICES];
		m_indices = new int[NUM_INDICES];
		
		m_vertices[0] = new Vertex3D(Math.sqrt((10 + (2 * Math.sqrt(5))) / 5), 0, 1);
		m_vertices[1] = new Vertex3D(Math.sqrt((10 + (2 * Math.sqrt(5))) / 5), 0, -1);
		m_vertices[2] = new Vertex3D(Math.sqrt((5 - Math.sqrt(5)) / 10), GOLDEN_RATIO, 1);
		m_vertices[3] = new Vertex3D(Math.sqrt((5 - Math.sqrt(5)) / 10), GOLDEN_RATIO, -1);
		m_vertices[4] = new Vertex3D(Math.sqrt((5 - Math.sqrt(5)) / 10), -GOLDEN_RATIO, 1);
		m_vertices[5] = new Vertex3D(Math.sqrt((5 - Math.sqrt(5)) / 10), -GOLDEN_RATIO, -1);
		m_vertices[6] = new Vertex3D(-Math.sqrt((5 + (2 * Math.sqrt(5))) / 5), 1, 1);
		m_vertices[7] = new Vertex3D(-Math.sqrt((5 + (2 * Math.sqrt(5))) / 5), 1, -1);
		m_vertices[8] = new Vertex3D(-Math.sqrt((5 + (2 * Math.sqrt(5))) / 5), -1, 1);
		m_vertices[9] = new Vertex3D(-Math.sqrt((5 + (2 * Math.sqrt(5))) / 5), -1, -1);
	}
	
	public int[] getIndices()
	{
		return m_indices;
	}
	
	public Vertex3D[] getVertices()
	{
		return m_vertices;
	}
}
