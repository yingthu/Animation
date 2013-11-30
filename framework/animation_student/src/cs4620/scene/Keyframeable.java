package cs4620.scene;

public interface Keyframeable {
	
	// Return a list of all the frames in the object
	public int [] getFrameNumbers();
	
	// Add the current interpolated state of the object as a keyframe
	public void addAsKeyframe(int frame);
	
	// Apply the current interpolated state of the object to all keyframes
	public void applyToAllKeyframes();
	
	// Delete the specified keyframe (if it exists)
	public void deleteKeyframe(int frame);
	
	// Linearly interpolate state to the specified frame
	public void linearInterpolateTo(int frame);
	
	// Interpolate state to the specified frame using Catmull-Rom splines
	public void catmullRomInterpolateTo(int frame);
}
