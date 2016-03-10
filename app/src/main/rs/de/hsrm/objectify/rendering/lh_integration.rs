#pragma version(1)
#pragma rs java_package_name(de.hsrm.objectify.rendering.lh_integration)

int32_t width;
int32_t height;
const float4 *pNormals;
const float *pHeights;

float __attribute__((kernel)) integrate(float4 in, uint32_t x, uint32_t y) {
    float out = pHeights[(y * width) + x];

    if (x < 1 || x >= width - 1 || y < 1 || y >= height - 1) { return out; }

    float up    = pNormals[((y - 1) * width) + x].w;
    float down  = pNormals[((y + 1) * width) + x].w;
    float left  = pNormals[(y * width) + (x - 1)].w;
    float right = pNormals[(y * width) + (x + 1)].w;

    if (up == 1.0f && down == 1.0f && left == 1.0f && right == 1.0f) {
        float zU  = pHeights[((y - 1) * width) + x];
        float zD  = pHeights[((y + 1) * width) + x];
        float zL  = pHeights[(y * width) + (x - 1)];
        float zR  = pHeights[(y * width) + (x + 1)];
        float nxC = in.x;
        float nyC = in.y;
        float nxU = pNormals[((y - 1) * width) + x].x;
        float nyL = pNormals[(y * width) + (x - 1)].y;
        out       = 1.0f / 4.0f * (zD + zU + zR + zL + nxU - nxC + nyL - nyC);
    }

    return out;
}