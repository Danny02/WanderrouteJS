var SCREEN_WIDTH = window.innerWidth;
var SCREEN_HEIGHT = window.innerHeight;

var container;

var camera, scene, renderer, mesh;

var shaderUniforms;

var mouseX = 0, mouseY = 0;

var clock = new THREE.Clock();

document.addEventListener('mousemove', onDocumentMouseMove, false);
window.addEventListener( 'resize', onWindowResize, false );

init();
animate();

function init() {
	scene = new THREE.Scene();
	scene.fog = new THREE.Fog( 0x050505, 800, 2000 );

	//camera
	camera = new THREE.PerspectiveCamera( 20, SCREEN_WIDTH / SCREEN_HEIGHT, 1, 2000 );
	camera.position.z = 800;
	scene.add( camera );

	// RENDERER
	renderer = new THREE.WebGLRenderer( { antialias: true } );
	renderer.setSize( SCREEN_WIDTH, SCREEN_HEIGHT );
	renderer.setClearColor( scene.fog.color, 1 );
	renderer.domElement.style.position = "relative";	
	renderer.gammaInput = true;
	renderer.gammaOutput = true;

	container = document.createElement( 'div' );
	document.body.appendChild( container );
	container.appendChild( renderer.domElement );


	//Terrain Model laden
	var loader = new THREE.CTMLoader( renderer.context );

	shaderUniforms = {
						normal: { type: "t", value: 0, texture: THREE.ImageUtils.loadTexture( "resources/normal.png" ) },
						time: { type: "f", value: 1.0 }
					};

	var useWorker = false;
	var useBuffers = false;

	loader.load( "resources/map1.ctm", function( geometry ){

	    var shaderMaterial = new THREE.ShaderMaterial({
			uniforms : shaderUniforms,
	        vertexShader:   $('customVertexshader'),
	        fragmentShader: $('customFragmentshader')
	    });

		mesh = new THREE.Mesh( geometry, shaderMaterial );

		mesh.position.set(0, -40, -100);
		mesh.scale.set(300, 300, 250 );
		mesh.rotation.x = -0.8;

		scene.add( mesh );
	}, useWorker, useBuffers );
	
	clock.start();

}

function $(id) {
  return document.getElementById(id).textContent
}

function onWindowResize( event ) {

	SCREEN_WIDTH = window.innerWidth;
	SCREEN_HEIGHT = window.innerHeight;

	renderer.setSize( SCREEN_WIDTH, SCREEN_HEIGHT );

	camera.aspect = SCREEN_WIDTH / SCREEN_HEIGHT;
	camera.updateProjectionMatrix();
}

function onDocumentMouseMove( event ) {
	var windowHalfX = window.innerWidth / 2;
	var windowHalfY = window.innerHeight / 2;

	mouseX = ( event.clientX - windowHalfX );
	mouseY = ( event.clientY - windowHalfY );
}

function animate() {
	requestAnimationFrame( animate );

	render();
}

function render() {
	camera.position.x += ( mouseX - camera.position.x ) * .05;
	camera.position.y += ( - mouseY - camera.position.y ) * .05;

	var delta = clock.getDelta();
	shaderUniforms.time.value += delta;

	mesh.rotation.z += 0.1 * delta;

	camera.lookAt( scene.position );

	renderer.render( scene, camera );
}