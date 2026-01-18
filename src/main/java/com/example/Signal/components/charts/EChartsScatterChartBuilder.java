package com.example.Signal.components.charts;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class EChartsScatterChartBuilder {

    private final String chartElementId;
    private final Map<LocalDate, Integer> data;
    private final LocalDate minDate;
    private final LocalDate maxDate;
    private final int rollingAverageWindow;

    public EChartsScatterChartBuilder(String chartElementId,
                                      Map<LocalDate, Integer> data,
                                      LocalDate minDate,
                                      LocalDate maxDate,
                                      int rollingAverageWindow) {
        this.chartElementId = chartElementId;
        this.data = data;
        this.minDate = minDate;
        this.maxDate = maxDate;
        this.rollingAverageWindow = rollingAverageWindow;
    }

    public String buildScript() {
        List<Map.Entry<LocalDate, Integer>> sortedData = data.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .toList();

        String dataPointsJson = buildDataPointsJson(sortedData);
        String scoresJson = buildScoresJson(sortedData);
        String rollingAverageJson = buildRollingAverageJson(sortedData);

        return String.format("""
                                     (function() {
                                         const chartDom = document.getElementById('%s');
                                         if (!chartDom) return;
                                     
                                         // Read score colors from CSS variables
                                         const rootStyles = getComputedStyle(document.documentElement);
                                         const scoreColors = {
                                             1: rootStyles.getPropertyValue('--score-1-color').trim(),
                                             2: rootStyles.getPropertyValue('--score-2-color').trim(),
                                             3: rootStyles.getPropertyValue('--score-3-color').trim(),
                                             4: rootStyles.getPropertyValue('--score-4-color').trim(),
                                             5: rootStyles.getPropertyValue('--score-5-color').trim(),
                                             6: rootStyles.getPropertyValue('--score-6-color').trim(),
                                             7: rootStyles.getPropertyValue('--score-7-color').trim()
                                         };
                                     
                                         const myChart = echarts.init(chartDom);
                                         const dataPoints = %s;
                                         const scores = %s;
                                         const colors = scores.map(score => scoreColors[score]);
                                         const rollingAverage = %s;
                                     
                                         const option = %s;
                                     
                                         myChart.setOption(option);
                                     
                                         // Resize after a short delay to ensure proper dimensions
                                         setTimeout(function() {
                                             myChart.resize();
                                         }, 1);
                                     
                                         window.addEventListener('resize', function() {
                                             myChart.resize();
                                         });
                                     })();
                                     """,
                             chartElementId,
                             dataPointsJson,
                             scoresJson,
                             rollingAverageJson,
                             buildChartOptions());
    }

    private String buildDataPointsJson(List<Map.Entry<LocalDate, Integer>> sortedData) {
        StringBuilder dataPoints = new StringBuilder("[");

        for (int i = 0; i < sortedData.size(); i++) {
            Map.Entry<LocalDate, Integer> entry = sortedData.get(i);
            if (i > 0) {
                dataPoints.append(",");
            }
            dataPoints.append(String.format("['%s', %d]", entry.getKey().toString(), entry.getValue()));
        }

        dataPoints.append("]");
        return dataPoints.toString();
    }

    private String buildScoresJson(List<Map.Entry<LocalDate, Integer>> sortedData) {
        StringBuilder scores = new StringBuilder("[");

        for (int i = 0; i < sortedData.size(); i++) {
            if (i > 0) {
                scores.append(",");
            }
            scores.append(sortedData.get(i).getValue());
        }

        scores.append("]");
        return scores.toString();
    }

    private String buildRollingAverageJson(List<Map.Entry<LocalDate, Integer>> sortedData) {
        if (sortedData.size() < rollingAverageWindow) {
            return "[]";
        }

        StringBuilder rollingAverage = new StringBuilder("[");
        boolean first = true;

        for (int i = 0; i < sortedData.size(); i++) {
            int windowStart = Math.max(0, i - rollingAverageWindow + 1);
            int windowEnd = i + 1;

            double sum = 0;
            int count = 0;

            for (int j = windowStart; j < windowEnd; j++) {
                int score = sortedData.get(j).getValue();
                if (score != 7) {
                    sum += score;
                    count++;
                } else {
                    sum += 7;
                    count++;
                }
            }

            if (count >= rollingAverageWindow) {
                double average = sum / count;
                LocalDate date = sortedData.get(i).getKey();

                if (!first) {
                    rollingAverage.append(",");
                }
                rollingAverage.append(String.format("['%s', %.2f]", date.toString(), average));
                first = false;
            }
        }

        rollingAverage.append("]");
        return rollingAverage.toString();
    }

    private String buildChartOptions() {
        return String.format("""
                                     {
                                         tooltip: {
                                             trigger: 'item',
                                             formatter: function(params) {
                                                 if (params.seriesName === '%d-Game Rolling Average') {
                                                     const date = new Date(params.value[0]).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
                                                     return date + '<br/>Avg: ' + params.value[1].toFixed(2);
                                                 }
                                                 const score = params.value[1] === 7 ? 'X' : params.value[1];
                                                 const date = new Date(params.value[0]).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
                                                 return date + '<br/>Score: ' + score;
                                             }
                                         },
                                         grid: {
                                             left: '50px',
                                             right: '20px',
                                             top: '20px',
                                             bottom: '50px',
                                             containLabel: true
                                         },
                                          xAxis: {
                                              type: 'time',
                                              min: '%s',
                                              max: '%s',
                                              axisLabel: {
                                                  formatter: function(value) {
                                                      const date = new Date(value);
                                                      const month = date.toLocaleDateString('en-US', { month: 'short' });
                                                      const day = date.getDate();
                                                      return month + ' ' + day;
                                                  }
                                              }
                                          },
                                         yAxis: {
                                             type: 'value',
                                             inverse: true,
                                             min: 1,
                                             max: 7,
                                             interval: 1,
                                             splitLine: {
                                                 show: true,
                                                 lineStyle: {
                                                     color: '#e0e0e0'
                                                 }
                                             },
                                             axisLabel: {
                                                 formatter: function(value) {
                                                     return value === 7 ? 'X' : value.toString();
                                                 }
                                             }
                                         },
                                          series: [{
                                              name: 'Score',
                                              type: 'scatter',
                                              data: dataPoints,
                                              itemStyle: {
                                                  color: function(params) {
                                                      return colors[params.dataIndex];
                                                  }
                                              },
                                              symbolSize: 10,
                                              z: 2
                                          }, {
                                              name: '%d-Game Rolling Average',
                                              type: 'line',
                                              data: rollingAverage,
                                              smooth: true,
                                              lineStyle: {
                                                  color: '#666',
                                                  width: 2,
                                                  type: 'solid'
                                              },
                                              itemStyle: {
                                                  color: '#666'
                                              },
                                              showSymbol: false,
                                              z: 1
                                          }]
                                      }
                                     """,
                             rollingAverageWindow,
                             minDate.toString(),
                             maxDate.toString(),
                             rollingAverageWindow);
    }
}
