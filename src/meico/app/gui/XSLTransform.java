package meico.app.gui;

import javafx.scene.effect.Glow;
import javafx.scene.layout.StackPane;
import meico.mei.Helper;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Xslt30Transformer;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * This represents XSL transforms in the meico GUI.
 * It is no replacement of nu.xom.xslt.XSLTransform which is actually used when performing an XSL transform.
 * @author Axel Berndt
 */
public class XSLTransform {
    private DataObject graphicalInstance;
    private File file;                              // the xsl file
    private Xslt30Transformer transform;
    private boolean isActive = false;               // will be set true when it is activated for transformations

    /**
     * constructor
     * @param xslt
     */
    public XSLTransform(File xslt, DataObject graphiccalInstance) throws FileNotFoundException, SaxonApiException {
        // compile the stylesheet and get a Transformer instance (the plain Java version)
//        TransformerFactory tFactory = TransformerFactory.newInstance();
//        this.transform = tFactory.newTransformer(new StreamSource(xslt));

        // compile the stylesheet and get a Transformer instance (the Saxon version)
        this.transform = Helper.makeXslt30Transformer(xslt);

        this.graphicalInstance = graphiccalInstance;
        this.file = xslt;
    }

    /**
     * a getter for the file
     * @return
     */
    public File getFile() {
        return this.file;
    }

    /**
     * a getter for the transform
     * @return
     */
    public Xslt30Transformer getTransform() {
        return this.transform;
    }

    /**
     * returns true as long as it is activated
     * @return
     */
    protected boolean isActive() {
        return this.isActive;
    }

    /**
     * triggers the usage of this transform
     */
    protected synchronized void activate() {
        this.isActive = true;
        StackPane p = (StackPane) this.graphicalInstance.getChildren().get(this.graphicalInstance.getChildren().size() - 1);    // make the graphical representation light up
        Glow glow = new Glow(0.8);
        p.setEffect(glow);
    }

    /**
     * when another transform is activated, this one should to be deactivated
     */
    protected synchronized void deactivate() {
        this.isActive = false;
        StackPane p = (StackPane) this.graphicalInstance.getChildren().get(this.graphicalInstance.getChildren().size() - 1);    // switch the light off
        p.setEffect(null);
    }

}
