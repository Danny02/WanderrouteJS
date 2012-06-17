#pragma include includes/StdLib.vert

in vec4 in_position; //<Position>
in vec3 in_normal;   //<Normal>

out vec2 texcoord;
out vec3 normal;

uniform mat4 mat_mvp;    //<MAT_MVP>
uniform mat3 mat_nv;     //<MAT_NV>

void main() {
    texcoord = in_position.xz;
    normal = mat_nv * in_normal;
    gl_Position = mat_mvp * in_position;
}
