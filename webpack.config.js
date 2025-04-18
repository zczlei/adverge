const path = require('path');

module.exports = {
  mode: 'production',
  entry: './web-sdk/AdAggregator.js',
  output: {
    path: path.resolve(__dirname, 'dist'),
    filename: 'bundle.js',
    library: 'AdMediation',
    libraryTarget: 'umd',
    globalObject: 'this'
  },
  module: {
    rules: [
      {
        test: /\.js$/,
        exclude: /node_modules/,
        use: {
          loader: 'babel-loader',
          options: {
            presets: ['@babel/preset-env']
          }
        }
      }
    ]
  },
  optimization: {
    minimize: true
  },
  resolve: {
    extensions: ['.js']
  },
  devtool: 'source-map'
}; 