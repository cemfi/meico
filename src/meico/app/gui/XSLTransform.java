package meico.app.gui;

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
class XSLTransform {
    private File file;                              // the xsl file
    private Xslt30Transformer transform;

    /**
     * constructor
     * @param xslt
     */
    public XSLTransform(File xslt) throws FileNotFoundException, SaxonApiException {
        // compile the stylesheet and get a Transformer instance (the plain Java version)
//        TransformerFactory tFactory = TransformerFactory.newInstance();
//        this.transform = tFactory.newTransformer(new StreamSource(xslt));

        // compile the stylesheet and get a Transformer instance (the Saxon version)
        this.transform = Helper.makeXslt30Transformer(xslt);

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

}
