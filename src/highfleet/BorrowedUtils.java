package highfleet;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.EconomyAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.intel.deciv.DecivTracker;
import com.fs.starfarer.api.impl.campaign.procgen.Constellation;
import com.fs.starfarer.api.impl.campaign.procgen.NebulaEditor;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;
import com.fs.starfarer.api.util.Misc;
import org.lwjgl.util.vector.Vector2f;

import java.util.ArrayList;
import java.util.List;

public class BorrowedUtils {
    public static float secondsFromDays(float days){
        return Global.getSector().getClock().getSecondsPerDay()*days;
    }
    public static boolean isAngleRightRelitive(float angle1, float angle2){
        angle1= Misc.normalizeAngle(angle1);
        angle2=Misc.normalizeAngle(angle2);
        if(angle1+180>360){//rollover
            if(angle2>180){
                return(angle1<angle2);
            }else{
                return(angle1-180>angle2);
            }
        }else{//no rollover
            return(angle1<angle2&&angle1+180>angle2);
        }
    }
    public static Vector2f angleVector(float angle){
        float rad = (float) Math.toRadians(angle);
        return new Vector2f((float) Math.cos(rad), (float) Math.sin(rad));
    }
    //this exists because I always forget how to do this
    public static float vectorAngle(Vector2f vector){
        return Misc.getAngleInDegrees(vector);
    }
    public static float vectorToSpeed(Vector2f vector){
        return (float) Math.sqrt(Math.pow(vector.getX(),2)+ Math.pow(vector.getY(),2));
    }
    public static Vector2f addVector(Vector2f a, Vector2f b){
        return new Vector2f(a.x+b.x,a.y+b.y);
    }
    public static Vector2f minusVector(Vector2f a, Vector2f b){
        return new Vector2f(a.x-b.x,a.y-b.y);
    }
    public static Vector2f multiplyVector(Vector2f a, float b){
        return new Vector2f(a.x*b,a.y*b);
    }
    public static Vector2f modifyVector(Vector2f vector, float amount){
        return new Vector2f(vector.x*amount,vector.y*amount);
    }
    public static void clearDeepHyper(SectorEntityToken entity, float radius) {
        // deep hyperspace removal (copypasted from UW)
        HyperspaceTerrainPlugin plugin = (HyperspaceTerrainPlugin) Misc.getHyperspaceTerrain().getPlugin();
        NebulaEditor editor = new NebulaEditor(plugin);

        float minRadius = plugin.getTileSize() * 2f;
        editor.clearArc(entity.getLocation().x, entity.getLocation().y, 0, radius + minRadius * 0.5f, 0, 360f);
        editor.clearArc(entity.getLocation().x, entity.getLocation().y, 0, radius + minRadius, 0, 360f, 0.25f);
    }
    public static Constellation findNearestConstellationFromStar(Vector2f pos){
        float min = 10000000000000f;
        StarSystemAPI minDistSystem = null;
        for(StarSystemAPI s:Global.getSector().getStarSystems()) {
            float dist =Misc.getDistance(s.getLocation(),pos);
            if(dist<min&&dist>1){
                minDistSystem = s;
                min=dist;
            }
        }
        try {
            return minDistSystem.getConstellation();
        }catch(Exception e){
            return null;
        }
    }
    public static Vector2f starPlacer(Vector2f location, float radius, float minDist, float maxDist, int tries){
        ArrayList<StarSystemAPI> starSystems = new ArrayList<>();

        for(StarSystemAPI s:Global.getSector().getStarSystems()){
            if(Misc.getDistance(s.getLocation(),location)<radius +maxDist){
                starSystems.add(s);
            }
        }
        //generate random points in radius -mindist
        int i=0;
        while(i<tries){
            i++;
            float angle = (float) (Math.random()*360);
            float dist = (float) (Math.random()*radius);
            Vector2f av = angleVector(angle);
            Vector2f place = multiplyVector(av,dist);
            place = addVector(place,location);
            for(StarSystemAPI s :starSystems){
                if(Misc.getDistance(s.getLocation(),place)<minDist&&Misc.getDistance(s.getLocation(),place)>maxDist){
                    place=null;
                    break;
                }
            }
            if(place!=null){
                return place;
            }
        }
        return null;
    }
    //THIS IS PERFECT! The replacement to OMSAMFR FR FR
    //Yeah
    public static CombatEntityAPI modifyRangeAndSpawnProjectile(CombatEngineAPI engine, WeaponAPI weapon, String weaponId, float shotAngle, float speedFactor, boolean addShipSpeed){
        return modifyRangeAndSpawnProjectileAtLocation(engine,weapon,weaponId,shotAngle,speedFactor,addShipSpeed,weapon.getFirePoint(0));
    }
    public static CombatEntityAPI modifyRangeAndSpawnProjectileAtLocation(CombatEngineAPI engine, WeaponAPI weapon, String weaponId, float shotAngle, float speedFactor,boolean addShipSpeed, Vector2f location){
        ShipAPI ship = weapon.getShip();
        MutableShipStatsAPI stats = ship.getMutableStats();
        stats.getProjectileSpeedMult().modifyMult("thquest_subprojectile",speedFactor);
        Vector2f addedSpeed = new Vector2f();
        if(addShipSpeed){
            addedSpeed = ship.getVelocity();
        }
        CombatEntityAPI projectile = engine.spawnProjectile(ship, weapon,weaponId,location,shotAngle,addedSpeed);
        stats.getProjectileSpeedMult().unmodify("thquest_subprojectile");
        return projectile;

    }

    //more or less copied from nex
    public static void populateIntelBoard(SectorEntityToken place){
        MarketAPI market =place.getMarket();
        PersonAPI person = place.getFaction().createRandomPerson();
        person.setPostId(Ranks.POST_ADMINISTRATOR);
        market.getCommDirectory().addPerson(person);

        market.setAdmin(person);

        person = place.getFaction().createRandomPerson();
        person.setPostId(Ranks.POST_PORTMASTER);
        market.getCommDirectory().addPerson(person);

        person = place.getFaction().createRandomPerson();
        person.setPostId(Ranks.POST_SUPPLY_OFFICER);
        market.getCommDirectory().addPerson(person);

    }
    //not entirely sure where this comes from. Maybe tomato?
    public static MarketAPI addMarketplace(
            String factionID,
            SectorEntityToken primaryEntity,
            ArrayList<SectorEntityToken> connectedEntities,
            String name,
            int size,
            ArrayList<String> marketConditions,
            ArrayList<String> submarkets,
            ArrayList<String> industries,
            Boolean WithJunkAndChatter,
            Boolean PirateMode) {
        EconomyAPI globalEconomy = Global.getSector().getEconomy();
        String planetID = primaryEntity.getId();
        String marketID = planetID + "_market";

        MarketAPI newMarket = Global.getFactory().createMarket(marketID, name, size);
        newMarket.setFactionId(factionID);
        newMarket.setPrimaryEntity(primaryEntity);
        newMarket.getTariff().modifyFlat("generator", newMarket.getFaction().getTariffFraction());

        if (submarkets != null) {
            for (String market : submarkets) {
                newMarket.addSubmarket(market);
            }
        }

        for (String condition : marketConditions) {
            try {
                newMarket.addCondition(condition);
            } catch (RuntimeException e) {
                newMarket.addIndustry(condition);
            }
        }
        if (industries != null) {
            for (String industry : industries) {
                newMarket.addIndustry(industry);
            }

        }

        if (connectedEntities != null) {
            for (SectorEntityToken entity : connectedEntities) {
                newMarket.getConnectedEntities().add(entity);
            }
        }

        globalEconomy.addMarket(newMarket, WithJunkAndChatter);
        primaryEntity.setMarket(newMarket);
        primaryEntity.setFaction(factionID);

        if (connectedEntities != null) {
            for (SectorEntityToken entity : connectedEntities) {
                entity.setMarket(newMarket);
                entity.setFaction(factionID);
            }
        }

        if (PirateMode) {
            newMarket.setEconGroup(newMarket.getId());
            newMarket.setHidden(true);
//            primaryEntity.setSensorProfile(1f);
//            primaryEntity.setDiscoverable(true);
//            primaryEntity.getDetectedRangeMod().modifyFlat("gen", 5000f);
            newMarket.getMemoryWithoutUpdate().set(DecivTracker.NO_DECIV_KEY, true);
        } else {
            for (MarketConditionAPI mc : newMarket.getConditions()) {
                mc.setSurveyed(true);
            }
            newMarket.setSurveyLevel(MarketAPI.SurveyLevel.FULL);
        }

        newMarket.reapplyIndustries();
        return newMarket;
    }
    public static void discoverMarket(String marketId, boolean chatter){
        MarketAPI marketAPI = Global.getSector().getEntityById(marketId).getMarket();
        marketAPI.setHidden(false);
        marketAPI.setEconGroup(null);
        Global.getSector().getEconomy().addMarket(marketAPI,chatter);
        marketAPI.getMemoryWithoutUpdate().set(DecivTracker.NO_DECIV_KEY, false);
        for(MarketConditionAPI mc : marketAPI.getConditions()) {
            mc.setSurveyed(true);
        }
        marketAPI.setSurveyLevel(MarketAPI.SurveyLevel.FULL);
    }

    public static Vector2f findHyperspaceLocationForFleetSpawn(Vector2f locInHyperspace, float maxRadius){
        int maxTries = 100;
        int tries = 0;
        Vector2f player =new Vector2f(-100000000,-100000000);
        if(Global.getSector().getPlayerFleet().isInHyperspace()){
            player = Global.getSector().getPlayerFleet().getLocation();
        }
        Vector2f randomLocation;
        float playerView =Global.getSector().getViewport().getVisibleWidth();
        do {
            randomLocation = new Vector2f((float) ((Math.random() - .5f) * maxRadius), (float) ((Math.random() - .5f) * maxRadius));
            randomLocation = addVector(locInHyperspace,randomLocation);
            tries++;
            if(tries > maxTries){
                break; //whatever, spawns visibly
            }
        } while (!(Misc.getDistance(player, randomLocation) > playerView*2));
        return randomLocation;
    }
    public static <T> List<T> getIntelByClass(Class<T> clazz){
        return (List<T>) Global.getSector().getIntelManager().getIntel(clazz);
    }

}
