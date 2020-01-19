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

        // stop running when it's time to close
        while (!glfwWindowShouldClose(this.window)) {
            // update screen every cycle in debug mode
            if (debug) {
                previousRegisters = this.debugValues(previousRegisters);
                this.cpu.cycle();
            } else {
                // for loop cycle for roughly correct(tm) clock speed
                for (int i = 0; i <= 10; ++i) {
                    this.cpu.cycle();
                }
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
                    
                    // Set keyup
                    case GLFW_KEY_1:
                    case GLFW_KEY_2:
                    case GLFW_KEY_3:
                    case GLFW_KEY_4:
                    case GLFW_KEY_Q:
                    case GLFW_KEY_W:
                    case GLFW_KEY_E:
                    case GLFW_KEY_R:
                    case GLFW_KEY_A:
                    case GLFW_KEY_S:
                    case GLFW_KEY_D:
                    case GLFW_KEY_F:
                    case GLFW_KEY_Z:
                    case GLFW_KEY_X:
                    case GLFW_KEY_C:
                    case GLFW_KEY_V:
                        this.cpu.setKey(-1);
                        break;
                    
                    default:
                        break;
                }
            }

            if (action == GLFW_PRESS) {
                switch (key) {
                    // probably faster than a hashmap(tm)
                    case GLFW_KEY_1:
                        this.cpu.setKey(0x1);
                        break;

                    case GLFW_KEY_2:
                        this.cpu.setKey(0x2);
                        break;

                    case GLFW_KEY_3:
                        this.cpu.setKey(0x3);
                        break;
                        
                    case GLFW_KEY_4:
                        this.cpu.setKey(0xC);
                        break;
                    
                    case GLFW_KEY_Q:
                        this.cpu.setKey(0x4);
                        break;
                    
                    case GLFW_KEY_W:
                        this.cpu.setKey(0x5);
                        break;
                        
                    case GLFW_KEY_E:
                        this.cpu.setKey(0x6);
                        break;
                    
                    case GLFW_KEY_R:
                        this.cpu.setKey(0xD);
                        break;
                    
                    case GLFW_KEY_A:
                        this.cpu.setKey(0x7);
                        break;
                    
                    case GLFW_KEY_S:
                        this.cpu.setKey(0x8);
                        break;
                    
                    case GLFW_KEY_D:
                        this.cpu.setKey(0x9);
                        break;
                        
                    case GLFW_KEY_F:
                        this.cpu.setKey(0xE);
                        break;
                    
                    case GLFW_KEY_Z:
                        this.cpu.setKey(0xA);
                        break;
                    
                    case GLFW_KEY_X:
                        this.cpu.setKey(0x0);
                        break;
                    
                    case GLFW_KEY_C:
                        this.cpu.setKey(0xB);
                        break;
                    
                    case GLFW_KEY_V:
                        this.cpu.setKey(0xF);
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
        byte[] program = Files.readAllBytes(Paths.get("programs/BRIX"));
        Memory ram = new Memory();
        ram.loadProgram(program);

        new App().run(ram);
    }
}
