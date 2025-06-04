package edu.kis.powp.jobs2d.command.gui;

import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.*;

import edu.kis.legacy.drawer.panel.DrawPanelController;
import edu.kis.legacy.drawer.shape.LineFactory;
import edu.kis.powp.appbase.gui.WindowComponent;
import edu.kis.powp.jobs2d.command.DriverCommand;
import edu.kis.powp.jobs2d.command.manager.CommandHistoryManager;
import edu.kis.powp.jobs2d.command.manager.DriverCommandManager;
import edu.kis.powp.jobs2d.drivers.VisitableJob2dDriver;
import edu.kis.powp.jobs2d.drivers.WorkspaceManager;
import edu.kis.powp.jobs2d.drivers.adapter.LineDriverAdapter;
import edu.kis.powp.jobs2d.features.WorkspaceFeature;
import edu.kis.powp.jobs2d.transformations.ScaleTransformationDecorator;
import edu.kis.powp.jobs2d.transformations.TransformationDecorator;
import edu.kis.powp.observer.Subscriber;

public class CommandManagerWindow extends JFrame implements WindowComponent {

    private DriverCommandManager commandManager;
    private VisitableJob2dDriver previewDriver;
    private TransformationDecorator transformationDecorator;

    private JTextArea currentCommandField;

    private String observerListString;
    private JTextArea observerListField;

    private DrawPanelController drawPanelController;

    private CommandHistoryManager commandHistoryManager;
    private JList<CommandHistoryManager.HistoryEntry> commandHistoryList;
    private DefaultListModel<CommandHistoryManager.HistoryEntry> historyListModel;

    private JFrame workspaceWindow = null;


    /**
     * 
     */
    private static final long serialVersionUID = 9204679248304669948L;

    public CommandManagerWindow(DriverCommandManager commandManager, CommandHistoryManager commandHistoryManager)
    {
        this.setTitle("Command Manager");
        this.setSize(600, 400);
        Container content = this.getContentPane();
        content.setLayout(new GridBagLayout());

        this.commandManager = commandManager;
        this.commandHistoryManager = commandHistoryManager;

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new GridBagLayout());

        GridBagConstraints leftConstraints = new GridBagConstraints();
        leftConstraints.fill = GridBagConstraints.BOTH;
        leftConstraints.weightx = 1;
        leftConstraints.gridx = 0;

        observerListField = new JTextArea("");
        observerListField.setEditable(false);
        leftConstraints.gridy = 0;
        leftConstraints.weighty = 0.3;
        leftPanel.add(observerListField, leftConstraints);
        updateObserverListField();

        currentCommandField = new JTextArea("");
        currentCommandField.setEditable(false);
        leftConstraints.gridy = 1;
        leftConstraints.weighty = 0.3;
        leftPanel.add(currentCommandField, leftConstraints);
        updateCurrentCommandField();

        JLabel historyLabel = new JLabel("Commands History:");
        leftConstraints.gridy = 2;
        leftConstraints.weighty = 0;
        leftPanel.add(historyLabel, leftConstraints);

        historyListModel = new DefaultListModel<>();
        commandHistoryList = new JList<>(historyListModel);
        commandHistoryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        commandHistoryList.setVisibleRowCount(10);

        JScrollPane scrollPane = new JScrollPane(commandHistoryList);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setPreferredSize(new Dimension(200, 300));

        leftConstraints.gridy = 3;
        leftConstraints.weighty = 0.3;
        leftPanel.add(scrollPane, leftConstraints);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridBagLayout());

        GridBagConstraints buttonConstraints = new GridBagConstraints();
        buttonConstraints.fill = GridBagConstraints.HORIZONTAL;
        buttonConstraints.weightx = 1;
        buttonConstraints.gridx = 0;

        JButton btnClearCommand = new JButton("Clear command");
        btnClearCommand.addActionListener((ActionEvent e) -> this.clearCommand());
        buttonConstraints.gridy = 0;
        buttonPanel.add(btnClearCommand, buttonConstraints);

        JButton btnClearObservers = new JButton("Delete observers");
        btnClearObservers.addActionListener((ActionEvent e) -> this.deleteObservers());
        buttonConstraints.gridy = 1;
        buttonPanel.add(btnClearObservers, buttonConstraints);

        JButton btnClearPanel = new JButton("Clear Panel");
        btnClearPanel.addActionListener((ActionEvent e) -> this.clearPanel());
        buttonConstraints.gridy = 2;
        buttonPanel.add(btnClearPanel, buttonConstraints);

        JButton btnPreviewCommand = new JButton("Preview Command");
        btnPreviewCommand.addActionListener((ActionEvent e) -> this.previewCommand());
        buttonConstraints.gridy = 3;
        buttonPanel.add(btnPreviewCommand, buttonConstraints);

        JButton btnRefreshHistory = new JButton("Refresh Commands History");
        btnRefreshHistory.addActionListener((ActionEvent e) -> this.updateCommandHistoryField());
        buttonConstraints.gridy = 4;
        buttonPanel.add(btnRefreshHistory, buttonConstraints);

        JButton btnRestoreCommand = new JButton("Restore Selected Command");
        btnRestoreCommand.addActionListener((ActionEvent e) -> this.restoreSelectedCommand());
        buttonConstraints.gridy = 5;
        buttonPanel.add(btnRestoreCommand, buttonConstraints);

        JButton btnDisplayWorkspaceWindow = new JButton("Display Workspace Window");
        btnDisplayWorkspaceWindow.addActionListener((ActionEvent e) -> this.displayWorkspaceWindow());
        buttonConstraints.gridy = 6;
        buttonPanel.add(btnDisplayWorkspaceWindow, buttonConstraints);

        leftConstraints.gridy = 4;
        leftConstraints.weighty = 0.4;
        leftPanel.add(buttonPanel, leftConstraints);

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0.2;
        c.weighty = 1;
        content.add(leftPanel, c);

        JPanel drawPanel = new JPanel();
        drawPanel.setPreferredSize(new java.awt.Dimension(800, 800));
        drawPanel.setMinimumSize(new java.awt.Dimension(800, 800));

        drawPanelController = new DrawPanelController();
        drawPanelController.initialize(drawPanel);

        previewDriver = new LineDriverAdapter(drawPanelController, LineFactory.getBasicLine(), "preview");
        transformationDecorator = new ScaleTransformationDecorator(previewDriver, 3., 3.);

        c.fill = GridBagConstraints.BOTH;
        c.gridx = 1;
        c.gridy = 0;
        c.weightx = 0.8;
        content.add(drawPanel, c);

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (workspaceWindow != null) {
                    workspaceWindow.dispose();
                    workspaceWindow = null;
                }
            }
        });
    }

    private void displayWorkspaceWindow(){
        if (this.workspaceWindow != null){
            this.workspaceWindow.setSize(100, this.getHeight());
            this.workspaceWindow.setLocation(this.getLocation().x - this.workspaceWindow.getWidth(), this.getLocation().y);
            this.workspaceWindow.setVisible(!this.workspaceWindow.isVisible());
            return;
        }
        this.workspaceWindow = new JFrame();
        this.workspaceWindow.setTitle("Workspace Window");
        this.workspaceWindow.setSize(100, this.getHeight());
        this.workspaceWindow.setLayout(new GridBagLayout());
        this.workspaceWindow.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        // First time location is slightly over the main window but with next views it's moved to the left
        this.workspaceWindow.setLocation(this.getLocation().x - this.workspaceWindow.getWidth() - 21, this.getLocation().y);
        this.workspaceWindow.setVisible(true);

        GridBagConstraints buttonConstraints = new GridBagConstraints();
        buttonConstraints.fill = GridBagConstraints.HORIZONTAL;
        buttonConstraints.weightx = 1;
        buttonConstraints.gridx = 0;

        AtomicInteger i = new AtomicInteger();
        WorkspaceManager workspaceManager = new WorkspaceManager(this.drawPanelController);
        WorkspaceFeature.getWorkspaceManager().workspaceShapes.forEach((name, shape) -> {
            JButton btnDrawWorkspace = new JButton(name);
            btnDrawWorkspace.addActionListener((ActionEvent e) -> workspaceManager.setWorkspaceCanvaShape(shape));
            buttonConstraints.gridy = i.getAndIncrement();
            this.workspaceWindow.add(btnDrawWorkspace, buttonConstraints);
        });
    }

    private void clearCommand() {
        commandManager.clearCurrentCommand();
        updateCurrentCommandField();
    }

    public void updateCurrentCommandField() {
        currentCommandField.setText(commandManager.getCurrentCommandString());
    }

    public void deleteObservers() {
        commandManager.getChangePublisher().clearObservers();
        this.updateObserverListField();
    }

    private void clearPanel() {
        if (drawPanelController != null) {
            drawPanelController.clearPanel();
        }
    }

    private void previewCommand() {
        DriverCommand currentCommand = commandManager.getCurrentCommand();

        if (currentCommand != null) {
            clearPanel();

            currentCommand.execute(previewDriver);
        }
    }

    private void updateObserverListField() {
        observerListString = "";
        List<Subscriber> commandChangeSubscribers = commandManager.getChangePublisher().getSubscribers();
        for (Subscriber observer : commandChangeSubscribers) {
            observerListString += observer.toString() + System.lineSeparator();
        }
        if (commandChangeSubscribers.isEmpty())
            observerListString = "No observers loaded";

        observerListField.setText(observerListString);
    }

    public void updateCommandHistoryField() {
        List<CommandHistoryManager.HistoryEntry> history = commandHistoryManager.getHistory();
        historyListModel.clear();
        for (CommandHistoryManager.HistoryEntry entry : history) {
            historyListModel.addElement(entry);
        }
    }

    private void restoreSelectedCommand() {
        CommandHistoryManager.HistoryEntry selectedEntry = commandHistoryList.getSelectedValue();
        if (selectedEntry != null) {
            commandManager.setCurrentCommand(selectedEntry.getCommand());
            updateCurrentCommandField();
        }
    }

    @Override
    public void HideIfVisibleAndShowIfHidden() {
        updateObserverListField();
        this.setVisible(!this.isVisible());
    }

}
