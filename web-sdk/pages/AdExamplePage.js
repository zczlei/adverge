import React from 'react';
import AdManager from '../components/AdManager';
import { adConfigs } from '../config/adConfigs';
import '../styles/AdExamplePage.css';

const AdExamplePage = () => {
  return (
    <div className="ad-example-page">
      <h1>广告示例页面</h1>
      
      <div className="ad-container">
        <h2>横幅广告</h2>
        <AdManager config={adConfigs.banner} />
      </div>
      
      <div className="ad-container">
        <h2>插页广告</h2>
        <AdManager config={adConfigs.interstitial} />
      </div>
      
      <div className="ad-container">
        <h2>原生广告</h2>
        <AdManager config={adConfigs.native} />
      </div>
      
      <div className="ad-container">
        <h2>所有广告</h2>
        <AdManager config={adConfigs.all} />
      </div>
    </div>
  );
};

export default AdExamplePage; 