import 'package:flutter/material.dart';

import '../core/tokens.dart';
import '../models/sprite.dart';
import 'app_buttons.dart';

/// 精灵详情弹窗。
///
/// 两个入口：图鉴第 1 格、背包卡片。**未捕获时也能打开**，
/// 只是正文换成「还未收集」，用来吊胃口。
Future<void> showSpriteDetail(
  BuildContext context, {
  required bool unlocked,
}) {
  return showModalBottomSheet<void>(
    context: context,
    showDragHandle: true,
    isScrollControlled: true,
    builder: (ctx) => Padding(
      padding: const EdgeInsets.fromLTRB(24, 8, 24, 24),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          Image.asset(AppAssets.siegeLion, height: 165),

          const SizedBox(height: AppSpacing.base * 4),
          const Text(
            '攻城狮',
            style: TextStyle(
              fontSize: 24,
              fontWeight: FontWeight.bold,
              color: AppColors.text,
            ),
          ),

          const SizedBox(height: AppSpacing.base * 2),
          const Text(
            'No.001  /  图书馆',
            style: TextStyle(fontSize: AppFontSizes.body, color: AppColors.primary),
          ),

          const SizedBox(height: AppSpacing.base * 4),
          const Text(
            '它头顶养着一只小黄鸭，靠不停问「为什么」揪出藏在代码里的 bug。\n'
            '设计设定：图书馆的原创校园精灵。',
            textAlign: TextAlign.center,
            style: TextStyle(
              fontSize: AppFontSizes.body,
              color: AppColors.text,
              height: 1.8,
            ),
          ),

          const SizedBox(height: AppSpacing.base * 4),
          Text(
            unlocked
                ? '首次遇见地点：$siegeLionLocationDetail\n本次会话已收集 1 只'
                : '还未收集。前往地图，完成一次知识问答。',
            textAlign: TextAlign.center,
            style: const TextStyle(
              fontSize: AppFontSizes.body,
              color: AppColors.secondaryText,
              height: 1.6,
            ),
          ),

          const SizedBox(height: AppSpacing.base * 6),
          PrimaryButton(
            label: '返回',
            onPressed: () => Navigator.of(ctx).pop(),
          ),
        ],
      ),
    ),
  );
}
