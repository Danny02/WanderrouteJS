/*global THREE, Detector */
(function () {
    if (!Detector.webgl) {
        Detector.addGetWebGLMessage();
    }

    var main,
        Main;
        
    Main = function () {
        this.container = null;

        this.camera = null;
        this.scene = null;
        this.renderer = null;
        this.projector = null;
        this.controls = null;

        this.roadMesh = null;
        this.terrainMesh = null;
        this.line = null;

        this.shaderUniforms = null;

        this.itemsToLoad = 2;
        this.itemsLoaded = 0;

        this.postprocessing = {};

        this.clock = new THREE.Clock();
    
        this.animate = this.animate.bind(this);
        this.animateStart = this.animateStart.bind(this);
        this.onDocumentMouseDown = this.onDocumentMouseDown.bind(this);
        this.onWindowResize = this.onWindowResize.bind(this);
        this.onMeshLoaded = this.onMeshLoaded.bind(this);
        this.onTrackMashLoaded = this.onTrackMashLoaded.bind(this);

        this.init();
    };

    Main.prototype = {
        init : function () {
            var scene, projector;

            this.container = document.createElement('div');
            document.body.appendChild(this.container);

            scene = this.scene = new THREE.Scene();

            this.initCamera(); 

            this.initControls();

            this.projector = projector = new THREE.Projector();

            this.initRenderer(); 

            this.onWindowResize();

            this.initTerrain();
            this.initPostprocessing();

            this.initEventListeners();


        },

        initCamera : function () {
            //camera
            var camera = this.camera = new THREE.PerspectiveCamera(20, this.SCREEN_WIDTH / this.SCREEN_HEIGHT, 1, 2000);
            camera.position.x = 0;
            camera.position.y = 500;
            camera.position.z = 3;
            this.scene.add(camera);
        },

        initControls : function () {
            var controls = this.controls = new THREE.TrackballControls(this.camera);
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
        },

        initRenderer : function () {
            // RENDERER
            var renderer = this.renderer = new THREE.WebGLRenderer({
                antialias: true
            });
            renderer.setSize(window.innerWidth, window.innerHeight);
            renderer.setClearColor({
                r : 0,
                g : 0,
                b : 0
            }, 1);
            renderer.gammaInput = true;
            renderer.gammaOutput = true;
            renderer.autoClear = false;

            renderer.domElement.style.position = 'absolute';
            renderer.domElement.style.top = 0 + "px";
            renderer.domElement.style.left = "0px";

            this.container.appendChild(renderer.domElement);

        },

        initEventListeners : function () {
            //document.addEventListener('mousemove', onDocumentMouseMove, false);
            window.addEventListener('resize', this.onWindowResize, false);
            document.addEventListener('mousedown', this.onDocumentMouseDown, false);
        },

        initTerrain : function () {
            var loader,
                shaderUniforms;

            this.shaderUniforms = shaderUniforms = {
                normal: {
                    type: "t",
                    value: 0,
                    texture: THREE.ImageUtils.loadTexture("resources/normal.png")
                },
                time: {
                    type: "f",
                    value: 1.0
                }
            };

            loader = new THREE.CTMLoader(this.renderer.context);
            loader.load("resources/map1.ctm", this.onMeshLoaded, false, true);

        },

        initPostprocessing : function () {
            var postprocessing = this.postprocessing,
                options = {
                    minFilter: THREE.LinearFilter,
                    stencilBuffer: false
                },
                roadTest,
                roadMesh,
                loader;

            postprocessing.scene = new THREE.Scene();

            postprocessing.rtTextureDepth = new THREE.WebGLRenderTarget(this.SCREEN_WIDTH, this.SCREEN_HEIGHT, options);

            roadTest = new THREE.ShaderMaterial({
                vertexShader:   this.$('simple.vert').textContent,
                fragmentShader: this.$('test.frag').textContent
            });

            loader = new THREE.CTMLoader(this.renderer.context);
            loader.load("resources/path.ctm", this.onTrackMashLoaded, false, true);
        },

        $ : function (id) {
            return document.getElementById(id);
        },

        onMeshLoaded : function (geometry) {
            //line = createPath(geometry, 50);

            var terrainMesh,
                shaderMaterial = new THREE.ShaderMaterial({
                    uniforms : this.shaderUniforms,
                    vertexShader:   this.$('terrain.vert').textContent,
                    fragmentShader: this.$('terrain.frag').textContent
                });

            terrainMesh = this.terrainMesh = new THREE.Mesh(geometry, shaderMaterial);

            terrainMesh.rotation.x = -0.8;
            this.scene.add(terrainMesh);

            this.updateLoadCounter();
        },

        onTrackMashLoaded : function (geometry) {
            var roadMesh = this.roadMesh = new THREE.Mesh(geometry, this.roadTest);
            roadMesh.position.set(-0.5, -0.5, 0);
            roadMesh.rotation.x = -0.8;
            roadMesh.material.depthWrite = false;
            this.postprocessing.scene.add(this.roadMesh);

            this.updateLoadCounter();
        },

        updateLoadCounter : function () {
            this.itemsLoaded += 1;
            if (this.itemsLoaded === this.itemsToLoad) {
                this.clock.start();
                this.animateStart();
            }
        },

        onDocumentMouseDown : function (e) {
            if (e.button === 2) {
                e.preventDefault();

                var camera = this.camera,
                    vector = new THREE.Vector3((e.clientX / this.SCREEN_WIDTH) * 2 - 1,
                        - (e.clientY / this.SCREEN_HEIGHT) * 2 + 1,
                        0.5),
                    ray,
                    intersects,
                    point, 
                    cube;

                this.projector.unprojectVector(vector, camera);

                ray = new THREE.Ray(camera.position, vector.subSelf(camera.position).normalize());

                intersects = ray.intersectObject(this.terrainMesh);

                if (intersects.length > 0) {
                    point = intersects[0].point;
                    cube = new THREE.Mesh(new THREE.CubeGeometry(10, 10, 10), new THREE.MeshNormalMaterial());
                    cube.position.x = point.x;
                    cube.position.y = point.y;
                    cube.position.z = point.z + 0.1;

                    this.scene.add(cube);
                }
            }
        },

        onWindowResize : function (event) {
            this.SCREEN_WIDTH = window.innerWidth;
            this.SCREEN_HEIGHT = window.innerHeight;

            this.renderer.setSize(this.SCREEN_WIDTH, this.SCREEN_HEIGHT);

            this.camera.aspect = this.SCREEN_WIDTH / this.SCREEN_HEIGHT;
            this.camera.updateProjectionMatrix();
        },

        animateStart : function () {
            var delta = this.clock.getDelta();

            this.shaderUniforms.time.value += delta;

            this.camera.lookAt(this.scene.position);

            this.render();

            if (this.camera.position.y < 2) {
                window.requestAnimationFrame(this.animate);
            }
            else {
                window.requestAnimationFrame(this.animateStart);
                this.camera.position.y -= 250 * delta;
            }
        },

        animate : function () {
            var delta = this.clock.getDelta();
            this.controls.update();
            this.terrainMesh.rotation.z += 0.1 * delta;
            this.render();
            window.requestAnimationFrame(this.animate);
        },

        render : function () {
            var renderer = this.renderer,
                gl = renderer.getContext();

            renderer.clear(true, true, true);

            //Terrain rendern nd Depthbuffer füllen

            renderer.render(this.scene, this.camera);

            //Stencilbuffer füllen
            gl.enable(gl.STENCIL_TEST);
            gl.stencilFunc(gl.ALWAYS, 0, 0);
            gl.colorMask(false, false, false, false);
            this.roadMesh.material.depthTest = true;
            gl.stencilOp(gl.KEEP, gl.INCR, gl.KEEP);
            renderer.setFaceCulling("front");
            renderer.render(this.postprocessing.scene, this.camera);

            gl.stencilOp(gl.KEEP, gl.DECR, gl.KEEP);
            renderer.setFaceCulling("back");
            renderer.render(this.postprocessing.scene, this.camera);
            gl.colorMask(true, true, true, true);

            //weg rendern
            gl.stencilFunc(gl.NOTEQUAL, 0, 0xFF);
            renderer.setFaceCulling("front");
            this.roadMesh.material.depthTest = false;
            renderer.render(this.postprocessing.scene, this.camera);

            //GL state zurücksetzten
            gl.disable(gl.STENCIL_TEST);
            renderer.setFaceCulling("back");

        },

        createPath : function (geometry, len) {
            geometry.computeBoundingBox();
            var path = new THREE.Geometry(), i, line;

            /*
            var min = geometry.boundingBox.min;
            var max = geometry.boundingBox.max;
            var deltaX = max.x - min.x,
            deltaY = max.y - min.y,
            deltaZ = 0.5 - min.z;*/


            len || (len = 50);//?? wtf

            for (i = 0; i < len; i += 1) {
                path.vertices.push(
                    geometry.vertices[parseInt(Math.random(), 10)]
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
    };

    main = new Main();
}());


