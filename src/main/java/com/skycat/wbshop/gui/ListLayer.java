package com.skycat.wbshop.gui;

import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.layered.Layer;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

import java.util.ArrayList;

public class ListLayer extends Layer {
    private final ArrayList<GuiElement> listElements;
    private final int numberOfSlots = slots.length;
    /**
     * The page we're on. 1-indexed.
     */
    private int page = 1;

    public ListLayer(int height, int width, ArrayList<GuiElement> listElements) {
        super(height, width);
        this.listElements = listElements;
        refreshLayer(0);
    }

    public boolean hasNext() {
        return page * numberOfSlots < listElements.size(); // We are not showing the last element
    }

    public boolean hasPrev() {
        return page != 1; // This is not the first page
    }

    public GuiElementBuilder nextButtonBuilder() {
        return new GuiElementBuilder(Items.EMERALD_BLOCK) // TODO: use arrow head
                .setName(Text.of("Next page"))
                .setCallback(() -> {
                    if (hasNext()) {
                        nextPage();
                    }
                });
    }

    /**
     * Displays the next page of elements.
     *
     * @return {@link ListLayer#hasNext()}
     */
    public boolean nextPage() {
        clearSlots();
        refreshLayer(page * slots.length); // Start with the next page
        page++;
        return hasNext();
    }

    public GuiElementBuilder prevButtonBuilder() { // TODO: use arrow head
        return new GuiElementBuilder(Items.REDSTONE_BLOCK)
                .setName(Text.of("Previous page"))
                .setCallback(() -> {
                    if (hasPrev()) {
                        prevPage();
                    }
                });
    }

    /**
     * Displays the previous page of elements.
     *
     * @return {@link ListLayer#hasPrev()}
     */
    public boolean prevPage() {
        clearSlots();
        page--;
        refreshLayer(page * slots.length); // Start with the previous page
        return hasPrev();
    }

    private void refreshLayer(int nextElementIndex) {
        clearSlots();
        int nextSlot = getFirstEmptySlot();
        while (nextSlot != -1 && nextElementIndex < listElements.size()) {
            setSlot(nextSlot, listElements.get(nextElementIndex));

            nextElementIndex++;
            nextSlot = getFirstEmptySlot();
        }
    }

}
