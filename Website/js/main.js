/*global THREE, vertices */
var WIDTH = 800, 
    HEIGHT = 600,
    canvas,
    renderer,
    camera,
    controls,
    scene,
    geometry,
    cube,
    light,
    len,
    i;

function animate() {
    window.requestAnimationFrame(animate);
    controls.update();
}

function render() {
    renderer.render(scene, camera);
}

function setSize() {
    var compStyle = window.getComputedStyle(canvas, null);
    WIDTH = parseInt(compStyle.getPropertyValue("width"), 10);
    HEIGHT = parseInt(compStyle.getPropertyValue("height"), 10);
    renderer.setSize(WIDTH, HEIGHT);
}

function init() {
    canvas = document.getElementById("canvas");
    renderer = new THREE.WebGLRenderer({
        antialias: true,
        canvas : document.getElementById("canvas")
    });

    setSize();

    renderer.setClearColorHex(0xEEEEEE, 1.0);
    renderer.clear();	
    
    // new THREE.PerspectiveCamera( FOV, viewAspectRatio, zNear, zFar );
    camera = new THREE.PerspectiveCamera(45, WIDTH / HEIGHT, 1, 10000);

    scene = new THREE.Scene();

    camera.position.x = 0;
    camera.position.y = 500;
    camera.position.z = 5000;

    camera.lookAt(new THREE.Vector3([0, 0, 0]));

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

    controls.addEventListener('change', render);
  
    // add the camera to the scene
    scene.add(camera);
    
    /*var cube = new THREE.Mesh(new THREE.CubeGeometry(50,50,50),
               new THREE.MeshBasicMaterial({color: 0x000000}));
    scene.add(cube);*/
            
    geometry = new THREE.Geometry();

    
    /*geometry.vertices.push( new THREE.Vector3( 99,474,320 ) );
    geometry.vertices.push( new THREE.Vector3( 99,470,321 ) );
    geometry.vertices.push( new THREE.Vector3( 96,480,320 ) );
    geometry.vertices.push( new THREE.Vector3( 96,470,321 ) );
    geometry.vertices.push( new THREE.Vector3( 93,477,320 ) );
    geometry.vertices.push( new THREE.Vector3( 93,473,321 ) );*/
        
    /*geometry.vertices.push( new THREE.Vector3( 99, 470, 321 ) );
    geometry.vertices.push( new THREE.Vector3( 99, 474, 320 ) );
    geometry.vertices.push( new THREE.Vector3( 96, 480, 320 ) );
    geometry.vertices.push( new THREE.Vector3( 93,477,320 ) );
    geometry.vertices.push( new THREE.Vector3( 93,473,321 ) );
    geometry.vertices.push( new THREE.Vector3( 96,470,321 ) );*/
        
    /*geometry.vertices.push( new THREE.Vector3( 1, 470, 321 ) );
    geometry.vertices.push( new THREE.Vector3( 0, 477, 320 ) );
    geometry.vertices.push( new THREE.Vector3( 0,473, 321 ) );*/
    
    //for (i=0;i<vertices.length-2;i+=3){
    
    //for (i=0;i<27;i+=3){
    len = vertices.length - 2;
    for (i = 0; i < len; i += 3) {
        geometry.vertices.push(new THREE.Vector3(vertices[i] * 90, 
                                                 vertices[i + 1], 
                                                 vertices[i + 2] * 90));
    }

    //console.log('number of vertices ' + geometry.vertices.length);
    len = geometry.vertices.length - 2;
    for (i = 0; i < len; i += 3) {
        geometry.faces.push(new THREE.Face3(i, i + 1, i + 2));
    }
    geometry.computeFaceNormals();
    //console.log('number of faces ' + geometry.faces.length);

    
    //geometry.faces.push( new THREE.Face3( 0, 1, 2 ) );
    //geometry.faces.push( new THREE.Face3( 3, 4, 5 ) );

    //geometry.computeBoundingSphere();
    
    // lights

    light = new THREE.DirectionalLight(0xffffff);
    light.position.set(1, 1, 1);
    scene.add(light);

    light = new THREE.DirectionalLight(0x002288);
    light.position.set(-1, -1, -1);
    scene.add(light);

    light = new THREE.AmbientLight(0x222222);
    scene.add(light);
    
    cube = new THREE.Mesh(geometry, 
        new THREE.MeshLambertMaterial({
            color: 0xFFFFFF
        })
    );

    cube.doubleSided = true;
    scene.add(cube);

    document.body.appendChild(renderer.domElement);	

    window.addEventListener("resize", function () {
        setSize();
        render();
    });

}


init();
animate();
