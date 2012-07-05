/*global THREE, Detector */
(function (global, doc) {
    if (!Detector.webgl) {
        Detector.addGetWebGLMessage();
    }

    var main,
        Main,
        ProfilePanel,
        SignWindow;

    /**
     * Zeigt ein Fenster an, in dem sich Daten über einen Marker (Parkplatz, Toilette, Essen)
     * eingeben und speichern lassen.
     * 
     */
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

    /**
     * Initialisiert ein SignWindow Objekt.
     */
    SignWindow.init = function (id) {
        SignWindow = new SignWindow(id);
    };

    /**
     *
     */
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
    
    /**
     *
     */
    ProfilePanel = function (element) {
        this.element = element;

        this.render = this.render.bind(this);

        this.element.addEventListener("webkitTransitionEnd", this.render, false);
        this.element.addEventListener("transitionend", this.render, false);
        this.element.addEventListener("oTransitionEnd", this.render, false);
    };

    /**
     *
     */
    ProfilePanel.prototype = {
        toggle : function (show) {
            this.element.classList[show ? "add" : "remove"]("show");
        },
        calculateTrackProfile : function (geometry) {
            var i = 0, len = geometry.length,
                distance, completeDistance = 0,
                current, prev, 
                profile = [],
                maxHeight;

            current = geometry[0];

            profile.push({
                distance : 0,
                height : current[2]
            });

            maxHeight = current[2];
            prev = current;

            for (i = 1; i < len; i += 1) {
                current = geometry[i];

                if (current[2] > maxHeight) {
                    maxHeight = current[2];
                }

                distance = Math.sqrt(Math.pow(current[0] - prev[0], 2) + 
                                     Math.pow(current[1] - prev[1], 2) + 
                                     Math.pow(current[2] - prev[2], 2));

                completeDistance += distance;

                profile.push({
                    distance : completeDistance,
                    height : current[2]
                });

                prev = current;
            }

            profile.forEach(function (item, index, scope) {
                scope[index].distance /= completeDistance;
                scope[index].height /= maxHeight;
            });

            this.profile = profile;

            this.render();
        },

        getStyle : function (property) {
            return parseInt(window.getComputedStyle(this.element, null).getPropertyValue(property), 10);
        },
        
        render : function () {
            var canvas = this.element,
                context,
                profile = this.profile,
                height,
                width,
                scaledHeight;

            if (canvas) {
                height = canvas.height = this.getStyle("height");
                width = canvas.width = this.getStyle("width");
                scaledHeight = height * 0.75;


                context = canvas.getContext("2d");

                context.clearRect(0, 0, width, height);
                
                context.lineWidth = 1;
                context.strokeStyle = "#555555";
                context.beginPath();
                context.moveTo(0, scaledHeight);
                context.lineTo(width, scaledHeight);
                console.log(width, scaledHeight);
                context.stroke();
                context.closePath();

                context.lineWidth = 2;
                context.strokeStyle = "#ffffff";
                context.beginPath();

                profile.forEach(function (item, index, scope) {
                    //console.log(item.distance, item.height);
                    if (index === 0) {
                        context.moveTo(0, height - item.height * scaledHeight);
                    } 
                    else {
                        context.lineTo(item.distance * width, height - item.height * scaledHeight);
                    }
                });

                context.stroke();
                context.closePath();
            }
        },
    };

    /**
     * Hauptklasse
     * 
     */
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
        this.onShowTrackChange = this.onShowTrackChange.bind(this);
        this.onShowProfileChange = this.onShowProfileChange.bind(this);
        this.displaySign = this.displaySign.bind(this);
        this.initSigns = this.initSigns.bind(this);
        this.onToggleFlyAlongPath = this.onToggleFlyAlongPath.bind(this);

        this.chkShowTrack = document.querySelector("[name='show-track']");
        this.chkShowProfile = document.querySelector("[name='show-profile']");
        this.chkFlyAlongPath = document.querySelector("[name='fly-along-path']");

        this.showTrack = true;

        this.profilePanel = new ProfilePanel(document.querySelector(".profile"));

        this.init();
    };

    Main.prototype = {
        init : function () {
            var scene, projector, that = this;

            this.flying = false;
            this.origControls = null;

            this.container = document.createElement('div');
            document.body.appendChild(this.container);

            scene = this.scene = new THREE.Scene();
            //scene.add(new THREE.AxisHelper());

            this.initCamera(); 

            this.projector = projector = new THREE.Projector();

            this.initRenderer(); 

            this.onWindowResize();

            this.initTerrain();
            this.initPostprocessing();

            this.initEventListeners();

            $.get("includes/signs.json", this.initSigns);
        },

        initSigns : function (signs) {
            var that = this;
            if (this.terrainMesh !== null) {
                signs.forEach(this.displaySign);
            }
            else {
                window.setTimeout(function () {
                    that.initSigns(signs);
                }, 500);
            }
        },

        initCamera : function () {
            //camera
            var camera = this.camera = new THREE.PerspectiveCamera(20, 
                                                                   this.SCREEN_WIDTH / this.SCREEN_HEIGHT, 
                                                                   1, 
                                                                   2000);
            camera.position.x = 0;
            camera.position.y = 0
            camera.position.z = 500;
            this.scene.add(camera);
        },

        initControls : function () {
            var controls = this.controls = new THREE.TrackballControls(this.camera, this.container);
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
            this.container.addEventListener('mousedown', this.onDocumentMouseDown, true);
            this.chkShowProfile.addEventListener('change', this.onShowProfileChange, false);
            this.chkShowTrack.addEventListener('change', this.onShowTrackChange, false);
            this.chkFlyAlongPath.addEventListener('change', this.onToggleFlyAlongPath, false);
        },

        onShowProfileChange : function (e) {
            this.profilePanel.toggle(e.target.checked);
        },

        onShowTrackChange : function () {
            this.showTrack = this.chkShowTrack.checked;
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
                            callback(JSON.parse(xhr.responseText).Position);
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
            //roadMesh.rotation.x = -0.8;
            roadMesh.material.depthWrite = false;
            this.postprocessing.scene.add(this.roadMesh);

            this.updateLoadCounter();
        },

        onTrackJSONLoaded : function (geometry) {
            this.createPath(geometry);

            this.profilePanel.calculateTrackProfile(geometry);

            this.updateLoadCounter();
        },

        updateLoadCounter : function () {
            this.itemsLoaded += 1;
            if (this.itemsLoaded === this.itemsToLoad) {
                //this.terrainMesh.rotation.x = -0.8;
                this.clock.start();
                this.camera.lookAt(this.scene.position);
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
            $.ajax({
                type : "POST",
                url : "includes/jsonWriter.php",
                dataType : 'json',
                data : {
                    json : data,
                    p : "signs.json"
                }
            });
            this.displaySign(data);
        },

        onToggleFlyAlongPath : function (e) {
            if (!this.flying) {
                this.startFlying();
            }
            else {
                this.stopFlying();
            }
        },

        startFlying : function () {
            this.fying = true;


            this.origControls = this.controls;
            this.origCameraPosition = this.camera.position;

            var controls = new THREE.PathControls(this.camera);

            controls.waypoints = this.waypoints;
            controls.duration = 28
            controls.useConstantSpeed = true;
            //controls.createDebugPath = true;
            //controls.createDebugDummy = true;
            controls.lookSpeed = 0.06;
            controls.lookVertical = false;
            controls.lookHorizontal = false;
            controls.verticalAngleMap = { srcRange: [ 0, 2 * Math.PI ], dstRange: [ -0.5, -0.5 ] };
            controls.horizontalAngleMap = { srcRange: [ 0, 2 * Math.PI ], dstRange: [ -0.5, -0.5 ] };
            controls.lon = 180;
            controls.lat = 0;

            this.controls = controls;
            this.controls.init();

            this.scene.add(controls.animationParent);

            controls.animation.play(true, 0);
        },

        stopFlying : function () {
            this.flying = false;
            this.controls.animation.stop();
            this.camera.position = this.origCameraPosition;
            this.scene.remove(this.controls.animationParent);
            this.controls = this.origControls;
            this.controls.init();
        },

        displaySign : function (data) {
            var loader = new THREE.ColladaLoader(),
                material,
                terrainMesh = this.terrainMesh;

            loader.convertUpAxis = true;
            loader.load('resources/models/' + data.type + '.dae', function (collada) {
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

            this.render();

            if (this.camera.position.z === 2) {
                this.initControls();
                window.requestAnimationFrame(this.animate);
            }
            else {
                window.requestAnimationFrame(this.animateStart);
                this.camera.position.z -= 250 * delta;
                this.camera.position.z = this.camera.position.z < 2 ? 2 : this.camera.position.z;
            }
        },

        animate : function () {
            var delta = this.clock.getDelta();

            THREE.AnimationHandler.update(delta);
            this.controls.update(delta);
            
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

            if (this.showTrack) {
                renderer.render(this.track, this.camera);
            }
        },

        createPath : function (vertices) {
            var path = new THREE.Geometry(), 
                waypoints = [],
                vert, i, len = vertices.length, track,
                options = {
                    minFilter: THREE.LinearFilter,
                    stencilBuffer: false
                };

            for (i = 0; i < len; i += 1) {
                vert = vertices[i];
                waypoints.push(vert);
                path.vertices.push(
                    new THREE.Vector3(vert[0] - parseInt(vert[0], 10), 
                        vert[1] - parseInt(vert[1], 10), 
                        vert[2]) 
                );
            }

            this.waypoints = waypoints;

            path.computeVertexNormals();

            track = new THREE.Line(path, new THREE.LineBasicMaterial({
                color : 0xff0000,
                linewidth: 5,
                linecap : 'round',
                linejoin : 'round',
                vertexColors : false,
                fog : false
            }), THREE.LineStrip);
            
            track.position.set(-0.5, -0.5, 0.005);
            //track.rotation.x = -0.8;
            this.track = new THREE.Scene();

            this.track.add(track);
        }
    };

    main = new Main();
    SignWindow.init("window-sign");

}(window, document));


