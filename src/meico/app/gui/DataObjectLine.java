package meico.app.gui;

import javafx.geometry.Point2D;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;

/**
 * this represents the parent-child relation of two data objects
 * @author Axel Berndt
 */
public class DataObjectLine extends Line {
    private DataObject parent;
    private DataObject child;

    /**
     * constructor
     * @param parent
     * @param child
     */
    public DataObjectLine(DataObject parent, DataObject child) {
        super();
        this.parent = parent;
        this.child = child;
        this.draw();
    }

    /**
     * A line connects two data objects. This method returns the partner of the specified object or null if the object is not part of this line.
     * @param object
     * @return
     */
    public synchronized DataObject getPartner(DataObject object) {
        return (object == this.parent) ? this.child : ((object == this.child) ? this.parent : null);
    }

    /**
     * When the specified object has been moved (e.g. via drag interaction), this method adapts the connecting line.
     */
    protected synchronized void draw() {
        Point2D center = this.parent.getCenterPoint();
        Point2D target = this.child.getCenterPoint();

        Point2D start = target.subtract(center).normalize().multiply(Settings.dataItemRadius).add(center);
        this.setStartX(start.getX());
        this.setStartY(start.getY());

        Point2D end = center.subtract(target).normalize().multiply(Settings.dataItemRadius).add(target);
        this.setEndX(end.getX());
        this.setEndY(end.getY());

        LinearGradient linearGradient = new LinearGradient(start.getX(), start.getY(), end.getX(), end.getY(), false, CycleMethod.NO_CYCLE, new Stop(0.6,this.parent.getColor()), new Stop(1, this.child.getColor()));
        this.setStroke(linearGradient);

        this.setStrokeWidth(Settings.dataItemStrokeWidth * 2.5);
        this.setOpacity(Settings.dataItemOpacity);
        this.setStrokeLineCap(StrokeLineCap.BUTT);
    }
}
