package ltd.opens.mg.mc.fabric;

import ltd.opens.mg.mc.MaingraphforMC;
import net.fabricmc.api.ModInitializer;

public class MaingraphforMCFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        MaingraphforMC.init();
    }
}
