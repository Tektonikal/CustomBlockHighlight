package tektonikal.customblockhighlight.config;

//? if >1.8.9 {
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class ModMenuIntegration implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return BlockHighlightConfig.ACTIVE_INSTANCE::getConfigScreen;
	}
}
//?}
