package highfleet;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import exerelin.campaign.SectorManager;

public class HighfleetModPlugin extends BaseModPlugin {
    @Override
    public void onEnabled(boolean wasEnabledBefore){
        if(!wasEnabledBefore){
            boolean haveNexerelin = Global.getSettings().getModManager().isModEnabled("nexerelin");
            if (!haveNexerelin || SectorManager.getManager().isCorvusMode()) {
                setup(true);
            }
        }
    }
    public void setup(boolean inProgressGame) {
        GenerateStarSystem.generate(inProgressGame);
    }
}
