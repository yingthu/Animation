package cs4620.scene;

import java.util.Arrays;
import java.util.Map;

import javax.media.opengl.GL2;
import javax.vecmath.Vector3f;

import cs4620.material.Material;
import cs4620.material.PhongMaterial;
import cs4620.shape.Spline;
import cs4620.spline.BSpline;

/*
 * A node responsible for a spline. This class computes the splineOffset
 * based on the time, which is used for animation.
 */
public class SplineNode extends MeshNode {
	private static final long serialVersionUID = 1L;

	public final Vector3f splineOffset = new Vector3f();

	private float speed = 1.0f;

	SplineNode() {
		super("Spline", new Spline(), new PhongMaterial());
	}
	
	SplineNode(String name, Spline spline) {
		super(name, spline);
	}
	
	SplineNode(String name, Spline spline, Material mat) {
		super(name, spline, mat);
	}

	public float getSpeed() {
		return speed;
	}

	public void setSpeed(float speed) {
		this.speed = speed;
	}

	// A two-way wrap around an arbitrary set of indices.
	private static int wrapIndex(int i, int min, int max) {
		if (i < min)
			return max - (min - i) + 1;
		if (i > max)
			return min + (i - max) - 1;
		else
			return i;
	}

	public void setTime(float t) {
		// TODO: PPA2 Problem 3, Section 5.2:
		// Accounting for speed, do a binary search for time over the 'normalized'
		// lengths array and set the splineOffset to the closest position in time.
		BSpline b = ((Spline) getMesh()).getBspline();
		int bLength = b.getLengthBuffer().length;
		if(t == 0) {
			splineOffset.set(b.vertices[0], b.vertices[1], 0);
			return;
		}
		//We do not make the simplifying assumption that when t = 1, 
		//we can just use the endpoints of the spline
		//because if t is not an integer, this will not be true.
		
		float position = (t*speed) % b.getTotalLength();
		float[] l = b.getLengthBuffer();
		
		int i = Arrays.binarySearch(l, position);
		if(i >= 0) {
			i = i % bLength;
			splineOffset.set(b.vertices[2*i], b.vertices[2*i+1], 0);
		} else {
			//System.out.println("i = " + i);
			i = (-i + 1) % bLength;
			//System.out.println("i = " + i + "\n");
			splineOffset.set(b.vertices[2*i], b.vertices[2*i+1], 0);
		}
		//System.out.println("Number is "+i+" Total Number is: "+b.vertices.length/2);
	}

	public static SceneNode fromYamlObject(GL2 gl, Object yamlObject)
	{
		if (!(yamlObject instanceof Map))
			throw new RuntimeException("yamlObject not a Map");
		Map<?, ?> yamlMap = (Map<?, ?>)yamlObject;

		SplineNode result = new SplineNode();
		result.setName((String)yamlMap.get("name"));
		result.setSpeed(Float.valueOf((String)yamlMap.get("speed")));
		result.extractTransformationFromYamlObject(yamlObject);
		result.addChildrenFromYamlObject(gl, yamlObject);
		result.extractMeshFromYamlObject(gl, yamlObject);
		result.extractMaterialFromYamlObject(yamlObject);

		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object getYamlObjectRepresentation()
	{
		Map<String, Object> result = (Map<String, Object>)super.getYamlObjectRepresentation();
		result.put("type", "SplineNode");
		result.put("speed", Float.toString(speed));
		return result;
	}
}
