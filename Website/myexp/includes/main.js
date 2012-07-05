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
     * @class SignWindow
     * @singleton
     *
     * @constructor Die Instanz des Fensters sollte über
     * SignWindow.init erzeugt werden erzeugt werden
     * @private
     * @param {String} id Id-Attributwert des HTML-Elements, das das 
     * Fenster-HTML beinhaltet. 
     */
    SignWindow = function (id) {
        /** @property 
        * Beinhaltet eine Referenz auf das Fensterelement */
        this.container = document.getElementById(id);

        /** @property 
        * Beinhaltet eine Referenz auf das Eingabeelement für Markertypen */
        this.signType = this.container.querySelector("[name='type']");
        /** @property 
        * Beinhaltet eine Referenz auf das Eingabeelement für Markernamen */
        this.signName = this.container.querySelector("[name='name']");
        /** @property 
        * Beinhaltet eine Referenz auf das Eingabeelement für x-Achsenabschnitt des Markers */
        this.signPositionX = this.container.querySelector("[name='x']");
        /** @property 
        * Beinhaltet eine Referenz auf das Eingabeelement für y-Achsenabschnitt des Markers */
        this.signPositionY = this.container.querySelector("[name='y']");
        /** @property 
        * Beinhaltet eine Referenz auf das Eingabeelement für z-Achsenabschnitt des Markers */
        this.signPositionZ = this.container.querySelector("[name='z']");

        /** @property 
        * Beinhaltet eine Referenz auf den OK-Button */
        this.ok = this.container.querySelector(".ok");
        /** @property 
        * Beinhaltet eine Referenz auf den Cancel-Button */
        this.cancel = this.container.querySelector(".cancel");

        // bindet die onOK und onCancel Methoden an this, sodass innerhalb
        // diser Methoden this IMMER auf die Instanz diese Objekts zeigen muss,
        // selbst wenn sie z.B. als EventHandler ausgeführt werden.
        this.onOk = this.onOk.bind(this);
        this.onCancel = this.onCancel.bind(this);

        this.ok.addEventListener("click", this.onOk, false);
        this.cancel.addEventListener("click", this.onCancel, false);


        /** @property 
        * Callback, der ausgeführt werden soll, wenn auf OK geklickt wurde */
        this.okCallback = null;
    };

    /**
     * Initialisiert das SignWindow Singleton.
     * SignWindow verweist dannach auf die Singleton Instanz des Fensters.
     * @param {String} id Id-Attributwert des HTML-Elements, das das 
     * Fenster-HTML beinhaltet.
     */
    SignWindow.init = function (id) {
        SignWindow = new SignWindow(id);
    };

    SignWindow.prototype = {
        /**
         * Zeigt die Fensterinstanz an.
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
         * @param {Function} okCallback Die Funktion die ausgeführt wird, sobald
         * der Dialog mit OK abgeschlossen wird.
         * @param {Object} okCallback.data Ein Datenobjekt selber Struktur, wie
         * das, das der SignWindo#show Methode mitgegeben wurde und das die Werte der
         * Formulareingabe beinhaltet.
         */
        show : function (data, okCallback) {
            this.signPositionX.value = data.position.x;
            this.signPositionY.value = data.position.y;
            this.signPositionZ.value = data.position.z;

            this.callback = okCallback;

            this.container.classList.add("show");
        },

        /**
         * Schließt das Fenster.
         */
        close : function () {
            this.container.classList.remove("show");
        },

        /**
         * @private
         * Behandelt Klicks auf den OK-Button. Ruft den OK-Callback auf und
         * schließt das Fenster.
         */
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

        /**
         * @private
         * Behandelt Klicks auf den Cancel-Button. Schließt das Fenster.
         */
        onCancel : function () {
            this.close();
        }
    };
    
    /**
     * Controller für das Panel auf dem das Profil einer Strecke gerendert
     * wird. 
     * @class
     * @constructor Erzeugt eine neue Instanz des ProfilPanels und verwendet
     * das HTMLElement als Leinwand für die Zeichnung.
     * @param {HTMLCanvasElement} element Das Element, das die View des Panels
     * darstellt.
     */
    ProfilePanel = function (element) {
        /** @property Referenz auf das Panel-Element. */
        this.element = element;

        this.render = this.render.bind(this);

        this.element.addEventListener("webkitTransitionEnd", this.render, false);
        this.element.addEventListener("transitionend", this.render, false);
        this.element.addEventListener("oTransitionEnd", this.render, false);
    };

    ProfilePanel.prototype = {
        /**
         * Blendet das ProfilPanel ein oder aus.
         * @param {Boolean} show Gint an, ob das Panel ein (true) oder
         * ausgeblendet (false) werden soll.
         */
        toggle : function (show) {
            this.element.classList[show ? "add" : "remove"]("show");
        },
        
        /**
         * Zeichnet die Strecke auf die Leinwand.
         */ 
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

        /** 
         * Berechnet das Profil der Strecke.
         * Sobald die Berechnung abgeschlossen ist, wird die Strecke durch
         * einen Aufruf von ProfilePanel#render gezeichnet
         * @param {Array} geometry Eine Liste von Punkten in der Form [x, y,
         * z] die die einzelnen Wegpunkte der Strecke darstellen.
         */
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
        
        /**
         * @private
         * Hilfsfuntion um eine berechnete CSS-Property zu ermitteln.
         * @param {String} property Name der CSS-Property dessen berechneter
         * Wert berechnet werden soll.
         */
        getStyle : function (property) {
            return parseInt(window.getComputedStyle(this.element, null).getPropertyValue(property), 10);
        }
    };

    /**
     * Die Hauptklasse, die für das initialisieren und den Ablauf der
    * render-Schleife zuständig ist.
     * @class
     *
     * @constructor Erstellt eine Instanz der Haupklasse, initialisiert alle
     * Properties und startet dann die Initialisierung aller benötigten Daten
     * um die Karte zu zeichnen, indem Main#init aufgerufen wird.
     */
    Main = function () {
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

        /** @property 
         * Enthält die sekundäre Szene, in die das Pfad-Prisma
         * gezeichnet wird, das dann per Stenciltest auf die Karte projeziiert
         * wird.
         */
        this.trackProjection = null;
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
        /**
         * Initialisiert alles was nötig ist um die Karte zu zeichnen. 
         * @private
         */ 
        init : function () {
            var scene, projector, that = this;

            this.flying = false;
            this.origControls = null;

            this.container = document.createElement('div');
            document.body.appendChild(this.container);

            scene = this.scene = new THREE.Scene();
            scene.add(new THREE.AxisHelper());

            this.initCamera(); 

            this.projector = projector = new THREE.Projector();

            this.initRenderer(); 

            this.onWindowResize();

            this.initTerrain();
            this.initPostprocessing();

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
            if (this.terrainMesh !== null) {
                signs.forEach(this.displaySign);
            }
            else {
                window.setTimeout(function () {
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
            camera.position.y = 0;
            camera.position.z = 500;
            this.scene.add(camera);
        },

        /**
         * Initialisiert die Steuerelemente.
         * @private
         * @param {Array} [keys=65,83,68] Die drei Tasten, mit denen
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

        /**
         * Initialisiert die event listener.
         * @private
         */ 
        initEventListeners : function () {
            //document.addEventListener('mousemove', onDocumentMouseMove, false);
            window.addEventListener('resize', this.onWindowResize, false);
            this.container.addEventListener('mousedown', this.onDocumentMouseDown, true);
            this.chkShowProfile.addEventListener('change', this.onShowProfileChange, false);
            this.chkShowTrack.addEventListener('change', this.onShowTrackChange, false);
            this.chkFlyAlongPath.addEventListener('change', this.onToggleFlyAlongPath, false);
        },

        /**
         * Behandlt Änderungen der "Profil anzeigen" Checkbox.
         * @private 
         */ 
        onShowProfileChange : function (e) {
            this.profilePanel.toggle(e.target.checked);
        },

        /**
         * Behandlt Änderungen der "Strecke anzeigen" Checkbox.
         * @private 
         */ 
        onShowTrackChange : function () {
            this.showTrack = this.chkShowTrack.checked;
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
            return document.getElementById(id);
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

            roadTest = new THREE.ShaderMaterial({
                vertexShader:   this.$('simple.vert').textContent,
                fragmentShader: this.$('test.frag').textContent
            });


            roadMesh = this.roadMesh = new THREE.Mesh(geometry, this.roadTest);
            roadMesh.position.set(-0.5, -0.5, 0.0);
            //roadMesh.rotation.x = -0.8;
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
            controls.duration = 28;
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


