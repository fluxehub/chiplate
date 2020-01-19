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
        int currentPixel = Byte.toUnsignedInt(buffer.get(index)) & 1;

        int toDraw = value ^ currentPixel;

        if (value == 1 && currentPixel == 1) {
            unset = true;
        }

        this.buffer.put(index, (byte) (toDraw * 0xFF));

        return unset;
    }

    public void clear() {
        this.buffer.position(0);

        // buffer.clear() doesn't actually clear buffer
        // so fill with 0s
        // probably fast
        for (int i = 0; i < 64 * 32; ++i) {
            this.buffer.put((byte) 0);
        }

        this.buffer.position(0);
    }

    public void render() {
        this.renderer.render(this.buffer);
    }
}