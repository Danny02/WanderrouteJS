/*global THREE, Detector */
(function (global, doc) {
    if (!Detector.webgl) {
        Detector.addGetWebGLMessage();
    }

    global.WanderUte || (global.WanderUte = {});

    var WanderUte = global.WanderUte;

    /**
     * Die Hauptklasse, die für das initialisieren und den Ablauf der
     * render-Schleife zuständig ist.
     * @class
     *
     * @constructor Erstellt eine Instanz der Haupklasse, initialisiert alle
     * Properties und startet dann die Initialisierung aller benötigten Daten
     * um die Karte zu zeichnen, indem Main#init aufgerufen wird.
     */
    WanderUte.App = function () {
        this.container = null;

        this.camera = null;
        /** @property
         * Die Haupszene, auf die die Karte gezeichnet wird.
         */
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

        this.timeToWaitTillGrowAnimation = 0.5;
        this.growing = false;

        /** @property
         * Enthält die sekundäre Szene, in die das Pfad-Prisma
         * gezeichnet wird, das dann per Stenciltest auf die Karte projeziiert
         * wird.
         */
        this.trackProjection = {};
        /** @property
         * Scene, die den schwebenden Pfad und die Marker enthält.
         */
        this.trackScene = null;

        this.clock = new THREE.Clock();


        this.animate = this.animate.bind(this);
        this.animateStart = this.animateStart.bind(this);
        this.onDocumentMouseDown = this.onDocumentMouseDown.bind(this);
        this.onWindowResize = this.onWindowResize.bind(this);
        this.onMeshLoaded = this.onMeshLoaded.bind(this);
        this.onTrackMashLoaded = this.onTrackMashLoaded.bind(this);
        this.onTrackJSONLoaded = this.onTrackJSONLoaded.bind(this);
        this.onCreateSign = this.onCreateSign.bind(this);
        this.onShowMarkerChange = this.onShowMarkerChange.bind(this);
        this.onShowProfileChange = this.onShowProfileChange.bind(this);
        this.displaySign = this.displaySign.bind(this);
        this.initSigns = this.initSigns.bind(this);
        this.onToggleFlyAlongPath = this.onToggleFlyAlongPath.bind(this);

        this.onDetailScaleChange = this.onDetailScaleChange.bind(this);
        this.onAmbientScaleChange = this.onAmbientScaleChange.bind(this);
        this.onAmbientShiftChange = this.onAmbientShiftChange.bind(this);

        this.onDirLight1Change = this.onDirLight1Change.bind(this);
        this.onDirLight2Change = this.onDirLight2Change.bind(this);
        this.onMountainColorChange = this.onMountainColorChange.bind(this);
        this.onValleyColorChange = this.onValleyColorChange.bind(this);

        this.onTerrainBorderChange = this.onTerrainBorderChange.bind(this);


        this.chkShowMarker = doc.querySelector("[name='show-marker']");
        this.chkShowProfile = doc.querySelector("[name='show-profile']");
        this.chkFlyAlongPath = doc.querySelector("[name='fly-along-path']");

        this.rngTerrainBorder = doc.querySelector("[name='terrain-border']");
        this.rngDetailScale = doc.querySelector("[name='detail-scale']");
        this.rngAmbientScale = doc.querySelector("[name='ambient-scale']");
        this.rngAmbientShift = doc.querySelector("[name='ambient-shift']");

        this.colDirLight1 = doc.querySelector("[name='dir-light-1-color']");
        this.colDirLight2 = doc.querySelector("[name='dir-light-2-color']");

        this.colValley = doc.querySelector("[name='valley-color']");
        this.colMountain = doc.querySelector("[name='mountain-color']");
  
        this.showMarker = true;

        this.profilePanel = new global.WanderUte.ProfilePanel("profile-panel");
        this.signWindow = new global.WanderUte.SignWindow("window-sign");

        this.init();
    };

    WanderUte.App.prototype = {
        /**
         * Initialisiert alles was nötig ist um die Karte zu zeichnen.
         * @private
         */
        init : function () {
            var scene, projector, that = this;

            this.flying = false;
            this.origControls = null;

            this.container = doc.createElement('div');
            doc.body.appendChild(this.container);

            scene = this.scene = new THREE.Scene();
            scene.add(new THREE.AxisHelper());

            this.initCamera();

            this.projector = projector = new THREE.Projector();

            this.initRenderer();

            this.onWindowResize();

            this.initTerrain();
            this.initTrackProjection();
            this.initEventListeners();

            $.get("includes/signs.json", this.initSigns);
        },

        /**
         * Initialisiert die Marker
         * @private
         * @param {Array} signs Ein Array von Markern.
         */
        initSigns : function (signs) {
            var that = this;
            if (this.trackScene !== null) {
                // create signs and append to track scene
                signs.forEach(this.displaySign);
            }
            else {
                // wait 500ms and check again if the track scene is available
                // now.
                global.setTimeout(function () {
                    that.initSigns(signs);
                }, 500);
            }
        },

        /**
         * Initialisiert die Camera
         * @private
         */
        initCamera : function () {
            //camera
            var camera = this.camera = new THREE.PerspectiveCamera(20,
                                                                   this.SCREEN_WIDTH / this.SCREEN_HEIGHT,
                                                                   1,
                                                                   2000);
            camera.position.x = 0;
            camera.position.y = 1;
            camera.position.z = 20;
            this.scene.add(camera);
        },

        /**
         * Initialisiert die Steuerelemente.
         * @private
         * @param {Array} [keys=[65,83,68]] Die drei Tasten, mit denen
         * THREE.TrackballControls gesteuert werden können.
         */
        initControls : function (keys) {
            keys || (keys = [/*A*/ 65, /*S*/ 83, /*D*/ 68]);

            var controls = this.controls = new THREE.TrackballControls(this.camera, this.container);
            controls.rotateSpeed = 1.0;
            controls.zoomSpeed = 1.2;
            controls.panSpeed = 0.8;

            controls.noZoom = false;
            controls.noPan = false;

            controls.staticMoving = true;
            controls.dynamicDampingFactor = 0.3;

            controls.keys = keys;
        },

        /**
         * Initialisiert den Three.WebGLRenderer.
         * @private
         */
        initRenderer : function () {
            // RENDERER
            var renderer = this.renderer = new THREE.WebGLRenderer({
                antialias: true
            });
            renderer.setSize(global.innerWidth, global.innerHeight);
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

        /**
         * Initialisiert die event listener.
         * @private
         */
        initEventListeners : function () {
            //doc.addEventListener('mousemove', onDocumentMouseMove, false);
            global.addEventListener('resize', this.onWindowResize, false);
            this.container.addEventListener('mousedown', this.onDocumentMouseDown, true);
            this.chkShowProfile.addEventListener('change', this.onShowProfileChange, false);
            this.chkShowMarker.addEventListener('change', this.onShowMarkerChange, false);
            //this.chkFlyAlongPath.addEventListener('change', this.onToggleFlyAlongPath, false);
            
            this.rngDetailScale.addEventListener('change', this.onDetailScaleChange, false);
            this.rngAmbientScale.addEventListener('change', this.onAmbientScaleChange, false);
            this.rngAmbientShift.addEventListener('change', this.onAmbientShiftChange, false);
            this.rngTerrainBorder.addEventListener('change', this.onTerrainBorderChange, false);

            this.colDirLight1.addEventListener('change', this.onDirLight1Change, false);
            this.colDirLight2.addEventListener('change', this.onDirLight2Change, false);
            this.colMountain.addEventListener('change', this.onMountainColorChange, false);
            this.colValley.addEventListener('change', this.onValleyColorChange, false);
        },

        /**
         * Behandlt Änderungen der "Profil anzeigen" Checkbox.
         * @private
         */
        onShowProfileChange : function (e) {
            this.profilePanel.toggle(e.target.checked);
        },

        /**
         * Behandlt Änderungen der "Marker anzeigen" Checkbox.
         * @private
         */
        onShowMarkerChange : function (e) {
            this.showMarker = this.chkShowMarker.checked;
        },

        onTerrainBorderChange  : function (e) {
            this.shaderUniforms.terrainBorder.value = this.rngTerrainBorder.value;
        },

        onDetailScaleChange  : function (e) {
            this.shaderUniforms.DETAIL_SCALE.value = Math.sqrt(this.rngDetailScale.value);
        },

        onAmbientScaleChange : function (e) {
            this.shaderUniforms.AMBIENT_SCALE.value = this.rngAmbientScale.value;
        },

        onAmbientShiftChange : function (e) {
            this.shaderUniforms.AMBIENT_SHIFT.value = this.rngAmbientShift.value;
        },

        onDirLight1Change : function (e) {
            this.shaderUniforms.blue.value = this.hexColorToVector3f(this.colDirLight1.value);
        }, 

        onDirLight2Change : function (e) {
            this.shaderUniforms.orange.value = this.hexColorToVector3f(this.colDirLight2.value);
        },

        onMountainColorChange : function (e) {
            this.shaderUniforms.mountain_color.value = this.hexColorToVector3f(this.colMountain.value);
        }, 

        onValleyColorChange : function (e) {
            this.shaderUniforms.valley_color.value = this.hexColorToVector3f(this.colValley.value);
        },

        hexColorToVector3f : function (hex) {
            var r = parseInt(hex.substr(1, 2), 16),
                g = parseInt(hex.substr(3, 2), 16),
                b = parseInt(hex.substr(5, 2), 16);

            return new THREE.Vector3(r / 255, g / 255, b / 255);
            
        },

        /**
         * Initialisiert das Kartenterrain, indem die Terraindaten mittels
         * THREE.CTMLoader geladen werden. Sobald der Ladevorgang abgeschlossen
         * ist, wird Main#onMeshLoaded aufgerufen.
         * @private
         */
        initTerrain : function () {
            var loader,
                shaderUniforms;

            this.shaderUniforms = shaderUniforms = {
                normalMap: {
                    type: "t",
                    value: 0,
                    texture: THREE.ImageUtils.loadTexture("resources/normalmap.png")
                },
                time: {
                    type: "f",
                    value: 1.0
                },
                blue : {
                    type: "v3",
                    value : new THREE.Vector3(74 / 255.0, 96 / 255.0, 126 / 255.0)
                },
                orange : {
                    type : "v3",
                    value : new THREE.Vector3(126 / 255.0, 90 / 255.0, 51 / 255.0)
                },
                mountain_color : {
                    type: "v3",
                    value : new THREE.Vector3(126 / 255.0, 90 / 255.0, 51 / 255.0)
                },
                valley_color : {
                    type : "v3",
                    value : new THREE.Vector3(126 / 255.0, 90 / 255.0, 51 / 255.0)
                },
                DETAIL_SCALE : {
                    type : "f",
                    value : 2.0
                },
                AMBIENT_SCALE : {
                    type : "f",
                    value : 1.0
                },
                AMBIENT_SHIFT : {
                    type : "f",
                    value : 0.4
                },
                terrainBorder : {
                    type : "f",
                    value : 0.2
                }
            };

            this.onTerrainBorderChange();
            this.onDetailScaleChange();
            this.onAmbientScaleChange();
            this.onAmbientShiftChange();
            this.onDirLight1Change();
            this.onDirLight2Change();
            this.onMountainColorChange();
            this.onValleyColorChange();

            loader = new THREE.CTMLoader(this.renderer.context);
            loader.load("resources/map.ctm", this.onMeshLoaded, false, false);
        },


        /**
         * Initialisiert das Projektionsprisma des Pfades mittels
         * THREE.CTMLoader. Sobald der Pfad geladen ist, wird
         * Main#onTrackMashLoaded aufgerufen.
         * Zudem werden die Wegpunkte des Pfades als JSON geladen, und sobald
         * der Ladevorgang abgeschlossen ist, die Main#onTrackJSONLoaded
         * methode ausgeführt.
         * @private
         */
        initTrackProjection : function () {
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

        /**
         * Hilfsfunktion um ein Element anhand dessen id-Attributs zu
         * ermitteln.
         * @private
         */
        $ : function (id) {
            return doc.getElementById(id);
        },

        /**
         * Callback der aufgerufen wird, sobald das Mesh der Karte fertig
         * geladen wurde.
         * Initialisiert ein Three.Mesh objekt mit dieser Geometrie und fügt
         * dieses der Haupszene hinzu.
         * @private
         * @param {THREE.Geometry} geometry. Objekt, dass die Geoetrie der
         * Karte enthält.
         */
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

        /**
         * Callback, der ausgeführt wird, sobald das Mesh des Pfades
         * vollständig geladen ist.
         * Bereitet dann das Trackprisma für die Projektion auf die Karte vor
         * und platziert es an der richtigen Stelle im Raum und fügt es der
         * Trackprojektions-Szene hinzu.
         * @private
         * @param {THREE.Geometry} geometry. Objekt, dass die Geoetrie der
         * Strecke enthält.
         */
        onTrackMashLoaded : function (geometry) {
            var trackProjection = this.trackProjection,
                options = {
                    minFilter: THREE.LinearFilter,
                    stencilBuffer: false
                },
                roadTest,
                roadMesh;

            trackProjection.scene = new THREE.Scene();

            trackProjection.rtTextureDepth = new THREE.WebGLRenderTarget(this.SCREEN_WIDTH, this.SCREEN_HEIGHT, options);

            roadTest = new THREE.MeshBasicMaterial({
                opacity : 0.3,
                transparent : true,
                color : 0xff0000
            });


            roadMesh = this.roadMesh = new THREE.Mesh(geometry, roadTest);
            roadMesh.position.set(-0.5, 0.0, -0.5);
            roadMesh.rotation.x = 1.0;
            roadMesh.material.depthWrite = false;
            this.trackProjection.scene.add(this.roadMesh);

            this.updateLoadCounter();
        },

        /**
         * Callback, der aufgerufen wird, sobald die Wegpunkte der Strecke
         * vollständig geladen wurden.
         * Dannach werden zunächst #createPath und dann
         * ProfilePanel#calculateTrackProfile aufgerufen.
         * @private
         * @param {Array} geometry Array, der alle Wegpunkte in der Form [x, y, z] beinhaltet.
         */
        onTrackJSONLoaded : function (geometry) {
            this.createPath(geometry);

            this.profilePanel.calculateTrackProfile(geometry);

            this.updateLoadCounter();
        },

        /**
         * Updatet den Counter, der zählt, wie viele Asynchrone loader bereits mit dem Laden fertig sind.
         * Sobald alle Loader fertig sind, wird die Uhr und die Animation gestartet.
         * @private
         */
        updateLoadCounter : function () {
            this.itemsLoaded += 1;
            if (this.itemsLoaded === this.itemsToLoad) {
                //this.terrainMesh.rotation.x = -0.8;
                this.clock.start();
                this.camera.lookAt(this.scene.position);
                this.growing = true;
                this.animateStart();
            }
        },

        /**
         * Behandelt MouseDown Events.
         * @private
         * @param {MouseEvent} e MouseEvent.
         */
        onDocumentMouseDown : function (e) {
            if (e.button === 2) {
                e.preventDefault();
                var intersects = this.mousePositionToPointOnMap(e.clientX, e.clientY);
                if (intersects.length > 0) {
                    this.showSignWindow(intersects[0].point);
                }
                else {
                    console.log("No intersections found");
                }
            }
        },

        /**
         * Projeziert den Punkte (x, y) des Bildauschnitts in das
         * Weltkoordinatensystem der Karte und ermittlet den Schnittpunkt mit
         * der Karte.
         * @private
         * @param {Number} x Mausposition auf der x-Achse
         * @param {Number} y Mausposition auf der y-Achse
         * @return {Array} intersects Liste der Schnittpunkte.
         */
        mousePositionToPointOnMap : function (x, y) {
            var camera = this.camera,
                vector = new THREE.Vector3((x / this.SCREEN_WIDTH) * 2 - 1,
                    - (y / this.SCREEN_HEIGHT) * 2 + 1,
                    // Percentage of diff between near and far to go towards far
                    // starting at near to determine intersection.
                    // @see https://github.com/sinisterchipmunk/jax/blob/5d392c9d67cb9ae5623dc03846027c473f625925/src/jax/webgl/camera.js#L568
                    0.2),
                ray,
                intersects;

            this.projector.unprojectVector(vector, camera);

            ray = new THREE.Ray(camera.position, vector.subSelf(camera.position).normalize());

            intersects = ray.intersectObject(this.terrainMesh);

            return intersects;
        },

        /**
         * Öffnet das Fenster zum Erstellen eines neuen Markers.
         * @param {Object} position Position des Markers
         * @param {Number} position.x X-Achsen Abschnitt der
         * Markerposition
         * @param {Number} position.y Y-Achsen Abschnitt der
         * Markerposition
         * @param {Number} position.z Z-Achsen Abschnitt der
         * Markerposition
         * @param {Object} [data] Zusätzliche Markerinformationen
         * @param {String} [data.name] Der Name des Markers
         * @param {String} [data.type] Der Typ des markers (Dateinahme der
         * Marker-dateien ohne Dateiendung)
         */
        showSignWindow : function (position, data) {
            data || (data = {name : "", type : ""});
            this.signWindow.show({
                name : data.name || "",
                type : data.type || null,
                position: position || {x : "", y : "", z : ""}
            }, this.onCreateSign);
        },

        /**
         * Erstellt ein neues Zeichen und speichert es auf dem Server.
         * @private
         * @param {Object} data Datenobjekt, das die Informationen eines
         * Markers beinhaltet.
         * @param {Object} data.position Position des Markers
         * @param {Number} data.position.x X-Achsen Abschnitt der
         * Markerposition
         * @param {Number} data.position.y Y-Achsen Abschnitt der
         * Markerposition
         * @param {Number} data.position.z Z-Achsen Abschnitt der
         * Markerposition
         * @param {String} data.name Der Name des Markers
         * @param {String} data.type Der Typ des markers (Dateinahme der
         * Marker-dateien ohne Dateiendung)
         */
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



        /**
         * Läd den Marker per THREE.ColladaLoader und fügt ihn dann in
         * Main#trackScene ein.
         * @param {Object} data Datenobjekt, das die Informationen eines
         * Markers beinhaltet.
         * @param {Object} data.position Position des Markers
         * @param {Number} data.position.x X-Achsen Abschnitt der
         * Markerposition
         * @param {Number} data.position.y Y-Achsen Abschnitt der
         * Markerposition
         * @param {Number} data.position.z Z-Achsen Abschnitt der
         * Markerposition
         * @param {String} data.name Der Name des Markers
         * @param {String} data.type Der Typ des markers (Dateinahme der
         * Marker-dateien ohne Dateiendung)
         */
        displaySign : function (data) {
            var loader = new THREE.ColladaLoader(),
                material,
                trackScene = this.trackScene;

            loader.convertUpAxis = true;
            loader.load('resources/models/' + data.type + '.dae', function (collada) {
                THREE.SceneUtils.traverseHierarchy(collada.scene, function (object) {
                    object.scale.set(0.01, 0.01, 0.01);
                    object.position.set(data.position.x * 1, data.position.y * 1 + 0.05, data.position.z * 1);

                    if ((material = object.material)) {
                        object.material = new THREE.MeshBasicMaterial({
                            color : 0xFFFFFF,
                            map: material.map,
                            morphTargets: material.morphTargets
                        });
                    }
                });
                trackScene.add(collada.scene);
            });
        },

        /**
         * Behandelt window resize Events, indem die Cameraposition neu
         * berechnet.
         * @private
         * @param {Event} event Resize-Event
         */
        onWindowResize : function (event) {
            this.SCREEN_WIDTH = global.innerWidth;
            this.SCREEN_HEIGHT = global.innerHeight;

            this.renderer.setSize(this.SCREEN_WIDTH, this.SCREEN_HEIGHT);

            this.camera.aspect = this.SCREEN_WIDTH / this.SCREEN_HEIGHT;
            this.camera.updateProjectionMatrix();
        },

        /**
         * Animations-Schleife, die zu Beginn des Renderings aufgerufen wird und
         * für das Hineinfliegen in die Karte zuständig ist. Nachdem
         * Main#camera.position.z < 2 ist, wird im Folgenden Main#animate
         * aufgerufen.
         * @private
         */
        animateStart : function () {
            var delta = this.clock.getDelta(),
                posY, posZ;


            this.render();

            if (this.camera.position.z === 2) {
                this.initControls();
                global.requestAnimationFrame(this.animate);
            }
            else {
                global.requestAnimationFrame(this.animateStart);
                posZ = this.camera.position.z - 10 * delta;
                posY = this.camera.position.y - delta;
                this.camera.position.y = posY < 1 ? 1 : posY;
                this.camera.position.z = posZ < 2 ? 2 : posZ;
                this.camera.lookAt(this.scene.position);
            }
        },

        /**
         * Hauptanimations-Scheife.
         * @private
         */
        animate : function () {
            var delta = this.clock.getDelta();

            if (this.shaderUniforms.time.value / 3 < 1) {
                this.timeToWaitTillGrowAnimation -= delta;
                if (this.timeToWaitTillGrowAnimation < 0) {
                    this.shaderUniforms.time.value += delta;
                }
                this.growing = true;
            }
            else {
                this.growing = false;
            }
            THREE.AnimationHandler.update(delta);
            this.controls.update(delta);

            if (this.flight) {
                this.flight.update(delta);
            }


            this.render();
            global.requestAnimationFrame(this.animate);
        },

        /**
         * Zeichnet die Karte und die Projektion darauf.
         * Sofern Main#showMarker auf true gesetzt ist, wird auch der Track als
         * Linie, sowie die Marker gezeichnet.
         * @private
         */
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
            renderer.render(this.trackProjection.scene, this.camera);

            gl.stencilOp(gl.KEEP, gl.DECR, gl.KEEP);
            renderer.setFaceCulling("back");
            renderer.render(this.trackProjection.scene, this.camera);
            gl.colorMask(true, true, true, true);

            //weg rendern
            gl.stencilFunc(gl.NOTEQUAL, 0, 0xFF);
            renderer.setFaceCulling("front");
            this.roadMesh.material.depthTest = false;
            renderer.render(this.trackProjection.scene, this.camera);

            //GL state zurücksetzten
            gl.disable(gl.STENCIL_TEST);
            renderer.setFaceCulling("back");

            if (!this.growing && this.showMarker) {
                renderer.render(this.trackScene, this.camera);
            }
        },

        /**
         * Erstellt den Pfad aus einem Array von Wegpunkten.
         * @private
         * @param {Array} vertices Liste der Wegpunkte in der Form [x, y, z],
         * die den Pfad beschreiben.
         */
        createPath : function (vertices) {
            var path = new THREE.Geometry(),
                vert, 
                i, 
                len = vertices.length, 
                track,
                light,
                waypoints = []; 

            for (i = 0; i < len; i += 40) {
                vert = vertices[i];
                waypoints.push([vert[0], vert[1], vert[2]]);
                path.vertices.push(
                    new THREE.Vector3(vert[0],
                        vert[1] * (1 / 105),
                        vert[2])
                );
            }

            this.waypoints = waypoints;

            path.computeVertexNormals();

            track = new THREE.Line(path, new THREE.LineBasicMaterial({
                color : 0xff0000,
                linewidth: 2,
                linecap : 'round',
                linejoin : 'round',
                vertexColors : false,
                fog : false
            }), THREE.LineStrip);

            track.position.set(-0.5, 0.000, -0.5);
            //track.rotation.x = -0.8;
            this.trackScene = new THREE.Scene();

            this.trackScene.add(track);
        },



        /**
         * Sofern Main#flying true ist, man also über den Pfad fliegt,
         * wird Main#stopFlying aufgerufen, ansonsten Main#startFlying.
         * @private
         */
        onToggleFlyAlongPath : function (e) {
            if (!this.flying) {
                this.startFlying();
            }
            else {
                this.stopFlying();
            }
        },


        /**
         * Startet den Flug entlang des Pfads.
         */
        startFlying : function () {
            this.fying = true;

/*
            this.origControls = this.controls;
            this.origCameraPosition = this.camera.position;

            var controls = new THREE.PathControls(this.camera);

            controls.waypoints = this.waypoints;
            controls.duration = 28;
            controls.useConstantSpeed = true;
            //controls.createDebugPath = true;
            //controls.createDebugDummy = true;
            controls.lookSpeed = 0.06;
            controls.lookVertical = true;
            controls.lookHorizontal = true;
            controls.verticalAngleMap = { srcRange: [ 0, 2 * Math.PI ], dstRange: [ -2.1, -4.8 ] };
            controls.horizontalAngleMap = { srcRange: [ 0, 2 * Math.PI ], dstRange: [0.3, Math.PI - 0.3 ] };
            controls.lon = 180;
            controls.lat = 0;
            this.controls = controls;
            this.controls.init();

            this.scene.add(controls.animationParent);

            controls.animation.play(true, 0);
            */

            this.flight = (new WanderUte.PathFlight({
                camera : this.camera,
                waypoints : this.waypoints,
                duration : 60
            })).init();
        },

        /**
         * Stopt den Flug entlang des Pfads.
         */
        stopFlying : function () {
            this.flying = false;
            this.controls.animation.stop();

            delete this.controls.animation;
            delete this.controls;

            this.scene.remove(this.controls.animationParent);
            this.controls = this.origControls;
            this.camera.position = this.origCameraPosition;
            this.controls.init();
        },
/*
        onFlyPath : function(){
            this.flyPath(0);
        },

        flyPath : function(i){
            var that = this;

            console.log("Fly Path!");
            i || (i = 0);

            //remember old camera position


            console.log(this.track);

            var vertices = this.track.children[0].geometry.vertices;
            
            if(vertices.length > i){
                this.camera.position.z = vertices[i].z + 3;
                this.camera.position.y = vertices[i].y;
                this.camera.position.x = vertices[i].x;
                console.log(this.camera.position);
                window.setTimeout(function (){
                    that.flyPath(++i);
                }, 500);
            }
        }*/
    };

    WanderUte.App.start = function () {
        var app = new WanderUte.App();
    };

}(window, document));


