package meico.midi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import info.debatty.java.stringsimilarity.*;

/**
 * This is a helper class to parse a String to a program change number.
 * @author Axel Berndt
 */
public class InstrumentsDictionary {

    public static final byte Levenshtein = 0x00;
    public static final byte NormalizedLevenshtein = 0x01;
    public static final byte Damerau = 0x02;
    public static final byte JaroWinkler = 0x03;
    public static final byte LongestCommonSubsequence = 0x04;
    public static final byte MetricLCS = 0x05;
    public static final byte NGram = 0x06;
    public static final byte QGram = 0x07;
    public static final byte Cosine = 0x08;
    public static final byte Jaccard = 0x09;
    public static final byte SorensenDice = 0x0A;

    Map<String, Short> dict;

    /**
     * The constructor. It reads the dictionary file with all the instrument name strings.
     */
    public InstrumentsDictionary() throws IOException, NullPointerException {
        this.dict = new HashMap<String, Short>();

        // open input stream
        InputStream is = getClass().getResourceAsStream("/resources/instuments.dict");

        // initialize the readers with the input stream
        InputStreamReader ir = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(ir);

        // build (key, value) pairs where the key is the instrument name string and value is the program change number (pc) and add them to the dict map
        Short pc = 0;
        for(String line = br.readLine(); line != null; line = br.readLine()) {  // read all the lines in instruments.dict
            if (line.isEmpty()                                                  // an empty line
                || (line.charAt(0) == '%'))                                     // this is a comment line
                continue;                                                       // ignore it

            if (line.charAt(0) == '#') {                                        // this is a program change number line, it specifies the program change number that all further lines will be associated with until a new program change number is specified
                pc = Short.parseShort(line.substring(1).replaceAll("\\s+", ""));// switch the pc variable, delete any spaces in the string beforehand so that "# 100" -> "#100"
                if (pc > 127) pc = 127;                                         // make sure that the number is in [0, 127]
                if (pc < 0) pc = 0;                                             // cap any values outside this interval
                continue;
            }

            // put the string into the map, associate it with pc
//            if (dict.get(line) != null)                                         // in case of multiple occurrences of keys
//                continue;                                                       // ignore them, use only the first
            dict.put(line.toLowerCase(), pc);                                   // assign the string in this line (in lower case) to the program change number in pc
        }

        // close readers and input stream
        br.close();
        ir.close();
        is.close();
    }

    /**
     * This method parses the input string name and outputs its corresponding midi program change number. This is based on the Normalized Levenshtein distance between the input string and the strings in the instrument names dictionary.
     * @param name an instrument's name string
     * @return the suggested midi program change number; if instrument unknown, output is 0 (Acoustic Grand Piano)
     */
    public short getProgramChange(String name) {
        return this.getProgramChange(name, NormalizedLevenshtein);
    }

    /**
     *
     * @param name an instrument's name string
     * @param distanceMethod the distance method which the identification of the instrument in the dictionary is based on
     * @return the suggested midi program change number; if instrument unknown, output is 0 (Acoustic Grand Piano)
     */
    public short getProgramChange(String name, byte distanceMethod) {
        if (name.isEmpty())                                         // if the name string is empty
            return 0;                                               // default instrument is Acoustic Grand Piano (program Change = 0)

        String n = name.toLowerCase();                              // to ignore the case, the name string is changed to lower case and all the strings it is compared to will be in lower case, too
        short pc = 0;                                               // here comes the result
        double distance = Double.MAX_VALUE;                         // indicates the distance to the name string
        String foo = "";                                            // just for debugging
//        long time = System.currentTimeMillis();                     // for runtime measurements

        for (Map.Entry<String,Short> entry : this.dict.entrySet()) {
//            time = System.nanoTime();
            double cur_distance;
            switch (distanceMethod) {
                case Levenshtein:
                    cur_distance = (new Levenshtein()).distance(entry.getKey(), n);
                    break;
                case NormalizedLevenshtein:
                    cur_distance = (new NormalizedLevenshtein()).distance(entry.getKey(), n);
                    break;
                case Damerau:
                    cur_distance = (new Damerau()).distance(entry.getKey(), n);
                    break;
                case JaroWinkler:
                    cur_distance = (new JaroWinkler()).distance(entry.getKey(), n);
                    break;
                case LongestCommonSubsequence:
                    cur_distance = (new LongestCommonSubsequence()).distance(entry.getKey(), n);
                    break;
                case MetricLCS:
                    cur_distance = (new MetricLCS()).distance(entry.getKey(), n);
                    break;
                case NGram:
                    cur_distance = (new NGram(2)).distance(entry.getKey(), n);
                    break;
                case QGram:
                    cur_distance = (new QGram(2)).distance(entry.getKey(), n);
                    break;
                case Cosine:
                    cur_distance = (new Cosine()).distance(entry.getKey(), n);
                    break;
                case Jaccard:
                    cur_distance = (new Jaccard()).distance(entry.getKey(), n);
                    break;
                case SorensenDice:
                    cur_distance = (new SorensenDice()).distance(entry.getKey(), n);
                    break;
                default:
//                    cur_distance = this.levenshtein(entry.getKey(), n);      // an alternative (slower) version to the Levenshtein implementation in the string similarity library
                    cur_distance = (new NormalizedLevenshtein()).distance(entry.getKey(), n);
            }

            if (cur_distance == 0) {                                        // found perfect match
//                System.out.println(System.nanoTime() - time);
                System.out.println(name + " is mapped to " + entry.getKey() + " with " + cur_distance);
                return entry.getValue();                                    // return the value
            }

            if (cur_distance < distance) {
                distance = cur_distance;
                pc = entry.getValue();
                foo = entry.getKey();
            }
        }
//        System.out.println(System.nanoTime() - time);
        System.out.println(name + " is mapped to " + foo + " with " + distance);
        return pc;
    }

    /**
     * Compute the Levenshtein distance of the two strings str1 and str2.
     * @param str1 string 1
     * @param str2 string 2
     * @return Levenshtein distance of str1 and str2
     */
    private int levenshtein(String str1, String str2) {
        int matrix[][] = new int[str1.length() + 1][str2.length() + 1];
        for (int i = 0; i < str1.length() + 1; i++) {
            matrix[i][0] = i;
        }
        for (int i = 0; i < str2.length() + 1; i++)
            matrix[0][i] = i;
        for (int a = 1; a < str1.length() + 1; a++) {
            for (int b = 1; b < str2.length() + 1; b++) {
                int right = 0;

                if (str1.charAt(a - 1) != str2.charAt(b - 1))
                    right = 1;

                int mini = matrix[a - 1][b] + 1;

                if (matrix[a][b - 1] + 1 < mini)
                    mini = matrix[a][b - 1] + 1;

                if (matrix[a - 1][b - 1] + right < mini)
                    mini = matrix[a - 1][b - 1] + right;

                matrix[a][b] = mini;
            }
        }
        return matrix[str1.length()][str2.length()];
    }
}