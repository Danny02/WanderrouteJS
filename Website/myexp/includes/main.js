if ( ! Detector.webgl ) Detector.addGetWebGLMessage();

var container;

var camera, scene, renderer
var terrainMesh;

var shaderUniforms, postprocessing = {};

var mouseX = 0, mouseY = 0;

var clock = new THREE.Clock();

document.addEventListener('mousemove', onDocumentMouseMove, false);
window.addEventListener( 'resize', onWindowResize, false );

init();
animate();

function init() {

	container = document.createElement( 'div' );
	document.body.appendChild( container );

	//

	scene = new THREE.Scene();

	//camera
	camera = new THREE.PerspectiveCamera( 70, window.innerWidth / window.innerHeight, 1, 3000 );
	camera.position.z = 200;
	scene.add( camera );

	// RENDERER
	renderer = new THREE.WebGLRenderer( { antialias: true } );
	renderer.setSize(window.innerWidth, window.innerHeight);
	renderer.setClearColor({r:0,g:0,b:0}, 1 );
	renderer.gammaInput = true;
	renderer.gammaOutput = true;
				renderer.autoClear = false;

	renderer.domElement.style.position = 'absolute';
				renderer.domElement.style.top = 0 + "px";
				renderer.domElement.style.left = "0px";

	container = document.createElement( 'div' );
	document.body.appendChild( container );
	container.appendChild( renderer.domElement );


	//Terrain Model laden

	shaderUniforms = {
						normal: { type: "t", value: 0, texture: THREE.ImageUtils.loadTexture( "resources/normal.png" ) },
						time: { type: "f", value: 1.0 }
					};

	var loader = new THREE.CTMLoader( renderer.context );
	loader.load( "resources/map1.ctm", function( geometry ){

	    var shaderMaterial = new THREE.ShaderMaterial({
			uniforms : shaderUniforms,
	        vertexShader:   $('terrain.vert'),
	        fragmentShader: $('terrain.frag')
	    });

		terrainMesh = new THREE.Mesh( geometry, shaderMaterial );

		terrainMesh.position.set(0, -40, -100);
		terrainMesh.scale.set(300, 300, 250 );
		terrainMesh.rotation.x = -0.8;
		scene.add( terrainMesh );

	}, false, true );

	clock.start();
	initPostprocessing();
}

//
var roadMesh;
function initPostprocessing() {

	postprocessing.scene = new THREE.Scene();
	postprocessing.camera = new THREE.PerspectiveCamera( 70, window.innerWidth / window.innerHeight, 1, 3000 );
	postprocessing.camera.position.z = 200;
	postprocessing.scene.add(postprocessing.camera);

	var options = { minFilter: THREE.LinearFilter, stencilBuffer: false};				
	postprocessing.rtTextureDepth = new THREE.WebGLRenderTarget( window.innerWidth, window.innerHeight, options );
	
	postprocessing.depthOnlyMaterial = new THREE.ShaderMaterial({
		        vertexShader:   $('simple.vert'),
		        fragmentShader: $('depthSave.frag')
	});

	var roadTest = new THREE.ShaderMaterial({
		        vertexShader:   $('simple.vert'),
		        fragmentShader: $('test.frag')
	});
	
	roadMesh = new THREE.Mesh( new THREE.CubeGeometry( 300, 5, 300),
				 				roadTest);
	roadMesh.rotation.x = -0.8;
	roadMesh.material.depthWrite = false;
	postprocessing.scene.add( roadMesh );

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

	
	postprocessing.camera.aspect = SCREEN_WIDTH / SCREEN_HEIGHT;
	postprocessing.camera.updateProjectionMatrix();
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
	camera.position.x += ( mouseX - camera.position.x ) * .05;
	camera.position.y += ( - mouseY - camera.position.y ) * .05;
	
	postprocessing.camera.position.x += ( mouseX - postprocessing.camera.position.x ) * .05;
	postprocessing.camera.position.y += ( - mouseY - postprocessing.camera.position.y ) * .05;

	var delta = clock.getDelta();
	shaderUniforms.time.value += delta;

	//terrainMesh.rotation.z += 0.1 * delta;

	camera.lookAt( scene.position );
	camera.lookAt( scene.position );

	postprocessing.camera.lookAt( scene.position );
	postprocessing.camera.lookAt( scene.position );
}

function render() {
	var gl = renderer.getContext();

	renderer.clear(true, true, true);

	//Terrain rendern nd Depthbuffer füllen
	
	renderer.render(scene, camera);

	//Stencilbuffer füllen
	gl.enable(gl.STENCIL_TEST);
	gl.stencilFunc(gl.ALWAYS, 0, 0);	
	gl.colorMask(false,false,false,false);
	roadMesh.material.depthTest = true;
	    gl.stencilOp(gl.KEEP, gl.INCR, gl.KEEP);
	   	renderer.setFaceCulling("front");
		renderer.render(postprocessing.scene, postprocessing.camera);

	    gl.stencilOp(gl.KEEP, gl.DECR, gl.KEEP);
	   	renderer.setFaceCulling("back");
		renderer.render(postprocessing.scene, postprocessing.camera);
	gl.colorMask(true,true,true,true);

	//weg rendern
	gl.stencilFunc(gl.NOTEQUAL, 0, 0xFF);
   	renderer.setFaceCulling("front");
	roadMesh.material.depthTest = false;
	renderer.render(postprocessing.scene, postprocessing.camera);	

	//GL state zurücksetzten
	gl.disable(gl.STENCIL_TEST);
   	renderer.setFaceCulling("back");
}