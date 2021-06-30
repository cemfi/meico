package meico.app.gui;

import meico.mei.Helper;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * This is a wrapper around image datatype, only for the purpose of integration with the meico GUI, i.e. its visualization as a data object.
 * @author Axel Berndt
 */
class ImageData {
    private File file;                  // the file
    private BufferedImage image;        // the image

    /**
     * constructor
     */
    public ImageData(BufferedImage image, File file) {
        this.file = file;
        this.image = image;
    }

    /**
     * constructor
     * @param file
     * @throws IOException
     */
    public ImageData(File file) throws IOException {
        this.file = file;
        this.image = ImageIO.read(file);

    }

    /**
     * a getter for the file
     * @return
     */
    public File getFile() {
        return this.file;
    }

    /**
     * write image to file
     * @return
     */
    public boolean writeImageData() {
        try {
            return ImageIO.write(image, "png", new File(Helper.getFilenameWithoutExtension(this.file.getAbsolutePath() + ".png")));
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * another file writer with a specific filename
     * @param filename
     * @return
     */
    public boolean writeImageData(String filename) {
        try {
            return ImageIO.write(image, "png", new File(filename));
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * a getter for the image
     * @return
     */
    public BufferedImage getImage() {
        return this.image;
    }
}
