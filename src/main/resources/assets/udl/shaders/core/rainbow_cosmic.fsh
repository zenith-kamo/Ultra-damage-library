#version 150

#define M_PI 3.1415926535897932384626433832795

#moj_import <fog.glsl>

const int cosmiccount = 10;
const int cosmicoutof = 101;
const float lightmix = 0.2f;

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;

uniform float time;
uniform float yaw;
uniform float pitch;
uniform float externalScale;
uniform float opacity;
uniform mat2 cosmicuvs[cosmiccount];

in float vertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;
in vec4 normal;
in vec3 fPos;

out vec4 fragColor;

float randomValue(vec3 seed)
{
    return fract(sin(dot(seed, vec3(127.1, 311.7, 74.7))) * 43758.5453);
}

mat4 rotationMatrix(vec3 axis, float angle)
{
    axis = normalize(axis);
    float s = sin(angle);
    float c = cos(angle);
    float oc = 1.0 - c;

    return mat4(oc * axis.x * axis.x + c,           oc * axis.x * axis.y - axis.z * s,  oc * axis.z * axis.x + axis.y * s,  0.0,
                oc * axis.x * axis.y + axis.z * s,  oc * axis.y * axis.y + c,           oc * axis.y * axis.z - axis.x * s,  0.0,
                oc * axis.z * axis.x - axis.y * s,  oc * axis.y * axis.z + axis.x * s,  oc * axis.z * axis.z + c,           0.0,
                0.0,                                0.0,                                0.0,                                1.0);
}

void main (void)
{
    vec4 mask = texture(Sampler0, texCoord0.xy);
    float oneOverExternalScale = 1.0 / externalScale;
    int uvtiles = 16;
    vec4 col = vec4(mask.rgb, 1.0);
    float pulse = mod(time, 400.0) / 400.0;

    vec4 dir = normalize(vec4(-fPos, 0.0));
    float sb = sin(pitch);
    float cb = cos(pitch);
    dir = normalize(vec4(dir.x, dir.y * cb - dir.z * sb, dir.y * sb + dir.z * cb, 0.0));
    float sa = sin(-yaw);
    float ca = cos(-yaw);
    dir = normalize(vec4(dir.z * sa + dir.x * ca, dir.y, dir.z * ca - dir.x * sa, 0.0));

    vec4 ray;
    for (int i = 0; i < 16; i++) {
        int mult = 16 - i;
        int j = i + 7;
        float rand1 = (j * j * 4321 + j * 8) * 2.0;
        int k = j + 1;
        float rand2 = (k * k * k * 239 + k * 37) * 3.6;
        float rand3 = rand1 * 347.4 + rand2 * 63.4;
        vec3 axis = normalize(vec3(sin(rand1), sin(rand2), cos(rand3)));
        ray = dir * rotationMatrix(axis, mod(rand3, 2.0 * M_PI));

        float rawu = 0.5 + (atan(ray.z, ray.x) / (2.0 * M_PI));
        float rawv = 0.5 + (asin(ray.y) / M_PI);
        float scale = mult * 0.5 + 2.75;
        float u = rawu * scale * externalScale;
        float v = (rawv + time * 0.0002 * oneOverExternalScale) * scale * 0.6 * externalScale;

        int tu = int(mod(floor(u * float(uvtiles)), float(uvtiles)));
        int tv = int(mod(floor(v * float(uvtiles)), float(uvtiles)));
        int position = ((171 * tu) + (489 * tv) + (303 * (i + 31)) + 17209) ^ 10;
        int symbol = int(mod(float(position), float(cosmicoutof)));
        int rotation = int(mod(pow(float(tu), float(tv)) + float(tu) + 3.0 + float(tv * i), 8.0));
        bool flip = false;
        if (rotation >= 4) {
            rotation -= 4;
            flip = true;
        }

        if (symbol >= 0 && symbol < cosmiccount) {
            float ru = clamp(mod(u, 1.0) * float(uvtiles) - float(tu), 0.0, 1.0);
            float rv = clamp(mod(v, 1.0) * float(uvtiles) - float(tv), 0.0, 1.0);
            if (flip) {
                ru = 1.0 - ru;
            }
            float oru = ru;
            float orv = rv;
            if (rotation == 1) {
                oru = 1.0 - rv;
                orv = ru;
            } else if (rotation == 2) {
                oru = 1.0 - ru;
                orv = 1.0 - rv;
            } else if (rotation == 3) {
                oru = rv;
                orv = 1.0 - ru;
            }

            float umin = cosmicuvs[symbol][0][0];
            float umax = cosmicuvs[symbol][1][0];
            float vmin = cosmicuvs[symbol][0][1];
            float vmax = cosmicuvs[symbol][1][1];
            vec2 cosmictex = vec2(umin * (1.0 - oru) + umax * oru,
                                  vmin * (1.0 - orv) + vmax * orv);
            float textureCoverage = texture(Sampler0, cosmictex).r;
            float a = textureCoverage * (0.5 + (1.0 / float(mult))) *
                      (1.0 - smoothstep(0.15, 0.48, abs(rawv - 0.5)));
            vec3 colorSeed = vec3(float(tu), float(tv), float(i * cosmiccount + symbol));
            vec3 spriteColor = vec3(
                randomValue(colorSeed + vec3(1.0, 0.0, 0.0)),
                randomValue(colorSeed + vec3(0.0, 1.0, 0.0)),
                randomValue(colorSeed + vec3(0.0, 0.0, 1.0))
            );
            col.rgb += spriteColor * a;
        }
    }

    vec3 shade = vertexColor.rgb * lightmix + vec3(1.0 - lightmix);
    col.rgb *= shade;
    col.a *= mask.a * opacity;
    col = clamp(col, 0.0, 1.0);
    fragColor = linear_fog(col * ColorModulator, vertexDistance, FogStart, FogEnd, FogColor);
}
