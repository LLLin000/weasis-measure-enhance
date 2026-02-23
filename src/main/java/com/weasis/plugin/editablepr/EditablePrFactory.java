package com.weasis.plugin.editablepr;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.SwingUtilities;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.ui.editor.image.ImageViewerPlugin;
import org.weasis.core.ui.editor.image.ViewCanvas;
import org.weasis.core.ui.editor.image.ViewerPlugin;
import org.weasis.core.ui.model.GraphicModel;
import org.weasis.core.ui.model.layer.GraphicLayer;

/**
 * Plugin to make DICOM Presentation State (PR) graphics editable.
 * 
 * When DICOM files with saved measurements are loaded, the graphics are 
 * normally locked and cannot be moved. This plugin unlocks them so users
 * can edit the measurements.
 */
public class EditablePrFactory implements BundleActivator {
    private static final Logger LOGGER = LoggerFactory.getLogger(EditablePrFactory.class);
    
    private Timer monitorTimer;

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        LOGGER.info("=== Editable PR Plugin STARTING ===");
        
        // Start monitoring for views
        monitorTimer = new Timer("EditablePR-Monitor", true);
        monitorTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> checkAndUnlockLayers());
            }
        }, 2000, 2000); // Check every 2 seconds after 2 second delay
        
        LOGGER.info("=== Editable PR Plugin STARTED ===");
    }
    
    /**
     * Check all views and unlock any locked PR layers
     */
    private void checkAndUnlockLayers() {
        try {
            var uiCore = GuiUtils.getUICore();
            if (uiCore == null) {
                return;
            }
            List<ViewerPlugin<?>> plugins = uiCore.getViewerPlugins();
            if (plugins == null) {
                return;
            }
            for (ViewerPlugin<?> plugin : plugins) {
                if (plugin instanceof ImageViewerPlugin<?> imagePlugin) {
                    List<? extends ViewCanvas<?>> panels = imagePlugin.getImagePanels();
                    if (panels != null) {
                        for (ViewCanvas<?> view : panels) {
                            if (view != null) {
                                unlockLayersInView(view);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.debug("Error checking layers: {}", e.getMessage());
        }
    }
    
    /**
     * Unlock all locked layers in a view
     */
    private void unlockLayersInView(ViewCanvas<?> view) {
        try {
            GraphicModel graphicModel = view.getGraphicManager();
            if (graphicModel == null) {
                return;
            }
            
            List<GraphicLayer> layers = graphicModel.getLayers();
            if (layers == null) {
                return;
            }
            
            for (GraphicLayer layer : layers) {
                String layerName = layer.getName();
                if (layerName == null) {
                    continue;
                }
                
                // Unlock any locked layer that is not editable
                // This covers DICOM PR layers and any other locked layers
                boolean isLocked = Boolean.TRUE.equals(layer.getLocked());
                boolean isNotSelectable = Boolean.FALSE.equals(layer.getSelectable());
                
                if (isLocked || isNotSelectable) {
                    boolean changed = false;
                    
                    if (isLocked) {
                        layer.setLocked(false);
                        changed = true;
                        LOGGER.info("Unlocked layer: {}", layerName);
                    }
                    
                    if (isNotSelectable) {
                        layer.setSelectable(true);
                        changed = true;
                        LOGGER.info("Made layer selectable: {}", layerName);
                    }
                    
                    if (Boolean.FALSE.equals(layer.getSerializable())) {
                        layer.setSerializable(true);
                        LOGGER.info("Made layer serializable: {}", layerName);
                    }
                    
                    if (changed) {
                        view.getJComponent().repaint();
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Error unlocking layers: {}", e.getMessage());
        }
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        LOGGER.info("Stopping Editable PR Plugin");
        if (monitorTimer != null) {
            monitorTimer.cancel();
            monitorTimer = null;
        }
    }
}
