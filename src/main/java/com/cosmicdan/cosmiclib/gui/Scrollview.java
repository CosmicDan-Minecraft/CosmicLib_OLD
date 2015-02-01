package com.cosmicdan.cosmiclib.gui;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;

/**
 * Scrollview GUIScreen. When extending this class, things work a little differently to extending a GUIScreen - many 
 * GUIScreen methods either don't need to be overridden or should not be at all. Overriding required abstracts will provide 
 * all javadoc on how to use it effectively.
 * @author CosmicDan
 *
 */
public abstract class Scrollview extends GuiScreen {

    // constructed (set once when created)
    final List<RowEntry> rowEntries = new ArrayList<RowEntry>();
    final RenderItem itemRender;
    
    // initialized (set once per draw)
    private int contentX;
    private int contentY;
    private int contentWidth;
    private int contentHeight;
    private int rowHeight;
    private int startX; // each row padding start
    
    // dynamic (set many per draw)
    private int rowY = 0; // current Y-offset of row to draw on
    private int scrollY = 0;
    private int contentYsize = 0;
    
    protected Scrollview() {
        this.itemRender = Minecraft.getMinecraft().getRenderItem();
    }
    
    /*********************************
     ********************************* 
     *        Essential stuff
     *********************************
     *********************************/
    
    /**
     * Specify the padding of the scrollview port. Note that this is *not* the position of the scrollview widget itself. 
     * Padding provides a buffer zone between visual content and mouse sensitive areas; that is - the padded area will still 
     * respond to mouse hover/click actions whereas the actual content will be padded by these values. 
     * @return A <b>new int[2]</b> of the following values:
     * <ul>
     * <li>[0] = left-padding for the entire scrollview</li>
     * <li>[1] = top-padding for <i>each row</i> of the scroll view</li>
     * </ul>
     * Right padding is calculated automatically depending on size, and bottom padding for each row is determined by row size. 
     * See {@link #scrollviewAttr()}.
     */
    protected abstract int[] scrollviewPadding();
    
    
    /**
     * Specify some attributes (dimensions and row size) of the scrollview port, and row size. For your convenience, this array 
     * will be forwarded to onDraw to save recalculating dimensions for each render pass.
     * @return A <b>new int[5]</b> of the following values:
     * <ul>
     * <li>[0] = posX. Left-side position of the scrolling viewport.</li>
     * <li>[1] = posY. Top-side position of the scrolling viewport.</li>
     * <li>[2] = width. Total width of the scrolling viewport (inclusive of scroll bar)</li>
     * <li>[3] = height. Total height of the scrolling viewport</li>
     * <li>[4] = rowHeight. Total Height of each row.</li>
     * </ul>
     * Note that the row content is additionally padded by [0], and [1] has no padding since each row has individual top padding 
     * position as specified in {@link #scrollviewPadding()}. Finally, the rowHeight value should be greater than the row top 
     * padding + your expected content height - the remaining number acts as row bottom-padding. 
     */
    protected abstract int[] scrollviewAttr();
    
    /**
     * Override this instead of initGui() - the Scrollview will call it for you at the correct time.
     */
    protected abstract void onCreate();
    
    /**
     * Override this instead of drawScreen - the Scrollview will call it for you at the correct time. The final attr is passed-on from your {@link #scrollviewAttr()} implementation.
     * @param mouseX
     * @param mouseY
     * @param renderPartials
     * @param attr
     */
    protected abstract void onDraw(int mouseX, int mouseY, float renderPartials, int[] attr);
    
    /**
     * Called when the Scrollview is ready to add rows. Usually you'd want to do this in a for loop or something similar.
     * 
     * @see #addTextOnlyRow(String, int)
     * @see #addItemIconAndTextRow(ItemStack, String, int)
     */
    protected abstract void onAddRows();
    
    /**
     * Called when a particular row is being hovered by the mouse cursor.
     * @param index = The row that is currently being hovered.
     */
    protected abstract void onRowHover(int index);
    
    /**
     * Add a plain-text row. Use this in a loop in {@link #onAddRows()}.
     * @param text = String to display in this row;
     * @param color = Color of this text, in the standard hex notation (i.e. 0xRRGGBB)
     */
    public void addTextOnlyRow(String text, int color) {
        fontRendererObj.drawString(text, startX, contentY * 2 + rowY + scrollY + scrollviewPadding()[1], color); // multiply by 2 because we're scaled by half
        incRow();
    }
    
    /**
     * Add a plain-text row. Use this in a loop in {@link #onAddRows()}.
     * @param item = An ItemStack of the desired item to appear before the text;
     * @param text = String to display after the Item icon;
     * @param color = Color of this text, in the standard hex notation (i.e. 0xRRGGBB)
     */
    public void addItemIconAndTextRow(ItemStack item, String text, int color) {
        itemRender.renderItemIntoGUI(item, startX, contentY * 2 + rowY + scrollY);
        GL11.glDisable(GL11.GL_LIGHTING);
        fontRendererObj.drawString(text, startX + 20, contentY * 2 + rowY + scrollY + scrollviewPadding()[1], color); // multiply by 2 because we're scaled by half
        incRow();
    }
    
    /*********************************
     ********************************* 
     *      Semi-internal stuff
     *********************************
     *********************************/
    
    /**
     * Handle mouse scroll events. 
     * I've kept this public in-case you want to override it with special behavior. Be sure to call super though!
     */
    @Override
    public void handleMouseInput() {
        int delta = Mouse.getEventDWheel(); 
        if (delta > 0) { // scroll up, increase vertical padding
            if (scrollY < 0)
                scrollY += 20;
        } else if (delta < 0) { // scroll down, decrease vertical padding
            if (scrollY > (contentHeight * 2) - contentYsize)
                scrollY -= 20;
        }
    }
    
    /** If you must override this, be sure to replicate the Scrollview logic appropriately! */
    @Override
    public void initGui() {
        onCreate();
    }
    
    /** If you must override this, be sure to replicate the Scrollview logic appropriately! */
    @Override
    public void drawScreen(int mouseX, int mouseY, float renderPartials) {
        int[] attr = scrollviewAttr();
        onDraw(mouseX, mouseY, renderPartials, attr);
        scrollviewDraw(attr[0], attr[1], attr[2], attr[3], attr[4], fontRendererObj);
        onAddRows();
        scrollviewDone();
        int hoverIndex = scrollviewGetHoveredRow(mouseX, mouseY);
        if (hoverIndex >= 0)
            onRowHover(hoverIndex);
    }
    
    /*********************************
     ********************************* 
     *      Very-internal stuff
     *********************************
     *********************************/
    
    /** Start drawing the ScrollView. Should be called after onDraw but before all other Scrollview drawing. */
    private void scrollviewDraw(int contentX, int contentY, int contentWidth, int contentHeight, int rowHeight, FontRenderer fontRendererObj) {
        this.contentX = contentX;
        this.contentY = contentY;
        this.contentWidth = contentWidth;
        this.contentHeight = contentHeight;
        this.rowHeight = rowHeight;
        this.fontRendererObj = fontRendererObj;
        this.startX = contentX * 2 + scrollviewPadding()[0];
        this.contentYsize = 0;
        // scissor only what we want to draw
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        final int scale = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight).getScaleFactor(); // big thanks to diesieben07
        GL11.glScissor(0, mc.displayHeight - (contentY + contentHeight) * scale, (contentWidth + contentX) * scale, contentHeight * scale);
        // scale the row content to half-size
        GL11.glPushMatrix();
        GL11.glScaled(0.5D, 0.5D, 1.0D);
        rowEntries.clear();
    }

    /** Finalise the scrollview drawing */
    private void scrollviewDone() {
        GL11.glPopMatrix();
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        // also reset the runtime vars
        rowY = 0;
    }
    
    /** Increment some values so the widget is properly aligned for next run */ 
    private void incRow() {
        rowY += rowHeight;
        contentYsize += rowHeight;
        rowEntries.add(new RowEntry(contentX, contentY, contentX + contentWidth, contentY + (contentYsize + scrollY) / 2));
    }
    
    /**
     * 
     * @param mouseX = mouseX, as gotten from GUIScreen
     * @param mouseY = mouseY, as gotten from GUIScreen's 
     * @return
     */
    private int scrollviewGetHoveredRow(int mouseX, int mouseY) {
        if ((mouseX < contentX) || (mouseX > contentX + contentWidth))
            return -1;
        if ((mouseY < contentY) || (mouseY > contentY + contentHeight))
            return -1;
        // mouse is on the scroll content area, search for hover row
        RowEntry rowEntry;
        for (int i = 0; i < rowEntries.size(); i++) {
            rowEntry = rowEntries.get(i);
            if ((mouseX > rowEntry.minX) && (mouseX < rowEntry.maxX)) {
                if ((mouseY > rowEntry.minY) && (mouseY < rowEntry.maxY)) {
                    return i;
                }
            }
        }
        return -1;
    }
    
    private class RowEntry {
        private int minX;
        private int minY;
        private int maxX;
        private int maxY;
        
        private RowEntry(int minX, int minY, int maxX, int maxY) {
            this.minX = minX;
            this.minY = minY;
            this.maxX = maxX;
            this.maxY = maxY;
        }
    }
}
