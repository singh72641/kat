var oatest = angular.module('oatest', ['amChartsDirective']);


oatest.directive('oaProgress', [function () {
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


oatest.controller('ChartCtrl', function ($scope) {
    $scope.amChartOptions = {
        data: [{
            year: 2005,
            income: 23.5,
            expenses: 18.1
        }, {
            year: 2006,
            income: 26.2,
            expenses: 22.8
        }, {
            year: 2007,
            income: 30.1,
            expenses: 23.9
        }, {
            year: 2008,
            income: 29.5,
            expenses: 25.1
        }, {
            year: 2009,
            income: 24.6,
            expenses: 25
        }],
        type: "serial",
        theme: 'black',
        categoryField: "year",
        rotate: true,
        pathToImages: './images/',
        legend: {
            enabled: true
        },
        chartScrollbar: {
            enabled: true,
        },
        categoryAxis: {
            gridPosition: "start",
            parseDates: false
        },
        valueAxes: [{
            position: "top",
            title: "Million USD"
        }],
        graphs: [{
            type: "column",
            title: "Income",
            valueField: "income",
            fillAlphas: 1,
        }]
    };
});

oatest.controller('LineChartCtrl', function ($scope) {

    $scope.plotChartData = [];
    $scope.strikePrice = 125.00;
    $scope.maxPrice = 200.00;
    $scope.minPrice = 60.00;
    $scope.premium = 3.00;
    $scope.stdUpMarker1 = 0.00;
    $scope.stdUpMarker2 = 0.00;
    $scope.stdDownMarker1 = 0.00;
    $scope.stdDownMarker2 = 0.00;
    $scope.volatility = 32.12;
    $scope.currPrice = 122.00;
    $scope.stdVal1 = (($scope.currPrice * $scope.volatility) / 100.00);

    $scope.stdUpMarker1 = Math.round($scope.strikePrice + $scope.stdVal1);
    $scope.stdUpMarker2 = Math.round($scope.strikePrice + 2 * $scope.stdVal1);
    $scope.stdDownMarker1 = Math.round($scope.strikePrice - $scope.stdVal1);
    $scope.stdDownMarker2 = Math.round($scope.strikePrice - 2 * $scope.stdVal1);


    //----------------------------------------------------------------------------------------------
    // Calculates a point Z(x), the Probability Density Function, on any normal curve. 
    // This is the height of the point ON the normal curve.
    // For values on the Standard Normal Curve, call with Mean = 0, StdDev = 1.
    $scope.NormalDensityZx = function (x, Mean, StdDev) {
        var a = x - Mean;
        return Math.exp(-(a * a) / (2 * StdDev * StdDev)) / (Math.sqrt(2 * Math.PI) * StdDev);
    }


    $scope.amChartOptions = {
        "type": "serial",
        "theme": "light",
        "marginRight": 40,
        "marginLeft": 80,
        "autoMarginOffset": 10,
        "autoMargins": false,
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
        }, {
            "valueAxis": "v2",
            "balloonText": "[[category]]: <b>[[value]]</b>",
            "lineThickness": 2,
            "valueField": "std"
        }],
        "chartScrollbar": {
            "graph": "g1",
            "oppositeAxis": false,
            "offset": 30,
            "scrollbarHeight": 80,
            "backgroundAlpha": 0,
            "selectedBackgroundAlpha": 0.1,
            "selectedBackgroundColor": "#888888",
            "graphFillAlpha": 0,
            "graphLineAlpha": 0.5,
            "selectedGraphFillAlpha": 0,
            "selectedGraphLineAlpha": 1,
            "autoGridCount": true,
            "color": "#AAAAAA"
        },
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




    for (var i = $scope.minPrice; i <= $scope.maxPrice; i += 1) {
        var iPrice = i;
        var stdDev = (iPrice - $scope.currPrice) / $scope.volatility;
        var iVal = $scope.strikePrice - iPrice;
        if (iVal < -20) iVal = -20;
        var dp = {
            price: i,
            value: iVal * 1000,
            std: ($scope.NormalDensityZx(stdDev, 0, 1))
        };

        if (iPrice == $scope.currPrice) {
            console.log("Added bullet for curr Price ");
            dp.bullet = "diamond";
            dp.description = "Something good happened!"
        }
        $scope.plotChartData.push(dp);

    }

    //console.log(($scope.strikePrice - $scope.stdVal1));
    console.log($scope.strikePrice);
    console.log($scope.stdVal1);
    console.log(Math.round($scope.strikePrice + $scope.stdVal1));
    console.log(Math.round($scope.strikePrice - $scope.stdVal1));


    // Calculates Q(x), the right tail area under the Standard Normal Curve. 
    $scope.StandardNormalQx = function (x) {
        if (x === 0) // no approximation necessary for 0
            return 0.50;

        var t1, t2, t3, t4, t5, qx;
        var negative = false;
        if (x < 0) {
            x = -x;
            negative = true;
        }
        t1 = 1 / (1 + (0.2316419 * x));
        t2 = t1 * t1;
        t3 = t2 * t1;
        t4 = t3 * t1;
        t5 = t4 * t1;
        qx = $scope.NormalDensityZx(x, 0, 1) * ((0.319381530 * t1) + (-0.356563782 * t2) +
          (1.781477937 * t3) + (-1.821255978 * t4) + (1.330274429 * t5));
        if (negative == true)
            qx = 1 - qx;
        return qx;
    }

    $scope.StandardNormalPx = function (x) {
        return 1 - StandardNormalQx(x);
    }
    //----------------------------------------------------------------------------------------------
    // Calculates A(x), the area under the Standard Normal Curve between +x and -x. 
    $scope.StandardNormalAx = function (x) {
        return 1 - (2 * StandardNormalQx(Math.abs(x)));
    }

    /**
     * Define values where to put vertical lines at
    */
    $scope.verticals = [
      -2, -1.0, -0.0, 1.0, 2.0
    ];

    /**
    * Calculate data
    */
    var chartData = [];
    for (var i = -5; i < 5.1; i += 0.1) {
        var dp = {
            category: i,
            value: $scope.NormalDensityZx(i, 0, 1)
        };
        if ($scope.verticals.indexOf(Math.round(i * 10) / 10) !== -1) {
            dp.vertical = 0.45;
        }
        chartData.push(dp);
    }


    /**
        * Create a chart
    */
    var chart = AmCharts.makeChart("chartdiv", {
        "type": "serial",
        "theme": "light",
        "dataProvider": chartData,
        "precision": 2,
        "valueAxes": [{
            "gridAlpha": 0.2,
            "dashLength": 0
        }],
        "startDuration": 1,
        "graphs": [{
            "balloonText": "[[category]]: <b>[[value]]</b>",
            "lineThickness": 2,
            "valueField": "value"
        }, {
            "balloonText": "",
            "fillAlphas": 1,
            "type": "column",
            "valueField": "vertical",
            "fixedColumnWidth": 2,
            "labelText": "[[value]]",
            "labelOffset": 20
        }],
        "chartCursor": {
            "categoryBalloonEnabled": false,
            "cursorAlpha": 0,
            "zoomable": false
        },
        "categoryField": "category",
        "categoryAxis": {
            "gridAlpha": 0.05,
            "startOnAxis": true,
            "tickLength": 5,
            "labelFunction": function (label, item) {
                return '' + Math.round(item.dataContext.category * 10) / 10;
            }
        }

    });
});