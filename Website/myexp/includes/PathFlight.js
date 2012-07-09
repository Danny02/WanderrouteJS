/*global THREE */
(function (global, doc) {
    global.WanderUte || (global.WanderUte = {});

    var WanderUte = global.WanderUte;

    WanderUte.PathFlight = function (settings) {
        this.waypoints = settings.waypoints;
        this.duration = (settings.duration || 60);
        this.totalDistance = 0;
        this.stepDistance = [];
        this.camera = settings.camera;

        this.speed = 0;

        this.currentStep = 0;
        this.positionInStep = 0;
    };
    // better:
    // https://github.com/mrdoob/three.js/blob/master/src/extras/controls/PathControls.js
    WanderUte.PathFlight.prototype = {
    
        init : function () {
            this.calculateTotalLength();

            this.speed = this.totalDistance / this.duration;

            return this;
        },

        calculateTotalLength : function () {
            var waypoints = this.waypoints,
                len = waypoints.length,
                completeDistance = 0,
                distance,
                i = 0,
                current = null,
                prev = this.waypoints[0],
                that = this;
            
            this.waypoints.forEach(function (wp, i) {
                that.waypoints[i][1] *= (1 / 105);
            });

            for (i = 1; i < len; i += 1) {
                current = waypoints[i];

                distance = Math.sqrt(Math.pow(current[0] - prev[0], 2) + 
                                     Math.pow(current[1] - prev[1], 2) + 
                                     Math.pow(current[2] - prev[2], 2));

                this.stepDistance.push(distance);
                completeDistance += distance;

                prev = current;
            }

            this.totalDistance = completeDistance;
        },

        update : function (delta) { 
            delta *= 100;
            var stepDistance = this.stepDistance[this.currentStep],
                stepSpeed = stepDistance / ((stepDistance / this.totalDistance) * this.duration),
                newPosition = this.positionInStep + delta * stepSpeed,
                position,
                start, 
                end;


            if (newPosition > 1) {
                delta = (1.0 - this.positionInStep) / stepSpeed;

                this.currentStep = this.currentStep >= this.stepDistance.length ? 0 : this.currentStep + 1;

                this.positionInStep = 0;
                this.update(delta / 100);
            }
            else {
                start = this.waypoints[this.currentStep];
                end = this.waypoints[this.currentStep + 1];
                
                this.camera.position.x = start[0] + (end[0] - start[0]) * newPosition;
                this.camera.position.y = start[1] + (end[1] - start[1]) * newPosition + 2;
                this.camera.position.z = start[2] + (end[2] - start[2]) * newPosition;

                this.camera.lookAt({
                    x : this.camera.position.x,
                    y : 0,
                    z : this.camera.position.z
                });

                this.positionInStep = newPosition;
            }
            
        }
        
    };


}(window, document));
