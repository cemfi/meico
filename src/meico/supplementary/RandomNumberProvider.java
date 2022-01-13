package meico.supplementary;

import meico.audio.Audio;

import javax.sound.sampled.AudioFormat;
import java.io.File;
import java.util.ArrayList;
import java.util.Random;

/**
 * This class provides random numbers based on the specified distribution.
 * @author Axel Berndt
 */
public class RandomNumberProvider {
    public static final int DISTRIBUTION_UNIFORM = 0;
    public static final int DISTRIBUTION_GAUSSIAN = 1;
    public static final int DISTRIBUTION_TRIANGULAR = 2;
    public static final int DISTRIBUTION_CORRELATED_BROWNIANNOISE = 3;
    public static final int DISTRIBUTION_CORRELATED_COMPENSATING_TRIANGLE = 4;
    public static final int DISTRIBUTION_LIST = 5;

    private Random random;                                  // the random number generator
    private int distributionType;                           // indicates the distribution type which this random number provider uses to generate output
    private ArrayList<Double> series = new ArrayList<>();   // this is filled with the generated random numbers and allows us to recall them, necessary for correlated random numbers

    private double lowCut;
    private double highCut;
    private double standardDeviation;   // this is used by Gaussian distribution

    private double lowerLimit;
    private double upperLimit;
    private double maxStepWidth;        // this is used by Brownian noise distribution
    private double mode;                // this is used by triangular distribution
    private double degreeOfCorrelation; // this is used by compensating triangle distribution

    /**
     * constructor
     *
     * @param distributionType the distribution type
     */
    private RandomNumberProvider(int distributionType) {
        this.random = new Random();
        this.distributionType = distributionType;
    }

    /**
     * a factory for a uniform distribution RandomNumberProvider
     *
     * @param lowerLimit
     * @param upperLimit
     * @return
     */
    public static RandomNumberProvider createRandomNumberProvider_uniformDistribution(double lowerLimit, double upperLimit) {
        RandomNumberProvider rand = new RandomNumberProvider(RandomNumberProvider.DISTRIBUTION_UNIFORM);
        rand.lowerLimit = lowerLimit;
        rand.upperLimit = upperLimit;
        return rand;
    }

    /**
     * a factory for a Gaussian distribution RandomNumberProvider
     * @param standardDeviation
     * @param lowerLimit
     * @param upperLimit
     * @return
     */
    public static RandomNumberProvider createRandomNumberProvider_gaussianDistribution(double standardDeviation, double lowerLimit, double upperLimit) {
        RandomNumberProvider rand = new RandomNumberProvider(RandomNumberProvider.DISTRIBUTION_GAUSSIAN);
        rand.standardDeviation = standardDeviation;
        rand.lowerLimit = lowerLimit;
        rand.upperLimit = upperLimit;
        return rand;
    }

    /**
     * a factory for a triangular distribution RandomNumberProvider
     * @param lowerLimit
     * @param upperLimit
     * @param mode
     * @param lowCut
     * @param highCut
     * @return
     */
    public static RandomNumberProvider createRandomNumberProvider_triangularDistribution(double lowerLimit, double upperLimit, double mode, double lowCut, double highCut) {
        RandomNumberProvider rand = new RandomNumberProvider(RandomNumberProvider.DISTRIBUTION_TRIANGULAR);
        rand.lowerLimit = lowerLimit;
        rand.upperLimit = upperLimit;
        rand.mode = mode;
        rand.lowCut = lowCut;
        rand.highCut = highCut;
        return rand;
    }

    /**
     * a factory for a Brownian noise distribution RandomNumberProvider
     * @param maxStepWidth
     * @param lowerLimit
     * @param upperLimit
     * @return
     */
    public static RandomNumberProvider createRandomNumberProvider_brownianNoiseDistribution(double maxStepWidth, double lowerLimit, double upperLimit) {
        RandomNumberProvider rand = new RandomNumberProvider(RandomNumberProvider.DISTRIBUTION_CORRELATED_BROWNIANNOISE);
        rand.maxStepWidth = maxStepWidth;
        rand.lowerLimit = lowerLimit;
        rand.upperLimit = upperLimit;

        // set the first value in the series, this is later required by method compensatingTriangleDistribution()
        double scaleFactor = rand.upperLimit - rand.lowerLimit;
        double firstValue = (rand.random.nextDouble() * scaleFactor) + rand.lowerLimit;
        rand.series.add(firstValue);

        return rand;
    }

    /**
     * a factory for a compensating triangle distribution RandomNumberProvider
     * @param degreeOfCorrelation
     * @param lowerLimit
     * @param upperLimit
     * @param lowCut
     * @param highCut
     * @return
     */
    public static RandomNumberProvider createRandomNumberProvider_compensatingTriangleDistribution(double degreeOfCorrelation, double lowerLimit, double upperLimit, double lowCut, double highCut) {
        RandomNumberProvider rand = new RandomNumberProvider(RandomNumberProvider.DISTRIBUTION_CORRELATED_COMPENSATING_TRIANGLE);
        rand.degreeOfCorrelation = degreeOfCorrelation;
        rand.lowerLimit = lowerLimit;
        rand.upperLimit = upperLimit;
        rand.lowCut = lowCut;
        rand.highCut = highCut;

        // set the first value in the series, this is later required by method compensatingTriangleDistribution()
        double scaleFactor = rand.highCut - rand.lowCut;
        double firstValue = (rand.random.nextDouble() * scaleFactor) + rand.lowCut;
        rand.series.add(firstValue);

        return rand;
    }

    /**
     * a factory for a RandomNumberProvider with a predefined list of values
     * @param list
     * @return
     */
    public static RandomNumberProvider createRandomNumberProvider_distributionList(ArrayList<Double> list) {
        RandomNumberProvider rand = new RandomNumberProvider(RandomNumberProvider.DISTRIBUTION_LIST);
        rand.series = list;
        return rand;
    }

    /**
     * query the distribution type with which this random number provider works
     * @return
     */
    public int getDistributionType() {
        return this.distributionType;
    }

    /**
     * this can be used to set a specific seed, the series of random numbers so far will be rewritten
     * @param seed
     */
    public void setSeed(long seed) {
        this.random.setSeed(seed);
        this.series.clear();
    }

    /**
     * read the lowCut value
     * @return
     */
    public double getLowCut() {
        return lowCut;
    }

    /**
     * read the highCut value
     * @return
     */
    public double getHighCut() {
        return highCut;
    }

    /**
     * read the standardDeviation value
     * @return
     */
    public double getStandardDeviation() {
        return standardDeviation;
    }

    /**
     * read the lowerLimit value
     * @return
     */
    public double getLowerLimit() {
        return lowerLimit;
    }

    /**
     * read the upperLimit value
     * @return
     */
    public double getUpperLimit() {
        return upperLimit;
    }

    /**
     * read the maxStepWidth value
     * @return
     */
    public double getMaxStepWidth() {
        return maxStepWidth;
    }

    /**
     * read the mode value (triangular distribution)
     * @return
     */
    public double getMode() {
        return mode;
    }

    /**
     * read the degreeOfCorrelation value (Compensating Triangular distribution)
     * @return
     */
    public double getDegreeOfCorrelation() {
        return degreeOfCorrelation;
    }

    /**
     * This method reinitializes the random number series with a specific first value.
     * It works only for the correlated distribution types, i.e. Brownian or Compensating Triangular.
     * All other distribution types ignore this method invocation.
     * @param value
     */
    public void setInitialValue(double value) {
        switch (this.distributionType) {
            case RandomNumberProvider.DISTRIBUTION_CORRELATED_BROWNIANNOISE:
                // make sure the value is within [lowerLimit, upperLimit]
                if (value > this.upperLimit)
                    value = upperLimit;
                else if (value < lowerLimit)
                    value = lowerLimit;
                break;
            case RandomNumberProvider.DISTRIBUTION_CORRELATED_COMPENSATING_TRIANGLE:
                value = this.clip(value);       // make sure the value is within [lowCut, highCut]
                break;
            default:                            // if it is none of the above distributions
                return;                         // do nothing
        }
        this.series.clear();
        this.series.add(value);
    }

    /**
     * query a value from the random number series
     * @param index
     * @return
     */
    public double getValue(int index) {
        index = Math.max(0, index);             // ensure a positive index value

        if (this.distributionType == RandomNumberProvider.DISTRIBUTION_LIST)    // if distribution is based on predefined list
            return this.series.get(index % this.series.size());                 // read the list value and repeat the list if the index exeeds its length

        // for all other distribution types
        while (this.series.size() <= index)     // fill up the series to the desired index
            this.nextDouble();
        return this.series.get(index);          // return the value at the desired index
    }

    /**
     * query a value from the random number series, if that value is non-integer the two neighboring values of the series will be interpolated linearly
     * @param index
     * @return
     */
    public double getValue(double index) {
        int intex = (int)index;
        double rest = index - intex;
        double a = this.getValue(intex);

        if (rest <= 0.0)    // rest should never be < 0.0, but it the check does not hurt
            return a;

        // if the index is between two integer indices
        double b = this.getValue(intex + 1);    // get the value of the next integer index
        return a + ((b - a) * rest);            // interpolate linearly between the two values
    }

    /**
     * on the basis of the current distribution, generate the next random value and add it to the series
     * @return
     */
    private double nextDouble() {
        // get the next random value
        double d = 0.0;
        switch (this.distributionType) {
            case RandomNumberProvider.DISTRIBUTION_UNIFORM:
                d = (this.random.nextDouble() * (this.upperLimit - this.lowerLimit)) + this.lowerLimit;
                break;
            case RandomNumberProvider.DISTRIBUTION_GAUSSIAN:
                do {
                    d = this.random.nextGaussian() * this.standardDeviation;
                } while (!this.withinLimits(d));    // keep generating a new random number while the current value breaks the limits
                break;
            case RandomNumberProvider.DISTRIBUTION_TRIANGULAR:
                d = this.clip(this.triangularDistribution(this.lowerLimit, this.upperLimit, this.mode));
                break;
            case RandomNumberProvider.DISTRIBUTION_CORRELATED_BROWNIANNOISE:
                d = this.brownianNoiseDistribution();
                break;
            case RandomNumberProvider.DISTRIBUTION_CORRELATED_COMPENSATING_TRIANGLE:
                d = this.clip(this.compensatingTriangleDistribution());
                break;
//            case RandomNumberProvider.DISTRIBUTION_LIST:
//                // this is computed at the beginning of method getValue()
//                break;
        }

        // store and return
        this.series.add(d);
        return d;
    }

    /**
     * clip the input value at lowCut and highCut, respectively
     * @param d
     * @return
     */
    private double clip(double d) {
        if (d > this.highCut)
            return this.highCut;
        if (d < this.lowCut)
            return this.lowCut;
        return d;
    }

    /**
     * check whether the input value holds the specified limits
     * @param d
     * @return
     */
    private boolean withinLimits(double d) {
        return (d <= this.upperLimit) && (d >= this.lowerLimit);
    }

    /**
     * this method implements triangular distribution;
     * explanation: https://en.wikipedia.org/wiki/Triangular_distribution#Generating_Triangular-distributed_random_variates
     * @return
     */
    private double triangularDistribution(double lowerLimit, double upperLimit, double mode) {
        if (upperLimit == lowerLimit)               // avoid division by 0.0
            return upperLimit;                      // the limits allow only one value anyway
        double scale = upperLimit - lowerLimit;
        double ca = mode - lowerLimit;
        double F = ca / scale;
        double rand = this.random.nextDouble();
        if (rand < F)
            return lowerLimit + Math.sqrt(rand * scale * ca);
        return upperLimit - Math.sqrt((1 - rand) * scale * (upperLimit - mode));
    }

    /**
     * this method implements the compensating triangle distribution
     * @return
     */
    private double compensatingTriangleDistribution() {
        double prevRandomNum = this.series.get(this.series.size() - 1);
        double newLowerLimit = prevRandomNum - ((prevRandomNum - this.lowerLimit) / degreeOfCorrelation);
        double newUpperLimit = prevRandomNum + ((this.upperLimit - prevRandomNum) / degreeOfCorrelation);
        double result = this.triangularDistribution(newLowerLimit, newUpperLimit, prevRandomNum);

        // if 0.0 < degreeOfCorrelation < 1.0, the limits can be broken, here we have to clip the values
        if (result < this.lowerLimit)
            result = this.lowerLimit;
        if (result > this.upperLimit)
            result = this.upperLimit;

        return result;
    }

    /**
     * this method implements the Brownian noise distribution via a random walk algorithm
     * @return
     */
    private double brownianNoiseDistribution() {
        double result;

        do {
            result = this.series.get(this.series.size() - 1) + ((this.random.nextDouble() - 0.5) * 2.0 * this.maxStepWidth);    // compute uniformly distributed step
//            result = this.series.get(this.series.size() - 1) + ((this.random.nextGaussian() * this.maxStepWidth));              // compute Gaussian distributed step
        } while (!this.withinLimits(result));

        return result;
    }

    /**
     * This method can be used to generate audio data of the specified length from this RandomNumberProvider's distribution.
     * Be aware that the lower limit should not be lower than -1.0 an dthe upper limit should not be greater than 1.0! Set lowerLimit and upperLimit or lowCut and highCut accordingly!
     * @param seconds
     * @return
     */
    public Audio generateAudio(double seconds) {
        int length = (int)(44100.0 * seconds);                              // compute array length, we assume that the audio should be at 44100 sample rate
        double[] doubles = new double[length];                              // create double array from this's serien

        if (this.getDistributionType() == RandomNumberProvider.DISTRIBUTION_LIST) { // distribution lists are read a bit different than the other random number series
            for (int i = 0; i < length; ++i)
                doubles[i] = this.series.get(i % this.series.size());
        } else {
            this.getValue(length - 1);                                      // make sure we have enough values in the series
            for (int i = 0; i < this.series.size(); ++i)
                doubles[i] = this.series.get(i);
        }
        byte[] bytes = Audio.convertDoubleArray2ByteArray(doubles, 16);     // make a byte array from it

        AudioFormat format = new AudioFormat(44100.0f, 16, 1, true, false); // create audio format

        File file = null;
        switch (this.distributionType) {
            case RandomNumberProvider.DISTRIBUTION_UNIFORM:
                file = new File("uniformNoise.wav");
                break;
            case RandomNumberProvider.DISTRIBUTION_GAUSSIAN:
                file = new File("gaussianNoise.wav");
                break;
            case RandomNumberProvider.DISTRIBUTION_TRIANGULAR:
                file = new File("triangleNoise.wav");
                break;
            case RandomNumberProvider.DISTRIBUTION_CORRELATED_BROWNIANNOISE:
                file = new File("brownianNoise.wav");
                break;
            case RandomNumberProvider.DISTRIBUTION_CORRELATED_COMPENSATING_TRIANGLE:
                file = new File("compensatingTriangleNoise.wav");
                break;
            case RandomNumberProvider.DISTRIBUTION_LIST:
                file = new File("distributionListNoise.wav");
                break;
            default:
                break;
        }

        return new Audio(bytes, format, file);
    }
}
