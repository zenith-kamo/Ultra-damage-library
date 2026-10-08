#version 150

#moj_import <fog.glsl>

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;

uniform int glowColor;
uniform float glowWidth;

in float vertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;
in vec4 normal;

out vec4 fragColor;

vec3 rgbToVec3(int rgb) {
    float r = float((rgb >> 16) & 0xFF) / 255.0;
    float g = float((rgb >> 8) & 0xFF) / 255.0;
    float b = float(rgb & 0xFF) / 255.0;
    return vec3(r, g, b);
}

void main() {
    vec4 texColor = texture(Sampler0, texCoord0);
    if (glowWidth <= 0.0) {
        discard;
    }

    vec2 texel = 1.0 / vec2(textureSize(Sampler0, 0));
    bool isEdge = false;
    float edgeAlpha = texColor.a;
    for (int radius = 1; radius <= 8; radius++) {
        if (float(radius) > glowWidth) {
            break;
        }
        for (int direction = 0; direction < 8; direction++) {
            float angle = float(direction) * 0.78539816339;
            vec2 offset = vec2(cos(angle), sin(angle)) * texel * float(radius);
            float neighborAlpha = texture(Sampler0, texCoord0 + offset).a;
            if ((texColor.a >= 0.1 && neighborAlpha < 0.1)
                    || (texColor.a < 0.1 && neighborAlpha >= 0.1)) {
                isEdge = true;
                edgeAlpha = max(texColor.a, neighborAlpha);
                break;
            }
        }
        if (isEdge) {
            break;
        }
    }
    if (!isEdge) {
        discard;
    }

    vec3 glow = rgbToVec3(glowColor);
    vec4 color = vec4(glow, edgeAlpha * vertexColor.a);
    fragColor = linear_fog(color * ColorModulator, vertexDistance, FogStart, FogEnd, FogColor);
}
