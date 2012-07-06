/*global WanderUte */
(function (global, doc) {
 
    global.WanderUte || (global.WanderUte = {});

    var WanderUte = global.WanderUte;

    /**
     * Zeigt ein Fenster an, in dem sich Daten über einen Marker (Parkplatz, Toilette, Essen)
     * eingeben und speichern lassen.
     *
     * @constructor Initialisiert das SignWindow.
     * @param {String} id Id-Attributwert des HTML-Elements, das das 
     * Fenster-HTML beinhaltet. 
     */
    WanderUte.SignWindow = function (id) {
        /** @property 
        * Beinhaltet eine Referenz auf das Fensterelement */
        this.element = document.getElementById(id);

        /** @property 
        * Beinhaltet eine Referenz auf das Eingabeelement für Markertypen */
        this.signType = this.element.querySelector("[name='type']");
        /** @property 
        * Beinhaltet eine Referenz auf das Eingabeelement für Markernamen */
        this.signName = this.element.querySelector("[name='name']");
        /** @property 
        * Beinhaltet eine Referenz auf das Eingabeelement für x-Achsenabschnitt des Markers */
        this.signPositionX = this.element.querySelector("[name='x']");
        /** @property 
        * Beinhaltet eine Referenz auf das Eingabeelement für y-Achsenabschnitt des Markers */
        this.signPositionY = this.element.querySelector("[name='y']");
        /** @property 
        * Beinhaltet eine Referenz auf das Eingabeelement für z-Achsenabschnitt des Markers */
        this.signPositionZ = this.element.querySelector("[name='z']");

        /** @property 
        * Beinhaltet eine Referenz auf den OK-Button */
        this.ok = this.element.querySelector(".ok");
        /** @property 
        * Beinhaltet eine Referenz auf den Cancel-Button */
        this.cancel = this.element.querySelector(".cancel");

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


    WanderUte.SignWindow.prototype = {
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
            this.signName.value = data.name;
            this.signType.value = data.type;

            this.callback = okCallback;

            this.element.classList.add("show");
        },

        /**
         * Schließt das Fenster.
         */
        close : function () {
            this.element.classList.remove("show");
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

}(window, document));
