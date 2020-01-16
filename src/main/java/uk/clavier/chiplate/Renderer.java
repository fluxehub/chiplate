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
        // Setup an error callback. The default implementation
		// will print the error message in System.err.
		GLFWErrorCallback.createPrint(System.err).set();

		// Initialize GLFW. Most GLFW functions will not work before doing this.
		if ( !glfwInit() )
			throw new IllegalStateException("Unable to initialize GLFW");

		// Configure GLFW
		glfwDefaultWindowHints(); // optional, the current window hints are already the default
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
		glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE); // the window will be resizable

		// Create the window
		long window = glfwCreateWindow(64*scaleFactor, 32*scaleFactor, "Chiplate", NULL, NULL);
		if ( window == NULL )
			throw new RuntimeException("Failed to create the GLFW window");

		// Setup a key callback. It will be called every time a key is pressed, repeated or released.
		glfwSetKeyCallback(window, (lWindow, key, scancode, action, mods) -> {
			if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE )
				glfwSetWindowShouldClose(lWindow, true); // We will detect this in the rendering loop
		});

		// Get the thread stack and push a new frame
		try ( MemoryStack stack = stackPush() ) {
			IntBuffer pWidth = stack.mallocInt(1); // int*
			IntBuffer pHeight = stack.mallocInt(1); // int*

			// Get the window size passed to glfwCreateWindow
			glfwGetWindowSize(window, pWidth, pHeight);

			// Get the resolution of the primary monitor
			GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

			// Center the window
			glfwSetWindowPos(
				window,
				(vidmode.width() - pWidth.get(0)) / 2,
				(vidmode.height() - pHeight.get(0)) / 2
			);
		} // the stack frame is popped automatically

		// Make the OpenGL context current
        glfwMakeContextCurrent(window);
		// Enable v-sync
		glfwSwapInterval(1);
		// Make the window visible
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

		return Files.readString(Paths.get(toRead));
    }

    public void init() throws IOException {
        GL.createCapabilities();

        // set the clear color
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        // basic full-screen quad
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
        vaoId = glGenVertexArrays();
        glBindVertexArray(vaoId);

        vboId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_DYNAMIC_DRAW);

        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        glEnableVertexAttribArray(0);
        
        int eboId = glGenBuffers();
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboId);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_DYNAMIC_DRAW);
        
        // Create a base texture to update
        ByteBuffer baseTexture = BufferUtils.createByteBuffer(64 * 32);

        texture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, texture);
        
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RED, 64, 32, 0, GL_RED, GL_UNSIGNED_BYTE, baseTexture);
        
        // load shaders
        String vertexShaderSource = loadShader(ShaderType.VERTEX);
		String fragShaderSource = loadShader(ShaderType.FRAGMENT);
        
        int vertexShader = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexShader, vertexShaderSource);
        glCompileShader(vertexShader);
        
        int fragShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragShader, fragShaderSource);
        glCompileShader(fragShader);

        shaderProgram = glCreateProgram();
        glAttachShader(shaderProgram, vertexShader);
        glAttachShader(shaderProgram, fragShader);
		glLinkProgram(shaderProgram);
		
        glDeleteShader(vertexShader);
        glDeleteShader(fragShader);  
        
        glUseProgram(shaderProgram);
    }

    public void render(ByteBuffer buffer) {
        // update texture with new data and draw it
        glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, 64, 32, GL_RED, GL_UNSIGNED_BYTE, buffer);
        glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);

        // swap color buffers, poll events
        glfwSwapBuffers(window);
        glfwPollEvents();
    }

    public void end() {
        // Delete rendering objects
        glDeleteBuffers(new int[] {eboId, vboId});
        glDeleteVertexArrays(vaoId);
        glDeleteTextures(texture);
        glDeleteProgram(shaderProgram);

        // Free the window callbacks and destroy the window
		glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

		// Terminate GLFW and free the error callback
		glfwTerminate();
		glfwSetErrorCallback(null).free();
    }
}