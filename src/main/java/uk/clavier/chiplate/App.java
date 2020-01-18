package uk.clavier.chiplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.lwjgl.glfw.GLFW.*;

public class App {
    final int SCALE_FACTOR = 8; 

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
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

    private byte[] debugValues(byte[] previousRegisters) {
        byte[] registers = this.cpu.dumpRegisters();
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
        byte[] previousRegisters = new byte[16];
        Arrays.fill(previousRegisters, (byte) 0xFF);

        // stop running when it's time to close
        while (!glfwWindowShouldClose(this.window)) {
            // cycle 9 times for roughly correct(tm) clock speed
            for (int i = 0; i < 9; ++i) {
                if (debug) {
                    previousRegisters = this.debugValues(previousRegisters);
                }
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

        // setup the input handler
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
        this.debug = false;
        this.init(ram);
        this.loop();
        this.renderer.end();
    }

    public static void main(String[] args) throws IOException {
        // read program from file and into ram
        byte[] program = Files.readAllBytes(Paths.get("programs/MAZE"));
        Memory ram = new Memory();
        ram.loadProgram(program);

        new App().run(ram);
    }
}
