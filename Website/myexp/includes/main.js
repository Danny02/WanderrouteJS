if ( ! Detector.webgl ) Detector.addGetWebGLMessage();

var container;

var camera, scene, renderer, line, projector, controls;
var terrainMesh;

var shaderUniforms, postprocessing = {};

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

        var intersects = ray.intersectObject( terrainMesh );

        console.log(intersects);
        if ( intersects.length > 0 ) {
            var point = intersects[0].point;
            var cube = new THREE.Mesh(new THREE.CubeGeometry(10, 10, 10), new THREE.MeshNormalMaterial());
            cube.position.x = point.x;
            cube.position.y = point.y;
            cube.position.z = point.z + 0.1;

            scene.add(cube);
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
    camera.position.z = 3;
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

        //line = createPath(geometry, 50);

        var shaderMaterial = new THREE.ShaderMaterial({
            uniforms : shaderUniforms,
            vertexShader:   $('terrain.vert'),
            fragmentShader: $('terrain.frag')
        });

        terrainMesh = new THREE.Mesh( geometry, shaderMaterial );

        terrainMesh.rotation.x = -0.8;
        scene.add( terrainMesh );

    }, false, true );
    initPostprocessing();

    clock.start();
    animateStart();
}

//
var roadMesh;
function initPostprocessing() {

    postprocessing.scene = new THREE.Scene();

    var options = {
        minFilter: THREE.LinearFilter,
        stencilBuffer: false
    };
    postprocessing.rtTextureDepth = new THREE.WebGLRenderTarget( window.innerWidth, window.innerHeight, options );

    /*postprocessing.depthOnlyMaterial = new THREE.ShaderMaterial({
        vertexShader:   $('simple.vert'),
        fragmentShader: $('depthSave.frag')
    });*/

    var roadTest = new THREE.ShaderMaterial({
        vertexShader:   $('simple.vert'),
        fragmentShader: $('test.frag')
    });

    //roadMesh = new THREE.Mesh( new THREE.CubeGeometry( 0.7, 3., 0.7), roadTest);

    //	postprocessing.scene.add( roadMesh );

	 var loader = new THREE.CTMLoader( renderer.context );
	    loader.load( "resources/path.ctm", function( geometry ){


	        roadMesh = new THREE.Mesh( geometry, roadTest );

	        roadMesh.position.set(-0.5,-0.5,0);
	        roadMesh.rotation.x = -0.8;
		    roadMesh.material.depthWrite = false;
    	postprocessing.scene.add( roadMesh );

	    }, false, true );
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


function animateStart () {
    var delta = clock.getDelta();
	if(typeof roadMesh === "undefined")
	{
        requestAnimationFrame(animateStart);
    	return;
	}


    shaderUniforms.time.value += delta;



    camera.lookAt( scene.position );

    if (camera.position.y < 2) {
        requestAnimationFrame(animate);
    }
    else {
        requestAnimationFrame(animateStart);
        camera.position.y -= 250 * delta;
    }

    render();
}

function animate() {
    var delta = clock.getDelta();
    requestAnimationFrame( animate );
    controls.update();
    //terrainMesh.rotation.z += 0.1 * delta;
    render();
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
    renderer.render(postprocessing.scene, camera);

    gl.stencilOp(gl.KEEP, gl.DECR, gl.KEEP);
    renderer.setFaceCulling("back");
    renderer.render(postprocessing.scene, camera);
    gl.colorMask(true,true,true,true);

    //weg rendern
    gl.stencilFunc(gl.NOTEQUAL, 0, 0xFF);
    renderer.setFaceCulling("front");
    roadMesh.material.depthTest = false;
    renderer.render(postprocessing.scene, camera);

    //GL state zurücksetzten
    gl.disable(gl.STENCIL_TEST);
    renderer.setFaceCulling("back");

}

function createPath(geometry, len) {
    geometry.computeBoundingBox();
    var path = new THREE.Geometry();

    /*
    var min = geometry.boundingBox.min;
    var max = geometry.boundingBox.max;
    var deltaX = max.x - min.x,
    deltaY = max.y - min.y,
    deltaZ = 0.5 - min.z;*/


    len || (len = 50);//?? wtf

    var i;
    for (i = 0; i < len; i+=1) {
        path.vertices.push(
            geometry.vertices[parseInt(Math.random())]
            );
    }
    path.computeVertexNormals();

    var line = new THREE.Line(path, new THREE.LineBasicMaterial({
        color : 0xff0000,
        linewidth: 5,
        linecap : 'round',
        linejoin : 'round',
        vertexColors : false,
        fog : false
    }), THREE.LineStrip);

    return line;
}
