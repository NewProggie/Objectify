#pragma version(1)
#pragma rs java_package_name(de.hsrm.objectify.rendering.compute_normals)

int32_t width;
const int *pMask;
const float *pData;

float4 __attribute__((kernel)) compute_normals(float in, uint32_t y) {

//	float rSxyz = 1.0f / sqrt(pown(pData[y*width + 0], 2) +
//							  pown(pData[y*width + 1], 2) +
//							  pown(pData[y*width + 2], 2));
	/* EV contains the eigenvectors of A^TA, which are as well the z,x,y components of the surface
	 * normals for each pixel */
//	float sz = (128.0f + 127.0f * sign(pData[y*width + 0]) *
//	            fabs( pData[y*width + 0]) * rSxyz);
//    float sx = (128.0f + 127.0f * sign(pData[y*width + 1]) *
//                fabs(pData[y*width + 1]) * rSxyz);
//    float sy = (128.0f + 127.0f * sign(pData[y*width + 2]) *
//                fabs(pData[y*width + 2]) * rSxyz);
    float validFlag = 0.0f;
    if (pMask[y] == 0xFFFFFFFF) { validFlag = 1.0f; }
//    float4 n = {sx, sy, sz, validFlag};
    float4 n = {0.0f, 0.0f, 255.0f*validFlag, 1.0f};
//    float4 n = {sx, sy, sz, 1.0f};
    return n;
}