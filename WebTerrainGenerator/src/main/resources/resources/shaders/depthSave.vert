#pragma include includes/StdLib.vert

in vec4 in_position; //<Position>

uniform mat4 mat_mvp;    //<MAT_MVP>

void main() {
    gl_Position = mat_mvp * in_position;
}
