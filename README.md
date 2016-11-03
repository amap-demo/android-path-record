# android-path-record
轨迹回放和轨迹纠偏

本工程为基于高德地图Android SDK进行封装，实现了定位轨迹记录并回放的功能
## 前述 ##
- [高德官网申请Key](http://lbs.amap.com/dev/#/).
- 阅读[参考手册](http://a.amap.com/lbs/static/unzip/Android_Map_Doc/index.html).
- 工程基于Android 3D地图SDK实现

## 功能描述 ##
基于3D地图SDK，可以记录定位点信息并实时绘制轨迹，记录过程中每隔30个点进行轨迹纠偏（将定位点抓到道路上），停止记录时对原始定位轨迹进行保存。可以在轨迹记录中查看已保存轨迹并回放。

## 扫一扫安装##
![Screenshot]( https://raw.githubusercontent.com/amap-demo/android-path-record/master/apk/1478158919.png)  

## 使用方法##
###1:配置搭建AndroidSDK工程###
- [Android Studio工程搭建方法](http://lbs.amap.com/api/android-sdk/guide/creat-project/android-studio-creat-project/#add-jars).
- [通过maven库引入SDK方法](http://lbsbbs.amap.com/forum.php?mod=viewthread&tid=18786).

