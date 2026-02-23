package com.mycompany.weasis.measure.enhance;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.ArrayList;
import java.util.Optional;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.gui.util.ActionW;
import org.weasis.core.api.gui.util.ComboItemListener;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.ui.editor.image.ImageViewerEventManager;
import org.weasis.core.ui.editor.image.ImageViewerPlugin;
import org.weasis.core.ui.editor.image.MeasureToolBar;
import org.weasis.core.ui.editor.image.ViewCanvas;
import org.weasis.core.ui.editor.image.ViewerPlugin;
import org.weasis.core.ui.model.graphic.Graphic;
import org.weasis.core.ui.model.layer.LayerType;
import org.weasis.core.ui.util.Toolbar;
import org.weasis.dicom.codec.DicomImageElement;
import org.weasis.dicom.viewer2d.EventManager;

public class MeasureEnhanceFactory implements BundleActivator {
    private static final Logger LOGGER = LoggerFactory.getLogger(MeasureEnhanceFactory.class);
    
    private Timer monitorTimer;
    private boolean toolsRegisteredToStaticList = false;

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        LOGGER.info("Starting Weasis Measure Enhance Plugin");
        
        // Start a continuous monitor timer to handle late-loading viewers and multiple windows
        monitorTimer = new Timer("MeasureEnhance-Monitor", true);
        monitorTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> updatePluginState());
            }
        }, 2000, 3000); // Check every 3 seconds after 2s delay
        
        LOGGER.info("Weasis Measure Enhance Plugin started (monitoring active)");
    }
    
    private void updatePluginState() {
        try {
            // 1. Ensure tools are registered to the static MeasureToolBar list (for future viewers)
            if (!toolsRegisteredToStaticList) {
                List<Graphic> measureList = MeasureToolBar.getMeasureGraphicList();
                if (measureList != null) {
                    safeAddTool(measureList, new CircleCenterToolGraphic());
                    safeAddTool(measureList, new ContinueLineToolGraphic());
                    toolsRegisteredToStaticList = true;
                    LOGGER.info("Successfully registered tools to static MeasureToolBar list");
                }
            }

            // 2. Update all currently open viewers
            List<ViewerPlugin<?>> plugins = GuiUtils.getUICore().getViewerPlugins();
            if (plugins != null) {
                for (ViewerPlugin<?> plugin : plugins) {
                    if (plugin instanceof ImageViewerPlugin<?> imagePlugin) {
                        // Register tools to this plugin's event manager
                        registerToolsToEventManager(imagePlugin);

                        // Add buttons to toolbars
                        List<Toolbar> toolBars = imagePlugin.getSeriesViewerUI().getToolBars();
                        if (toolBars != null) {
                            for (Toolbar toolbar : toolBars) {
                                if (toolbar instanceof MeasureToolBar measureToolBar) {
                                    addButtonsToToolbar(measureToolBar);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.debug("Error in monitor cycle: {}", e.getMessage());
        }
    }

    private void addButtonsToToolbar(MeasureToolBar toolbar) {
        boolean changed = false;

        if (!hasButton(toolbar, "∠")) {
            JButton calcAngleButton = new JButton("∠");
            calcAngleButton.setToolTipText("Calculate angle between 2 selected lines (Ctrl+Click to multi-select)");
            calcAngleButton.setFont(calcAngleButton.getFont().deriveFont(16f));
            calcAngleButton.addActionListener(e -> onCalcAngleClick());
            toolbar.add(calcAngleButton);
            changed = true;
        }

        if (!hasButton(toolbar, "⊥")) {
            JButton perpBisectorButton = new JButton("⊥");
            perpBisectorButton.setToolTipText("Draw perpendicular bisector of selected line");
            perpBisectorButton.setFont(perpBisectorButton.getFont().deriveFont(16f));
            perpBisectorButton.addActionListener(e -> onPerpBisectorClick());
            toolbar.add(perpBisectorButton);
            changed = true;
        }

        if (!hasButton(toolbar, "⊥d")) {
            JButton perpDistanceButton = new JButton("⊥d");
            perpDistanceButton.setToolTipText("Measure perpendicular distance from a point to selected line");
            perpDistanceButton.setFont(perpDistanceButton.getFont().deriveFont(14f));
            perpDistanceButton.addActionListener(e -> onPerpDistanceClick());
            toolbar.add(perpDistanceButton);
            changed = true;
        }

        if (!hasButton(toolbar, "//")) {
            JButton parallelLineButton = new JButton("//");
            parallelLineButton.setToolTipText("Create parallel line from selected line (可拖动调整距离)");
            parallelLineButton.setFont(parallelLineButton.getFont().deriveFont(14f));
            parallelLineButton.addActionListener(e -> onParallelLineClick());
            toolbar.add(parallelLineButton);
            changed = true;
        }

        if (!hasButton(toolbar, "1/3⊥")) {
            JButton trisectionPerpButton = new JButton("1/3⊥");
            trisectionPerpButton.setToolTipText("Draw perpendiculars at 1/3 and 2/3 points of selected line");
            trisectionPerpButton.setFont(trisectionPerpButton.getFont().deriveFont(13f));
            trisectionPerpButton.addActionListener(e -> onTrisectionPerpClick());
            toolbar.add(trisectionPerpButton);
            changed = true;
        }

        if (changed) {
            toolbar.revalidate();
            toolbar.repaint();
            LOGGER.info("Added measure enhance buttons to a MeasureToolBar instance");
        }
    }

    private boolean hasButton(MeasureToolBar toolbar, String text) {
        for (java.awt.Component c : toolbar.getComponents()) {
            if (c instanceof JButton b && text.equals(b.getText())) {
                return true;
            }
        }
        return false;
    }
    
    private void registerToolsToEventManager(ImageViewerPlugin<?> imagePlugin) {
        try {
            ImageViewerEventManager<?> eventManager = imagePlugin.getEventManager();
            if (eventManager != null) {
                Optional<ComboItemListener<Graphic>> actionOpt = eventManager.getAction(ActionW.DRAW_MEASURE);
                if (actionOpt.isPresent()) {
                    ComboItemListener<Graphic> action = actionOpt.get();
                    addToolsToAction(action);
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to register tools to event manager", e);
        }
    }

    private void addToolsToAction(ComboItemListener<Graphic> action) {
        try {
            Object[] items = action.getAllItem();
            if (items == null) return;

            List<Graphic> itemList = new ArrayList<>();
            for (Object item : items) {
                if (item instanceof Graphic) {
                    itemList.add((Graphic) item);
                }
            }
            
            boolean changed = false;
            
            // Check and add CircleCenterToolGraphic
            boolean hasCircle = false;
            for (Graphic item : itemList) {
                if (item instanceof CircleCenterToolGraphic) {
                    hasCircle = true;
                    break;
                }
            }
            if (!hasCircle) {
                itemList.add(new CircleCenterToolGraphic());
                changed = true;
            }
            
            // Check and add ContinueLineToolGraphic
            boolean hasLine = false;
            for (Graphic item : itemList) {
                if (item instanceof ContinueLineToolGraphic) {
                    hasLine = true;
                    break;
                }
            }
            if (!hasLine) {
                itemList.add(new ContinueLineToolGraphic());
                changed = true;
            }
            
            if (changed) {
                action.setDataList(itemList.toArray(new Graphic[0]));
                LOGGER.info("Updated existing viewer with new tools");
            }
            
        } catch (Exception e) {
            LOGGER.warn("Could not update action tools: {}", e.getMessage());
        }
    }

    private void onCalcAngleClick() {
        try {
            ViewCanvas<?> view = getActiveViewCanvas();
            if (view != null) {
                AngleCalculationAction.calculateAngle(view);
            } else {
                JOptionPane.showMessageDialog(null, "No active view found.", "Angle Calculation", JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception e) {
            LOGGER.error("Error calculating angle", e);
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Angle Calculation", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void onPerpBisectorClick() {
        try {
            ViewCanvas<?> view = getActiveViewCanvas();
            if (view != null) {
                PerpendicularBisectorAction.drawPerpendicularBisector(view);
            } else {
                JOptionPane.showMessageDialog(null, "No active view found.", "Perpendicular Bisector", JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception e) {
            LOGGER.error("Error drawing perpendicular bisector", e);
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Perpendicular Bisector", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void onPerpDistanceClick() {
        try {
            ViewCanvas<?> view = getActiveViewCanvas();
            if (view != null) {
                PerpendicularDistanceAction.startMeasurement(view);
            } else {
                JOptionPane.showMessageDialog(null, "No active view found.", "Perpendicular Distance", JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception e) {
            LOGGER.error("Error measuring perpendicular distance", e);
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Perpendicular Distance", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void onParallelLineClick() {
        try {
            ViewCanvas<?> view = getActiveViewCanvas();
            if (view != null) {
                ParallelLineAction.createParallelLine(view);
            } else {
                JOptionPane.showMessageDialog(null, "No active view found.", "Create Parallel Line", JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception e) {
            LOGGER.error("Error creating parallel line", e);
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Create Parallel Line", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onTrisectionPerpClick() {
        try {
            ViewCanvas<?> view = getActiveViewCanvas();
            if (view != null) {
                TrisectionPerpendicularAction.drawTrisectionPerpendiculars(view);
            } else {
                JOptionPane.showMessageDialog(null, "No active view found.", "Trisection Perpendiculars", JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception e) {
            LOGGER.error("Error drawing trisection perpendiculars", e);
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Trisection Perpendiculars", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Get the currently active/focused view canvas.
     * This correctly handles multiple patient windows by using EventManager.
     */
    private ViewCanvas<?> getActiveViewCanvas() {
        // First try to get from EventManager (handles DICOM viewer correctly)
        try {
            ImageViewerPlugin<DicomImageElement> container = EventManager.getInstance().getSelectedView2dContainer();
            if (container != null) {
                ViewCanvas<DicomImageElement> view = container.getSelectedImagePane();
                if (view != null) {
                    return view;
                }
            }
        } catch (Exception e) {
            LOGGER.debug("Could not get view from EventManager: {}", e.getMessage());
        }
        
        // Fallback: iterate through plugins and find the one with focus
        List<ViewerPlugin<?>> plugins = GuiUtils.getUICore().getViewerPlugins();
        for (ViewerPlugin<?> plugin : plugins) {
            if (plugin instanceof ImageViewerPlugin<?> imagePlugin) {
                // Check if this plugin has focus
                if (imagePlugin.isShowing() && imagePlugin.hasFocus()) {
                    ViewCanvas<?> view = imagePlugin.getSelectedImagePane();
                    if (view != null) {
                        return view;
                    }
                }
            }
        }
        
        // Last resort: return any available view (original behavior)
        for (ViewerPlugin<?> plugin : plugins) {
            if (plugin instanceof ImageViewerPlugin<?> imagePlugin) {
                ViewCanvas<?> view = imagePlugin.getSelectedImagePane();
                if (view != null) {
                    return view;
                }
            }
        }
        
        return null;
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
        if (monitorTimer != null) {
            monitorTimer.cancel();
        }
    }
}
