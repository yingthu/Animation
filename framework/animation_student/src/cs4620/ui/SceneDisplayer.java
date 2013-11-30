package cs4620.ui;

import cs4620.scene.SceneKeyframeable;

public interface SceneDisplayer
{
	void stopAnimation();
	void startAnimation();
//	void captureNextFrame();
	void immediatelyRepaint();
	SceneKeyframeable getSceneKeyframeable();
//	boolean sceneRendered();
	void frameChanged();
}
