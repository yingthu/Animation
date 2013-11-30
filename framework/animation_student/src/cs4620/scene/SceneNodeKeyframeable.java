package cs4620.scene;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import javax.media.opengl.GL2;
import javax.vecmath.Quat4f;


public class SceneNodeKeyframeable extends SceneNode
		implements Keyframeable {
	private static final long serialVersionUID = 4345300320997053825L;
	
	protected TreeMap<Integer, SceneNode> keyframes = new TreeMap<Integer, SceneNode>();
	
	public SceneNodeKeyframeable()
	{
		super();
		keyframes.put(0, new SceneNode());
		
	}

	public SceneNodeKeyframeable(String name)
	{
		super(name);
		resetTransformation();
		keyframes.put(0, new SceneNode(name));
	}
	
	public SceneNodeKeyframeable(String name, int [] frames)
	{
		super(name);
		for(int f : frames)
		{
			keyframes.put(f, new SceneNode(name));
		}
	}
	
	public void setToTransformation(SceneNode node)
	{
		rotation.set(node.rotation);
		scaling.set(node.scaling);
		translation.set(node.translation);
	}

	@Override
	public int[] getFrameNumbers() {
		Integer [] integerArray = keyframes.keySet().toArray(new Integer[0]);
		int [] outArray = new int[integerArray.length];
		for(int i = 0; i < outArray.length; i++)
			outArray[i] = integerArray[i];
		return outArray;
	}

	@Override
	public void addAsKeyframe(int frame) {
		SceneNode keyframeNode = new SceneNode(getName());
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
	public void deleteKeyframe(int frame) {
		keyframes.remove(new Integer(frame));
	}

	@Override
	public void linearInterpolateTo(int frame) {
		// TODO (Animation P1): Set the state of this node to the specified frame by
		// linearly interpolating the states of the appropriate keyframes.
		Integer k1 = keyframes.floorKey(frame);
        Integer k2 = keyframes.ceilingKey(frame);
        if (k1 == null || k2 == null)
        {
                if (k1 == null) k1 = 0;
                if (k2 == null) k2 = k1;
        }
        SceneNode f1 = keyframes.get(k1);
        SceneNode f2 = keyframes.get(k2);
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
	public void catmullRomInterpolateTo(int frame) {
		// TODO (Animation P1): Set the state of this node to the specified frame by 
		// interpolating the states of the appropriate keyframes using Catmull-Rom splines.
		Integer k1 = keyframes.floorKey(frame);
        Integer k2 = keyframes.higherKey(frame);
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
        SceneNode f0 = keyframes.get(k0);
        SceneNode f1 = keyframes.get(k1);
        SceneNode f2 = keyframes.get(k2);
        SceneNode f3 = keyframes.get(k3);
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
			for(Entry<Integer, SceneNode> entry : keyframes.entrySet())
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
		result.put("type", "SceneNodeKeyframeable");
		
		Map<Object, Object> framesMap = new HashMap<Object, Object>();
		for (Entry<Integer, SceneNode> entry: keyframes.entrySet())
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
			SceneNode node = (SceneNode) SceneNode.fromYamlObject(gl, entry.getValue());
			keyframes.put(frameIndex, node);
		}
	}
	
	public static SceneNode fromYamlObject(GL2 gl, Object yamlObject)
	{
		if (!(yamlObject instanceof Map))
			throw new RuntimeException("yamlObject not a Map");
		Map<?,?> yamlMap = (Map<?,?>)yamlObject;

		SceneNodeKeyframeable result = new SceneNodeKeyframeable((String)yamlMap.get("name"));
		result.extractFramesFromYamlObject(gl, yamlObject);
		result.extractTransformationFromYamlObject(yamlObject);
		result.addChildrenFromYamlObject(gl, yamlObject);

		return result;
	}
	
}
