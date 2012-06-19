var SCREEN_WIDTH = window.innerWidth;
var SCREEN_HEIGHT = window.innerHeight;

var container, stats;

var camera, scene, renderer;

var mesh, zmesh, geometry;

var mouseX = 0, mouseY = 0;

var windowHalfX = window.innerWidth / 2;
var windowHalfY = window.innerHeight / 2;

document.addEventListener('mousemove', onDocumentMouseMove, false);

init();
animate();

function init() {

	container = document.createElement( 'div' );
	document.body.appendChild( container );

	scene = new THREE.Scene();
	scene.fog = new THREE.Fog( 0x050505, 800, 2000 );

	camera = new THREE.PerspectiveCamera( 20, SCREEN_WIDTH / SCREEN_HEIGHT, 1, 2000 );
	camera.position.z = 800;
	scene.add( camera );

	var path = "textures/cube/SwedishRoyalCastle/";
	var format = '.jpg';
	var urls = [
		path + 'px' + format, path + 'nx' + format,
		path + 'py' + format, path + 'ny' + format,
		path + 'pz' + format, path + 'nz' + format
	];

	reflectionCube = THREE.ImageUtils.loadTextureCube( urls );

	// LIGHTS

	var ambient = new THREE.AmbientLight( 0x050505 );
	scene.add( ambient );

	var directionalLight = new THREE.DirectionalLight( 0xffeedd );
	directionalLight.position.set( 0, 0, 1 ).normalize();
	scene.add( directionalLight );

	// RENDERER

	renderer = new THREE.WebGLRenderer( { antialias: true } );
	renderer.setSize( SCREEN_WIDTH, SCREEN_HEIGHT );

	renderer.setClearColor( scene.fog.color, 1 );

	renderer.domElement.style.position = "relative";
	container.appendChild( renderer.domElement );

	//

	renderer.gammaInput = true;
	renderer.gammaOutput = true;

	// STATS

	stats = new Stats();
	stats.domElement.style.position = 'absolute';
	stats.domElement.style.top = '0px';
	stats.domElement.style.zIndex = 100;
	container.appendChild( stats.domElement );

	// EVENTS

	window.addEventListener( 'resize', onWindowResize, false );

	// LOADER

	var c = 0, s = Date.now();

	function checkTime() {

		c ++;

		if ( c === 3 ) {

			var e = Date.now();
			console.log( "Total parse time: " + (e-s) + " ms" );

		}

	}

	var useWorker = true;
	var useBuffers = true;

	var loader = new THREE.CTMLoader( renderer.context );

/*	loader.load( "models/ctm/ben.ctm",   function( geometry ) {

		var material = new THREE.MeshLambertMaterial( { color: 0xffaa00, map: THREE.ImageUtils.loadTexture( "textures/ash_uvgrid01.jpg" ), envMap: reflectionCube, combine: THREE.MixOperation, reflectivity: 0.3 } );
		callbackModel( geometry, 450, material, 0, -200, 0, 0, 0 );
		checkTime();

	}, useWorker, useBuffers ); */

/*	loader.load( "models/ctm/WaltHead.ctm",  function( geometry ) {

		//geometry.computeVertexNormals();

		var material = new THREE.MeshLambertMaterial( { color: 0xffffff, envMaps: reflectionCube, combine: THREE.MixOperation, reflectivity: 0.3 } );
		callbackModel( geometry, 5, material, -200, 0, 0, 0, 0 );
		checkTime();

	}, useWorker, useBuffers );*/

	loader.load( "models/ctm/LeePerry.ctm",  function( geometry ) {

		


		// material parameters
		var ambient = 0x111111, diffuse = 0xbbbbbb, specular = 0x060606, shininess = 35;

		var shader = THREE.ShaderUtils.lib[ "normal" ];
		var uniforms = THREE.UniformsUtils.clone( shader.uniforms );

		uniforms[ "tNormal" ].texture = THREE.ImageUtils.loadTexture( "obj/leeperrysmith/Infinite-Level_02_Tangent_SmoothUV.jpg" );
		uniforms[ "uNormalScale" ].value = - 0.8;

		uniforms[ "tDiffuse" ].texture = THREE.ImageUtils.loadTexture( "obj/leeperrysmith/Map-COL.jpg" );
		uniforms[ "tSpecular" ].texture = THREE.ImageUtils.loadTexture( "obj/leeperrysmith/Map-SPEC.jpg" );

		uniforms[ "enableAO" ].value = false;
		uniforms[ "enableDiffuse" ].value = true;
		uniforms[ "enableSpecular" ].value = true;

		uniforms[ "uDiffuseColor" ].value.setHex( diffuse );
		uniforms[ "uSpecularColor" ].value.setHex( specular );
		uniforms[ "uAmbientColor" ].value.setHex( ambient );

		uniforms[ "uShininess" ].value = shininess;

		uniforms[ "wrapRGB" ].value.set( 0.575, 0.5, 0.5 );

		var parameters = { fragmentShader: shader.fragmentShader, vertexShader: shader.vertexShader, uniforms: uniforms, lights: true };
		var material = new THREE.ShaderMaterial( parameters );

		material.wrapAround = true;

		//THREE.Geometry.prototype.computeVertexNormals.apply(geometry);

		var material2 = new THREE.MeshPhongMaterial( { color: 0xffffff, specular: 0x444444, shininess: 30, map: THREE.ImageUtils.loadTexture( "obj/leeperrysmith/Map-COL.jpg" ), envMaps: reflectionCube, combine: THREE.MixOperation, reflectivity: 0.3 } );
		
		var material1 = new THREE.MeshBasicMaterial( { wireframe: true } );


		callbackModel( geometry, 1300, material2, 200, 50, 0, 0, 0 );
		checkTime();

	}, useWorker, useBuffers );

	loader.load( "models/ctm/map.ctm", function( geometry ){
		//geometry.computeVertexNormals();

		//Normal map in material
		// material parameters normal map

		var shader = THREE.ShaderUtils.lib[ "normal" ];
        var uniforms = THREE.UniformsUtils.clone( shader.uniforms );

        uniforms[ "tNormal" ].texture = THREE.ImageUtils.loadTexture( "textures/normal/map.png" );
       
        uniforms[ "enableDiffuse" ].value = false;
        uniforms[ "enableSpecular" ].value = false;

        //geometry.computeTangents();

        materialNormal = new THREE.ShaderMaterial( {
            uniforms: uniforms,
            vertexShader: shader.vertexShader,
            fragmentShader: shader.fragmentShader,
            lights: true
        } );

		var material = new THREE.MeshLambertMaterial( { color: 0xffffff, envMaps: reflectionCube, combine: THREE.MixOperation, reflectivity: 0.3 } );

		callbackModel( geometry, 200, material, -100, 0, 0, 0, 0 );
		checkTime();
	}, useWorker, useBuffers );

}

function callbackModel( geometry, s, material, x, y, z, rx, ry ) {

	var mesh = new THREE.Mesh( geometry, material );

	mesh.position.set( x, y, z );
	mesh.scale.set( s, s, s );
	mesh.rotation.x = rx;
	mesh.rotation.z = ry;
	//mesh.flipSided = true;

	scene.add( mesh );

}

//

function onWindowResize( event ) {

	SCREEN_WIDTH = window.innerWidth;
	SCREEN_HEIGHT = window.innerHeight;

	renderer.setSize( SCREEN_WIDTH, SCREEN_HEIGHT );

	camera.aspect = SCREEN_WIDTH / SCREEN_HEIGHT;
	camera.updateProjectionMatrix();

}

function onDocumentMouseMove( event ) {

	mouseX = ( event.clientX - windowHalfX );
	mouseY = ( event.clientY - windowHalfY );

}

//

function animate() {

	requestAnimationFrame( animate );

	render();
	stats.update();

}

function render() {

	camera.position.x += ( mouseX - camera.position.x ) * .05;
	camera.position.y += ( - mouseY - camera.position.y ) * .05;

	camera.lookAt( scene.position );

	renderer.render( scene, camera );

}