package cs4620.scene;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import javax.media.opengl.GL2;

public class LightNodeKeyframeable extends LightNode implements Keyframeable {
	private static final long serialVersionUID = 7302614078550499605L;
	
	protected TreeMap<Integer, LightNode> keyframes = new TreeMap<Integer, LightNode>();
	
	public LightNodeKeyframeable()
	{
		super();
		keyframes.put(0, new LightNode());
	}

	public LightNodeKeyframeable(String name)
	{
		super(name);
		keyframes.put(0, new LightNode(name));
	}
	
	public LightNodeKeyframeable(String name, int [] frames)
	{
		super(name);
		for(int f : frames)
		{
			keyframes.put(f, new LightNode(name));
		}
	}
	
	public void setToLightNode(LightNode node)
	{
		setIntensity(node.intensity[0], node.intensity[1], node.intensity[2]);
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
		LightNode keyframeNode = new LightNode(getName());
		
		keyframeNode.setIntensity(intensity[0], intensity[1], intensity[2]);
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
		// TODO (Animation P1): Set the state of this light to the specified frame by
		// linearly interpolating the states of the appropriate keyframes.
		Integer k1 = keyframes.floorKey(frame);
        Integer k2 = keyframes.ceilingKey(frame);
        if (k1 == null || k2 == null)
        {
                if (k1 == null) k1 = 0;
                if (k2 == null) k2 = k1;
        }
        LightNode f1 = keyframes.get(k1);
        LightNode f2 = keyframes.get(k2);
        float t = 0;
        if (k1.intValue() != k2.intValue())
                t = (frame - k1.floatValue()) / (k2.floatValue() - k1.floatValue());
        translation.setX((1 - t) * f1.translation.x + t * f2.translation.x);        
        translation.setY((1 - t) * f1.translation.y + t * f2.translation.y);
        translation.setZ((1 - t) * f1.translation.z + t * f2.translation.z);
        for (int i = 0; i < 3; i++)
        {
            intensity[i] = (1 - t) * f1.intensity[i] + t * f2.intensity[i];
        }
	}

	@Override
	public void catmullRomInterpolateTo(int frame) {
		// TODO (Animation P1): Set the state of this light to the specified frame by 
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
        LightNode f0 = keyframes.get(k0);
        LightNode f1 = keyframes.get(k1);
        LightNode f2 = keyframes.get(k2);
        LightNode f3 = keyframes.get(k3);
        float t = 0;
        if (k1.intValue() != k2.intValue())
                t = (frame - k1.floatValue()) / (k2.floatValue() - k1.floatValue());
        KeyframeAnimation.catmullRomInterpolation(f0.translation, f1.translation, f2.translation, f3.translation, t, translation);
        KeyframeAnimation.catmullRomInterpolation(f0.scaling, f1.scaling, f2.scaling, f3.scaling, t, scaling);
        KeyframeAnimation.catmullRomInterpolate4Float(f0.intensity, f1.intensity, f2.intensity, f3.intensity, t, intensity);
	}
	
	@Override
	public void setName(String name) {
		this.name = name;
		if(keyframes != null)
		{
			for(Entry<Integer, LightNode> entry : keyframes.entrySet())
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
		result.put("type", "LightNodeKeyframeable");
		
		Map<Object, Object> framesMap = new HashMap<Object, Object>();
		for (Entry<Integer, LightNode> entry: keyframes.entrySet())
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
			LightNode node = (LightNode) LightNode.fromYamlObject(gl, entry.getValue());
			keyframes.put(frameIndex, node);
		}
	}
	
	public static SceneNode fromYamlObject(GL2 gl, Object yamlObject)
	{
		if (!(yamlObject instanceof Map))
			throw new RuntimeException("yamlObject not a Map");
		Map<?,?> yamlMap = (Map<?,?>)yamlObject;

		LightNodeKeyframeable result = new LightNodeKeyframeable((String)yamlMap.get("name"));
		result.setName((String)yamlMap.get("name"));
		result.addChildrenFromYamlObject(gl, yamlObject);
		result.extractTransformationFromYamlObject(yamlObject);
		result.extractLightFromYamlObject(yamlObject);
		result.extractFramesFromYamlObject(gl, yamlObject);

		return result;
	}

}
