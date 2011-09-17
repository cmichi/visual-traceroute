#version 110

uniform sampler2D texNormal;
uniform sampler2D texHeight;
uniform sampler2D texEarth;
uniform sampler2D texSpecular;
uniform vec3 lightDir;

varying vec3 normal;
varying vec3 position;

void main()
{
	vec2 texCoord	= vec2(gl_TexCoord[0].s, gl_TexCoord[0].t);
	vec3 pxNormal = 2.0 * (texture2D(texNormal, texCoord).rgb - vec3(0.5));
	vec3 pxHeight = texture2D(texHeight, texCoord).rgb;
	
	vec3 pxEarth = texture2D(texEarth, texCoord).rgb;
	vec3 pxSpecular = texture2D(texSpecular, texCoord).rgb;
		
	vec3 lNormal	= normalize(normal + pxNormal);
	float lDiffuse	= abs(dot(lNormal, lightDir));
	float lSpecular = pow(lDiffuse, 100.0);
	
	vec3 landFinal = pxEarth * pxHeight.g + lSpecular * pxHeight.g;
	vec3 oceanFinal = pxEarth * pxSpecular.g + lSpecular * pxSpecular.g;
	
	gl_FragColor.rgb = landFinal + oceanFinal;
	gl_FragColor.a = 1.0;
}
