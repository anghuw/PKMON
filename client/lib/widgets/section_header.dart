import 'package:flutter/material.dart';

import '../core/tokens.dart';

/// 编辑式页面头：小标签 + 大标题 + 副标题。
///
/// 设计稿里图鉴页的 `CAMPUS FIELD NOTES`、背包页的 `YOUR COMPANIONS`、
/// 个人页的 `EXPLORER PROFILE` 都是这个结构，所以抽成一个组件。
///
/// 小标签用大写英文 + 加大字距，是为了做出「杂志栏目」的观感，
/// 中文字距加大反而难看，所以副标题保持正常字距。
class SectionHeader extends StatelessWidget {
  const SectionHeader({
    super.key,
    required this.eyebrow,
    required this.title,
    required this.subtitle,
  });

  /// 上方小标签，如 `CAMPUS FIELD NOTES`
  final String eyebrow;

  /// 大标题，如 `校园图鉴`
  final String title;

  /// 副标题，如 `每一次探索，都留下一页新的相遇。`
  final String subtitle;

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          eyebrow,
          style: const TextStyle(
            fontSize: AppFontSizes.secondary,
            color: AppColors.primary,
            fontWeight: FontWeight.w600,
            letterSpacing: 1.6,
          ),
        ),
        const SizedBox(height: AppSpacing.base * 2),
        Text(
          title,
          style: const TextStyle(
            fontSize: AppFontSizes.pageTitle,
            fontWeight: FontWeight.bold,
            color: AppColors.text,
          ),
        ),
        const SizedBox(height: AppSpacing.base * 2),
        Text(
          subtitle,
          style: const TextStyle(
            fontSize: AppFontSizes.body,
            color: AppColors.secondaryText,
          ),
        ),
      ],
    );
  }
}
