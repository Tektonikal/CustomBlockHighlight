package tektonikal.customblockhighlight.util;

import net.minecraft.util.Mth;
import tektonikal.customblockhighlight.Blockhighlight;
import tektonikal.customblockhighlight.Renderer;

import java.util.function.Supplier;

public class Tweener {
	Supplier<Number> target;
	double value;
	float speed;

	public Tweener(Supplier<Number> target) {
		this(target, 20);
	}

	public Tweener(Supplier<Number> target, float speed) {
		this.target = target;
		value = target.get().doubleValue();
		this.speed = speed;
	}

	public void update() {
		value = Blockhighlight.ease(value, target.get().doubleValue(), speed);
		if (isAtTarget()) value = target.get().doubleValue();
	}

	public double get() {
		return value;
	}

	public float getF() {
		return (float) value;
	}

	public int getI() {
		return (int) value;
	}

	public float getFloatingRemainder() {
		return this.getF() - (int) this.get();
	}

	public boolean isAtTarget() {
		// approxEq takes too long to say true
		var targetV = target.get();
		return Math.abs(value - targetV.doubleValue()) < 1.0E-3F;
	}

	public double getLerped(double start, double end) {
		return Mth.lerp(value, start, end);
	}

	public void snapToTarget() {
		this.value = this.target.get().doubleValue();
	}
}
