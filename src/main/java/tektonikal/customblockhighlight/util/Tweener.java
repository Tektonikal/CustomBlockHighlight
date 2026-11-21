package tektonikal.customblockhighlight.util;

import tektonikal.customblockhighlight.CustomBlockHighlight;

import java.util.function.Supplier;

public class Tweener {
	Supplier<Number> target;
	double value;
	float speed;

	public Tweener(Supplier<Number> target, float speed) {
		this.target = target;
		value = target.get().doubleValue();
		this.speed = speed;
	}

	public void update() {
		value = CustomBlockHighlight.ease(value, target.get().doubleValue(), speed);
		if (isAtTarget()) value = target.get().doubleValue();
	}

	public float getF() {
		return (float) value;
	}

	public boolean isAtTarget() {
		var targetV = target.get();
		return Math.abs(value - targetV.doubleValue()) < 1.0E-3F;
	}
}
