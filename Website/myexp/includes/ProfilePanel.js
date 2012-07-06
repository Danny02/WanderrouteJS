(function (global, doc) {
    
    global.WanderUte || (global.WanderUte = {});

    var WanderUte = global.WanderUte;

    /**
     * Controller für das Panel auf dem das Profil einer Strecke gerendert
     * wird. 
     * @class
     * @constructor Erzeugt eine neue Instanz des ProfilPanels und verwendet
     * das HTMLElement als Leinwand für die Zeichnung.
     * @param {String} id Id-Attributwert des HTMLCanvas-Elements, das das 
     * als Leinwand für das Profil dienen soll. 
     */
    WanderUte.ProfilePanel = function (id) {
        /** @property Referenz auf das Panel-Element. */
        this.element = doc.getElementById(id);

        this.render = this.render.bind(this);

        this.element.addEventListener("webkitTransitionEnd", this.render, false);
        this.element.addEventListener("transitionend", this.render, false);
        this.element.addEventListener("oTransitionEnd", this.render, false);
    };

    WanderUte.ProfilePanel.prototype = {
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
            return parseInt(global.getComputedStyle(this.element, null).getPropertyValue(property), 10);
        }
    };

}(window, document));
