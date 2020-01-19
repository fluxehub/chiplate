package uk.clavier.chiplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;

import org.lwjgl.PointerBuffer;
import static org.lwjgl.system.MemoryUtil.*;

import static org.lwjgl.glfw.GLFW.*;

public class App {
    final int SCALE_FACTOR = 16; 

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m ";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    private Display display;
    private CPU cpu;
    private Renderer renderer;
    private long window;
    private boolean debug;
    private HashMap<Integer, Integer> keybindings;
    private boolean shouldBreak;

    private App() throws IOException {
        this.shouldBreak = false;
        this.keybindings = new HashMap<Integer, Integer>();

        keybindings.put(GLFW_KEY_1, 0x1);
        keybindings.put(GLFW_KEY_2, 0x2);
        keybindings.put(GLFW_KEY_3, 0x3);
        keybindings.put(GLFW_KEY_4, 0xC);

        keybindings.put(GLFW_KEY_Q, 0x4);
        keybindings.put(GLFW_KEY_W, 0x5);
        keybindings.put(GLFW_KEY_E, 0x6);
        keybindings.put(GLFW_KEY_R, 0xD);
    
        keybindings.put(GLFW_KEY_A, 0x7);
        keybindings.put(GLFW_KEY_S, 0x8);
        keybindings.put(GLFW_KEY_D, 0x9);
        keybindings.put(GLFW_KEY_F, 0xE);
    
        keybindings.put(GLFW_KEY_Z, 0xA);
        keybindings.put(GLFW_KEY_X, 0x0);
        keybindings.put(GLFW_KEY_C, 0xB);
        keybindings.put(GLFW_KEY_V, 0xF);
    }

    private Memory loadProgram(String path) throws IOException {
        byte[] program = Files.readAllBytes(Paths.get(path));

        Memory ram = new Memory();
        ram.loadProgram(program);
        return ram;
    }

    private int[] debugValues(int[] previousRegisters) {
        int[] registers = this.cpu.dumpRegisters();
        System.out.println(String.format("Next PC: %04x\tNext Opcode: %04x", this.cpu.dumpPC(), this.cpu.dumpOpcode()));

        for (int i = 0; i < 16; ++i) {
            // Highlight changed registers in red
            if (registers[i] != previousRegisters[i]) {
                System.out.println(ANSI_RED + String.format("v%01x: %02x\t", i, registers[i]) + ANSI_RESET);
            } else {
                System.out.println(String.format("v%01x: %02x\t", i, registers[i]));
            }
        }

        System.out.println("");

        return registers.clone();
    }

    private void loop() {
        // Define dummy values for debugging
        int[] previousRegisters = new int[16];
        Arrays.fill(previousRegisters, 0xFF);
        
        this.shouldBreak = false;

        // stop running when it's time to close or if new file dropped
        while (!glfwWindowShouldClose(this.window) && !this.shouldBreak) {
            // update screen every cycle in debug mode
            if (debug) {
                previousRegisters = this.debugValues(previousRegisters);
                this.cpu.cycle();
            } else {
                // for loop cycle for roughly correct(tm) clock speed
                for (int i = 0; i < 9; ++i) {
                    this.cpu.cycle();
                }
            }

            this.cpu.doTimerTick();
            this.display.render();
        }
    }

    public void launch() throws IOException {
        // create and display window
        this.window = Renderer.createWindow(SCALE_FACTOR);

        // setup the input handler
        glfwSetKeyCallback(window, (win, key, scancode, action, mods) -> {
            if (action == GLFW_RELEASE) {
                if (key == GLFW_KEY_ESCAPE) {
                    glfwSetWindowShouldClose(win, true);
                } else if (this.keybindings.containsKey(key)) {
                    // set keyup
                    this.cpu.setKey(-1);
                }
            }

            if (action == GLFW_PRESS) {
                Integer chipKey = this.keybindings.get(key);

                // will be null if key doesn't exist
                if (chipKey != null) {
                    // translate key to chip8 input
                    this.cpu.setKey(chipKey);
                }
            }
        });

        // setup file drop handler
        glfwSetDropCallback(window, (win, count, paths) -> {
            if (count > 0) {
                this.shouldBreak = true;
                PointerBuffer nameBuffer = memPointerBuffer(paths, count);

                try {
                    this.run(memUTF8(memByteBufferNT1(nameBuffer.get(0))));
                } catch (IOException e) {
                    System.out.println("Error on file drop read");
                    System.exit(1);
                }
            }
        });

        this.renderer = new Renderer(window);
        this.renderer.init();

        // TODO: Add to classpath
        this.run("programs/SPLASH");
    }

    public void init(Memory ram) throws IOException {
        this.display = new Display(this.renderer);
        this.cpu = new CPU(ram, this.display, true);
    }

    public void run(String path) throws IOException {
        this.debug = false;

        this.init(loadProgram(path));
        this.loop();

        this.renderer.end();
    }

    public static void main(String[] args) throws IOException {
        new App().launch();
    }
}
