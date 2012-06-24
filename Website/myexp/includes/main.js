var SCREEN_WIDTH = window.innerWidth;
var SCREEN_HEIGHT = window.innerHeight;

var container;

var camera, scene, renderer, mesh, line;

var shaderUniforms;

var mouseX = 0, mouseY = 0;

var clock = new THREE.Clock();

//document.addEventListener('mousemove', onDocumentMouseMove, false);
window.addEventListener( 'resize', onWindowResize, false );

init();

function init() {
	scene = new THREE.Scene();
	scene.fog = new THREE.Fog( 0x050505, 800, 2000 );

	//camera
	camera = new THREE.PerspectiveCamera( 20, SCREEN_WIDTH / SCREEN_HEIGHT, 1, 2000 );
    camera.position.x = 0;
    camera.position.y = 500;
    camera.position.z = 800;
	scene.add( camera );



    controls = new THREE.TrackballControls(camera);
    controls.rotateSpeed = 1.0;
    controls.zoomSpeed = 1.2;
    controls.panSpeed = 0.8;

    controls.noZoom = false;
    controls.noPan = false;

    controls.staticMoving = true;
    controls.dynamicDampingFactor = 0.3;

    controls.keys = [
        65, // = A
        83, // = S
        68  // = D
    ];


    

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

        line = createPath(geometry, 50);

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

        animateStart();

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
/*
function onDocumentMouseMove( event ) {
	var windowHalfX = window.innerWidth / 2;
	var windowHalfY = window.innerHeight / 2;

	mouseX = ( event.clientX - windowHalfX );
	mouseY = ( event.clientY - windowHalfY );
}

*/

function animateStart () {
	var delta = clock.getDelta();
	shaderUniforms.time.value += delta;

	camera.position.x += ( mouseX - camera.position.x ) * .05;
	camera.position.y += ( - mouseY - camera.position.y ) * .05;

    if (camera.position.y <= 2) {
        controls.addEventListener('change', render);
        mesh.add( line );
	    requestAnimationFrame(animate);
    }
    else {
	    requestAnimationFrame(animateStart);
    }

    render();
}

function animate() {
	requestAnimationFrame( animate );
    controls.update();
}

function render() {
	camera.lookAt( scene.position );

	renderer.render( scene, camera );
}

function createPath(geometry, len) {
    geometry.computeBoundingBox();
    var min = geometry.boundingBox.min,
        max = geometry.boundingBox.max,
        meshSize = geometry.vertices.length,
        path = new THREE.Geometry(),
        line,
        i = 0,
        deltaX = max.x - min.x,
        deltaY = max.y - min.y,
        deltaZ = 0.5 - min.z;


    len || (len = 50);

    for (i = 0; i < len; i+=1) {
        path.vertices.push(
            geometry.vertices[parseInt(meshSize * Math.random())]
        );
    }
    path.computeVertexNormals();

    line = new THREE.Line(path, new THREE.LineBasicMaterial({
        color : 0xff0000,
        linewidth: 5,
        linecap : 'round',
        linejoin : 'round',
        vertexColors : false,
        fog : false
    }), THREE.LineStrip);



    return line;
}
