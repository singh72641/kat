var oa = angular.module('oa', ['ngWebSocket','amChartsDirective']);

oa.directive('ngEnter', function() {
        return function(scope, element, attrs) {
            element.bind("keydown keypress", function(event) {
                if(event.which === 13) {
                        scope.$apply(function(){
                                scope.$eval(attrs.ngEnter);
                        });

                        event.preventDefault();
                }
            });
        };
});

oa.directive( 'editInPlace', function() {
  return {
    restrict: 'E',
    scope: { value: '=' },
    template: '<span ng-click="edit()" ng-bind="value"></span><input ng-model="value"></input>',
    link: function ( $scope, element, attrs ) {
      // Let's get a reference to the input element, as we'll want to reference it.
      var inputElement = angular.element( element.children()[1] );

      // This directive should have a set class so we can style it.
      element.addClass( 'edit-in-place' );

      // Initially, we're not editing.
      $scope.editing = false;

      // ng-click handler to activate edit-in-place
      $scope.edit = function () {
        $scope.editing = true;

        // We control display through a class on the directive itself. See the CSS.
        element.addClass( 'active' );

        // And we must focus the element.
        // `angular.element()` provides a chainable array, like jQuery so to access a native DOM function,
        // we have to reference the first element in the array.
        inputElement[0].focus();
      };

      // When we leave the input, we're done editing.
      inputElement.prop( 'onblur', function() {
        $scope.editing = false;
        element.removeClass( 'active' );
      });
    }
  };
});

oa.directive('oaProgress', [function () {
        return {
            restrict: 'E',
            scope: {
                size: '@', // Size of element in pixels.
                strokeWidth: '@', // Width of progress arc stroke.
                stroke: '@', // Color/appearance of stroke.
                complete: '=' // Expression evaluating to float [0.0, 1.0]
            },
            compile: function (element, attr) {

                return function (scope, element, attr) {
                    // Firefox has a bug where it doesn't handle rotations and stroke dashes correctly.
                    // https://bugzilla.mozilla.org/show_bug.cgi?id=949661
                    scope.offset = /firefox/i.test(navigator.userAgent) ? -89.9 : -90;
                    var updateRadius = function () {
                        scope.strokeWidthCapped = Math.min(scope.strokeWidth, scope.size / 2 - 1);
                        scope.radius = Math.max((scope.size - scope.strokeWidthCapped) / 2 - 1, 0);
                        scope.circumference = 2 * Math.PI * scope.radius;
                    };
                    scope.$watchCollection('[size, strokeWidth]', updateRadius);
                    updateRadius();
                    console.log("Testing compiler");
                    console.log(scope.radius);
                    console.log(scope.circumference);
                    console.log(scope.complete);
                };
            },
            template:
                '<svg ng-attr-width="{{size}}" ng-attr-height="{{size}}">' +
                    '<circle class="ngpa-background" fill="transparent" ' +
                        'cx="{{size/2}}" ' +
                        'cy="{{size/2}}" ' +
                        'r="{{radius}}" ' +
                        'stroke-dasharray="{{circumference}}" ' +
                        'stroke-dashoffset="0" ' +
                        'stroke="#666" ' +
                        'stroke-width="{{strokeWidthCapped}}"' +
                        '/>' +
                         '<circle class="ngpa-background" fill="transparent" ' +
                        'cx="{{size/2}}" ' +
                        'cy="{{size/2}}" ' +
                        'r="{{radius}}" ' +
                        'stroke-dasharray="{{circumference}}" ' +
                        'stroke-dashoffset="{{(1 - complete) * circumference}}"' +
                        'stroke="#FF9F1E" ' +
                        'stroke-width="{{strokeWidthCapped}}"' +
                        '/>' +
                '</svg>'
        };
    }]);

oa.controller('OaMainCtrl', ['$scope', '$websocket',function ($scope, $websocket) {

    $scope.ws = $websocket('ws://localhost:8080/trading');
    $scope.tradeType = "Call Spread";
    $scope.symbol = "NIFTY";
    $scope.lots = 1;
    $scope.plotChartData = [];

    $scope.send = function(message) {
        if (angular.isString(message)) {
          $scope.ws.send(message);
        }
        else if (angular.isObject(message)) {
          $scope.ws.send(JSON.stringify(message));
        }
    };

    $scope.sendEvent = function(event, message) {
        var msg = {};
        msg.event = event;
        msg.data = message;
        $scope.send(msg);
    };

    $scope.addPos = function(option, opType){
      var msg = {};
      msg.event = "AddPos";
      msg.opType = opType;
      msg.option = option;
      msg.lots = $scope.lots;
      $scope.send(msg);
    };

    $scope.ws.onMessage(function(message) {
           var data = JSON.parse(message.data);

           if( data.event == "oc" )
           {
             $scope.optionData = data.data;
           }
           else if( data.event == "trade")
           {
             $scope.trade = data.data;
           }
           else if( data.event == "chartdata")
           {
             $scope.plotChartData = data.data;
             console.log($scope.plotChartData);
             $scope.$broadcast('amCharts.updateData', $scope.plotChartData );
           }
    });

    $scope.remvovePos = function(position, idx) {
      var msg = {};
      msg.event = "removePos";
      msg.data = position;
      $scope.send(msg);
      $scope.trade.positions.splice(idx,1);
    };

    $scope.send({"symbol":$scope.symbol, "event": "oc"});

    $scope.amChartOptions = {
        "type": "serial",
        "theme": "light",
        "marginRight": 40,
        "marginLeft": 80,
        "autoMarginOffset": 10,
        "autoMargins": true,
        "dataDateFormat": "YYYY-MM-DD",
        "valueAxes": [{
            "id": "v1",
            "axisAlpha": 0,
            "position": "left",
            "ignoreAxisWidth": true
        }, {
            "id": "v2",
            "axisAlpha": 0,
            "gridAlpha": 0,
            "position": "right",
            "ignoreAxisWidth": true
        }],
        "balloon": {
            "borderThickness": 1,
            "shadowAlpha": 0
        },
        "graphs": [{
            "id": "g1",
            "balloon": {
                "drop": false,
                "adjustBorderColor": false,
                "color": "#ffffff"
            },
            "lineThickness": 2,
            "title": "red line",
            "useLineColorForBulletBorder": true,
            "valueField": "value",
            "bulletField": "bullet",
            "bulletSize": 15,
            "balloonText": "<span style='font-size:18px;'>[[value]]</span>"
        }],
        "chartCursor": {
            "pan": true,
            "valueLineEnabled": true,
            "valueLineBalloonEnabled": true,
            "cursorAlpha": 1,
            "cursorColor": "#258cbb",
            "limitToGraph": "g1",
            "valueLineAlpha": 0.2
        },
        "valueScrollbar": {
            "oppositeAxis": false,
            "offset": 50,
            "scrollbarHeight": 10
        },
        "categoryField": "price",
        "categoryAxis": {
            "parseDates": false,
            "dashLength": 1,
            "minorGridEnabled": false,
            "guides": [
               {
                   "fillAlpha": 0.5,
                   "category": $scope.strikePrice,
                   "toCategory": ($scope.maxPrice),
                   "lineColor": "#CC0000",
                   "lineAlpha": 1,
                   "dashLength": 2,
                   "inside": true
               },
               {
                   "fillAlpha": 0.5,
                   "category": $scope.stdUpMarker1,
                   "lineColor": "#FF00FF",
                   "lineAlpha": 1,
                   "lineThickness": 2,
                   "dashLength": 2,
                   "inside": true
               },
               {
                   "fillAlpha": 0.5,
                   "category": $scope.stdDownMarker1,
                   "lineColor": "#ff0000",
                   "lineAlpha": 1,
                   "lineThickness": 2,
                   "dashLength": 2,
                   "inside": true
               }
            ]
        },
        "export": {
            "enabled": true
        },
        "data": $scope.plotChartData
    };
}]);
