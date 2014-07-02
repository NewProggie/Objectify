#pragma version(1)
#pragma rs java_package_name(de.hsrm.objectify.rendering.lh_integration)
#pragma rs_fp_relaxed

int32_t width;
int32_t height;
int32_t iter;
const float4 *pNormals;
const float *pHeights;

float __attribute__((kernel)) integrate(float4 in, uint32_t x, uint32_t y) {
    float out = pHeights[(y*width) + x];
    int k;

    if (x < 1 || x >= width-1 || y < 1 || y >= height-1) {
        return out;
    }
    // return (row * width) + col;
    for (k = 0; k < iter; k++) {
        float4 up       = pNormals[((y-1) * width ) + x];
        float4 down     = pNormals[((y+1) * width ) + x];
        float4 left     = pNormals[(y * width) + (x-1)];
        float4 right    = pNormals[(y * width) + (x+1)];
        float zU        = pHeights[((y-1) * width) + x];
        float zD        = pHeights[((y+1) * width) + x];
        float zL        = pHeights[(y * width) + (x-1)];
        float zR        = pHeights[(y * width) + (x+1)];
        float nxC       = pNormals[(y * width) + x].x;
        float nyC       = pNormals[(y * width) + x].y;
        float nxU       = pNormals[((y-1) * width) + x].x;
        float nyL       = pNormals[(y * width) + (x-1)].y;
        if (!(up.x == 0.0f && up.y == 0.0f && up.z == 255.0f) &&
            !(down.x == 0.0f && down.y == 0.0f && down.z == 255.0f) &&
            !(left.x == 0.0f && left.y == 0.0f && left.z == 255.0f) &&
            !(right.x == 0.0f && right.y == 0.0f && right.z == 255.0f)) {
            out = 1.0f/4.0f * ( zD + zU + zR + zL + nxU - nxC + nyL - nyC );
        }
    }

    return out;
}