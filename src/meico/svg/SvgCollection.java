package meico.svg;

import meico.mei.Helper;

import java.util.ArrayList;

/**
 * This class interfaces a collection of SVGs.
 * One such SVG is one page of the score. This class comprizes the whole score.
 *
 * @author Axel Berndt
 */

public class SvgCollection {
    protected ArrayList<Svg> svgs = new ArrayList<>();      // an ArrayList of all score pages
    protected String title = "";

    /**
     * constructor
     */
    public SvgCollection() {
    }

    /**
     * constructor
     * @param svgs
     */
    public SvgCollection(ArrayList<Svg> svgs) {
        this.svgs = svgs;
    }

    /**
     * set a title for this collection
     * @param title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * get the title of this collection
     * @return
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * returns the number of SVGs in this collection
     * @return
     */
    public int size() {
        return this.svgs.size();
    }

    /**
     * add an SVG to the collection
     * @param svg
     */
    public void add(Svg svg) {
        this.svgs.add(svg);
    }

    /**
     * remove an SVG from the collection
     * @param index
     * @return the Svg that was removed from the list
     */
    public Svg removeSvg(int index) {
        return this.svgs.remove(index);
    }

    /**
     * remove an SVG from the collection
     * @param svg
     * @return true if this list contained the specified element
     */
    public boolean removeSvg(Svg svg) {
        return this.svgs.remove(svg);
    }

    /**
     * is there some data in this collection?
     * @return
     */
    public boolean isEmpty() {
        return this.svgs.size() == 0;
    }

    /**
     * access the element at the specified index
     * @param index
     * @return
     */
    public Svg get(int index) {
        return this.svgs.get(index);
    }

    /**
     * a getter to access the collection of SVGs
     * @return
     */
    public ArrayList<Svg> getSvgs() {
        return this.svgs;
    }

    /**
     * writes the SVGs to files;
     * if there is already a file with this name, it is replaces!
     *
     * @return true if all files could be written, false if an error occured
     */
    public boolean writeSvgs() {
        boolean success = true;

        for (Svg svg : this.svgs)
            success = success && svg.writeSvg();

        return success;
    }

    /**
     * writes the SVGs to files (filename should include the path and the extension, page numbers will be added automatically)
     *
     * @param filename the filename string; it should include the path and the extension, page numbers will be added automatically
     * @return
     */
    public boolean writeSvgs(String filename) {
        String name = Helper.getFilenameWithoutExtension(filename);
        String extension = filename.substring(filename.lastIndexOf('.'));

        boolean success = true;

        for (int i=0; i < this.svgs.size(); ++i)
            success = success && svgs.get(i).writeSvg(name + "-" + String.format("%04d", i) + extension);    // add the page number to the filename, format it as a 4-digit number with leading zeros

        return success;
    }
}
