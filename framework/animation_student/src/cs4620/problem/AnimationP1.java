package cs4620.problem;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLRunnable;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;

import org.yaml.snakeyaml.Yaml;

import layout.TableLayout;
import cs4620.framework.Camera;
import cs4620.framework.CameraController;
import cs4620.framework.GLPickingDrawer;
import cs4620.framework.GLSceneDrawer;
import cs4620.framework.GlslException;
import cs4620.framework.PickingManager;
import cs4620.framework.PickingProgram;
import cs4620.framework.Program;
import cs4620.framework.VerticalScrollPanel;
import cs4620.manip.Manip;
import cs4620.manip.RotateManip;
import cs4620.manip.ScaleManip;
import cs4620.manip.TranslateManip;
import cs4620.material.PhongMaterial;
import cs4620.scene.LightNode;
import cs4620.scene.MeshNode;
import cs4620.scene.PickingSceneProgram;
import cs4620.scene.ProgramInfo;
import cs4620.scene.Scene;
import cs4620.scene.SceneKeyframeable;
import cs4620.scene.SceneNode;
import cs4620.scene.SceneProgram;
import cs4620.shape.Cube;
import cs4620.shape.Cylinder;
import cs4620.shape.Mesh;
import cs4620.shape.Sphere;
import cs4620.shape.Teapot;
import cs4620.shape.Torus;
import cs4620.ui.AnimationPanel;
import cs4620.ui.BasicAction;
import cs4620.ui.PhongMaterialSettingPanel;
import cs4620.ui.LightSettingPanel;
import cs4620.ui.NameSettingPanel;
import cs4620.ui.OneFourViewPanel;
import cs4620.ui.PopupListener;
import cs4620.ui.SceneDisplayer;
import cs4620.ui.SceneViewPanel;
import cs4620.ui.SliderPanel;
import cs4620.ui.ToleranceSliderPanel;
import cs4620.ui.TransformSettingPanel;
import cs4620.ui.TreeRenderer;

/**
 * Main window of Problem 3.
 *
 * This application can:
 * 1) Load, display, and save scene graph.
 * 2) Let the user manipulate scene graph by elements by entering text.
 *
 * @author pramook, arbree
 */
public class AnimationP1 extends JFrame implements GLPickingDrawer,
	ChangeListener, ActionListener, TreeSelectionListener, SceneDisplayer

{
	private static final long serialVersionUID = 1L;

	//Menu commands
	private static final String SAVE_AS_MENU_TEXT = "Save As";
	private static final String OPEN_MENU_TEXT = "Open";
	private static final String EXIT_MENU_TEXT = "Exit";
	private static final String CLEAR_SELECTED_TEXT = "Clear selection";
	private static final String GROUP_MENU_TEXT = "Group selected";
	private static final String REPARENT_MENU_TEXT = "Reparent selected";
	private static final String DELETE_MENU_TEXT = "Delete selected";
	private static final String PICK_MENU_TEXT = "Select";
	private static final String ROTATE_MENU_TEXT = "Rotate selected";
	private static final String TRANSLATE_MENU_TEXT = "Translate selected";
	private static final String SCALE_MENU_TEXT = "Scale selected";
	private static final String ADD_LIGHT_MENU_TEXT = "Add Light";
	private static final String ADD_SPHERE_MENU_TEXT = "Add Sphere";
	private static final String ADD_CUBE_MENU_TEXT = "Add Cube";
	private static final String ADD_CYLINDER_MENU_TEXT = "Add Cylinder";
	private static final String ADD_TORUS_MENU_TEXT = "Add Torus";
	private static final String ADD_TEAPOT_MENU_TEXT = "Add Teapot";
	private static final String REMOVE_KEYFRAME_MENU_TEXT = "Remove Keyframe";

	JSplitPane mainSplitPane;
	JSplitPane scenePane;
	JSplitPane leftSplitPane;
	AnimationPanel animationPanel;
	SceneViewPanel sceneViewPanel;
	ToleranceSliderPanel sliderPanel;
	JFileChooser fileChooser;
	JTree treeView;
	PhongMaterialSettingPanel phongMaterialPanel;
	TransformSettingPanel transformSettingPanel;
	LightSettingPanel lightSettingPanel;
	NameSettingPanel nameSettingPanel;
	JPanel nodeSettingPanel;

	TranslateManip translateManip;
	RotateManip rotateManip;
	ScaleManip scaleManip;
	Manip currentManip;
	boolean showManip;
	boolean isManipulatingManip;

	SceneKeyframeable scene;

	// GL resources
	SceneProgram flatColorProgram;
	PickingSceneProgram pickingProgram;
	
	boolean sliderChanged = true;
	
	boolean drawForPicking = false;
	SceneNode[] nodesToReparent = null;
	boolean isReparenting = false;

	protected boolean initialized = false;
	boolean shadersInitialized = false;
	
	public AnimationP1() {
		super("CS 4621 Animation Assignment / Problem 1");
		//setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		addWindowListener( new WindowAdapter() {
			public void windowClosing( WindowEvent windowevent ) {
				terminate();
			}
		});

		initMainSplitPane();
		getContentPane().add(mainSplitPane, BorderLayout.CENTER);
		sliderPanel = new ToleranceSliderPanel(this);
		getContentPane().add(sliderPanel, BorderLayout.EAST);

		initActionsAndMenus();

		initManip();

		fileChooser = new JFileChooser(new File("data"));
	}

	public static void main(String[] args)
	{
		new AnimationP1().run();
	}

	public void run()
	{
		setSize(950, 600);
		setLocationRelativeTo(null);
		setVisible(true);
		sceneViewPanel.startAnimation();
	}

	/**
	 * Maps all GUI actions to listeners in this object and builds the menu
	 */
	protected void initActionsAndMenus()
	{
		//Create all the actions
		BasicAction group = new BasicAction(GROUP_MENU_TEXT, this);
		BasicAction reparent = new BasicAction(REPARENT_MENU_TEXT, this);
		BasicAction delete = new BasicAction(DELETE_MENU_TEXT, this);
		BasicAction clear = new BasicAction(CLEAR_SELECTED_TEXT, this);

		BasicAction addLight = new BasicAction(ADD_LIGHT_MENU_TEXT, this);
		BasicAction addSphere = new BasicAction(ADD_SPHERE_MENU_TEXT, this);
		BasicAction addCube = new BasicAction(ADD_CUBE_MENU_TEXT, this);
		BasicAction addCylinder = new BasicAction(ADD_CYLINDER_MENU_TEXT, this);
		BasicAction addTorus = new BasicAction(ADD_TORUS_MENU_TEXT, this);
		BasicAction addTeapot = new BasicAction(ADD_TEAPOT_MENU_TEXT, this);

		BasicAction saveAs = new BasicAction(SAVE_AS_MENU_TEXT, this);
		BasicAction open = new BasicAction(OPEN_MENU_TEXT, this);
		BasicAction exit = new BasicAction(EXIT_MENU_TEXT, this);

		BasicAction pickTool = new BasicAction(PICK_MENU_TEXT, this);
		BasicAction rotateTool = new BasicAction(ROTATE_MENU_TEXT, this);
		BasicAction translateTool = new BasicAction(TRANSLATE_MENU_TEXT, this);
		BasicAction scaleTool = new BasicAction(SCALE_MENU_TEXT, this);
		
		BasicAction removeKeyframe = new BasicAction(REMOVE_KEYFRAME_MENU_TEXT, this);

		//Set shortcut keys
		group.setAcceleratorKey(KeyEvent.VK_G, KeyEvent.CTRL_MASK);
		reparent.setAcceleratorKey(KeyEvent.VK_R, KeyEvent.CTRL_MASK);
		delete.setAcceleratorKey(KeyEvent.VK_DELETE, 0);

		pickTool.setAcceleratorKey(KeyEvent.VK_Q, KeyEvent.CTRL_MASK | KeyEvent.SHIFT_DOWN_MASK);
		translateTool.setAcceleratorKey(KeyEvent.VK_W, KeyEvent.CTRL_MASK | KeyEvent.SHIFT_DOWN_MASK);
		rotateTool.setAcceleratorKey(KeyEvent.VK_E, KeyEvent.CTRL_MASK | KeyEvent.SHIFT_DOWN_MASK);
		scaleTool.setAcceleratorKey(KeyEvent.VK_R, KeyEvent.CTRL_MASK | KeyEvent.SHIFT_DOWN_MASK);

		saveAs.setAcceleratorKey(KeyEvent.VK_A, KeyEvent.CTRL_MASK);
		open.setAcceleratorKey(KeyEvent.VK_O, KeyEvent.CTRL_MASK);
		exit.setAcceleratorKey(KeyEvent.VK_Q, KeyEvent.CTRL_MASK);

		//Create the menu
		JMenuBar bar = new JMenuBar();
		JMenu menu;

		menu = new JMenu("File");
		menu.setMnemonic('F');
		menu.add(new JMenuItem(open));
		menu.add(new JMenuItem(saveAs));
		menu.addSeparator();
		menu.add(new JMenuItem(exit));
		bar.add(menu);

		menu = new JMenu("Edit");
		menu.setMnemonic('E');
		menu.add(new JMenuItem(group));
		menu.add(new JMenuItem(reparent));
		menu.add(new JMenuItem(delete));
		menu.add(new JSeparator());
		menu.add(new JMenuItem(pickTool));
		menu.add(new JMenuItem(translateTool));
		menu.add(new JMenuItem(rotateTool));
		menu.add(new JMenuItem(scaleTool));
		bar.add(menu);

		menu = new JMenu("Scene");
		menu.setMnemonic('S');
		menu.add(new JMenuItem(addLight));
		menu.add(new JMenuItem(addSphere));
		menu.add(new JMenuItem(addCube));
		menu.add(new JMenuItem(addCylinder));
		menu.add(new JMenuItem(addTorus));
		menu.add(new JMenuItem(addTeapot));
		bar.add(menu);

		setJMenuBar(bar);

		JPopupMenu p = new JPopupMenu();
		p.add(new JMenuItem(group));
		p.add(new JMenuItem(reparent));
		p.add(new JMenuItem(delete));
		p.add(new JMenuItem(clear));
		p.addSeparator();
		p.add(new JMenuItem(addLight));
		p.add(new JMenuItem(addSphere));
		p.add(new JMenuItem(addCube));
		p.add(new JMenuItem(addCylinder));
		p.add(new JMenuItem(addTorus));
		p.add(new JMenuItem(addTeapot));

		treeView.addMouseListener(new PopupListener(p));
		treeView.addTreeSelectionListener(this);
		
		JPopupMenu animationPopup = new JPopupMenu();
		animationPopup.add(new JMenuItem(removeKeyframe));
		animationPanel.addMouseListener(new PopupListener(animationPopup));
	}

	private void initMainSplitPane()
	{
		mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false);
		initLeftSplitPane();
		mainSplitPane.setLeftComponent(leftSplitPane);

		sceneViewPanel = new SceneViewPanel(new PickingManager(this));
		mainSplitPane.setRightComponent(sceneViewPanel);

		mainSplitPane.resetToPreferredSizes();
		mainSplitPane.setOneTouchExpandable(true);
		mainSplitPane.setResizeWeight(0.2);
	}

	private void initLeftSplitPane()
	{
		leftSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false);
		
		scenePane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, false);
		initTreeView();
		VerticalScrollPanel verticalScrollPanel = new VerticalScrollPanel(new BorderLayout());
		verticalScrollPanel.add(treeView, BorderLayout.CENTER);
		scenePane.setTopComponent(new JScrollPane(verticalScrollPanel));
		
		animationPanel = new AnimationPanel(this);

		nodeSettingPanel = new JPanel();
		nodeSettingPanel.setLayout(new TableLayout(new double[][] {
			{
				TableLayout.FILL
			},
			{
				TableLayout.MINIMUM,
				TableLayout.MINIMUM,
				//TableLayout.MINIMUM,
				TableLayout.MINIMUM
			}
		}));
		scenePane.setBottomComponent(nodeSettingPanel);
		
		nameSettingPanel = new NameSettingPanel();
		//nodeSettingPanel.add(nameSettingPanel, "0,0,0,0");
		//nameSettingPanel.setVisible(false);

		transformSettingPanel = new TransformSettingPanel();
		//nodeSettingPanel.add(transformSettingPanel, "0,1,0,1");
		//transformSettingPanel.setVisible(false);

		phongMaterialPanel = new PhongMaterialSettingPanel();
		//nodeSettingPanel.add(phongMaterialPanel, "0,2,0,2");
		//phongMaterialPanel.setVisible(false);

		lightSettingPanel = new LightSettingPanel();
		//nodeSettingPanel.add(lightSettingPanel, "0,3,0,3");
		//lightSettingPanel.setVisible(false);
		
		//applyToAllButton = new JButton("Apply to all keyframes");
		//nodeSettingPanel.add(applyToAllButton, "0,4,0,4");
		//applyToAllButton.setVisible(false);
		//applyToAllButton.addActionListener(this);

		scenePane.resetToPreferredSizes();
		scenePane.setOneTouchExpandable(true);
		scenePane.setResizeWeight(0.95);
		int minHeight = scenePane.getMinimumSize().height;
		scenePane.setMinimumSize(new Dimension(300, minHeight));
		
		leftSplitPane.setLeftComponent(animationPanel);
		leftSplitPane.setRightComponent(scenePane);
	}

	private void initTreeView()
	{
		// Create the tree views
		treeView = new JTree();
		
		DefaultTreeCellRenderer renderer = new TreeRenderer();
		treeView.setCellRenderer(renderer);
		treeView.setEditable(true);
		treeView.setCellEditor(new DefaultTreeCellEditor(treeView, renderer));
		treeView.setShowsRootHandles(true);
		treeView.setRootVisible(true);

		KeyListener[] kls = treeView.getKeyListeners();
		for (int i=0; i<kls.length; i++)
			treeView.removeKeyListener(kls[i]);
	}

	private void initManip()
	{
		translateManip = new TranslateManip();
		rotateManip = new RotateManip();
		scaleManip = new ScaleManip();

		translateManip.addChangeListener(this);
		rotateManip.addChangeListener(this);
		scaleManip.addChangeListener(this);

		currentManip = null;
		showManip = false;
		isManipulatingManip = false;
	}

	private float getTolerance()
	{
		return sliderPanel.getTolerance();
	}

	protected void initShaders(GL2 gl) {
		if(shadersInitialized)
			return;
		
		try {
			//diffuseProgram = new SceneProgram(gl, "diffuse.vs", "diffuse.fs");
			flatColorProgram = new SceneProgram(gl, "flatcolor.vs", "flatcolor.fs");
			pickingProgram = new PickingSceneProgram(gl, "picking.vs", "picking.fs");
		} catch (GlslException e) {
			System.err.println("FAIL: making shader programs");
			e.printStackTrace();
			System.exit(1);
		}
		
		shadersInitialized = true;
	}
	
	public void init(GLAutoDrawable drawable, CameraController cameraController) {
		if(initialized)
			return;
		
		final GL2 gl = drawable.getGL().getGL2();

		scene = new SceneKeyframeable(gl);
		treeView.setModel(scene.getTreeModel());
		
		if (animationPanel != null)
			animationPanel.setCurrentScene(scene);
		
		rotateManip.setScene(scene);
		scaleManip.setScene(scene);
		translateManip.setScene(scene);
		
		gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

		// Set depth buffer.
		gl.glClearDepth(1.0f);
		gl.glDepthFunc(GL2.GL_LESS);
		gl.glEnable(GL2.GL_DEPTH_TEST);

		// Set blending mode.
		gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
		gl.glDisable(GL2.GL_BLEND);

		// Forces OpenGL to normalize transformed normals to be of
		// unit length before using the normals in OpenGL's lighting equations.
		gl.glEnable(GL2.GL_NORMALIZE);

		// Cull back faces.
		gl.glEnable(GL2.GL_CULL_FACE);
		gl.glCullFace(GL2.GL_BACK);

		gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL2.GL_NICEST);

		initShaders(gl);
		rebuildMeshes(gl);
		sceneViewPanel.startAnimation();
		
		initialized = true;
	}

	protected void setProjectionAndLighting(GL2 gl, SceneProgram program, CameraController cameraController)
	{
		program.setProjection(gl, cameraController.getProjection());
		
		// give program info about all lights in the scene
		scene.setupLighting(gl, program, cameraController.getView());
		program.setLightAmbientIntensity(gl, lightSettingPanel.getAmbient());
	}
	
	protected ProgramInfo constructProgramInfo(GL2 gl, CameraController cameraController)
	{
		ProgramInfo info = new ProgramInfo();
		
		info.un_Projection = cameraController.getProjection();
		scene.getLightingInfo(gl, info, cameraController.getView());
		info.un_LightAmbientIntensity = lightSettingPanel.getAmbient();
		return info;
	}
	
	
	public void draw(GLAutoDrawable drawable, CameraController cameraController)
	{
		final GL2 gl = drawable.getGL().getGL2();
		rebuildMeshes(gl);

		if (!sceneViewPanel.isLightingMode())
		{
			// Render with flatColorProgram
			Program.use(gl, flatColorProgram);
			
			setProjectionAndLighting(gl, flatColorProgram, cameraController);
			
			if (sceneViewPanel.isWireframeMode())
			{
				scene.renderWireframeWithProgram(gl, flatColorProgram, cameraController.getView());
			}
			else
			{
				scene.renderWithProgram(gl, flatColorProgram, cameraController.getView());
			}
			
			Program.unuse(gl);
		}
		else
		{
			// Let each node use its own material
			// Get ProgramInfo to use
			ProgramInfo info = constructProgramInfo(gl, cameraController);
			
			if (sceneViewPanel.isWireframeMode())
			{
				scene.renderWireframe(gl, info, cameraController.getView());
			}
			else
			{
				scene.render(gl, info, cameraController.getView());
			}
		}
		
		if (showManip && currentManip != null)
		{
			Program.use(gl, flatColorProgram);
			setProjectionAndLighting(gl, flatColorProgram, cameraController);
			
			currentManip.renderConstantSize(gl, flatColorProgram, cameraController.getCamera());
		}
		
		Program.unuse(gl);
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() == sliderPanel.getSlider())
		{
			sliderChanged = true;
		}
		else if (e.getSource() == currentManip)
		{
			SceneNode node = currentManip.getSceneNode();
			transformSettingPanel.setTransformationNode(node);
			transformSettingPanel.repaint();
		}
	}

	protected void rebuildMeshes(GL2 gl)
	{
		if (sliderChanged)
		{
			scene.rebuildMeshes(gl, getTolerance());
			sliderChanged = false;
		}
	}

	public void terminate()
	{
		sceneViewPanel.stopAnimation();
		animationPanel.stopAnimation();
		dispose();
		System.exit(0);
	}

	protected void refresh()
	{
		sceneViewPanel.repaint();
	}

	/**
	 * Save the current tree to a file.
	 */
	protected void saveTreeAs(String filename)
	{
		animationPanel.setCurrentFrame(0);
		
		// Serialize keyframes.
		Yaml yaml = new Yaml();
		Map<Object, Object> yamlMap = (Map) scene.getYamlRepresentation();
		
		String data = yaml.dump(yamlMap);
		
		//Write the tree out
		try 
		{			
			FileWriter fstream = new FileWriter(filename);				
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(data);			
			out.close();
		}
		catch (IOException ioe) 
		{
			showExceptionDialog(ioe);
		}
		
		refresh();
	}

	/**
	 * Displays an exception in a window
	 * @param e
	 */
	protected void showExceptionDialog(Exception e)
	{
		String str = "The following exception was thrown: " + e.toString() + ".\n\n" + "Would you like to see the stack trace?";
		int choice = JOptionPane.showConfirmDialog(this, str, "Exception Thrown", JOptionPane.YES_NO_OPTION);

		if (choice == JOptionPane.YES_OPTION) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Stolen from http://snippets.dzone.com/posts/show/1335
	 */
	private static String readFileAsString(String filePath) throws IOException{
	    byte[] buffer = new byte[(int) new File(filePath).length()];
	    BufferedInputStream f = null;
	    try {
	        f = new BufferedInputStream(new FileInputStream(filePath));
	        f.read(buffer);
	    } finally {
	        if (f != null) try { f.close(); } catch (IOException ignored) { }
	    }
	    return new String(buffer);
	}

	/**
	 * Loads a tree stored in a file
	 */
	protected void openTree(GL2 gl, String filename)
	{
		//Load the tree
		try 
		{
			// load yaml file
			String fileContent = readFileAsString(filename);
			Yaml yaml = new Yaml();
			Object yamlObject = yaml.load(fileContent);
			
			if (!(yamlObject instanceof Map))
				throw new RuntimeException("yamlObject not a Map");		
			Map<Object, Object> yamlMap = (Map)yamlObject;

			// load keyframes into tree			
			animationPanel.removeAllKeyframes();
			
			scene = SceneKeyframeable.fromYamlObject(gl, yamlObject);
			scene.rebuildMeshes(gl, getTolerance());
			
			treeView.setModel(scene.getTreeModel());
			
			animationPanel.setCurrentScene(scene);
			int [] frameIndices = scene.getFrameNumbers();
			for (int f : frameIndices)
			{
				animationPanel.insertKeyframeShallow(f);
			}
			
			// set current frame to first frame
			animationPanel.setCurrentFrame(0);//animationPanel.controller.keyframes.ceilingKey(0));
		}
		catch (Exception e) {
			showExceptionDialog(e);
		}
		
		//Update the window
		refresh();
	}

	/**
	 * Add a new shape to the tree
	 */
	protected void addNewShape(Mesh mesh, String name)
	{
		TreePath path = treeView.getSelectionPath();

		//Add the node
		try
		{
			scene.addNewShape(path, mesh, name, new PhongMaterial());
			sliderChanged = true;
			refresh();
		}
		catch (Exception e) {
			showExceptionDialog(e);
		}
	}

	protected SceneNode[] getSelection()
	{
		TreePath[] paths = treeView.getSelectionPaths();
		if (paths == null) {
			return new SceneNode[] {};
		}
		SceneNode[] ts = new SceneNode[paths.length];
		for (int i = 0; i < paths.length; i++) {
			ts[i] = (SceneNode) paths[i].getLastPathComponent();
		}
		return ts;
	}
	
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		String filename = null;
		if (cmd != null && cmd.equals(SAVE_AS_MENU_TEXT)) {
			// get name of scene to be saved after next draw
			int choice = fileChooser.showSaveDialog(this);
			if (choice != JFileChooser.APPROVE_OPTION)
			{
				refresh();
				return;
			}
			filename = fileChooser.getSelectedFile().getAbsolutePath();
		}
		else if (cmd != null && cmd.equals(OPEN_MENU_TEXT)) {
			// get name of scene to be opened after next draw
			int choice = fileChooser.showOpenDialog(this);
			if (choice != JFileChooser.APPROVE_OPTION)
			{
				refresh();
				return;
			}
			filename = fileChooser.getSelectedFile().getAbsolutePath();
		}
		// queue up this action for right after the next display()
		ActionPerformedAnimationP1Runnable runnable = new ActionPerformedAnimationP1Runnable(this, e, filename);
		sceneViewPanel.invoke(false, runnable);
	}
	
	/**
	 * A deferred version of actionPerformed, called with the appropriate
	 * GL context just after a display() has happened and while the running
	 * thread is guaranteed not to run concurrently with any GL commands
	 */

	public void processAction(GL2 gl, ActionEvent e, String filename) {
		String cmd = e.getActionCommand();
		if (cmd == null) {
			return;
		}
		else if (cmd.equals(REMOVE_KEYFRAME_MENU_TEXT))
		{
			animationPanel.removeSelectedKeyframe();
		}
		else if (cmd.equals(GROUP_MENU_TEXT)) {
			SceneNode groupNode = scene.groupNodes(getSelection(), "Group");
			treeView.expandPath(new TreePath(groupNode.getPath()));
			refresh();
		}
		else if (cmd.equals(CLEAR_SELECTED_TEXT)) {
			treeView.clearSelection();
		}
		else if (cmd.equals(REPARENT_MENU_TEXT)) {
			nodesToReparent = getSelection();
			isReparenting = true;
		}
		else if (cmd.equals(DELETE_MENU_TEXT)) {
			scene.deleteNodes(getSelection());
			refresh();
		}
		else if (cmd.equals(PICK_MENU_TEXT)) {
			currentManip = null;
			refresh();
		}
		else if (cmd.equals(TRANSLATE_MENU_TEXT)) {
			SceneNode ts[] = getSelection();
			showManip = ts.length == 1;
			if (showManip && ts[0] instanceof SceneNode)
			{
				currentManip = translateManip;
				currentManip.setSceneNode(ts[0]);
			}
			refresh();
		}
		else if (cmd.equals(ROTATE_MENU_TEXT)) {
			SceneNode ts[] = getSelection();
			showManip = ts.length == 1;
			if (showManip && ts[0] instanceof SceneNode)
			{
				currentManip = rotateManip;
				currentManip.setSceneNode(ts[0]);
			}
			refresh();
		}
		else if (cmd.equals(SCALE_MENU_TEXT)) {
			SceneNode ts[] = getSelection();
			showManip = ts.length == 1;
			if (showManip && ts[0] instanceof SceneNode)
			{
				currentManip = scaleManip;
				currentManip.setSceneNode(ts[0]);
			}
			refresh();
		}
		else if (cmd.equals(ADD_LIGHT_MENU_TEXT)) {
			scene.addNewLight(treeView.getSelectionPath());
		}
		else if (cmd.equals(ADD_SPHERE_MENU_TEXT)) {
			addNewShape(new Sphere(gl), "Sphere");
		}
		else if (cmd.equals(ADD_CUBE_MENU_TEXT)) {
			addNewShape(new Cube(gl), "Cube");
		}
		else if (cmd.equals(ADD_CYLINDER_MENU_TEXT)) {
			addNewShape(new Cylinder(gl), "Cylinder");
		}
		else if (cmd.equals(ADD_TORUS_MENU_TEXT)) {
			addNewShape(new Torus(gl), "Torus");
		}
		else if (cmd.equals(ADD_TEAPOT_MENU_TEXT)) {
			try {
				addNewShape(new Teapot(gl), "Teapot");
			} catch (Exception e1) {
				showExceptionDialog(e1);
			}
		}
		else if (cmd.equals(OPEN_MENU_TEXT)) {
			openTree(gl, filename);
		}
		else if (cmd.equals(SAVE_AS_MENU_TEXT)) {
			saveTreeAs(filename);
		}
		else if (cmd.equals(EXIT_MENU_TEXT)) {
			terminate();
		}
	}
	
	public void updateFromSelection()
	{
		SceneNode[] selection = getSelection();

		// Handle reparenting.
		if (isReparenting)
		{
			// Invalid number of new parents selected?
			if (selection.length != 1) return;
			SceneNode parent = selection[0];
			scene.reparent(nodesToReparent, parent);
			isReparenting = false;
		}

		showHideSettingPanels(selection);

		showManip = selection.length == 1;
		if (showManip && currentManip != null)
		{
			if (selection[0] instanceof SceneNode)
				currentManip.setSceneNode(selection[0]);
			else
				currentManip = null;
		}
	}

	@Override
	public void valueChanged(TreeSelectionEvent e)
	{
		updateFromSelection();

		refresh();
	}

	private void showHideSettingPanels(SceneNode[] selection)
	{
		if (selection.length == 1)
		{
			SceneNode node = selection[0];

			int visibleCount = 0;
			
			nameSettingPanel.setNode(node);
			nameSettingPanel.setVisible(true);
			
			nodeSettingPanel.add(nameSettingPanel, "0,"+Integer.toString(visibleCount)+",0,"+Integer.toString(visibleCount));
			visibleCount += 1;

			if (node instanceof SceneNode)
			{
				SceneNode SceneNode = (SceneNode)node;
				transformSettingPanel.setTransformationNode(SceneNode);
				transformSettingPanel.setVisible(true);

				nodeSettingPanel.add(transformSettingPanel, "0,"+Integer.toString(visibleCount)+",0,"+Integer.toString(visibleCount));
				visibleCount += 1;
			}
			else
				transformSettingPanel.setVisible(false);

			if (node instanceof MeshNode)
			{
				MeshNode meshNode = (MeshNode)node;
				PhongMaterial material = (PhongMaterial)meshNode.getMaterial();
				phongMaterialPanel.setMaterial(material);
				phongMaterialPanel.setVisible(true);

				nodeSettingPanel.add(phongMaterialPanel, "0,"+Integer.toString(visibleCount)+",0,"+Integer.toString(visibleCount));
				visibleCount += 1;
			}
			else
				phongMaterialPanel.setVisible(false);

			if (node instanceof LightNode)
			{
				LightNode lightNode = (LightNode)node;
				lightSettingPanel.setLightNode(lightNode);
				lightSettingPanel.setVisible(true);

				nodeSettingPanel.add(lightSettingPanel, "0,"+Integer.toString(visibleCount)+",0,"+Integer.toString(visibleCount));
				visibleCount += 1;
			}
			else
				lightSettingPanel.setVisible(false);
			
			// add "apply to all keyframes" button
			//System.out.println("Adding button...");
			//applyToAllButton.setVisible(true);
			//nodeSettingPanel.add(applyToAllButton, "0,"+Integer.toString(visibleCount)+",0,"+Integer.toString(visibleCount));
			//visibleCount += 1;
		}
		else
		{
			nameSettingPanel.setVisible(false);
			phongMaterialPanel.setVisible(false);
			transformSettingPanel.setVisible(false);
			lightSettingPanel.setVisible(false);
			//applyToAllButton.setVisible(false);
		}
		scenePane.resetToPreferredSizes();
		nodeSettingPanel.repaint();
	}

	@Override
	public void mousePressed(MouseEvent e, CameraController controller) {
		// NOP
	}

	@Override
	public void mouseReleased(MouseEvent e, CameraController controller) {
		if (currentManip != null && showManip && isManipulatingManip)
		{
			currentManip.released();
		}
		isManipulatingManip = false;
	}

	@Override
	public void mouseDragged(MouseEvent e, CameraController controller) {
		if (currentManip != null && showManip && isManipulatingManip)
		{
			currentManip.dragged(controller.getCurrentMousePosition(),
					controller.getMouseDelta());
		}
	}
	
	public SceneKeyframeable getSceneKeyframeable()
    {
	    return scene;
    }

	@Override
    public void startAnimation()
    {
		sceneViewPanel.startAnimation();
    }

	@Override
    public void stopAnimation()
    {
		sceneViewPanel.stopAnimation();
    }
	
	@Override
	public void immediatelyRepaint()
	{
		sceneViewPanel.repaintGLView();
	}
	
	@Override
	public void frameChanged()
	{
		updateFromSelection();
	}

	@Override
	public void drawPicking(GLAutoDrawable drawable, CameraController controller) {
		final GL2 gl = drawable.getGL().getGL2();
		rebuildMeshes(gl);
		
		Program.use(gl, pickingProgram);
		
		setProjectionAndLighting(gl, pickingProgram, controller);

		scene.renderPicking(gl, pickingProgram, controller.getView());
		
		if (showManip && currentManip != null)
		{
			currentManip.renderConstantSize(gl, pickingProgram, controller.getCamera());
		}
		
		Program.unuse(gl);
		
	}

	@Override
	public void handlePicking(GLAutoDrawable drawable,
			CameraController controller, int pickedId) {
		// if node was selected, indicate this in sidebar
		
		SceneNode node = scene.getNodeById(pickedId);
		if (node != null)
		{
			treeView.setSelectionPath(new TreePath(node.getPath()));
		}

		// notify active manipulator if selected
		isManipulatingManip = false;
		if (pickedId == Manip.PICK_CENTER ||
				pickedId == Manip.PICK_X ||
				pickedId == Manip.PICK_Y ||
				pickedId == Manip.PICK_Z)
		{
			if (currentManip != null)
			{
				currentManip.setPickedInfo(pickedId, controller.getCamera(), controller.getLastMousePosition());
				isManipulatingManip = true;
			}
		}

	}
}

class ActionPerformedAnimationP1Runnable implements GLRunnable {
	
	AnimationP1 problem;
	ActionEvent e;
	String filename;
	
	public ActionPerformedAnimationP1Runnable(AnimationP1 problem, ActionEvent e)
	{
		this.problem = problem;
		this.e = e;
		this.filename = null;
	}
	
	public ActionPerformedAnimationP1Runnable(AnimationP1 problem, ActionEvent e, String filename)
	{
		this.problem = problem;
		this.e = e;
		this.filename = filename;
	}

	@Override
	public boolean run(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();
		
		problem.processAction(gl, e, filename);
		
		return true;
	}
	
}
