/*global THREE, Detector */
(function () {
    if (!Detector.webgl) {
        Detector.addGetWebGLMessage();
    }

    var main,
        Main,
        SignWindow;


    SignWindow = function (id) {
        this.container = document.getElementById(id);

        this.signType = this.container.querySelector("[name='type']");
        this.signName = this.container.querySelector("[name='name']");
        this.signPositionX = this.container.querySelector("[name='x']");
        this.signPositionY = this.container.querySelector("[name='y']");
        this.signPositionZ = this.container.querySelector("[name='z']");

        this.ok = this.container.querySelector(".ok");
        this.cancel = this.container.querySelector(".cancel");

        this.onOk = this.onOk.bind(this);

        this.ok.addEventListener("click", this.onOk, false);

        this.callback = null;
    };

    SignWindow.init = function (id) {
        SignWindow = new SignWindow(id);
    };

    SignWindow.prototype = {
        show : function (data, callback) {
            this.signPositionX.value = data.position.x;
            this.signPositionY.value = data.position.y;
            this.signPositionZ.value = data.position.z;

            this.callback = callback;

            this.container.classList.add("show");
        },

        close : function () {
            this.container.classList.remove("show");
        },

        onOk : function () {
            if (this.callback) {
                this.callback({
                    name : this.signName.value,
                    type : this.signType.value,
                    position : {
                        x : this.signPositionX.value * 1,
                        y : this.signPositionY.value * 1,
                        z : this.signPositionZ.value * 1
                    }
                });
            }
            this.close();
        },

        onCancel : function () {
            this.close();
        }
    };


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

        this.itemsToLoad = 3;
        this.itemsLoaded = 0;

        this.postprocessing = {};
        this.track = {};

        this.clock = new THREE.Clock();
    
        this.animate = this.animate.bind(this);
        this.animateStart = this.animateStart.bind(this);
        this.onDocumentMouseDown = this.onDocumentMouseDown.bind(this);
        this.onWindowResize = this.onWindowResize.bind(this);
        this.onMeshLoaded = this.onMeshLoaded.bind(this);
        this.onTrackMashLoaded = this.onTrackMashLoaded.bind(this);
        this.onTrackJSONLoaded = this.onTrackJSONLoaded.bind(this);
        this.onCreateSign = this.onCreateSign.bind(this);

        this.init();
    };

    Main.prototype = {
        init : function () {
            var scene, projector;

            this.container = document.createElement('div');
            document.body.appendChild(this.container);

            scene = this.scene = new THREE.Scene();
            //scene.add(new THREE.AxisHelper());

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
            loader.load("resources/map1.ctm", this.onMeshLoaded, false, false);

        },

        initPostprocessing : function () {
            var loader,
                xhr, callback;

            
            loader = new THREE.CTMLoader(this.renderer.context);
            loader.load("resources/path.ctm", this.onTrackMashLoaded, false, true);

            xhr = new XMLHttpRequest();
            callback = this.onTrackJSONLoaded;

            xhr.onreadystatechange = function () {
                if (xhr.readyState === xhr.DONE) {
                    if (xhr.status === 200 || xhr.status === 0) {
                        if (xhr.responseText) {
                            callback(JSON.parse(xhr.responseText));
                        }
                    }
                }
            };

            xhr.open("GET", "resources/path.json", true);
            xhr.setRequestHeader("Content-Type", "application/json");
            xhr.send(null);
        },

        $ : function (id) {
            return document.getElementById(id);
        },

        onMeshLoaded : function (geometry) {
            var shaderMaterial = new THREE.ShaderMaterial({
                    uniforms : this.shaderUniforms,
                    vertexShader:   this.$('terrain.vert').textContent,
                    fragmentShader: this.$('terrain.frag').textContent
                });

            geometry.computeFaceNormals();

            this.terrainMesh = new THREE.Mesh(geometry, shaderMaterial);

            this.scene.add(this.terrainMesh);

            this.updateLoadCounter();
        },

        onTrackMashLoaded : function (geometry) {
            var postprocessing = this.postprocessing,
                options = {
                    minFilter: THREE.LinearFilter,
                    stencilBuffer: false
                },
                roadTest,
                roadMesh;
            
            postprocessing.scene = new THREE.Scene();

            postprocessing.rtTextureDepth = new THREE.WebGLRenderTarget(this.SCREEN_WIDTH, this.SCREEN_HEIGHT, options);

            roadTest = new THREE.ShaderMaterial({
                vertexShader:   this.$('simple.vert').textContent,
                fragmentShader: this.$('test.frag').textContent
            });


            roadMesh = this.roadMesh = new THREE.Mesh(geometry, this.roadTest);
            roadMesh.position.set(-0.5, -0.5, 0.0);
//            roadMesh.rotation.x = -0.8;
            roadMesh.material.depthWrite = false;
            this.postprocessing.scene.add(this.roadMesh);

            this.updateLoadCounter();
        },
        
        onTrackJSONLoaded : function (geometry) {
            this.createPath(geometry);

            this.updateLoadCounter();
        },

        updateLoadCounter : function () {
            this.itemsLoaded += 1;
            if (this.itemsLoaded === this.itemsToLoad) {
//                this.terrainMesh.rotation.x = -0.8;
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
                        // Percentage of diff between near and far to go towards far
                        // starting at near to determine intersection.
                        // @see https://github.com/sinisterchipmunk/jax/blob/5d392c9d67cb9ae5623dc03846027c473f625925/src/jax/webgl/camera.js#L568
                        0.2),
                    ray,
                    intersects,
                    point, 
                    cube;

                this.projector.unprojectVector(vector, camera);

                ray = new THREE.Ray(camera.position, vector.subSelf(camera.position).normalize());

                intersects = ray.intersectObject(this.terrainMesh);

                if (intersects.length > 0) {
                    point = intersects[0].point;

                    SignWindow.show({
                        name : "",
                        type : null,
                        position: point, 
                    }, this.onCreateSign);

                }
                else {
                    console.log("No intersection found");
                }
            }
        },

        onCreateSign : function (data) {
            console.log(data);
            var loader = new THREE.ColladaLoader(),
                material,
                terrainMesh = this.terrainMesh;

            loader.convertUpAxis = true;
            loader.load('resources/models/cubicSign_' + data.type + '.dae', function (collada) {
                //var hlMaterial = new THREE.MeshPhongMaterial({color: 0x750004});

                THREE.SceneUtils.traverseHierarchy(collada.scene, function (object) { 
                    object.scale.set(0.01, 0.01, 0.01);
                    object.position.set(data.position.x, data.position.y, data.position.z + 0.05);
                    /*if (object.material) {
                        object.material = new THREE.MeshBasicMaterial({ wireframe: true });
                    }*/
                    if ((material = object.material)) {
                        object.material = new THREE.MeshBasicMaterial({
                            map: material.map, 
                            morphTargets: material.morphTargets
                        });
                    }
                });
                terrainMesh.add(collada.scene);
            });
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

            renderer.render(this.track, this.camera);
        },

        createPath : function (vertices) {
            var path = new THREE.Geometry(), vert, i, len = vertices.length, track,
                options = {
                    minFilter: THREE.LinearFilter,
                    stencilBuffer: false
                };

            for (i = 0; i < len; i += 1) {
                vert = vertices[i];
                path.vertices.push(
                    new THREE.Vector3(vert[0], vert[1], vert[2]) 
                );
            }
            path.computeVertexNormals();

            track = new THREE.Line(path, new THREE.LineBasicMaterial({
                color : 0xff0000,
                linewidth: 5,
                linecap : 'round',
                linejoin : 'round',
                vertexColors : false,
                fog : false
            }), THREE.LineStrip);
            
            track.position.set(-0.5, -0.5, 0.2);
            //track.rotation.x = -0.8;
            this.track = new THREE.Scene();

            this.track.add(track);

        }
    };

    main = new Main();
    SignWindow.init("window-sign");
}());


