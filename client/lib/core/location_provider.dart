/// 位置来源的抽象。
///
/// 现在只有一个纯模拟的实现。**真实 GPS 留到接高德 SDK 时再做**——
/// 到时候新写一个 `RealLocationProvider implements LocationProvider`，
/// 上层代码一行都不用改。这就是把定位抽成接口的意义。
abstract interface class LocationProvider {
  /// 当前所在地点的显示名，显示在地图页底部面板上
  String get label;

  /// 模拟「走到图书馆」
  Future<void> moveToLibrary();

  /// 回到初始位置
  void reset();
}

/// 演示用的假定位：只是把地点名字改一改，不碰任何真实坐标。
class DemoLocationProvider implements LocationProvider {
  @override
  String label = '校园南门';

  @override
  Future<void> moveToLibrary() async {
    label = '图书馆入口';
  }

  @override
  void reset() {
    label = '校园南门';
  }
}
