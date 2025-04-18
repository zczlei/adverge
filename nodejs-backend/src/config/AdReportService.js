const fs = require('fs').promises;
const path = require('path');
const AdStatsService = require('./AdStatsService');
const ExcelJS = require('exceljs');

class AdReportService {
  constructor() {
    this.reportsDir = path.join(__dirname, '../data/reports');
    this.ensureReportsDirectory();
  }

  async ensureReportsDirectory() {
    try {
      await fs.mkdir(this.reportsDir, { recursive: true });
    } catch (error) {
      console.error('创建报表目录失败:', error);
    }
  }

  async generateDailyReport(date) {
    const stats = await AdStatsService.getStats(date, date);
    const report = {
      date,
      summary: {
        totalImpressions: stats.total.impressions,
        totalClicks: stats.total.clicks,
        totalRevenue: stats.total.revenue,
        ctr: stats.total.ctr
      },
      byPlatform: stats.byPlatform,
      byAdType: stats.byAdType,
      byDevice: stats.byDevice
    };

    // 保存JSON格式报表
    await this.saveJsonReport(date, report);
    
    // 生成Excel格式报表
    await this.generateExcelReport(date, report);

    return report;
  }

  async generateDateRangeReport(startDate, endDate) {
    const stats = await AdStatsService.getStats(startDate, endDate);
    const report = {
      dateRange: {
        start: startDate,
        end: endDate
      },
      summary: {
        totalImpressions: stats.total.impressions,
        totalClicks: stats.total.clicks,
        totalRevenue: stats.total.revenue,
        ctr: stats.total.ctr,
        averageDailyImpressions: stats.total.impressions / stats.daily.length,
        averageDailyClicks: stats.total.clicks / stats.daily.length,
        averageDailyRevenue: stats.total.revenue / stats.daily.length
      },
      daily: stats.daily,
      byPlatform: stats.byPlatform,
      byAdType: stats.byAdType,
      byDevice: stats.byDevice
    };

    // 保存JSON格式报表
    await this.saveJsonReport(`${startDate}_${endDate}`, report);
    
    // 生成Excel格式报表
    await this.generateExcelReport(`${startDate}_${endDate}`, report);

    return report;
  }

  async saveJsonReport(filename, report) {
    try {
      const filePath = path.join(this.reportsDir, `${filename}.json`);
      await fs.writeFile(filePath, JSON.stringify(report, null, 2));
    } catch (error) {
      console.error('保存JSON报表失败:', error);
    }
  }

  async generateExcelReport(filename, report) {
    const workbook = new ExcelJS.Workbook();
    
    // 创建汇总工作表
    const summarySheet = workbook.addWorksheet('汇总');
    this.addSummarySheet(summarySheet, report);

    // 创建平台统计工作表
    const platformSheet = workbook.addWorksheet('平台统计');
    this.addPlatformSheet(platformSheet, report);

    // 创建广告类型统计工作表
    const adTypeSheet = workbook.addWorksheet('广告类型统计');
    this.addAdTypeSheet(adTypeSheet, report);

    // 创建设备统计工作表
    const deviceSheet = workbook.addWorksheet('设备统计');
    this.addDeviceSheet(deviceSheet, report);

    // 如果是日期范围报表，添加每日统计工作表
    if (report.daily) {
      const dailySheet = workbook.addWorksheet('每日统计');
      this.addDailySheet(dailySheet, report);
    }

    // 保存Excel文件
    try {
      const filePath = path.join(this.reportsDir, `${filename}.xlsx`);
      await workbook.xlsx.writeFile(filePath);
    } catch (error) {
      console.error('保存Excel报表失败:', error);
    }
  }

  addSummarySheet(sheet, report) {
    sheet.columns = [
      { header: '指标', key: 'metric', width: 20 },
      { header: '数值', key: 'value', width: 15 }
    ];

    sheet.addRow({ metric: '总展示量', value: report.summary.totalImpressions });
    sheet.addRow({ metric: '总点击量', value: report.summary.totalClicks });
    sheet.addRow({ metric: '总收入', value: report.summary.totalRevenue });
    sheet.addRow({ metric: '点击率', value: `${report.summary.ctr.toFixed(2)}%` });

    if (report.summary.averageDailyImpressions) {
      sheet.addRow({ metric: '平均每日展示量', value: report.summary.averageDailyImpressions });
      sheet.addRow({ metric: '平均每日点击量', value: report.summary.averageDailyClicks });
      sheet.addRow({ metric: '平均每日收入', value: report.summary.averageDailyRevenue });
    }
  }

  addPlatformSheet(sheet, report) {
    sheet.columns = [
      { header: '平台', key: 'platform', width: 15 },
      { header: '展示量', key: 'impressions', width: 15 },
      { header: '点击量', key: 'clicks', width: 15 },
      { header: '收入', key: 'revenue', width: 15 },
      { header: '点击率', key: 'ctr', width: 15 }
    ];

    Object.entries(report.byPlatform).forEach(([platform, data]) => {
      const ctr = data.impressions > 0 ? (data.clicks / data.impressions) * 100 : 0;
      sheet.addRow({
        platform,
        impressions: data.impressions,
        clicks: data.clicks,
        revenue: data.revenue,
        ctr: `${ctr.toFixed(2)}%`
      });
    });
  }

  addAdTypeSheet(sheet, report) {
    sheet.columns = [
      { header: '广告类型', key: 'adType', width: 15 },
      { header: '展示量', key: 'impressions', width: 15 },
      { header: '点击量', key: 'clicks', width: 15 },
      { header: '收入', key: 'revenue', width: 15 },
      { header: '点击率', key: 'ctr', width: 15 }
    ];

    Object.entries(report.byAdType).forEach(([adType, data]) => {
      const ctr = data.impressions > 0 ? (data.clicks / data.impressions) * 100 : 0;
      sheet.addRow({
        adType,
        impressions: data.impressions,
        clicks: data.clicks,
        revenue: data.revenue,
        ctr: `${ctr.toFixed(2)}%`
      });
    });
  }

  addDeviceSheet(sheet, report) {
    sheet.columns = [
      { header: '设备类型', key: 'deviceType', width: 15 },
      { header: '展示量', key: 'impressions', width: 15 },
      { header: '点击量', key: 'clicks', width: 15 },
      { header: '收入', key: 'revenue', width: 15 },
      { header: '点击率', key: 'ctr', width: 15 }
    ];

    Object.entries(report.byDevice).forEach(([deviceType, data]) => {
      const ctr = data.impressions > 0 ? (data.clicks / data.impressions) * 100 : 0;
      sheet.addRow({
        deviceType,
        impressions: data.impressions,
        clicks: data.clicks,
        revenue: data.revenue,
        ctr: `${ctr.toFixed(2)}%`
      });
    });
  }

  addDailySheet(sheet, report) {
    sheet.columns = [
      { header: '日期', key: 'date', width: 15 },
      { header: '展示量', key: 'impressions', width: 15 },
      { header: '点击量', key: 'clicks', width: 15 },
      { header: '收入', key: 'revenue', width: 15 },
      { header: '点击率', key: 'ctr', width: 15 }
    ];

    report.daily.forEach(day => {
      const ctr = day.impressions > 0 ? (day.clicks / day.impressions) * 100 : 0;
      sheet.addRow({
        date: day.date,
        impressions: day.impressions,
        clicks: day.clicks,
        revenue: day.revenue,
        ctr: `${ctr.toFixed(2)}%`
      });
    });
  }

  async getReport(filename) {
    try {
      const filePath = path.join(this.reportsDir, `${filename}.json`);
      const data = await fs.readFile(filePath, 'utf8');
      return JSON.parse(data);
    } catch (error) {
      console.error('获取报表失败:', error);
      return null;
    }
  }

  async listReports() {
    try {
      const files = await fs.readdir(this.reportsDir);
      return files
        .filter(file => file.endsWith('.json'))
        .map(file => file.replace('.json', ''));
    } catch (error) {
      console.error('获取报表列表失败:', error);
      return [];
    }
  }
}

module.exports = new AdReportService(); 