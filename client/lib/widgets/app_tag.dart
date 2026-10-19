import 'package:flutter/material.dart';

import '../core/tokens.dart';

/// 小标签：圆角胶囊 + 图标 + 文字。
///
/// 设计稿里地图精灵下面那个「点击遇见」用它。
/// [gold] 为 true 时底色是浅金色（强调"可以点"），否则是白色。
///
/// 注意：金色标签的**文字和图标仍然是主色绿**，只有底色变金。
class AppTag extends StatelessWidget {
  const AppTag({
    super.key,
    required this.label,
    required this.icon,
    this.gold = false,
  });

  final String label;
  final IconData icon;
  final bool gold;

  /// 浅金底色，设计稿里定为这个值
  static const _goldSurface = Color(0xFFFFF3D9);

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 7),
      decoration: BoxDecoration(
        color: gold ? _goldSurface : Colors.white.withValues(alpha: 0.94),
        borderRadius: BorderRadius.circular(24),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(icon, size: 13, color: AppColors.primary),
          const SizedBox(width: AppSpacing.base),
          Text(
            label,
            style: const TextStyle(
              fontSize: 10,
              fontWeight: FontWeight.bold,
              color: AppColors.primary,
            ),
          ),
        ],
      ),
    );
  }
}
