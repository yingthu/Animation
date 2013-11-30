package cs4620.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import cs4620.scene.Scene;
import cs4620.scene.SceneKeyframeable;

public class AnimationPanel extends JPanel implements ActionListener, ChangeListener,
        DocumentListener
{
	public final int frameRate = 30;
	
	JList animationStateList;
	DefaultListModel animationStates;
	JButton keyframeButton;
	JButton playButton;
	SceneKeyframeable currentScene;
	SceneDisplayer displayer;
	JSlider slider;
	JTextField frameTextField;
	JPanel framePanel;
	JPanel southPanel;
	JCheckBox splineInterpolation;
	//JCheckBox capture;
	int numTicks = 300;
	int initValue = 0;
	int currentFrame = 0;
	boolean linearInterpolation = true;
	Timer controllersValueRefresher;
	Timer animationTimer;
	boolean frameChanged = false;
	boolean isPlaying = false;
	int frameCount = 0;
	
	public AnimationPanel(final SceneDisplayer displayer)
	{
		this.displayer = displayer;
		
		setLayout(new BorderLayout());
		
		slider = new JSlider(JSlider.VERTICAL);
		slider.setMinorTickSpacing(1);
		slider.setMaximum(numTicks);
		slider.setMinimum(0);
		slider.setPaintTicks(true);
		slider.setValue(initValue);
		slider.addChangeListener(this);
		this.add(slider, BorderLayout.CENTER);
		
		frameTextField = new JTextField();
		frameTextField.setText("" + initValue);
		frameTextField.setHorizontalAlignment(JTextField.RIGHT);
		frameTextField.getDocument().addDocumentListener(this);
		
		framePanel = new JPanel();
		framePanel.setLayout(new BorderLayout());
		framePanel.add(slider, BorderLayout.CENTER);
		framePanel.add(frameTextField, BorderLayout.NORTH);
		
		animationStates = new DefaultListModel();
		animationStates.addElement(new Integer(0));
		animationStateList = new JList(animationStates);
		animationStateList.setSelectedIndex(0);
		
		MouseListener mouseListener = new MouseAdapter() {  
		      public void mouseClicked(MouseEvent mouseEvent) {  
		        JList theList = (JList) mouseEvent.getSource();  
		        if (mouseEvent.getClickCount() == 1) {  
		          int index = theList.locationToIndex(mouseEvent.getPoint());  
		          if (index >= 0) {
		        	Integer frame = (Integer)theList.getModel().getElementAt(index);
		            setCurrentFrame(frame.intValue());
		          }  
		        }  
		      }  
		    };  
		animationStateList.addMouseListener(mouseListener);
		
		keyframeButton = new JButton("Keyframe it");
		keyframeButton.addActionListener(this);
		
		playButton = new JButton("Play!");
		playButton.addActionListener(this);
		
		splineInterpolation = new JCheckBox("Cubic spline interp.");
		splineInterpolation.addActionListener(this);
		
		//capture = new JCheckBox("Capture to file");
		
		southPanel = new JPanel();
		southPanel.setLayout(new BorderLayout());
		//southPanel.add(capture, BorderLayout.CENTER);
		southPanel.add(playButton, BorderLayout.SOUTH);
		southPanel.add(splineInterpolation, BorderLayout.NORTH);
		
		this.add(keyframeButton, BorderLayout.NORTH);
		this.add(southPanel, BorderLayout.SOUTH);
		this.add(framePanel, BorderLayout.WEST);
		this.add(animationStateList, BorderLayout.CENTER);
		
		controllersValueRefresher = new Timer(1000 / frameRate, this);
		controllersValueRefresher.start();
		
		animationTimer = new Timer(1000 / frameRate, this);
	}
	
	public void setCurrentScene(SceneKeyframeable scene)
	{
		currentScene = scene;
	}
	
	public void setCurrentFrame(int frame)
	{
		// Make change to the list.
		int idxInList = findKeyframeInList(currentFrame);
		if (idxInList != -1)
		{
			// stash changes to this keyframe before interpolating
			currentScene.addAsKeyframe(currentFrame);
			animationStateList.setSelectedIndex(idxInList);
		}
		else
			animationStateList.clearSelection();
		
		// Make change to currentScene.
		if (linearInterpolation)
			currentScene.linearInterpolateTo(frame);
		else
			currentScene.catmullRomInterpolateTo(frame);
		
		currentFrame = frame;
		frameChanged = true;
		
		if(!isPlaying)
			displayer.frameChanged();
	}
		
	@Override
	public void stateChanged(ChangeEvent e) {
		setCurrentFrame(slider.getValue());
	}
	
	public int findKeyframeInList(int frame)
	{
		for (int i = 0; i < animationStates.size(); i++)
		{
			int f = ((Integer)animationStates.get(i)).intValue();
			if (f == frame)
				return i;
		}
		return -1;
	}
	
	public void insertKeyframe(int frameIndex)
	{
		currentScene.addAsKeyframe(frameIndex);
    	
    	Integer frame = new Integer(frameIndex);
    	int f = -1;
    	for (int i = 0; i < animationStates.size(); i++)
    		if (((Integer) animationStates.get(i)).intValue() < frameIndex)
    			f = i;
    	animationStates.add(f + 1, frame);
	}
	
	public void insertKeyframeShallow(int frameIndex)
	{
    	Integer frame = new Integer(frameIndex);
    	int f = -1;
    	for (int i = 0; i < animationStates.size(); i++)
    		if (((Integer) animationStates.get(i)).intValue() < frameIndex)
    			f = i;
    	animationStates.add(f + 1, frame);
	}
	
	public void removeAllKeyframes()
	{
		animationStates.clear();
	}

	@Override
    public void actionPerformed(ActionEvent e)
    {
		if (e.getSource() == animationTimer)
		{
			if (isPlaying)
			{
				//if (capture.isSelected() && displayer.sceneRendered() == false)
				//	return;
				if (currentFrame <= getMaxFrameNumber())
				{
					//if (capture.isSelected())
					//	displayer.captureNextFrame();
					setCurrentFrame(currentFrame + 1);
					displayer.immediatelyRepaint();
					frameCount = (frameCount + 1) % frameRate;
					if(frameCount % (frameRate / 8) == 0)
						displayer.frameChanged(); // only refresh transformation settings panel periodically to avoid lag
				}
				else
				{
					stopAnimation();
					setCurrentFrame(0);
				}
			}
		}
		else if (e.getSource() == controllersValueRefresher)
		{
			if (frameChanged)
			{
				// Make change to the slider.
				slider.setValue(currentFrame);
				
				// Make change to the text.
				frameTextField.setText("" + currentFrame);
				
				frameChanged = false;
			}
		}
		else if (e.getSource() == keyframeButton)
	    {
			int [] keyframes = currentScene.getFrameNumbers();
			for(int f : keyframes)
				if(f == currentFrame)
					return; // we already have a keyframe at currentFrame
			
	    	insertKeyframe(currentFrame);
	    	setCurrentFrame(currentFrame);
	    }
	    else if (e.getSource() == playButton)
	    {
	    	if (isPlaying == false)
	    	{
	    		startAnimation();
	    	}
	    	else
	    	{
	    		stopAnimation();
	    	}
	    }
	    else if (e.getSource() == splineInterpolation)
	    {
	    	linearInterpolation = !splineInterpolation.isSelected();
	    }
    }
	
	public int getMaxFrameNumber()
	{
		return numTicks;
	}
	
	public int getCurrentFrame()
	{
		return currentFrame;
	}
	
	public void textfieldChanged()
	{
		int frame = 0;
    	try
    	{
    		frame = Integer.parseInt(frameTextField.getText());
    		setCurrentFrame(frame);
    	}
    	catch (Exception ex)
    	{
    		return;
    	}
	}

	@Override
    public void changedUpdate(DocumentEvent arg0)
    {
		textfieldChanged();
    }

	@Override
    public void insertUpdate(DocumentEvent arg0)
    {
		textfieldChanged();
    }

	@Override
    public void removeUpdate(DocumentEvent arg0)
    {
		textfieldChanged();
    }
	
	public void startAnimation()
	{
		isPlaying = true;
		playButton.setText("Stop!");
		// Disable camera adjustment.
		displayer.stopAnimation();
		
		animationTimer.start();
	}
	
	public void stopAnimation()
	{
		animationTimer.stop();
		
		isPlaying = false;
		playButton.setText("Play!");
		
		displayer.frameChanged();
		// Enable camera adjustment.
		displayer.startAnimation();
	}
	
	@Override
	public void addMouseListener(MouseListener l)
	{
		animationStateList.addMouseListener(l);
	}

	public void removeSelectedKeyframe()
    {
		int idx = animationStateList.getSelectedIndex();
		if (idx <= 0) return; // We don't want to delete frame 0.
		
		currentScene.deleteKeyframe(((Integer) animationStates.get(idx)).intValue());
		
		animationStates.remove(idx);
    }
}
