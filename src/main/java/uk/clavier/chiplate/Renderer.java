package uk.clavier.chiplate;

import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.io.IOException;
import java.nio.*;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

enum ShaderType {
    VERTEX,
    FRAGMENT
}

public class Renderer {
    private long window;
    private int vaoId;
    private int vboId;
    private int eboId;
    private int shaderProgram;
    private int texture;

    public Renderer(long window) {
        this.window = window;
    }
    
    public static long createWindow(int scaleFactor) {
        // setup an error callback
        GLFWErrorCallback.createPrint(System.err).set();

        // initialize GLFW
        if ( !glfwInit() )
            throw new IllegalStateException("Unable to initialize GLFW");

        // configure GLFW
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE); // the window will not be resizable
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3); // set OpenGL version 3.3
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

        // create the window
        long window = glfwCreateWindow(64*scaleFactor, 32*scaleFactor, "Chiplate", NULL, NULL);
        if ( window == NULL )
            throw new RuntimeException("Failed to create the GLFW window");

        // get the thread stack and push a new frame
        try ( MemoryStack stack = stackPush() ) {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*

            // get the window size passed to glfwCreateWindow
            glfwGetWindowSize(window, pWidth, pHeight);

            // get the resolution of the primary monitor
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            // center the window
            glfwSetWindowPos(
                window,
                (vidmode.width() - pWidth.get(0)) / 2,
                (vidmode.height() - pHeight.get(0)) / 2
            );
        }

        // make the OpenGL context current
        glfwMakeContextCurrent(window);
        // enable v-sync
        glfwSwapInterval(1);
        // make the window visible
        glfwShowWindow(window);

        return window;
    }

	private String loadShader(ShaderType type) throws IOException {
        String toRead = "src/shaders/screen.";

        if (type == ShaderType.VERTEX) {
            toRead += "vert";
        } else {
            toRead += "frag";
        }

        return new String (Files.readAllBytes(Paths.get(toRead)));
    }

    public void createQuad() {
        // basic full-screen quad for display
        float vertices[] = {
             1.0f,  1.0f, 0.0f,  // top right
             1.0f, -1.0f, 0.0f,  // bottom right
            -1.0f, -1.0f, 0.0f,  // bottom left
            -1.0f,  1.0f, 0.0f,  // top left
        };

        // ebo for E F F I C I E N T R E N D E R I N G
        int indices[] = {
            0, 1, 3, // right triangle
            1, 2, 3  // left triangle
        };

        // create vao, vbo, ebo
        this.vaoId = glGenVertexArrays();
        glBindVertexArray(vaoId);

        this.vboId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, this.vboId);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_DYNAMIC_DRAW);

        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        glEnableVertexAttribArray(0);

        this.eboId = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, this.eboId);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_DYNAMIC_DRAW);
    }

    public void createTexture() {
        // create a base empty texture to update (needed to use the Sub function)
        ByteBuffer baseTexture = BufferUtils.createByteBuffer(64 * 32);

        texture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, texture);

        // set texture parameteres (clamp to edge, nearest neighbour scaling)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        // generate empty texture
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RED, 64, 32, 0, GL_RED, GL_UNSIGNED_BYTE, baseTexture);
    }

    public void createShaderProgram() throws IOException {
        // load shaders from file
        String vertexShaderSource = loadShader(ShaderType.VERTEX);
        String fragShaderSource = loadShader(ShaderType.FRAGMENT);

        // create/compile shaders
        int vertexShader = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexShader, vertexShaderSource);
        glCompileShader(vertexShader);

        int fragShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragShader, fragShaderSource);
        glCompileShader(fragShader);

        // link and use the shader program
        shaderProgram = glCreateProgram();
        glAttachShader(shaderProgram, vertexShader);
        glAttachShader(shaderProgram, fragShader);
        glLinkProgram(shaderProgram);
        glUseProgram(shaderProgram);

        // delete the shaders, not needed after link
        glDeleteShader(vertexShader);
        glDeleteShader(fragShader);  
    }

    public void init() throws IOException {
        GL.createCapabilities();
        this.createQuad();
        this.createTexture();
        this.createShaderProgram();

        // set base color (probably not necessary)
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
    }

    public void render(ByteBuffer buffer) {
        // clear the screen (possibly unnecessary)
        glClear(GL_COLOR_BUFFER_BIT);

        // update texture with new data and draw it
        glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, 64, 32, GL_RED, GL_UNSIGNED_BYTE, buffer);
        glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);

        // swap color buffers, poll events
        glfwSwapBuffers(window);
        glfwPollEvents();
    }

    public void end() {
        // delete rendering objects
        glDeleteBuffers(new int[] {eboId, vboId});
        glDeleteVertexArrays(vaoId);
        glDeleteTextures(texture);
        glDeleteProgram(shaderProgram);

        // free the window callbacks and destroy the window
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        // terminate GLFW and free the error callback
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }
}