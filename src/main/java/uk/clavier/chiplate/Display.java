package uk.clavier.chiplate;

import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;

public class Display {
    private ByteBuffer buffer;
    private Renderer renderer;

    public Display(Renderer renderer) {
        this.buffer = BufferUtils.createByteBuffer(64 * 32);
        this.renderer = renderer;
    }

    public boolean setPixel(int x, int y, byte value) {
        boolean unset = false;
        int index = x + (y * 64);
        byte currentPixel = this.buffer.get(index);

        if (currentPixel == (byte) 0xFF) {
            unset = true;
        }

        byte toDraw;

        if (value == 1) {
            toDraw = (byte) 0xFF;
        } else {
            toDraw = 0x00;
        }

        this.buffer.put(index, toDraw);

        return unset;
    }

    public void clear() {
        this.buffer.clear();
    }

    public void render() {
        this.renderer.render(this.buffer);
    }
}