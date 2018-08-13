package meico.app.gui;

import javafx.scene.effect.Glow;
import javafx.scene.layout.StackPane;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.ParsingException;
import nu.xom.xslt.XSLException;

import java.io.File;
import java.io.IOException;

/**
 * This represents XSL transforms in the meico GUI.
 * It is no replacement of nu.xom.xslt.XSLTransform which is actually used when performing an XSL transform.
 * @author Axel Berndt
 */
public class XSLTransform {
    private DataObject graphicalInstance;
    private File file;                              // the xsl file
    private nu.xom.xslt.XSLTransform transform;     // the actual xslt stylesheet
    private boolean isActive = false;               // will be set true when it is activated for transformations

    /**
     * constructor
     * @param xslt
     */
    public XSLTransform(File xslt, DataObject graphiccalInstance) throws ParsingException, IOException, XSLException {
        // The following two statements will throw Exceptions if something goes wrong. Thus, this will not appear in the workspace.
        Document stylesheet = (new Builder()).build(xslt);          // read the XSLT stylesheet
        this.transform = new nu.xom.xslt.XSLTransform(stylesheet);  // instantiate XSLTransform object from XSLT stylesheet

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
    public nu.xom.xslt.XSLTransform getTransform() {
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
