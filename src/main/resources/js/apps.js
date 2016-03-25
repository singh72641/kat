var oa = angular.module('oa', ['ui.bootstrap','ngWebSocket','amChartsDirective','angularAutocomplete']);

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

oa.controller('OaMainCtrl', ['$scope', '$websocket', '$q', function ($scope, $websocket, $q) {

    $scope.ws = $websocket('ws://localhost:8080/trading');
    $scope.tradeType = "VERTICALSC";
    $scope.symbol = "NIFTY";
    $scope.lots = 1;
    $scope.plotChartData = {chartData:[]};
    $scope.oiData = [];
    $scope.symbolData = [];
    $scope.expMonths = [];
    $scope.algoChartData = [];
    $scope.AlgoTrade = [];
    $scope.selectedSymbol= {symbol: "NIFTY"};
    $scope.template = "algo.html";
    $scope.algo = {start_date: "2010-01-01", end_date: "2016-01-01"};
    $scope.page = 1;

    $scope.datePickOpen=false;

    $scope.init = function(){
      //$scope.processSymbol($scope.selectedSymbol);
      //$scope.switchTemplate("analyze.html");
      $scope.switchTemplate("optionchain.html");
    }

    $scope.openDatePick = function(){
        $scope.datePickOpen = true;
    }

    $scope.startAlgo = function() {
      $scope.AlgoTrade = [];
      $scope.algoChartData = [];
      $scope.sendEvent("Algo",{startDate: $scope.algo.start_date, endDate: $scope.algo.end_date, symbol:$scope.selectedSymbol.symbol});
    }

    $scope.getEod = function() {
      $scope.page = 1;
      $scope.AlgoTrade = [];
      $scope.algoChartData = [];
      $scope.sendEvent("Eod",{page: $scope.page,startDate: $scope.algo.start_date, endDate: $scope.algo.end_date, symbol:$scope.selectedSymbol.symbol});
    }

    $scope.getEodPrev = function() {
      if( $scope.page > 1 ) $scope.page = $scope.page - 1;
      $scope.sendEvent("Eod",{page: $scope.page,startDate: $scope.algo.start_date, endDate: $scope.algo.end_date, symbol:$scope.selectedSymbol.symbol});
    }

    $scope.getEodNext = function() {
      $scope.page = $scope.page + 1;
      $scope.sendEvent("Eod",{page: $scope.page,startDate: $scope.algo.start_date, endDate: $scope.algo.end_date, symbol:$scope.selectedSymbol.symbol});
    }


    $scope.algoSymbol = function(sym) {
        console.log($scope.selectedSymbol);
        console.log("Asking for symbol: " + sym);
        $scope.selectedSymbol = {symbol: sym};
    }

    $scope.switchTemplate = function(t)
    {
        $scope.template = t;
        if(  t == "optionchain.html" )
        {
          console.log("Switching to Option Chain");
          $scope.processSymbol($scope.selectedSymbol.symbol);
        }
        else if (t == "positions.html") {
          $scope.sendEvent("Positions",{});
        }
        else if (t == "analyze.html") {
          $scope.sendEvent("Analyze",{symbol: $scope.selectedSymbol.symbol, tradeType: $scope.tradeType});
        }

    };

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

    $scope.symbolSearchCall = function(){
      if( $scope.symbolData.length < 1 )
      {
        $scope.sendEvent("SymbolSearch",{});
      }
    };

    $scope.process = function(expMonth, refreshChart) {
      var msg = {};
      msg.event = "OptionChain";
      msg.data = {"symbol":$scope.selectedSymbol.symbol, "month":expMonth, "chart": refreshChart};
      $scope.send(msg);
    };

    $scope.processSymbol = function(sym) {
       console.log("Asking for symbol: " + sym);
       $scope.selectedSymbol = {symbol: sym};
       $scope.send({"symbol":sym, "month":$scope.expiryMonth, "event": "oc"});
    };

    $scope.addPos = function(option, opType){
      console.log("addPos event");
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
           else if( data.event == "SymbolSearch")
           {
             $scope.symbolData = data.data;
           }
           else if( data.event == "Eod")
           {
             $scope.eod = data.data;
             $scope.$broadcast('amCharts.updateData', $scope.eod, "eodChart" );
           }
           else if( data.event == "Fii")
           {
             $scope.fii = data.data;
             $scope.$broadcast('amCharts.updateData', $scope.fii, "fiiChart" );
           }
           else if( data.event == "FiiChart")
           {
             $scope.fiiChart = data.data;
             $scope.fiiChart.data = $scope.fii;
             $scope.$broadcast('amCharts.renderChart', $scope.fiiChart , "fiiChart" );
             $scope.$broadcast('amCharts.updateData', $scope.fii, "fiiChart" );
           }
           else if( data.event == "EodChart")
           {
             $scope.eodChart = data.data;
             $scope.eodChart.data = $scope.eod;
             console.log($scope.eodChart);
             console.log($scope.eod);
             $scope.$broadcast('amCharts.renderChart', $scope.eodChart , "eodChart" );
             $scope.$broadcast('amCharts.updateData', $scope.eod, "eodChart" );
           }
           else if( data.event == "Positions")
           {
             $scope.positions = data.data;
           }
           else if( data.event == "ExpDates")
           {
             $scope.expMonths = data.data;
             $scope.expiryMonth = $scope.expMonths[0];
           }
           else if( data.event == "AlgoTrade")
           {
             $scope.AlgoTrade.push(data.data);
           }
           else if( data.event == "AlgoChart")
           {
             $scope.algoChartData = $scope.algoChartData.concat(data.data);
             //console.log($scope.algoChartData);
             //console.log(data.data);
             $scope.$broadcast('amCharts.updateData', $scope.algoChartData, "algoChart" );
             $scope.$broadcast('amCharts.updateData', $scope.algoChartData, "perfChart" );
           }
           else if( data.event == "chartdata")
           {
             $scope.plotChartData = data.data;
             console.log($scope.plotChartData.guide1);
             console.log($scope.plotChartData.guide2);

             //$scope.$broadcast('amCharts.renderChart', $scope.amChartOptions, "lineChart" );
             $scope.$broadcast('amCharts.updateData', $scope.plotChartData.chartData, "lineChart" );
             $scope.$broadcast('amCharts.addGuide', $scope.plotChartData.minValue, $scope.plotChartData.guide1, "lineChart" );
             $scope.$broadcast('amCharts.addGuide', $scope.plotChartData.guide2, $scope.plotChartData.maxValue, "lineChart" );
           }
           else if( data.event == "oidata")
           {
             $scope.oiData = data.data;
             console.log($scope.oiData );
             $scope.$broadcast('amCharts.updateData', $scope.oiData , "oiChart" );
             $scope.$broadcast('amCharts.updateData', $scope.oiData , "oicChart" );
           }


    });

    $scope.remvovePos = function(position, idx) {
      var msg = {};
      msg.event = "removePos";
      msg.data = position;
      $scope.send(msg);
      $scope.trade.positions.splice(idx,1);
    };

    $scope.init();

    $scope.oiChartOptions = {
         "type": "serial",
         "theme": "light",
         "autoMarginOffset": 10,
         "autoMargins": true,
         "legendAlpha" : 0.8,
         "valueAxes": [{
             "id": "v1",
             "axisAlpha": 0,
             "position": "left",
             "ignoreAxisWidth": true
         }],
         "graphs": [{
             "id": "g1",
             "fillAlphas": 0.9,
             "fillColors": "#00b300",
             "valueField": "oi_call",
             "title": "Call",
             "type": "column",
             "balloonText": "Call: [[value]]"
         },{
             "id": "g2",
             "title": "Put",
             "fillAlphas": 0.9,
             "fillColors": "#cc6699",
             "valueField": "oi_put",
             "type": "column",
             "balloonText": "Put: [[value]]"
         }],
         "categoryField": "strike",
         "categoryAxis": {
             "parseDates": false,
             "dashLength": 1,
             "minorGridEnabled": false,
         },
         "data": $scope.oiData
    };

    $scope.oicChartOptions = {
         "type": "serial",
         "theme": "light",
         "autoMarginOffset": 10,
         "autoMargins": true,
         "legendAlpha" : 0.8,
         "valueAxes": [{
             "id": "v1",
             "axisAlpha": 0,
             "position": "left",
             "ignoreAxisWidth": true
         }],
         "graphs": [{
             "id": "g1",
             "fillAlphas": 0.9,
             "fillColors": "#00b300",
             "valueField": "oic_call",
             "title": "Call",
             "type": "column",
             "balloonText": "Call: [[value]]"
         },{
             "id": "g2",
             "title": "Put",
             "fillAlphas": 0.9,
             "valueField": "oic_put",
             "type": "column",
             "fillColors": "#cc6699",
             "balloonText": "Put: [[value]]"
         }],
         "categoryField": "strike",
         "categoryAxis": {
             "parseDates": false,
             "dashLength": 1,
             "minorGridEnabled": false,
         },
         "data": $scope.oiData
    };


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
                   "category": $scope.plotChartData.guide1,
                   "toCategory": $scope.plotChartData.guide1,
                   "lineColor": "#FF00FF",
                   "lineAlpha": 1,
                   "lineThickness": 2,
                   "dashLength": 2,
                   "inside": true
               },
               {
                   "fillAlpha": 0.5,
                   "category": $scope.plotChartData.guide2,
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
        "data": $scope.plotChartData.chartData
    };

    $scope.algoChartOptions = {
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
            "lineThickness": 1,
            "title": "Price",
            "useLineColorForBulletBorder": true,
            "valueField": "Price"
        },{
            "id": "g2",
            "balloon": {
                "drop": false,
                "adjustBorderColor": false,
                "color": "#ffffff"
            },
            "lineThickness": 1,
            "title": "MA1",
            "useLineColorForBulletBorder": true,
            "valueField": "MA1"
        },{
            "id": "g3",
            "balloon": {
                "drop": false,
                "adjustBorderColor": false,
                "color": "#ffffff"
            },
            "lineThickness": 1,
            "title": "MA2",
            "useLineColorForBulletBorder": true,
            "valueField": "MA2"
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
        "chartScrollbar": {
          "autoGridCount": true,
          "graph": "g1",
          "scrollbarHeight": 40
        },
        "legend": {
          divId: "legenddiv"
        },
        "mouseWheelZoomEnabled": true,
        "valueScrollbar": {
            "oppositeAxis": true,
            "offset": 50,
            "scrollbarHeight": 10
        },
        "categoryField": "point",
        "categoryAxis": {
            "parseDates": false,
            "dashLength": 1,
            "minorGridEnabled": false
        },
        "export": {
            "enabled": true
        },
        "data": $scope.algoChartData
    };

    $scope.perfChartOptions = {
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
        },{
            "id": "v2",
            "axisAlpha": 0,
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
            "lineThickness": 1,
            "title": "Price",
            "useLineColorForBulletBorder": true,
            "valueField": "Equity",
            "valueAxis": "v1"
        },{
            "id": "g2",
            "fillAlphas": 0.9,
            "fillColors": "#00b300",
            "valueField": "Trade",
            "title": "Trade",
            "type": "column",
            "valueAxis": "v2"
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
        "mouseWheelZoomEnabled": true,
        "categoryField": "point",
        "categoryAxis": {
            "parseDates": false,
            "dashLength": 1,
            "minorGridEnabled": false
        },
        "export": {
            "enabled": true
        },
        "data": $scope.algoChartData
    };

    $scope.eodChart = {"data": []};

    $scope.fiiChart = {"data": []};
}]);
