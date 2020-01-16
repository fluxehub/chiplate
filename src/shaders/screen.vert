#version 330 core

layout (location = 0) in vec3 aPos;

out vec2 texCoord;

void main() {
    gl_Position = vec4(aPos, 1.0);

    // Scale the vector positions for the texture coordinates
    float tX = 0.5 + (aPos.x / 2);
    float tY = 0.5 - (aPos.y / 2);
    texCoord = vec2(tX, tY);
}