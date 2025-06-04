package edu.kis.powp.jobs2d.features;

import edu.kis.powp.appbase.Application;

import edu.kis.powp.jobs2d.drivers.SelectWorkspaceMenuOptionListener;
import edu.kis.powp.jobs2d.drivers.WorkspaceManager;
import edu.kis.powp.jobs2d.canva.shapes.CanvaShape;
import edu.kis.powp.jobs2d.plugin.FeaturePlugin;

/**
 * Provides functionality for integrating workspace shape selection into the application UI.
 * <p>
 * This class is responsible for setting up a "Workspaces" menu and allowing the user to add and select
 * predefined {@link CanvaShape} instances through menu options. It holds a shared {@link WorkspaceManager}
 * instance to manage the currently active workspace shape.
 */
public class WorkspaceFeature implements FeaturePlugin {
    private static Application app;
    private static WorkspaceManager workspaceManager;

    /**
     * Initializes the workspace plugin by adding a "Workspaces" component menu to the application.
     *
     * @param application the main {@link Application} instance to which the plugin is attached
     */
    public static void setupWorkspacePlugin(Application application) {
        workspaceManager = new WorkspaceManager();
        app = application;
        app.addComponentMenu(WorkspaceFeature.class, "Workspaces");
    }

    @Override
    public void setup(Application application) {
        setupWorkspacePlugin(application);
    }

    /**
     * Returns the shared {@link WorkspaceManager} instance used by the workspace feature.
     *
     * @return the {@code WorkspaceManager} managing the current canvas shape
     */
    public static WorkspaceManager getWorkspaceManager() {
        return workspaceManager;
    }

    /**
     * Adds a selectable workspace shape option to the "Workspaces" menu.
     * When selected, the shape will be applied to the workspace.
     *
     * @param name  the display name of the menu option
     * @param shape the {@link CanvaShape} to apply when the menu option is selected
     */
    public static void addWorkspaceShape(String name, CanvaShape shape) {
        SelectWorkspaceMenuOptionListener listener = new SelectWorkspaceMenuOptionListener(shape, workspaceManager);
        app.addComponentMenuElement(WorkspaceFeature.class, name, listener);
    }
}
