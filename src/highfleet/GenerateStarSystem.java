package highfleet;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.EconomyAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.procgen.Constellation;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

public class GenerateStarSystem {
    public static void generate(boolean inProgressGame){
        Vector2f location = new Vector2f(-8500,7000);
        Constellation constellation=null;
        EconomyAPI globalEconomy = Global.getSector().getEconomy();
        StarSystemAPI system = Global.getSector().createStarSystem("St. Brendan's Star");
        system.setBackgroundTextureFilename("graphics/backgrounds/background6.jpg");
        SectorEntityToken star = system.initStar("highfleetBrendanStar","star_yellow",850f,location.getX(),location.getY(),400f);
        system.setLightColor(new Color(255, 226, 205));

        //inner jump point
        JumpPointAPI jumpPoint = Global.getFactory().createJumpPoint("highfleet_brendan_star_inner_jump_point","Brendan's Star Inner Jump-point");
        OrbitAPI orbit = Global.getFactory().createCircularOrbit(star,100f,2000f,120f);
        jumpPoint.setOrbit(orbit);
        jumpPoint.setStandardWormholeToHyperspaceVisual();
        system.addEntity(jumpPoint);

        //asteroid belt around everything
        system.addRingBand(star,"misc","rings_asteroids0",256f,0,Color.white,256f,2500f,160f);
        system.addAsteroidBelt(star,100, 2500f, 256f, 130f, 160f, Terrain.ASTEROID_BELT, "Asteroid Belt");

        //Pirate planet
        PlanetAPI p1 = system.addPlanet("highfleet_elaat", star, "Elaat", Planets.ARID,
                80,//angle
                250,//radius
                3500,//orbitRadius
                325);//orbit days
        p1.setFaction(Factions.PERSEAN);
        MarketAPI m1 = BorrowedUtils.addMarketplace(Factions.PERSEAN,p1,null,"Elaat",7,
                new ArrayList<>(Arrays.asList(
                        Conditions.HOT,
                        Conditions.RARE_ORE_MODERATE,
                        Conditions.ORE_MODERATE,
                        Conditions.ORGANICS_ABUNDANT,
                        Conditions.RUINS_EXTENSIVE,
                        Conditions.POPULATION_7,
                        Conditions.POLLUTION
                )),//conditions
                new ArrayList<String>(Arrays.asList(Submarkets.SUBMARKET_OPEN,Submarkets.SUBMARKET_BLACK,Submarkets.SUBMARKET_STORAGE)),//submarkets
                new ArrayList<String>(Arrays.asList(
                        Industries.POPULATION,
                        Industries.HEAVYINDUSTRY,
                        Industries.HEAVYBATTERIES,
                        Industries.MILITARYBASE,
                        Industries.MINING,
                        Industries.LIGHTINDUSTRY,
                        Industries.SPACEPORT
                )),//industries
                true,
                false);
        BorrowedUtils.populateIntelBoard(p1);
        m1.getIndustry(Industries.HEAVYINDUSTRY).setSpecialItem(new SpecialItemData("corrupted_nanoforge",null));
        m1.setSurveyLevel(MarketAPI.SurveyLevel.FULL);
        globalEconomy.addMarket(m1,false);
        //remnants of Kharu
        //1 dust ring
        //2 asteroid ring with overlapping dangerous asteroids
        system.addRingBand(p1,"misc","rings_dust0",256f,0,Color.white,256f,500f,160f);
        system.addRingBand(p1,"misc","rings_asteroids0",256f,2,Color.white,256f,600f,160f);
        system.addRingBand(p1,"misc","rings_asteroids0",256f,3,Color.white,256f,700f,160f);
        system.addAsteroidBelt(p1,50, 700f, 256f, 130f, 160f, Terrain.ASTEROID_BELT, "Remnants of Kharu");

        //second moon
        PlanetAPI p2 = system.addPlanet("highfleet_moon_2",p1, "Second Unnamed Moon",Planets.BARREN2,10,80,1000,75f);
        MarketAPI m2 = p2.getMarket();
        m2.setPlanetConditionMarketOnly(true);
        m2.addCondition(Conditions.NO_ATMOSPHERE);
        m2.addCondition(Conditions.ORE_SPARSE);
        m2.getCondition(Conditions.ORE_SPARSE).setSurveyed(true);
        m2.addCondition(Conditions.RARE_ORE_SPARSE);
        m2.getCondition(Conditions.RARE_ORE_SPARSE).setSurveyed(true);
        m2.setSurveyLevel(MarketAPI.SurveyLevel.FULL);
        //stable location with comm realy
        SectorEntityToken relay = system.addCustomEntity("highfleet_relay","Comm Relay", Entities.COMM_RELAY,Factions.PERSEAN);
        relay.setCircularOrbitPointingDown(star,260f,4500f,400f);
        //other planet out in the system
        PlanetAPI p3 = system.addPlanet("highfleet_ice_planet",star, "Unnamed Ice Planet",Planets.ROCKY_ICE,10,80,4750,425f);
        MarketAPI m3 = p3.getMarket();
        m3.setPlanetConditionMarketOnly(true);
        m3.addCondition(Conditions.NO_ATMOSPHERE);
        m3.addCondition(Conditions.COLD);
        m3.addCondition(Conditions.LOW_GRAVITY);
        m3.addCondition(Conditions.VOLATILES_DIFFUSE);
        m3.getCondition(Conditions.VOLATILES_DIFFUSE).setSurveyed(true);
        m3.addCondition(Conditions.LOW_GRAVITY);
        m3.setSurveyLevel(MarketAPI.SurveyLevel.FULL);

        //two other stable locations
        SectorEntityToken buoy = system.addCustomEntity("highfleet_bouy","Nav Buoy",Entities.NAV_BUOY,Factions.PERSEAN);
        buoy.setCircularOrbit(star,87f,5000,500f);
        SectorEntityToken array = system.addCustomEntity("highfleet_sensor","Sensor Array",Entities.SENSOR_ARRAY_MAKESHIFT,Factions.PERSEAN);
        array.setCircularOrbit(star,87f,6000,600f);

        //outer jump point
        JumpPointAPI jumpPoint2 = Global.getFactory().createJumpPoint("highfleet_brendan_star_fringe_jump_point","Brendan's Star Fringe Jump-point");
        OrbitAPI orbit2 = Global.getFactory().createCircularOrbit(star,100f,7000f,700f);
        jumpPoint2.setOrbit(orbit2);
        jumpPoint2.setStandardWormholeToHyperspaceVisual();
        system.addEntity(jumpPoint2);

        //other things
        system.setEnteredByPlayer(true);
        system.generateAnchorIfNeeded();
        BorrowedUtils.clearDeepHyper(system.getHyperspaceAnchor(),1200);
        system.autogenerateHyperspaceJumpPoints(false,false);

    }
}
