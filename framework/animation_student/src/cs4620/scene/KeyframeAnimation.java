package cs4620.scene;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

public class KeyframeAnimation {
	public static void linearInterpolateTransformation(SceneNode sNode, SceneNode eNode, float t, SceneNode iNode)
	{
		// TODO (Animation P1): Implement if you find useful -- set the state of SceneNode iNode
		// (which can be a superclass of SceneNode, such as SceneNodeKeyframeable) to be
		// the linear interpolation of the states of nodes sNode and eNode.
		
	}
	
	public static void catmullRomInterpolateTransformation(SceneNode p0, SceneNode p1,
			SceneNode p2, SceneNode p3, float t, SceneNode iNode)
	{
		// TODO (Animation P1): Implement if you find useful -- set the state of SceneNode iNode
		// (which can be a superclass of SceneNode, such as SceneNodeKeyframeable) to be the
		// result of evaluating the Catmull-Rom spline whose "control points" are the states
		// of the four SceneNode objects.
		
	}
	
	public static void linearInterpolate4Float(float [] sFloat, float [] eFloat, float t, float [] iFloat)
	{
		// TODO (Animation P1): Implement if you find useful -- interpolate linearly between two size-four
		// arrays and write result to iFloat.
		
	}
	
	public static void catmullRomInterpolate4Float(float [] p0, float [] p1, float [] p2, float [] p3,
			float t, float [] iNode)
	{
		// TODO (Animation P1): Implement if you find useful -- evaluate a Catmull-Rom spline on four
		// four-element-array "control points" and write result to iNode.
		for (int i = 0; i < p0.length; i++)
			iNode[i] = (p0[i] * (-t + 2f*t*t - t*t*t) + p1[i] * (2f - 5f*t*t + 3f*t*t*t)
				       + p2[i] * (t + 4f*t*t - 3f*t*t*t) + p3[i] * (-t*t + t*t*t)) / 2f;
	}
	
	public static void catmullRomRotationInterpolation(Vector3f p0, Vector3f p1,
	        Vector3f p2, Vector3f p3, float t, Vector3f iNode)
	{
		// TODO (Animation P1): Implement if you find useful -- evaluate a Catmull-Rom spline
		// using quaternions with the given four control points, and write result to iNode.
		
	}
	
	public static void catmullRomInterpolation(Vector3f p0, Vector3f p1,
			Vector3f p2, Vector3f p3, float t, Vector3f iNode)
	{
		// TODO (Animation P1): Implement if you find useful -- evaluate a Catmull-Rom spline
		// with the given four control points, and write result to iNode.
		float []result = new float[3];
		float []q0 = {p0.x, p0.y, p0.z};
		float []q1 = {p1.x, p1.y, p1.z};
		float []q2 = {p2.x, p2.y, p2.z};
		float []q3 = {p3.x, p3.y, p3.z};
		catmullRomInterpolate4Float(q0, q1, q2, q3, t, result);
		iNode.x = result[0];
		iNode.y = result[1];
		iNode.z = result[2];
	}
	
	public static Quat4f slerp(Quat4f i1, Quat4f i2, float t)
	{
		// TODO (Animation P1): Implement slerp.
		float dot = i1.w * i2.w + i1.x * i2.x + i1.y * i2.y + i1.z * i2.z;
        if (dot > 1) dot = 1;
        else if (dot < -1) dot = -1;
        float psi = (float)Math.acos(dot);
        boolean reverse = psi < -Math.PI / 2 || psi > Math.PI / 2;
        if (reverse)
                psi = (float)Math.acos(-dot);
        if (Math.abs(psi) < 0.001)
                psi = 0.001f;
        float denom = (float)Math.sin(psi);
        float c1 = (float)Math.sin((1 - t) * psi) / denom;
        float c2 = (float)Math.sin(t * psi) / denom;
        if (reverse)
                c2 = -c2;
        Quat4f result = new Quat4f();
        result.set(c1 * i1.x + c2 * i2.x,
                           c1 * i1.y + c2 * i2.y,
                           c1 * i1.z + c2 * i2.z,
                           c1 * i1.w + c2 * i2.w);
        result.normalize();
        return result;
	}
	
	public static Vector3f getEulerAnglesFromQuaternion(Quat4f quat)
	{
		// TODO (Animation P1): Convert the given quaternion into a vector of 
		// three Euler angles that encode the same rotation.
		float test = 2 * (quat.w * quat.y - quat.z * quat.x);
        Vector3f eulerAngles = new Vector3f();
        if (Math.abs(test) > 0.9999)
        {
        	eulerAngles.set(2 * (float)Math.atan2(quat.x, quat.w), (float)Math.PI / 2, 0);
            if (test < 0)
                eulerAngles.scale(-1);
        }
        else
        {
            eulerAngles.x = (float)Math.atan2(2 * (quat.w * quat.x + quat.y * quat.z),
                                              1 - 2 * (quat.x * quat.x + quat.y * quat.y));
            eulerAngles.y = (float)Math.asin(2 * (quat.w * quat.y - quat.z * quat.x));
            eulerAngles.z = (float)Math.atan2(2 * (quat.w * quat.z + quat.x * quat.y),
                                              1 - 2 * (quat.y * quat.y + quat.z * quat.z));
        }
        eulerAngles.scale(180 / (float)Math.PI);
        return eulerAngles;
	}
	
	public static Quat4f getQuaternionFromEulerAngles(Vector3f eulerAngles)
	{
		// TODO (Animation P1): Convert the given Euler angles into a quaternion
		// that encodes the same rotation.
		Vector3f radians = new Vector3f(eulerAngles);
        radians.scale((float)Math.PI / 180);
        Quat4f qx = new Quat4f();
        Quat4f qy = new Quat4f();
        Quat4f qz = new Quat4f();
        qx.set((float)Math.sin(radians.x / 2), 0, 0, (float)Math.cos(radians.x / 2));
        qy.set(0, (float)Math.sin(radians.y / 2), 0, (float)Math.cos(radians.y / 2));
        qz.set(0, 0, (float)Math.sin(radians.z / 2), (float)Math.cos(radians.z / 2));
        Quat4f q = new Quat4f();
        q.set(qz);
        q.mul(qy);
        q.mul(qx);
        return q;
	}
}
