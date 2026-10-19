import 'package:flutter/material.dart';

import '../core/tokens.dart';

/// 白底圆角胶囊标签。
///
/// 设计稿里地图左上角的「模拟位置 · 示意地图」用它。
class PillChip extends StatelessWidget {
  const PillChip({super.key, required this.icon, required this.label});

  final IconData icon;
  final String label;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
      decoration: BoxDecoration(
        color: AppColors.surface,
        borderRadius: BorderRadius.circular(AppRadius.pill),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withValues(alpha: 0.06),
            blurRadius: 6,
            offset: const Offset(0, 2),
          ),
        ],
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(icon, size: 14, color: AppColors.primary),
          const SizedBox(width: AppSpacing.base * 1.5),
          Text(
            label,
            style: const TextStyle(
              fontSize: AppFontSizes.secondary,
              color: AppColors.text,
            ),
          ),
        ],
      ),
    );
  }
}
