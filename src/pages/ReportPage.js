import React, { useState, useEffect } from 'react';
import { DatePicker, Button, Table, message, Modal, Select } from 'antd';
import { DownloadOutlined, FileExcelOutlined, FileTextOutlined } from '@ant-design/icons';
import axios from 'axios';
import moment from 'moment';
import 'moment/locale/zh-cn';

const { RangePicker } = DatePicker;
const { Option } = Select;

const ReportPage = () => {
  const [dateRange, setDateRange] = useState([]);
  const [reports, setReports] = useState([]);
  const [loading, setLoading] = useState(false);
  const [selectedReport, setSelectedReport] = useState(null);
  const [reportModalVisible, setReportModalVisible] = useState(false);

  useEffect(() => {
    fetchReports();
  }, []);

  const fetchReports = async () => {
    try {
      const response = await axios.get('/api/reports');
      if (response.data.success) {
        setReports(response.data.data);
      }
    } catch (error) {
      message.error('获取报表列表失败');
    }
  };

  const handleGenerateReport = async () => {
    if (!dateRange || dateRange.length !== 2) {
      message.error('请选择日期范围');
      return;
    }

    setLoading(true);
    try {
      const response = await axios.post('/api/reports/generate', {
        startDate: dateRange[0].format('YYYY-MM-DD'),
        endDate: dateRange[1].format('YYYY-MM-DD')
      });

      if (response.data.success) {
        message.success('报表生成成功');
        fetchReports();
      }
    } catch (error) {
      message.error('报表生成失败');
    } finally {
      setLoading(false);
    }
  };

  const handleViewReport = async (filename) => {
    try {
      const response = await axios.get(`/api/reports/${filename}`);
      if (response.data.success) {
        setSelectedReport(response.data.data);
        setReportModalVisible(true);
      }
    } catch (error) {
      message.error('获取报表详情失败');
    }
  };

  const handleDownloadReport = async (filename, format) => {
    try {
      const response = await axios.get(`/api/reports/${filename}/download?format=${format}`, {
        responseType: 'blob'
      });

      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `${filename}.${format === 'excel' ? 'xlsx' : 'json'}`);
      document.body.appendChild(link);
      link.click();
      link.remove();
    } catch (error) {
      message.error('下载报表失败');
    }
  };

  const columns = [
    {
      title: '报表名称',
      dataIndex: 'filename',
      key: 'filename',
      render: (text) => (
        <a onClick={() => handleViewReport(text)}>{text}</a>
      )
    },
    {
      title: '生成时间',
      dataIndex: 'lastUpdated',
      key: 'lastUpdated',
      render: (text) => moment(text).format('YYYY-MM-DD HH:mm:ss')
    },
    {
      title: '操作',
      key: 'action',
      render: (_, record) => (
        <span>
          <Button
            type="link"
            icon={<FileExcelOutlined />}
            onClick={() => handleDownloadReport(record.filename, 'excel')}
          >
            Excel
          </Button>
          <Button
            type="link"
            icon={<FileTextOutlined />}
            onClick={() => handleDownloadReport(record.filename, 'json')}
          >
            JSON
          </Button>
        </span>
      )
    }
  ];

  const renderReportDetail = () => {
    if (!selectedReport) return null;

    return (
      <div>
        <h3>汇总数据</h3>
        <Table
          dataSource={[
            {
              key: 'impressions',
              metric: '总展示量',
              value: selectedReport.summary.totalImpressions
            },
            {
              key: 'clicks',
              metric: '总点击量',
              value: selectedReport.summary.totalClicks
            },
            {
              key: 'revenue',
              metric: '总收入',
              value: selectedReport.summary.totalRevenue
            },
            {
              key: 'ctr',
              metric: '点击率',
              value: `${selectedReport.summary.ctr.toFixed(2)}%`
            }
          ]}
          columns={[
            { title: '指标', dataIndex: 'metric', key: 'metric' },
            { title: '数值', dataIndex: 'value', key: 'value' }
          ]}
          pagination={false}
        />

        <h3>平台统计</h3>
        <Table
          dataSource={Object.entries(selectedReport.byPlatform).map(([platform, data]) => ({
            key: platform,
            platform,
            impressions: data.impressions,
            clicks: data.clicks,
            revenue: data.revenue,
            ctr: `${((data.clicks / data.impressions) * 100).toFixed(2)}%`
          }))}
          columns={[
            { title: '平台', dataIndex: 'platform', key: 'platform' },
            { title: '展示量', dataIndex: 'impressions', key: 'impressions' },
            { title: '点击量', dataIndex: 'clicks', key: 'clicks' },
            { title: '收入', dataIndex: 'revenue', key: 'revenue' },
            { title: '点击率', dataIndex: 'ctr', key: 'ctr' }
          ]}
          pagination={false}
        />

        <h3>广告类型统计</h3>
        <Table
          dataSource={Object.entries(selectedReport.byAdType).map(([adType, data]) => ({
            key: adType,
            adType,
            impressions: data.impressions,
            clicks: data.clicks,
            revenue: data.revenue,
            ctr: `${((data.clicks / data.impressions) * 100).toFixed(2)}%`
          }))}
          columns={[
            { title: '广告类型', dataIndex: 'adType', key: 'adType' },
            { title: '展示量', dataIndex: 'impressions', key: 'impressions' },
            { title: '点击量', dataIndex: 'clicks', key: 'clicks' },
            { title: '收入', dataIndex: 'revenue', key: 'revenue' },
            { title: '点击率', dataIndex: 'ctr', key: 'ctr' }
          ]}
          pagination={false}
        />

        <h3>设备统计</h3>
        <Table
          dataSource={Object.entries(selectedReport.byDevice).map(([deviceType, data]) => ({
            key: deviceType,
            deviceType,
            impressions: data.impressions,
            clicks: data.clicks,
            revenue: data.revenue,
            ctr: `${((data.clicks / data.impressions) * 100).toFixed(2)}%`
          }))}
          columns={[
            { title: '设备类型', dataIndex: 'deviceType', key: 'deviceType' },
            { title: '展示量', dataIndex: 'impressions', key: 'impressions' },
            { title: '点击量', dataIndex: 'clicks', key: 'clicks' },
            { title: '收入', dataIndex: 'revenue', key: 'revenue' },
            { title: '点击率', dataIndex: 'ctr', key: 'ctr' }
          ]}
          pagination={false}
        />
      </div>
    );
  };

  return (
    <div style={{ padding: '24px' }}>
      <div style={{ marginBottom: '16px' }}>
        <RangePicker
          value={dateRange}
          onChange={setDateRange}
          style={{ marginRight: '16px' }}
        />
        <Button
          type="primary"
          onClick={handleGenerateReport}
          loading={loading}
        >
          生成报表
        </Button>
      </div>

      <Table
        columns={columns}
        dataSource={reports}
        rowKey="filename"
      />

      <Modal
        title="报表详情"
        visible={reportModalVisible}
        onCancel={() => setReportModalVisible(false)}
        footer={null}
        width={800}
      >
        {renderReportDetail()}
      </Modal>
    </div>
  );
};

export default ReportPage; 