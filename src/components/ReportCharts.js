import React, { useEffect, useRef } from 'react';
import * as echarts from 'echarts';
import { Card, Row, Col } from 'antd';

const ReportCharts = ({ report }) => {
  const impressionChartRef = useRef(null);
  const revenueChartRef = useRef(null);
  const ctrChartRef = useRef(null);
  const platformChartRef = useRef(null);

  useEffect(() => {
    if (!report) return;

    // 初始化图表
    const impressionChart = echarts.init(impressionChartRef.current);
    const revenueChart = echarts.init(revenueChartRef.current);
    const ctrChart = echarts.init(ctrChartRef.current);
    const platformChart = echarts.init(platformChartRef.current);

    // 设置图表配置
    setImpressionChart(impressionChart);
    setRevenueChart(revenueChart);
    setCtrChart(ctrChart);
    setPlatformChart(platformChart);

    // 响应式调整
    const handleResize = () => {
      impressionChart.resize();
      revenueChart.resize();
      ctrChart.resize();
      platformChart.resize();
    };

    window.addEventListener('resize', handleResize);

    return () => {
      window.removeEventListener('resize', handleResize);
      impressionChart.dispose();
      revenueChart.dispose();
      ctrChart.dispose();
      platformChart.dispose();
    };
  }, [report]);

  const setImpressionChart = (chart) => {
    const option = {
      title: {
        text: '展示量趋势',
        left: 'center'
      },
      tooltip: {
        trigger: 'axis'
      },
      xAxis: {
        type: 'category',
        data: report.daily.map(item => item.date)
      },
      yAxis: {
        type: 'value',
        name: '展示量'
      },
      series: [{
        data: report.daily.map(item => item.impressions),
        type: 'line',
        smooth: true,
        areaStyle: {
          opacity: 0.3
        }
      }]
    };

    chart.setOption(option);
  };

  const setRevenueChart = (chart) => {
    const option = {
      title: {
        text: '收入趋势',
        left: 'center'
      },
      tooltip: {
        trigger: 'axis'
      },
      xAxis: {
        type: 'category',
        data: report.daily.map(item => item.date)
      },
      yAxis: {
        type: 'value',
        name: '收入'
      },
      series: [{
        data: report.daily.map(item => item.revenue),
        type: 'line',
        smooth: true,
        areaStyle: {
          opacity: 0.3
        }
      }]
    };

    chart.setOption(option);
  };

  const setCtrChart = (chart) => {
    const option = {
      title: {
        text: '点击率趋势',
        left: 'center'
      },
      tooltip: {
        trigger: 'axis'
      },
      xAxis: {
        type: 'category',
        data: report.daily.map(item => item.date)
      },
      yAxis: {
        type: 'value',
        name: '点击率(%)'
      },
      series: [{
        data: report.daily.map(item => (item.clicks / item.impressions) * 100),
        type: 'line',
        smooth: true,
        areaStyle: {
          opacity: 0.3
        }
      }]
    };

    chart.setOption(option);
  };

  const setPlatformChart = (chart) => {
    const platformData = Object.entries(report.byPlatform).map(([platform, data]) => ({
      name: platform,
      value: data.revenue
    }));

    const option = {
      title: {
        text: '平台收入占比',
        left: 'center'
      },
      tooltip: {
        trigger: 'item',
        formatter: '{a} <br/>{b}: {c} ({d}%)'
      },
      legend: {
        orient: 'vertical',
        left: 'left'
      },
      series: [{
        name: '平台收入',
        type: 'pie',
        radius: '50%',
        data: platformData,
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }]
    };

    chart.setOption(option);
  };

  return (
    <Row gutter={[16, 16]}>
      <Col span={12}>
        <Card>
          <div ref={impressionChartRef} style={{ height: '300px' }} />
        </Card>
      </Col>
      <Col span={12}>
        <Card>
          <div ref={revenueChartRef} style={{ height: '300px' }} />
        </Card>
      </Col>
      <Col span={12}>
        <Card>
          <div ref={ctrChartRef} style={{ height: '300px' }} />
        </Card>
      </Col>
      <Col span={12}>
        <Card>
          <div ref={platformChartRef} style={{ height: '300px' }} />
        </Card>
      </Col>
    </Row>
  );
};

export default ReportCharts; 