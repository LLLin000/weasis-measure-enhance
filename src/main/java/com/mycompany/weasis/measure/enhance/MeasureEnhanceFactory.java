package com.mycompany.weasis.measure.enhance;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.ui.editor.image.ImageViewerPlugin;
import org.weasis.core.ui.editor.image.MeasureToolBar;
import org.weasis.core.ui.editor.image.ViewCanvas;
import org.weasis.core.ui.editor.image.ViewerPlugin;
import org.weasis.core.ui.model.graphic.Graphic;
import org.weasis.core.ui.model.layer.LayerType;
import org.weasis.core.ui.util.Toolbar;

public class MeasureEnhanceFactory implements BundleActivator {
    private static final Logger LOGGER = LoggerFactory.getLogger(MeasureEnhanceFactory.class);
    
    private JButton calcAngleButton;
    private JButton perpBisectorButton;
    private JButton perpDistanceButton;
    private Timer retryTimer;

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        LOGGER.info("Starting Weasis Measure Enhance Plugin");
        
        // Register tools to MeasureToolBar
        List<Graphic> measureList = MeasureToolBar.getMeasureGraphicList();
        if (measureList != null) {
            safeAddTool(measureList, new CircleCenterToolGraphic());
            safeAddTool(measureList, new ContinueLineToolGraphic());
            // PerpendicularBisector added as a toolbar button instead
        }
        
        // Try to add button with retry mechanism
        retryTimer = new Timer("AngleButtonRetry", true);
        retryTimer.schedule(new TimerTask() {
            private int attempts = 0;
            @Override
            public void run() {
                attempts++;
                if (attempts > 30) { // Give up after 30 seconds
                    LOGGER.warn("Giving up on adding angle calculation button after {} attempts", attempts);
                    retryTimer.cancel();
                    return;
                }
                SwingUtilities.invokeLater(() -> {
                    if (tryAddAngleCalculationButton()) {
                        retryTimer.cancel();
                    }
                });
            }
        }, 1000, 1000); // Start after 1 second, retry every 1 second
    }
    
    private boolean tryAddAngleCalculationButton() {
        try {
            List<ViewerPlugin<?>> plugins = GuiUtils.getUICore().getViewerPlugins();
            for (ViewerPlugin<?> plugin : plugins) {
                if (plugin instanceof ImageViewerPlugin<?> imagePlugin) {
                    List<Toolbar> toolBars = imagePlugin.getSeriesViewerUI().getToolBars();
                    if (toolBars != null) {
                        for (Toolbar toolbar : toolBars) {
                            if (toolbar instanceof MeasureToolBar measureToolBar) {
                                if (calcAngleButton == null) {
                                    // Add angle calculation button
                                    calcAngleButton = new JButton("∠");
                                    calcAngleButton.setToolTipText("Calculate angle between 2 selected lines (Ctrl+Click to multi-select)");
                                    calcAngleButton.setFont(calcAngleButton.getFont().deriveFont(16f));
                                    calcAngleButton.addActionListener(e -> onCalcAngleClick());
                                    measureToolBar.add(calcAngleButton);
                                    
                                    // Add perpendicular bisector button
                                    perpBisectorButton = new JButton("⊥");
                                    perpBisectorButton.setToolTipText("Draw perpendicular bisector of selected line");
                                    perpBisectorButton.setFont(perpBisectorButton.getFont().deriveFont(16f));
                                    perpBisectorButton.addActionListener(e -> onPerpBisectorClick());
                                    measureToolBar.add(perpBisectorButton);
                                    
                                    // Add perpendicular distance measurement button
                                    perpDistanceButton = new JButton("⊥d");
                                    perpDistanceButton.setToolTipText("Measure perpendicular distance from a point to selected line");
                                    perpDistanceButton.setFont(perpDistanceButton.getFont().deriveFont(14f));
                                    perpDistanceButton.addActionListener(e -> onPerpDistanceClick());
                                    measureToolBar.add(perpDistanceButton);
                                    
                                    measureToolBar.revalidate();
                                    LOGGER.info("Added angle calculation and perpendicular bisector buttons to MeasureToolBar");
                                }
                                return true;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.debug("Retry adding buttons: {}", e.getMessage());
        }
        return false;
    }
    
    private void onCalcAngleClick() {
        try {
            List<ViewerPlugin<?>> plugins = GuiUtils.getUICore().getViewerPlugins();
            for (ViewerPlugin<?> plugin : plugins) {
                if (plugin instanceof ImageViewerPlugin<?> imagePlugin) {
                    ViewCanvas<?> view = imagePlugin.getSelectedImagePane();
                    if (view != null) {
                        AngleCalculationAction.calculateAngle(view);
                        return;
                    }
                }
            }
            JOptionPane.showMessageDialog(null, "No active view found.", "Angle Calculation", JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            LOGGER.error("Error calculating angle", e);
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Angle Calculation", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void onPerpBisectorClick() {
        try {
            List<ViewerPlugin<?>> plugins = GuiUtils.getUICore().getViewerPlugins();
            for (ViewerPlugin<?> plugin : plugins) {
                if (plugin instanceof ImageViewerPlugin<?> imagePlugin) {
                    ViewCanvas<?> view = imagePlugin.getSelectedImagePane();
                    if (view != null) {
                        PerpendicularBisectorAction.drawPerpendicularBisector(view);
                        return;
                    }
                }
            }
            JOptionPane.showMessageDialog(null, "No active view found.", "Perpendicular Bisector", JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            LOGGER.error("Error drawing perpendicular bisector", e);
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Perpendicular Bisector", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void onPerpDistanceClick() {
        try {
            List<ViewerPlugin<?>> plugins = GuiUtils.getUICore().getViewerPlugins();
            for (ViewerPlugin<?> plugin : plugins) {
                if (plugin instanceof ImageViewerPlugin<?> imagePlugin) {
                    ViewCanvas<?> view = imagePlugin.getSelectedImagePane();
                    if (view != null) {
                        PerpendicularDistanceAction.startMeasurement(view);
                        return;
                    }
                }
            }
            JOptionPane.showMessageDialog(null, "No active view found.", "Perpendicular Distance", JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            LOGGER.error("Error measuring perpendicular distance", e);
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Perpendicular Distance", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void safeAddTool(List<Graphic> list, Graphic graphic) {
        try {
            addTool(list, graphic);
        } catch (Exception e) {
            LOGGER.error("Failed to register tool: {}", graphic != null ? graphic.getClass().getName() : "null", e);
        }
    }

    private void addTool(List<Graphic> list, Graphic graphic) {
        graphic.setLayerType(LayerType.MEASURE);
        list.add(graphic);
        LOGGER.info("Registered tool: {}", graphic.getUIName());
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        LOGGER.info("Stopping Weasis Measure Enhance Plugin");
        if (retryTimer != null) {
            retryTimer.cancel();
        }
    }
}