package meico.mei.ornament;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * The OrnamentDictionary cares about the ornaments.dict to provide an ornament's alterations.
 * @author Lars Engeln
 */
public class OrnamentDictionary {
    protected HashMap<String, List<String>> ornamentLookup; // lookup table for ornament names and their corresponding alterations

    /**
     * default constructor, initiating the lookUp
     */
    public OrnamentDictionary() {
        try {
            createOrnamentLookUp();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * returns the ornamentLookup with ornamentName->alterations
     * @return
     */
    public HashMap<String, List<String>> getOrnamentLookup() {
        return ornamentLookup;
    }

    /**
     * checks if the lookUp has alterations for the given ornamentName
     * @param ornamentName
     * @return true if ornamentName is in the lookUp, false otherwise
     */
    public Boolean has(String ornamentName) {
        return ornamentLookup.containsKey(ornamentName);
    }

    /**
     * return the alterations for the given ornamentName, null if no alterations are found
     * @param ornamentName
     * @return alterations of ornamentName, or null
     */
    public List<String> get(String ornamentName) {
        if(has(ornamentName))
            return ornamentLookup.get(ornamentName);
        else
            return null;
    }

    /**
     * creates the lookUp table with ornament descriptions. Names for lookUp are identical to getOrnamentFullName results
     */
    private void createOrnamentLookUp() throws IOException, NullPointerException {
        this.ornamentLookup = new HashMap<String, List<String>>();

        // open input stream
        InputStream is = getClass().getResourceAsStream("/resources/ornaments.dict");
        if(is == null)
            return;

        // initialize the readers with the input stream
        InputStreamReader ir = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(ir);

        // build (key, value) pairs where the key is the ornament name string and value is the List of alterations and add them to the dict map
        List<String> ornamentNames = new ArrayList<String>();
        boolean isCollectingNames = false;
        for(String line = br.readLine(); line != null; line = br.readLine()) {  // read all the lines in *.dict
            if (line.isEmpty()                                                  // an empty line
                    || (line.charAt(0) == '%'))                                     // this is a comment line
                continue;                                                       // ignore it

            if (line.charAt(0) == '#') {                                        // this is an ornament name line, it specifies that all further lines will be associated with it until an ornament line is read
                if(!isCollectingNames) {                                        // if it is the first line with an ornament name
                    isCollectingNames = true;
                    ornamentNames.clear();
                }
                ornamentNames.add(line.substring(1).trim());                 // add the ornamentName, delete any spaces in the string beforehand so that "# trill " -> "trill"

                continue;
            }
            else if (isCollectingNames) {                                       // if I am currently collecting names, but I have read a line that is not an ornament name line, I am now collecting alterations for the current ornament name
                isCollectingNames = false;
            }

            if(!line.isEmpty()) {
                ArrayList<String> alterationEntries = new ArrayList<String>(Arrays.asList(line.split(" "))); // split the line by " " to get the single alteration entries, e.g. "1 0 -1" -> ["1", "0", "-1"]

                for (String ornamentName : ornamentNames) {                      // for all currently collected ornament names, add the alteration to the lookUp table
                    ornamentLookup.put(ornamentName, new ArrayList<String>());
                    List<String> lookUp = ornamentLookup.get(ornamentName);
                    for(String alterationEntry : alterationEntries) {            // cleanUp the alteration entries, e.g. ["", "1 "] -> ["1"]
                        if(!alterationEntry.isEmpty())
                            lookUp.add(alterationEntry.trim());
                    }
                }
            }
        }

        // close readers and input stream
        br.close();
        ir.close();
        is.close();
    }
}
