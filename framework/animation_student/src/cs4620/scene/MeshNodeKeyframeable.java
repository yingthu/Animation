package cs4620.scene;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import javax.media.opengl.GL2;
import javax.vecmath.Quat4f;

import cs4620.material.Material;
import cs4620.shape.Mesh;

public class MeshNodeKeyframeable extends MeshNode implements Keyframeable {
	private static final long serialVersionUID = -2756185791348683068L;
	
	protected TreeMap<Integer, MeshNode> keyframes = new TreeMap<Integer, MeshNode>();
	
	public MeshNodeKeyframeable()
	{
		super();
		keyframes.put(0, new MeshNode());
	}
	
	public MeshNodeKeyframeable(GL2 gl, String name)
	{
		super(gl, name);
		keyframes.put(0, new MeshNode(gl, name));
	}
	
	public MeshNodeKeyframeable(String name, Mesh mesh)
	{
		super(name, mesh);
		keyframes.put(0, new MeshNode(name, mesh));
	}
	
	public MeshNodeKeyframeable(String name, Mesh mesh, Material material)
	{
		super(name, mesh, material);
		keyframes.put(0, new MeshNode(name, mesh, material));
	}
	
	public MeshNodeKeyframeable(String name, Mesh mesh, int [] frames)
	{
		super(name, mesh);
		for(int f : frames)
		{
			keyframes.put(f, new MeshNode(name, mesh));
		}
	}
	
	public MeshNodeKeyframeable(GL2 gl, String name, int[] frames)
	{
		super(gl, name);
		for(int f : frames)
		{
			keyframes.put(f, new MeshNode(gl, name));
		}
	}
	
	public MeshNodeKeyframeable(String name, Mesh mesh, Material mat,
			int[] frames) {
		super(name, mesh, mat);
		for(int f : frames)
		{
			keyframes.put(f, new MeshNode(name, mesh, mat));
		}
	}

	public void setToMeshNode(MeshNode node)
	{
		rotation.set(node.rotation);
		scaling.set(node.scaling);
		translation.set(node.translation);
	}

	@Override
	public int[] getFrameNumbers()
	{
		Integer [] integerArray = keyframes.keySet().toArray(new Integer[0]);
		int [] outArray = new int[integerArray.length];
		for(int i = 0; i < outArray.length; i++)
			outArray[i] = integerArray[i];
		return outArray;
	}

	@Override
	public void addAsKeyframe(int frame)
	{
		MeshNode keyframeNode = new MeshNode(getName(), getMesh(), getMaterial());
		keyframeNode.rotation.set(rotation);
		keyframeNode.scaling.set(scaling);
		keyframeNode.translation.set(translation);
		
		keyframes.put(frame, keyframeNode);
	}
	
	@Override
	public void applyToAllKeyframes()
	{
		int [] keyframeNumbers = getFrameNumbers();
		
		for(int f : keyframeNumbers)
		{
			addAsKeyframe(f);
		}
	}

	@Override
	public void deleteKeyframe(int frame)
	{
		keyframes.remove(new Integer(frame));

	}

	@Override
	public void linearInterpolateTo(int frame)
	{
		// TODO (Animation P1): Set the state of this node to the specified frame by
		// linearly interpolating the states of the appropriate keyframes.
		 Integer k1 = keyframes.floorKey(frame);
         Integer k2 = keyframes.ceilingKey(frame);
         if (k1 == null || k2 == null)
         {
                 if (k1 == null) k1 = 0;
                 if (k2 == null) k2 = k1;
         }
         MeshNode f1 = keyframes.get(k1);
         MeshNode f2 = keyframes.get(k2);
         float t = 0;
         if (k1.intValue() != k2.intValue())
                 t = (frame - k1.floatValue()) / (k2.floatValue() - k1.floatValue());
         translation.interpolate(f1.translation, f2.translation, t);
         Quat4f q1 = KeyframeAnimation.getQuaternionFromEulerAngles(f1.rotation);
         Quat4f q2 = KeyframeAnimation.getQuaternionFromEulerAngles(f2.rotation);
         Quat4f result = KeyframeAnimation.slerp(q1, q2, t);
         rotation.set(KeyframeAnimation.getEulerAnglesFromQuaternion(result));
         scaling.interpolate(f1.scaling, f2.scaling, t);
	}

	@Override
	public void catmullRomInterpolateTo(int frame)
	{
		// TODO (Animation P1): Set the state of this node to the specified frame by 
		// interpolating the states of the appropriate keyframes using Catmull-Rom splines.
		Integer k1 = keyframes.floorKey(frame);
        Integer k2 = keyframes.ceilingKey(frame);
        if (k1 == null || k2 == null)
        {
                if (k1 == null) k1 = 0;
                if (k2 == null) k2 = k1;
        }
        Integer k0 = keyframes.lowerKey(k1);
        Integer k3 = keyframes.higherKey(k2);
        if (k0 == null || k3 == null)
        {
                if (k0 == null) k0 = k1;
                if (k3 == null) k3 = k2;
        }
        MeshNode f0 = keyframes.get(k0);
        MeshNode f1 = keyframes.get(k1);
        MeshNode f2 = keyframes.get(k2);
        MeshNode f3 = keyframes.get(k3);
        float t = 0;
        if (k1.intValue() != k2.intValue())
                t = (frame - k1.floatValue()) / (k2.floatValue() - k1.floatValue());
        KeyframeAnimation.catmullRomInterpolation(f0.translation, f1.translation, f2.translation, 
        		                                  f3.translation, t, translation);
        Quat4f q1 = KeyframeAnimation.getQuaternionFromEulerAngles(f1.rotation);
        Quat4f q2 = KeyframeAnimation.getQuaternionFromEulerAngles(f2.rotation);
        Quat4f result = KeyframeAnimation.slerp(q1, q2, t);
        rotation.set(KeyframeAnimation.getEulerAnglesFromQuaternion(result));
        KeyframeAnimation.catmullRomInterpolation(f0.scaling, f1.scaling, f2.scaling,
                                                  f3.scaling, t, scaling);
	}
	
	@Override
	public void setName(String name) {
		this.name = name;
		if(keyframes != null)
		{
			for(Entry<Integer, MeshNode> entry : keyframes.entrySet())
			{
				entry.getValue().setName(name);
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object getYamlObjectRepresentation()
	{
		Map<String, Object> result = (Map<String, Object>)super.getYamlObjectRepresentation();
		result.put("type", "MeshNodeKeyframeable");
		
		Map<Object, Object> framesMap = new HashMap<Object, Object>();
		for (Entry<Integer, MeshNode> entry: keyframes.entrySet())
		{
			framesMap.put(entry.getKey(), entry.getValue().getYamlObjectRepresentation());
		}
		result.put("frames", framesMap);
		
		return result;
	}
	
	public void extractFramesFromYamlObject(GL2 gl, Object yamlObject)
	{
		if (!(yamlObject instanceof Map))
			throw new RuntimeException("yamlObject not a Map");
		Map<?,?> yamlMap = (Map<?,?>)yamlObject;
		
		Map<Object, Object> framesMap = (Map) yamlMap.get("frames");
		
		for (Entry<Object, Object> entry: framesMap.entrySet())
		{
			int frameIndex = Integer.parseInt(entry.getKey().toString());
			MeshNode node = (MeshNode) MeshNode.fromYamlObject(gl, entry.getValue());
			keyframes.put(frameIndex, node);
		}
	}
	
	public static SceneNode fromYamlObject(GL2 gl, Object yamlObject)
	{
		if (!(yamlObject instanceof Map))
			throw new RuntimeException("yamlObject not a Map");
		Map<?,?> yamlMap = (Map<?,?>)yamlObject;

		MeshNodeKeyframeable result = new MeshNodeKeyframeable(gl, (String)yamlMap.get("name"));
		result.extractTransformationFromYamlObject(yamlObject);
		result.addChildrenFromYamlObject(gl, yamlObject);
		result.extractMeshFromYamlObject(gl, yamlObject);
		result.extractMaterialFromYamlObject(yamlObject);
		result.extractFramesFromYamlObject(gl, yamlObject);

		return result;
	}
	
}
