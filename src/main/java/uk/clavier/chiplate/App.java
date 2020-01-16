package uk.clavier.chiplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.lwjgl.glfw.GLFW.*;

public class App {
    final int SCALE_FACTOR = 16; 

    private Display display;
    private CPU cpu;
    private Renderer renderer;
    private long window;

    private void loop() {
        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.
        while (!glfwWindowShouldClose(this.window)) {
            // Cycle 9 times for roughly correct clock speed
            for (int i = 0; i < 9; ++i) {
                this.cpu.cycle();
            }

            this.cpu.doTimerTick();
            this.display.render();
        }
    }

    public void init(Memory ram) throws IOException {
        this.window = Renderer.createWindow(SCALE_FACTOR);
        this.renderer = new Renderer(window);
        this.renderer.init();

        // Setup the input handler
        glfwSetKeyCallback(window, (win, key, scancode, action, mods) -> {
            if (action == GLFW_RELEASE) {
                switch (key) {
                    case GLFW_KEY_ESCAPE:
                        glfwSetWindowShouldClose(win, true);
                        break;
                    
                    default:
                        break;
                }
            }
        });
        
        this.display = new Display(this.renderer);
        this.cpu = new CPU(ram, this.display, true);
    }

    public void run(Memory ram) throws IOException {
        this.init(ram);
        this.loop();
        this.renderer.end();
    }

    public static void main(String[] args) throws IOException {
        byte[] program = Files.readAllBytes(Paths.get("programs/test_opcode.ch8"));
        Memory ram = new Memory();

        byte[] test = {
            (byte) 0x00, (byte) 0xE0,
            (byte) 0xF5, (byte) 0x29,
            (byte) 0xD0, (byte) 0x05,
            (byte) 0xFA, (byte) 0x29,
            (byte) 0xD4, (byte) 0x55,
            (byte) 0x12, (byte) 0x0A,
        };

        ram.loadProgram(program);
        new App().run(ram);
    }
}
