import React, { useEffect, useRef } from 'react';
import AdService from '../services/AdService';
import { message } from 'antd';

const AdComponent = ({ adType, platform, options = {} }) => {
  const containerRef = useRef(null);

  useEffect(() => {
    const loadAd = async () => {
      try {
        const adData = await AdService.requestAd(adType, platform, options);
        if (containerRef.current) {
          await AdService.renderAd(containerRef.current, adData);
        }
      } catch (error) {
        message.error('加载广告失败: ' + error.message);
      }
    };

    loadAd();

    return () => {
      // 清理广告容器
      if (containerRef.current) {
        containerRef.current.innerHTML = '';
      }
    };
  }, [adType, platform, options]);

  return (
    <div 
      ref={containerRef} 
      className="ad-component"
      style={{ 
        width: '100%',
        minHeight: '100px',
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center'
      }}
    />
  );
};

export default AdComponent; 