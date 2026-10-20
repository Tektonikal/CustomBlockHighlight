package tektonikal.customblockhighlight.config;

//? if >1.8.9 {
import org.polyfrost.oneconfig.api.config.v1.ConfigEntrypoint;

public class CBHOneConfigEntrypoint implements ConfigEntrypoint {
	@Override
	public void registerConfigs(Context context) {
		CBHOneConfig.init();
	}
}
//?}
