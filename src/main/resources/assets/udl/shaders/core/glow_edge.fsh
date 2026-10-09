#version 150

#moj_import <fog.glsl>

uniform sampler2D Sampler0;
uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;
uniform vec3 glowColor1;
uniform vec3 glowColor2;
uniform vec3 glowColor3;
uniform int glowColorCount;
uniform float glowTime;
uniform int glowAnimation;
uniform float glowCycleWidth;

in float vertexDistance;
in vec2 texCoord0;

out vec4 fragColor;

void main() {
    float textureAlpha = texture(Sampler0, texCoord0).a;
    if (textureAlpha < 0.1) {
        discard;
    }

    float cyclePosition = fract(glowTime);
    bool animatedPalette = glowAnimation == 1 || glowAnimation == 2;
    float palettePosition = texCoord0.x;
    if (glowAnimation == 1) {
        palettePosition = fract((gl_FragCoord.x + gl_FragCoord.y) / glowCycleWidth - cyclePosition);
    } else if (glowAnimation == 2) {
        palettePosition = cyclePosition;
    }

    vec3 glowColor = glowColor1;
    if (glowColorCount == 2) {
        if (animatedPalette) {
            palettePosition *= 2.0;
            palettePosition = palettePosition <= 1.0 ? palettePosition : 2.0 - palettePosition;
        }
        glowColor = mix(glowColor1, glowColor2, palettePosition);
    } else if (glowColorCount >= 3) {
        if (animatedPalette) {
            float segmentPosition = palettePosition * 4.0;
            if (segmentPosition < 1.0) {
                glowColor = mix(glowColor1, glowColor2, segmentPosition);
            } else if (segmentPosition < 2.0) {
                glowColor = mix(glowColor2, glowColor3, segmentPosition - 1.0);
            } else if (segmentPosition < 3.0) {
                glowColor = mix(glowColor3, glowColor2, segmentPosition - 2.0);
            } else {
                glowColor = mix(glowColor2, glowColor1, segmentPosition - 3.0);
            }
        } else {
            float segmentPosition = palettePosition * 2.0;
            glowColor = segmentPosition < 1.0
                ? mix(glowColor1, glowColor2, segmentPosition)
                : mix(glowColor2, glowColor3, segmentPosition - 1.0);
        }
    }

    float intensity = glowAnimation == 2
        ? 0.65 + 0.35 * (0.5 + 0.5 * sin(cyclePosition * 6.2831853))
        : 1.0;
    vec4 color = vec4(glowColor * intensity, textureAlpha);
    fragColor = linear_fog(color * ColorModulator, vertexDistance, FogStart, FogEnd, FogColor);
}
