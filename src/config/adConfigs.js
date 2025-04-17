export const adConfigs = [
  {
    platform: 'topon',
    adType: 'banner',
    options: {
      placementId: 'your-topon-placement-id',
      size: '320x50'
    }
  },
  {
    platform: 'bigo',
    adType: 'interstitial',
    options: {
      placementId: 'your-bigo-placement-id',
      autoShow: true
    }
  },
  {
    platform: 'inmobi',
    adType: 'native',
    options: {
      placementId: 'your-inmobi-placement-id',
      style: {
        width: '300px',
        height: '250px'
      }
    }
  }
];

export default adConfigs; 