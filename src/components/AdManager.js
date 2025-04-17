import React, { useState, useEffect } from 'react';
import AdComponent from './AdComponent';
import { message } from 'antd';

const AdManager = ({ adConfigs = [] }) => {
  const [activeAds, setActiveAds] = useState([]);

  useEffect(() => {
    const loadAds = async () => {
      try {
        // 根据配置加载广告
        const ads = adConfigs.map(config => ({
          ...config,
          key: `${config.platform}-${config.adType}-${Date.now()}`
        }));
        setActiveAds(ads);
      } catch (error) {
        message.error('加载广告配置失败: ' + error.message);
      }
    };

    loadAds();
  }, [adConfigs]);

  return (
    <div className="ad-manager">
      {activeAds.map(ad => (
        <div key={ad.key} className="ad-container" style={{ marginBottom: '20px' }}>
          <AdComponent
            adType={ad.adType}
            platform={ad.platform}
            options={ad.options}
          />
        </div>
      ))}
    </div>
  );
};

export default AdManager; 