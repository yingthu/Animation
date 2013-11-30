package cs4620.scene;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;
import java.util.Vector;

import javax.media.opengl.GL2;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import cs4620.material.Material;
import cs4620.material.PhongMaterial;
import cs4620.shape.Mesh;
import cs4620.shape.Sphere;

abstract class KeyframeableHandler
{
	abstract void handle(Keyframeable obj, int frame);
}

public class SceneKeyframeable extends Scene implements Keyframeable
{
	
	protected TreeSet<Integer> keyframes = new TreeSet<Integer>();
	
	public SceneKeyframeable(GL2 gl)
	{
		SceneNode root = new SceneNodeKeyframeable("Root", getFrameNumbers());

		MeshNode sphereNode = new MeshNodeKeyframeable("Sphere", new Sphere(gl), new PhongMaterial());
		root.add(sphereNode);

		LightNode lightNode = new LightNodeKeyframeable("Light", getFrameNumbers());
		lightNode.setIntensity(1.0f, 1.0f, 1.0f);
		lightNode.setTranslation(5, 5, 5);
		lightNode.setAmbient(0.2f, 0.2f, 0.2f);
		root.add(lightNode);

		treeModel = new DefaultTreeModel(root);
		addAsKeyframe(0);
	}

	@Override
	public int[] getFrameNumbers() {
		if(keyframes == null)
			return new int[0];
		
		int numFrames = keyframes.size();
		int [] frameNumbers = new int[numFrames];
		
		Iterator<Integer> iter = keyframes.iterator();
		for(int i = 0; i < numFrames; i++)
		{
			frameNumbers[i] = iter.next();
		}
		
		return frameNumbers;
	}
	
	@Override
	public void addAsKeyframe(int frame)
	{
		keyframes.add(new Integer(frame));
		// anonymous classes FTW!
		handleAllNodes(new KeyframeableHandler () {
			void handle(Keyframeable obj, int frame)
			{
				obj.addAsKeyframe(frame);
			}
		}, frame);

	}
	
	@Override
	public void applyToAllKeyframes()
	{
		handleAllNodes(new KeyframeableHandler () {
			void handle(Keyframeable obj, int frame)
			{
				obj.applyToAllKeyframes();
			}
		}, 0);
	}

	@Override
	public void deleteKeyframe(int frame)
	{
		keyframes.remove(new Integer(frame));
		
		handleAllNodes(new KeyframeableHandler () {
			void handle(Keyframeable obj, int frame)
			{
				obj.deleteKeyframe(frame);
			}
		}, frame);

	}

	@Override
	public void linearInterpolateTo(int frame)
	{
		handleAllNodes(new KeyframeableHandler () {
			void handle(Keyframeable obj, int frame)
			{
				obj.linearInterpolateTo(frame);
			}
		}, frame);
	}

	@Override
	public void catmullRomInterpolateTo(int frame)
	{
		handleAllNodes(new KeyframeableHandler () {
			void handle(Keyframeable obj, int frame)
			{
				obj.catmullRomInterpolateTo(frame);
			}
		}, frame);
	}
	
	// Visit each node in the scene and let handler do something to it
	public void handleAllNodes(KeyframeableHandler handler, int frame)
	{
		handleAllNodesHelper(handler, frame, getSceneRoot()); 
	}
	
	public void handleAllNodesHelper(KeyframeableHandler handler, int frame, SceneNode node)
	{
		handler.handle((Keyframeable) node, frame);
		for (int i = 0; i < node.getChildCount(); i++)
			handleAllNodesHelper(handler, frame, node.getSceneNodeChild(i));
	}
	
	// add a keyframe entry to the map without doing any traversal
	public void addKeyframeShallow(int frame)
	{
		keyframes.add(new Integer(frame));
	}
	
	/**
	 * Returns a YAML map of this scene.
	 */
	public Object getYamlRepresentation() 
	{
		Map<Object, Object> yamlMap = new HashMap<Object, Object>();
		Map<Object, Object> framesMap = new HashMap<Object, Object>();
		int [] frameNumbers = getFrameNumbers();
		for(int f : frameNumbers)
			framesMap.put(new Integer(f), new Integer(f));
		yamlMap.put("root", ((SceneNode)treeModel.getRoot()).getYamlObjectRepresentation());
		yamlMap.put("frames", framesMap);
		return yamlMap;
	}
	
	public static SceneKeyframeable fromYamlObject(GL2 gl, Object yamlObject)
	{
		if (!(yamlObject instanceof Map))
			throw new RuntimeException("yamlObject not a Map");		
		Map yamlMap = (Map)yamlObject;
		
		SceneKeyframeable scene = new SceneKeyframeable(gl);
		Map<Object, Object> framesMap = (Map) yamlMap.get("frames");
		for (Entry<Object, Object> entry: framesMap.entrySet())
		{
			int frameIndex = Integer.parseInt(entry.getKey().toString());
			scene.addKeyframeShallow(frameIndex);
		}
		
		scene.treeModel.setRoot(SceneNodeKeyframeable.fromYamlObject(gl, yamlMap.get("root")));
		return scene;
	}
	
	/**
	 * Save the current scene to a file.
	 */
	@Override
	public void save(String filename) throws IOException
	{
		// NOP
	}

	//@Override
	public void load(String filename) throws java.io.IOException
	{
		// NOP
	}
	
	/**
	 * Add a new shape to the tree
	 */
	@Override
	public void addNewShape(TreePath path, Mesh mesh, String name)
	{
		//Get the node to insert into
		SceneNode selected = getSceneRoot();
		if (path != null && path.getLastPathComponent() instanceof SceneNode)
			selected = (SceneNode) path.getLastPathComponent();

		MeshNode node = new MeshNodeKeyframeable(name, mesh, getFrameNumbers());
		treeModel.insertNodeInto(node, selected, selected.getChildCount());
	}
	
	public void addNewShape(TreePath path, Mesh mesh, String name, Material mat)
	{
		//Get the node to insert into
		SceneNode selected = getSceneRoot();
		if (path != null &&  path.getLastPathComponent() instanceof SceneNode)
			selected = (SceneNode) path.getLastPathComponent();

		MeshNode node = new MeshNodeKeyframeable(name, mesh, mat, getFrameNumbers());
		treeModel.insertNodeInto(node, selected, selected.getChildCount());
	}
	
	/**
	 * Add a new light to the tree at the end of the given path.
	 */
	@Override
	public void addNewLight(TreePath path)
	{
		//Create a light
		LightNodeKeyframeable node = new LightNodeKeyframeable("Light", getFrameNumbers());

		//Get the node to insert into
		SceneNode selected = (SceneNode)treeModel.getRoot();
		if (path != null && path.getLastPathComponent() instanceof SceneNode)
			selected = (SceneNode) path.getLastPathComponent();

		treeModel.insertNodeInto(node, selected, selected.getChildCount());
	}
	
	/**
	 * Groups a set of selected nodes into a new parent. Return
	 * the new parent node.
	 * @param nodes
	 * @param groupName
	 */
	@Override
	public SceneNode groupNodes(SceneNode[] nodes, String groupName)
	{
		Vector<SceneNode> filtered = filterChildren(nodes);

		if (filtered.size() == 0) return null;

		//Form the new group and add it to the tree
		SceneNode groupNode = new SceneNodeKeyframeable(groupName, getFrameNumbers());
		SceneNode firstSelected = (SceneNode)filtered.get(0);
		SceneNode groupParent = (SceneNode)firstSelected.getParent();
		if (groupParent == null) return null;
		int groupIdx = groupParent.getIndex(firstSelected);

		treeModel.insertNodeInto(groupNode, groupParent, groupIdx);
		for (int i = 0; i < filtered.size(); i++)
		{
			SceneNode node = (SceneNode) filtered.get(i);
			treeModel.removeNodeFromParent(node);
			treeModel.insertNodeInto(node, groupNode, groupNode.getChildCount());
		}
		treeModel.reload();

		return groupNode;
	}

}
