package com.example.Signal.utils;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.dnd.DragSource;
import com.vaadin.flow.component.dnd.DropEffect;
import com.vaadin.flow.component.dnd.DropTarget;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

/**
 * Utility class for enabling drag-and-drop reordering functionality on Vaadin components.
 * Provides visual feedback during drag operations with drop indicators showing insertion points.
 */
public class DragAndDropUtil {

    private DragAndDropUtil() {
        // Private constructor to prevent instantiation
    }

    /**
     * Enables drag-and-drop reordering for all child components within a HorizontalLayout.
     * Components can be dragged and dropped to reorder them within the same container.
     * Visual indicators show where the dragged component will be inserted.
     *
     * @param container The HorizontalLayout container whose children should be made draggable
     */
    public static void enableReordering(HorizontalLayout container) {
        Component[] draggedComponent = new Component[1]; // Array to hold reference in lambda

        container.getChildren().forEach(component -> {
            // Make component draggable
            DragSource<Component> dragSource = DragSource.create(component);
            dragSource.setDraggable(true);

            dragSource.addDragStartListener(event -> {
                draggedComponent[0] = component;
                component.getElement().getStyle().set("opacity", "0.5");
                component.addClassName("dragging");
            });

            dragSource.addDragEndListener(event -> {
                component.getElement().getStyle().remove("opacity");
                component.removeClassName("dragging");
                // Remove all drop indicators
                container.getChildren().forEach(c -> {
                    c.removeClassName("drop-indicator-left");
                    c.removeClassName("drop-indicator-right");
                });
                draggedComponent[0] = null;
            });

            // Make component a drop target
            DropTarget<Component> dropTarget = DropTarget.create(component);
            dropTarget.setDropEffect(DropEffect.MOVE);

            // Use JavaScript to handle dragover for visual feedback
            component.getElement().addEventListener("dragover", event -> {
                if (draggedComponent[0] != null && draggedComponent[0] != component) {
                    int draggedIndex = container.indexOf(draggedComponent[0]);
                    int targetIndex = container.indexOf(component);

                    // Remove previous indicators
                    container.getChildren().forEach(c -> {
                        c.removeClassName("drop-indicator-left");
                        c.removeClassName("drop-indicator-right");
                    });

                    // Determine which side to show the indicator
                    // If target is first item and dragging from right, always show left
                    // Otherwise show right for left-to-right, left for right-to-left
                    if (targetIndex == 0) {
                        component.addClassName("drop-indicator-left");
                    } else if (draggedIndex < targetIndex) {
                        component.addClassName("drop-indicator-right");
                    } else {
                        component.addClassName("drop-indicator-left");
                    }
                }
            }).synchronizeProperty("dragover");

            // Remove indicator on dragleave
            component.getElement().addEventListener("dragleave", event -> {
                component.removeClassName("drop-indicator-left");
                component.removeClassName("drop-indicator-right");
            });

            // Handle the drop
            dropTarget.addDropListener(event -> event.getDragSourceComponent().ifPresent(dragged -> {
                // Remove all indicators
                container.getChildren().forEach(c -> {
                    c.removeClassName("drop-indicator-left");
                    c.removeClassName("drop-indicator-right");
                });

                // Get current positions
                int draggedIndex = container.indexOf(dragged);
                int dropIndex = container.indexOf(component);

                if (draggedIndex != -1 && dropIndex != -1 && draggedIndex != dropIndex) {
                    // Remove dragged component
                    container.remove(dragged);
                    // Re-insert at new position
                    container.addComponentAtIndex(dropIndex, dragged);
                }
            }));
        });
    }
}
