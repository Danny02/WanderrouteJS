#pragma include includes/StdLib.frag

in vec3 normal;
in vec2 texcoord;

uniform vec3 halfvector;
uniform vec3 light_pos;

const vec3 diffuse = vec3(1.);
const vec3 ambient = vec3(0.1);
const vec3 specular = vec3(1.);
const float mat_spec_exp = 20.;

void main() {
    vec3 N = normalize(normal);

    vec3 color = vec3(0.);

    float NdotL = dot(N, light_pos);

    if(NdotL > 0.0){
        float HdotN = dot(N, halfvector);
        vec3 scolor = specular;
        color += scolor * pow(max(HdotN, 0.), mat_spec_exp);
        color += diffuse * NdotL;
    }

    color += ambient * diffuse;

    FragColor =  vec4(color, 1.);
}

