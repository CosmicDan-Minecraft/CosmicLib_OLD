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
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.item.ItemStack;

/**
 * Scrollview-powered GUIScreen. When extending this class, use is as follows:
 * <ol>
 * <li>Call <i>scrollviewInit(...)</i> from initGui();</li>
 * <li>Call <i>scrollviewDraw(...)</i> from drawScreen(...);</li>
 * <li>Use any number/combination of <i>addTextOnlyRow(...)</i>, </i>addItemIconAndTextRow(...)</i>, etc. to add your row content in a draeScreen loop. Row incrementing is automatic;</li>
 * <li>Call <i>scrollviewDone()</i> when done adding rows;</li>
 * <li>Finally, use scrollviewGetHoveredRow(...) to determine where the mouse cursor currently is.</li>
 * </ol>
 * Refer to the javadoc for individual methods for additional information.
 * @author CosmicDan
 *
 */
public abstract class Scrollview extends GuiScreen {

    // constructed (set once when created)
    final List<RowEntry> rowEntries = new ArrayList<RowEntry>();
    final RenderItem itemRender;
    
    // initialized (set once per draw)
    protected int contentX;
    protected int contentY;
    protected int contentWidth;
    protected int contentHeight;
    protected int rowHeight;
    protected int startX; // each row padding start
    
    // dynamic (set many per draw)
    protected int rowY = 0; // current Y-offset of row to draw on
    protected int scrollY = 0;
    protected int contentYsize = 0;
    
    public Scrollview() {
        this.itemRender = Minecraft.getMinecraft().getRenderItem();
    }
    
    /**
     * Specify the padding of the scrollview port itself. 
     * @return An int[] of two elements, containing the following values in this order:
     * <ol>
     * <li>left-padding for the entire scrollview</li>
     * <li>top-padding for the scrollview</li>
     * </ol>
     * Note that this is *not* the position of the scrollview itself. Padding provides a buffer zone between visual content and mouse sensitivity; that is - the padded area will still respond to mouse hover/click actions while the actual content will be padded by these values.
     */
    protected abstract int[] scrollviewPadding();
    
    
    /**
     * Specify some attributes (dimensions and row size) of the scroll view. 
     * @return An int[] of five elements, containing the following values in this order:
     * <ol>
     * <li>posX - left-side position of the scrolling viewport (before margin)</li>
     * <li>posY - top-side position of the scrolling viewport</li>
     * <li>width - total width of the scrolling viewport (inclusive of scroll bar)</li>
     * <li>height - total height of the scrolling viewport</li>
     * <li>rowHeight - the total height of each row. This should total the top padding + expected content height + desired bottom padding</li>
     * </ol>
     * For your convenience, this array will be forwarded to onDraw.
     */
    protected abstract int[] scrollviewAttr();
    
    protected abstract void onCreate();
    
    protected abstract void onDraw(int mouseX, int mouseY, float renderPartials, int[] attr);
    
    protected abstract void onAddRows();
    
    protected abstract void onRowHover(int index);
    
    @Override
    public void initGui() {
        onCreate();
    }
    
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
    
    /**
     * Start drawing the ScrollView. Should be called as the very last GUI element in drawScreen. 
     * 
     * Don't forget to call done() after all rows are added!
     */
    public void scrollviewDraw(int contentX, int contentY, int contentWidth, int contentHeight, int rowHeight, FontRenderer fontRendererObj) {
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

    public void scrollviewDone() {
        GL11.glPopMatrix();
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        // also reset the runtime vars
        rowY = 0;
    }
    
    public void addTextOnlyRow(String text, int color) {
        fontRendererObj.drawString(text, startX, contentY * 2 + rowY + scrollY + scrollviewPadding()[1], color); // multiply by 2 because we're scaled by half
        incRow();
    }
    
    /**
     * Draw an item + text row
     */
    public void addItemIconAndTextRow(ItemStack item, String text, int color) {
        itemRender.renderItemIntoGUI(item, startX, contentY * 2 + rowY + scrollY);
        GL11.glDisable(GL11.GL_LIGHTING);
        fontRendererObj.drawString(text, startX + 20, contentY * 2 + rowY + scrollY + scrollviewPadding()[1], color); // multiply by 2 because we're scaled by half
        incRow();
    }
    
    private void incRow() {
        rowY += rowHeight;
        contentYsize += rowHeight;
        rowEntries.add(new RowEntry(contentX, contentY, contentX + contentWidth, contentY + (contentYsize + scrollY) / 2));
    }
    
    /**
     * Handle mouse scroll events. 
     * I've kpt this public in-case you want to override it with special behavior.
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
    
    public int scrollviewGetHoveredRow(int mouseX, int mouseY) {
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
