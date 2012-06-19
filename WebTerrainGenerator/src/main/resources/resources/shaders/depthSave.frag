#pragma include includes/StdLib.frag
#pragma include util/pack.h

void main() {
    FragColor =  vec4(PackFloat(gl_FragDepth), 1.);
}