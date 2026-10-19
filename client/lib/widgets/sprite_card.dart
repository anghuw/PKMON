import 'package:flutter/material.dart';

import '../core/tokens.dart';
import '../models/sprite.dart';

/// 图鉴里的三列格子卡片。
///
/// 已解锁 → 显示立绘；未解锁 → 显示锁图标 + 「未解锁」。
/// 卡片尺寸由外面的 GridView 决定，所以内部用 Expanded 让立绘区自适应。
class SpriteCard extends StatelessWidget {
  const SpriteCard({
    super.key,
    required this.slot,
    required this.unlocked,
  });

  final DexSlot slot;

  /// 这一格是否已解锁。目前只有第 1 格会亮（后端只返回一个全局布尔值）。
  final bool unlocked;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(AppSpacing.base * 3),
      decoration: BoxDecoration(
        color: AppColors.surface,
        borderRadius: BorderRadius.circular(AppRadius.listCard),
        border: Border.all(color: AppColors.divider),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            slot.id,
            style: const TextStyle(
              fontSize: AppFontSizes.secondary,
              color: AppColors.secondaryText,
            ),
          ),
          const SizedBox(height: AppSpacing.base * 2),

          Expanded(child: Center(child: _buildPreview())),

          const SizedBox(height: AppSpacing.base * 2),

          Text(
            unlocked ? slot.name : '未解锁',
            maxLines: 1,
            overflow: TextOverflow.ellipsis,
            style: const TextStyle(
              fontSize: AppFontSizes.body,
              fontWeight: FontWeight.w600,
              color: AppColors.text,
            ),
          ),
          const SizedBox(height: AppSpacing.base * 0.5),

          Text(
            slot.location,
            maxLines: 1,
            overflow: TextOverflow.ellipsis,
            style: const TextStyle(
              fontSize: AppFontSizes.secondary,
              color: AppColors.secondaryText,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildPreview() {
    if (!unlocked || slot.assetPath == null) {
      return const Icon(Icons.lock_outline, size: 32, color: Color(0xFFB6C8C2));
    }
    return Image.asset(
      slot.assetPath!,
      fit: BoxFit.contain,
      // 立绘缺失时不要让页面崩掉，退化成锁图标
      errorBuilder: (context, error, stackTrace) =>
          const Icon(Icons.lock_outline, size: 32, color: Color(0xFFB6C8C2)),
    );
  }
}
