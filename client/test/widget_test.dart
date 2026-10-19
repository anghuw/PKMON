// 界面冒烟测试。
//
// 这些测试**只渲染单个页面**，不启动 HomeShell，所以不会去连后端，
// 跑起来快而且不会因为网络偶发失败。
//
// 运行：flutter test

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:pkmon_app/features/bag_page.dart';
import 'package:pkmon_app/features/dex_page.dart';
import 'package:pkmon_app/features/map_page.dart';

/// 把被测组件套进一个最小的 App 里（页面要用到 Material 主题和 Scaffold）
Widget wrap(Widget child) => MaterialApp(home: Scaffold(body: child));

/// 地图页的默认参数，各测试按需覆盖
Widget mapPage({
  bool nearby = false,
  bool captured = false,
  bool actionEnabled = true,
}) {
  return wrap(MapPage(
    locationLabel: '校园南门',
    nearby: nearby,
    captured: captured,
    taskRunning: false,
    actionEnabled: actionEnabled,
    collected: captured ? 1 : 0,
    totalSpecies: 3,
    helloText: 'hello world',
    onExplore: () {},
    onCapture: () {},
    onGoToDex: () {},
    onTasks: () {},
  ));
}

void main() {
  group('地图页的三种形态', () {
    testWidgets('初始态：按钮是「模拟走到图书馆」，地图上没有精灵', (tester) async {
      await tester.pumpWidget(mapPage());

      expect(find.text('PKMon'), findsOneWidget);
      expect(find.text('模拟走到图书馆'), findsOneWidget);
      expect(find.text('点击遇见'), findsNothing);
      expect(find.text('收集 0 / 3'), findsOneWidget);
      // 地标应该在
      expect(find.text('图书馆'), findsOneWidget);
      expect(find.text('银杏湖'), findsOneWidget);
    });

    testWidgets('精灵出现：按钮变「遇见攻城狮」，地图上出现「点击遇见」', (tester) async {
      await tester.pumpWidget(mapPage(nearby: true));

      expect(find.text('遇见攻城狮'), findsOneWidget);
      expect(find.text('点击遇见'), findsOneWidget);
      expect(find.text('模拟走到图书馆'), findsNothing);
    });

    testWidgets('已捕获：按钮变「查看我的图鉴」，精灵不再显示', (tester) async {
      await tester.pumpWidget(mapPage(captured: true));

      expect(find.text('查看我的图鉴'), findsOneWidget);
      expect(find.text('点击遇见'), findsNothing);
      expect(find.text('收集 1 / 3'), findsOneWidget);
    });

    testWidgets('后端没就绪时主按钮不可点', (tester) async {
      await tester.pumpWidget(mapPage(actionEnabled: false));

      final button = tester.widget<FilledButton>(find.byType(FilledButton));
      expect(button.onPressed, isNull);
    });
  });

  group('图鉴页', () {
    testWidgets('未捕获：进度 0/3，第 1 格显示未解锁，另两格是待开发占位', (tester) async {
      await tester.pumpWidget(wrap(const DexPage(
        captured: false,
        collected: 0,
        totalSpecies: 3,
        onOpenDetail: _noop,
      )));

      expect(find.text('0 / 3'), findsOneWidget);
      // 三格都是「未解锁」
      expect(find.text('未解锁'), findsNWidgets(3));
      expect(find.text('湖畔 · 待开发'), findsOneWidget);
      expect(find.text('跑道 · 待开发'), findsOneWidget);
    });

    testWidgets('已捕获：进度 1/3，第 1 格亮出攻城狮', (tester) async {
      await tester.pumpWidget(wrap(const DexPage(
        captured: true,
        collected: 1,
        totalSpecies: 3,
        onOpenDetail: _noop,
      )));

      expect(find.text('1 / 3'), findsOneWidget);
      expect(find.text('攻城狮'), findsOneWidget);
      // 只剩 2 个未解锁
      expect(find.text('未解锁'), findsNWidgets(2));
    });
  });

  group('背包页', () {
    testWidgets('未捕获：显示空态引导', (tester) async {
      await tester.pumpWidget(wrap(const BagPage(
        captured: false,
        count: 0,
        onOpenDetail: _noop,
      )));

      expect(find.text('背包里还没有精灵'), findsOneWidget);
      expect(find.text('回到地图，完成第一次相遇。'), findsOneWidget);
    });

    testWidgets('已捕获：显示精灵条目和持有数量', (tester) async {
      await tester.pumpWidget(wrap(const BagPage(
        captured: true,
        count: 1,
        onOpenDetail: _noop,
      )));

      expect(find.text('攻城狮'), findsOneWidget);
      expect(find.text('× 1'), findsOneWidget);
      expect(find.text('背包里还没有精灵'), findsNothing);
    });
  });
}

void _noop() {}
