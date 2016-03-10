#pragma version(1)
#pragma rs java_package_name(de.hsrm.objectify.rendering.compute_normals)

int32_t width;
const int *pMask;

float4 __attribute__((kernel))
compute_normals(float4 in, uint32_t x, uint32_t y) {

    float rSxyz = 1.0f / sqrt(pown(in.x, 2) + pown(in.y, 2) + pown(in.z, 2));
    float sz    = 128.0f + 127.0f * sign(in.x) * fabs(in.x) * rSxyz;
    float sx    = 128.0f + 127.0f * sign(in.y) * fabs(in.y) * rSxyz;
    float sy    = 128.0f + 127.0f * sign(in.z) * fabs(in.z) * rSxyz;

    float validFlag = 0.0f;
    if (pMask[y * width + x] == 0xFFFFFFFF) { validFlag = 1.0f; }
    float4 n = {sx, sy, sz, validFlag};

    return n;
}