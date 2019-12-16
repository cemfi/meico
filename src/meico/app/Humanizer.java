package meico.app;

import meico.midi.Midi;
import meico.mpm.Mpm;
import meico.mpm.elements.Performance;
import meico.mpm.elements.maps.ImprecisionMap;

/**
 * This is a simple demo application class for the Music Performance Markup (MPM) API.
 * It reads an input Midi/Mpm object, generates some basic humanizing and creates a humanized output Midi/Mpm object.
 * Warning: This does not reproduce all information of the original MIDI file with just some slide changes! Only a basic subset of MIDI events is currently supported. But it works fine with basic MIDI sequences and MIDI data that was generated with meico (e.g. from MEI-MSM-MIDI conversion).
 * @author Axel Berndt
 */
public class Humanizer {
    /**
     * humanize a Midi object
     * @param midi
     * @return
     */
    public static Midi humanize(Midi midi) {
        if (midi == null)
            return null;

        Performance performance = Performance.createPerformance("Humanize");    // create a performance; it is not really necessary to integrate it in an Mpm object as shown  in the 2 alternative code lines below as far as we do not want to store it as an MPM file
//        Mpm mpm = new Mpm();                                                    // initiallize a new MPM object
//        Performance performance = mpm.addPerformance("Humanize");               // add a performance to the MPM (an MPM can host several performance)
        if (performance == null)
            return null;

        performance.getGlobal().getDated().addMap(midi.getTempoMap());          // export the tempomap from MIDI as an MPM tempoMap and add it to the performance

        ImprecisionMap timing = (ImprecisionMap) performance.getGlobal().getDated().addMap(Mpm.IMPRECISION_MAP_TIMING);             // add a timing imprecisionMap
        timing.addDistributionCompensatingTriangle(0.0, 4.0, -80.0, 80.0, -80.0, 80.0, 300.0);                                      // timing imprecision is best achieved by a correlated distribution

        ImprecisionMap toneDuration = (ImprecisionMap) performance.getGlobal().getDated().addMap(Mpm.IMPRECISION_MAP_TONEDURATION); // add an imprecisionMap for articulation/tone duration
        toneDuration.addDistributionTriangular(0.0, -30.0, 0.0, 0.0, -30.0, 0.0);

        ImprecisionMap dynamics = (ImprecisionMap) performance.getGlobal().getDated().addMap(Mpm.IMPRECISION_MAP_DYNAMICS);         // add a dynamics imprecisionMap
        dynamics.addDistributionGaussian(0.0, 7.5, -15.0, 15.0);

        return midi.exportMsm().exportExpressiveMidi(performance);              // render all performance data into expressive MIDI
//        return performance.perform(midi).exportExpressiveMidi();                // an alternative call to render all performance data into expressive MIDI
    }

    /**
     * Add basic humanizing data (timing, articulation and dynamics variation) to an MPM Performance.
     * Only global information (the respective imprecisionMaps) are added, hence local (part-specific) maps are left unaltered.
     * If any of these global imprecisionMaps does already exist it is also left unaltered.
     * @param performance
     */
    public static void addHumanizing(Performance performance) {
        if (performance == null)
            return;

        ImprecisionMap timing = (ImprecisionMap) performance.getGlobal().getDated().getMap(Mpm.IMPRECISION_MAP_TIMING);             // get the performance's global timing imprecisionMap
        if (timing == null) {                                                                                                       // if there is none
            timing = (ImprecisionMap) performance.getGlobal().getDated().addMap(Mpm.IMPRECISION_MAP_TIMING);                        // add a timing imprecisionMap
        }
        if (timing.size() == 0)                                                                                                     // we do not want to alter a non-empty imprecisionMap, thus this check
            timing.addDistributionCompensatingTriangle(0.0, 4.0, -80.0, 80.0, -80.0, 80.0, 300.0);                                  // timing imprecision is best achieved by a correlated distribution

        ImprecisionMap toneDuration = (ImprecisionMap) performance.getGlobal().getDated().getMap(Mpm.IMPRECISION_MAP_TONEDURATION); // get the performance's gloabel tone duration imprecisionMap
        if (toneDuration == null) {                                                                                                 // if there is none
            toneDuration = (ImprecisionMap) performance.getGlobal().getDated().addMap(Mpm.IMPRECISION_MAP_TONEDURATION);            // add an imprecisionMap for articulation/tone duration
        }
        if (toneDuration.size() == 0)                                                                                               // we do not want to alter a non-empty imprecisionMap, thus this check
            toneDuration.addDistributionTriangular(0.0, -30.0, 0.0, 0.0, -30.0, 0.0);

        ImprecisionMap dynamics = (ImprecisionMap) performance.getGlobal().getDated().getMap(Mpm.IMPRECISION_MAP_DYNAMICS);         // get the performance's global dynamics imprecisionMap
        if (dynamics == null) {                                                                                                     // if there is none
            dynamics = (ImprecisionMap) performance.getGlobal().getDated().addMap(Mpm.IMPRECISION_MAP_DYNAMICS);                    // add a dynamics imprecisionMap
        }
        if (dynamics.size() == 0)                                                                                                   // we do not want to alter a non-empty imprecisionMap, thus this check
            dynamics.addDistributionGaussian(0.0, 7.5, -15.0, 15.0);
    }



    ////// mpm test code ... nothing meaningful ////////////////////////////////////////
//        Mpm mpm = new Mpm();
//        Performance performance1 = Performance.createPerformance("test1");
//        Element p2Elt = new Element("performance");
//
//        p2Elt.addAttribute(new Attribute("name", "test2"));
//        Performance performance2 = Performance.createPerformance(p2Elt);
//
//        mpm.addPerformance(performance1);
//        mpm.addPerformance(performance2);
//
//        performance1.addPart(Part.createPart("test part", 0, 0, 0));
//
//        performance1.getPart("test part").getHeader().addStyleDef("testStyles", "testStyle1");
//        performance1.getPart("test part").getHeader().addStyleDef("testStyles", "testStyle2");
//        performance1.getPart("test part").getHeader().addStyleType("anotherTestTypeStyles");
//
//        performance1.getPart(0).getHeader().addStyleDef(Mpm.RUBATO_STYLE, RubatoStyle.createRubatoStyle("rubaStyle"));
//        Header header = mpm.getPerformance("test1").getPart("test part").getHeader();
//        ((RubatoStyle)header.getStyleDef(Mpm.RUBATO_STYLE, "rubaStyle")).addRubatoDef(RubatoDef.createRubatoDef("rubi", 20.5, 0.6, 0.1, 0.9));
//
//        header.addStyleDef(Mpm.METRICAL_ACCENTUATION_STYLE, MetricalAccentuationStyle.createMetricalAccentuationStyle("my accentuation style"));
//        MetricalAccentuationStyle mas = (MetricalAccentuationStyle) header.getStyleDef(Mpm.METRICAL_ACCENTUATION_STYLE, "my accentuation style");
//        AccentuationPatternDef pattern = AccentuationPatternDef.createAccentuationPatternDef("pattern 1", 4.0);
//        pattern.addAccentuation(1.0, 1.0, -0.5, 0.0);
//        pattern.addAccentuation(3.0, 0.5, -0.5, 0.5);
//        pattern.addAccentuation(2.0, 0.25, -1.0, 0.25);
//        mas.addAccentuationPatternDef(pattern);
//
//        DynamicsMap dynamicsMap = (DynamicsMap)performance1.getGlobal().getDated().addMap(Mpm.DYNAMICS_MAP);
//        dynamicsMap.setStartStyle("a dynamics style");
//        dynamicsMap.addDynamics(0.0, "mp");
//        dynamicsMap.addDynamics(2030.0, "loud", "pp", 0.2, -0.4, 0.1);
//        dynamicsMap.addDynamics(305.0, "mf", "115.0", 0.0, 0.0);
//        dynamicsMap.addStyleSwitch(305.0, "another dynamics style");
//        DynamicsData dd = dynamicsMap.getDynamicsDataAt(500.0);
//        System.out.println(dd.startDate  + ", " + dd.endDate + ", " + dd.volume + ", " + dd.transitionTo + ", " + dd.curvature + ", " + dd.protraction + ", " + dd.subNoteDynamics + ", " + dd.style);
//
//        ImprecisionMap imprecisionMap = (ImprecisionMap)performance1.getPart("test part").getDated().addMap(Mpm.IMPRECISION_MAP);
//        imprecisionMap.setDomain("tuning");
//        imprecisionMap.setDetuneUnit("Hertz");
//
//        System.out.println(imprecisionMap.toXml());

    // RandomNumberProvider-Test
//        RandomNumberProvider rand = RandomNumberProvider.createRandomNumberProvider_uniformDistribution(-1.0, 1.0);
//        rand.generateAudio(60.0).writeAudio();
//        rand = RandomNumberProvider.createRandomNumberProvider_gaussianDistribution(1.0, -1.0, 1.0);
//        rand.generateAudio(60.0).writeAudio();
//        rand = RandomNumberProvider.createRandomNumberProvider_triangularDistribution(-1.0, 1.0, 0.0, -1.0, 1.0);
//        rand.generateAudio(60.0).writeAudio();
//        rand = RandomNumberProvider.createRandomNumberProvider_brownianNoiseDistribution(0.1, -1.0, 1.0);
//        rand.generateAudio(60.0).writeAudio();
//        rand = RandomNumberProvider.createRandomNumberProvider_compensatingTriangleDistribution(10.0, -1.0, 1.0, -1.0, 1.0);
//        rand.setInitialValue(-0.5);
//        rand.generateAudio(60.0).writeAudio();
    ///////////////////////////////////////////////////////////
}
