package tektonikal.customblockhighlight.config;

//? if >1.8.9 {
import dev.isxander.yacl3.api.Binding;
import dev.isxander.yacl3.api.StateManager;

public class EvilInstantStateManager<T> implements StateManager<T> {

	private final Binding<T> binding;
	private T saved;
	private T pendingValue;
	private StateListener<T> stateListener;

	public EvilInstantStateManager(Binding<T> binding) {
		this.binding = binding;
		this.saved = binding.getValue();
		this.pendingValue = binding.getValue();
		this.stateListener = StateListener.noop();
	}

	@Override
	public void set(T value) {
		boolean changed = !this.pendingValue.equals(value);
		T oldValue = this.pendingValue;

		this.binding.setValue(value);
		this.pendingValue = value;

		if (changed) {
			this.stateListener.onStateChange(oldValue, value);
		}
	}

	@Override
	public T get() {
		return this.pendingValue;
	}

	@Override
	public void apply() {
		this.saved = this.pendingValue;
	}

	@Override
	public void resetToDefault(ResetAction action) {
		this.set(this.binding.defaultValue());
	}

	@Override
	public void sync() {
		if (!YaclMenu.refreshingAvailability) this.set(this.saved);
	}

	@Override
	public boolean isSynced() {
		return this.saved.equals(this.pendingValue);
	}

	@Override
	public boolean isDefault() {
		return this.binding.defaultValue().equals(this.pendingValue);
	}

	@Override
	public void addListener(StateListener<T> stateListener) {
		this.stateListener = this.stateListener.andThen(stateListener);
	}

	public Binding<T> getBinding() {
		return this.binding;
	}
}
//?}
