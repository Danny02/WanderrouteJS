if ( ! Detector.webgl ) Detector.addGetWebGLMessage();

var container;

var camera, scene, renderer, line, projector;
var terrainMesh;

var shaderUniforms, postprocessing = {};

var mouseX = 0, mouseY = 0;

var clock = new THREE.Clock();

//document.addEventListener('mousemove', onDocumentMouseMove, false);
window.addEventListener( 'resize', onWindowResize, false );

init();
function onDocumentMouseDown( event ) {
    if (event.button === 2) {
        event.preventDefault();

        var vector = new THREE.Vector3( ( event.clientX / window.innerWidth ) * 2 - 1,
            - ( event.clientY / window.innerHeight ) * 2 + 1,
            0.5 );

        projector.unprojectVector( vector, camera );

        var ray = new THREE.Ray( camera.position, vector.subSelf( camera.position ).normalize() );

        var intersects = ray.intersectObject( mesh );

        console.log(intersects);
        if ( intersects.length > 0 ) {
            var point = intersects[0].point,
            cube = new THREE.Mesh(new THREE.CubeGeometry(10, 10, 10), new THREE.MeshNormalMaterial());
            cube.position.x = point.x;
            cube.position.y = point.y;
            cube.position.z = point.z + 0.1;

            scene.add(cube);
            render();
        }

    /*
        // Parse all the faces
        for ( var i in intersects ) {

            intersects[ i ].face.material[ 0 ].color.setHex( Math.random() * 0xffffff | 0x80000000 );

        }
        */
    }
}
function init() {

    container = document.createElement( 'div' );
    document.body.appendChild( container );

    //

    scene = new THREE.Scene();

    //camera
    camera = new THREE.PerspectiveCamera( 20, window.innerWidth / window.innerHeight, 1, 2000 );
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

    projector = new THREE.Projector();



    // RENDERER
    renderer = new THREE.WebGLRenderer( {
        antialias: true
    } );
    renderer.setSize(window.innerWidth, window.innerHeight);
    renderer.setClearColor({
        r:0,
        g:0,
        b:0
    }, 1 );
    renderer.gammaInput = true;
    renderer.gammaOutput = true;
    renderer.autoClear = false;

    renderer.domElement.style.position = 'absolute';
    renderer.domElement.style.top = 0 + "px";
    renderer.domElement.style.left = "0px";

    container = document.createElement( 'div' );
    document.body.appendChild( container );
    container.appendChild( renderer.domElement );

    document.addEventListener( 'mousedown', onDocumentMouseDown, false );


    //Terrain Model laden

    shaderUniforms = {
        normal: {
            type: "t",
            value: 0,
            texture: THREE.ImageUtils.loadTexture( "resources/normal.png" )
        },
        time: {
            type: "f",
            value: 1.0
        }
    };

    var loader = new THREE.CTMLoader( renderer.context );
    loader.load( "resources/map1.ctm", function( geometry ){

        var shaderMaterial = new THREE.ShaderMaterial({
            uniforms : shaderUniforms,
            vertexShader:   $('terrain.vert'),
            fragmentShader: $('terrain.frag')
        });
        line = createPath(geometry, 50);

        var shaderMaterial = new THREE.ShaderMaterial({
            uniforms : shaderUniforms,
            vertexShader:   $('customVertexshader'),
            fragmentShader: $('customFragmentshader')
        });

        terrainMesh = new THREE.Mesh( geometry, shaderMaterial );

        terrainMesh.position.set(0, -40, -100);
        terrainMesh.scale.set(300, 300, 250 );
        terrainMesh.rotation.x = -0.8;
        scene.add( terrainMesh );

    }, false, true );
    clock.start();
    initPostprocessing();
    animateStart();
}

//
var roadMesh;
function initPostprocessing() {

    postprocessing.scene = new THREE.Scene();
    postprocessing.camera = new THREE.PerspectiveCamera( 70, window.innerWidth / window.innerHeight, 1, 3000 );
    postprocessing.camera.position.z = 200;
    postprocessing.scene.add(postprocessing.camera);

    var options = {
        minFilter: THREE.LinearFilter,
        stencilBuffer: false
    };
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


    //terrainMesh.rotation.z += 0.1 * delta;

    camera.position.x += ( mouseX - camera.position.x ) * .05;
    camera.position.y += ( - mouseY - camera.position.y ) * .05;

    postprocessing.camera.position.x += ( mouseX - postprocessing.camera.position.x ) * .05;
    postprocessing.camera.position.y += ( - mouseY - postprocessing.camera.position.y ) * .05;

    camera.lookAt( scene.position );
    postprocessing.camera.lookAt( scene.position );

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
