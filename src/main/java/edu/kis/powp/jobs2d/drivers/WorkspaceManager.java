package edu.kis.powp.jobs2d.drivers;

import edu.kis.legacy.drawer.panel.DrawPanelController;
import edu.kis.legacy.drawer.shape.LineFactory;
import edu.kis.powp.jobs2d.Job2dDriver;
import edu.kis.powp.jobs2d.drivers.adapter.LineDriverAdapter;
import edu.kis.powp.jobs2d.canva.shapes.CanvaShape;
import edu.kis.powp.jobs2d.features.DrawerFeature;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages the current drawing workspace by handling the canvas shape and its visual representation.
 * <p>
 * This class allows setting a {@link CanvaShape} to represent the workspace area,
 * and automatically renders its border using a special {@link Job2dDriver}.
 */
public class WorkspaceManager {
    private CanvaShape currentCanvaShape;
    private final Job2dDriver borderDriver;
    public Map<String, CanvaShape> workspaceShapes = new HashMap<>();

    /**
     * Constructs a new {@code WorkspaceManager} with a preconfigured border drawing driver.
     * <p>
     * The border driver uses a dotted line and is labeled "border".
     */
    public WorkspaceManager() {
        borderDriver = new LineDriverAdapter(
                DrawerFeature.getDrawerController(),
                LineFactory.getDottedLine(),
                "border"
        );
    }

    /**
     * Sets the current workspace canvas shape and draws its border.
     * <p>
     * This method is synchronized to prevent concurrent modifications to the workspace shape.
     *
     * @param canvaShape the {@link CanvaShape} to be used as the current workspace area
     */
    public synchronized void setWorkspaceCanvaShape(CanvaShape canvaShape) {
        this.currentCanvaShape = canvaShape;
        canvaShape.draw(borderDriver);
    }

    /**
     * Returns the current canvas shape set for the workspace.
     *
     * @return the currently active {@link CanvaShape}, or {@code null} if none is set
     */
    public CanvaShape getCurrentCanvaShape() {
        return currentCanvaShape;
    }
}
