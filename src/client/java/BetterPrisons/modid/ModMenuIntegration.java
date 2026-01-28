package BetterPrisons.modid;

import BetterPrisons.modid.ui.custom.screens.CustomConfigScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return CustomConfigScreen::new;
    }
}
