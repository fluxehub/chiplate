#version 330 core 

out vec4 color;
in vec2 texCoord;

uniform sampler2D screen;

void main() {
    // monochrome display, just read the red channel
    float luminance = texture(screen, texCoord).r;
    color = vec4(luminance, luminance, luminance, 1.0);
}