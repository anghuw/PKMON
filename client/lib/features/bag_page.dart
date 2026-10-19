import 'package:flutter/material.dart';

import '../core/tokens.dart';
import '../widgets/bag_row.dart';
import '../widgets/section_header.dart';

/// 我的精灵（背包）页。对应设计稿 `06-背包.png`。
///
/// 和图鉴的区别：
/// - 图鉴记录「见过哪些种类」（去重），一只只出现一次；
/// - 背包记录「手上有几只」（实物），同一只可能有 ×N。
///
/// 所以这里用列表（每行信息更多），图鉴用网格。
class BagPage extends StatelessWidget {
  const BagPage({
    super.key,
    required this.captured,
    required this.count,
    required this.onOpenDetail,
  });

  final bool captured;

  /// 持有数量（来自后端）
  final int count;

  final VoidCallback onOpenDetail;

  @override
  Widget build(BuildContext context) {
    return SingleChildScrollView(
      padding: const EdgeInsets.fromLTRB(
        AppSpacing.mapContentSide,
        AppSpacing.base * 5,
        AppSpacing.mapContentSide,
        AppSpacing.base * 8,
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const SectionHeader(
            eyebrow: 'YOUR COMPANIONS',
            title: '我的精灵',
            subtitle: '记录已经收入背包的伙伴。',
          ),

          const SizedBox(height: AppSpacing.base * 8),

          if (captured)
            BagRow(count: count, onTap: onOpenDetail)
          else
            _buildEmptyState(),

          const SizedBox(height: AppSpacing.base * 8),

          const Text(
            '本地演示使用内存数据。刷新页面会创建新会话，重启服务会清空全部演示记录。',
            style: TextStyle(
              fontSize: AppFontSizes.secondary,
              color: AppColors.secondaryText,
              height: 1.8,
            ),
          ),
        ],
      ),
    );
  }

  /// 一只都没有时的空态。设计规范要求「不出现空白页」。
  Widget _buildEmptyState() {
    return Padding(
      padding: const EdgeInsets.fromLTRB(
        AppSpacing.base * 6,
        AppSpacing.base * 20,
        AppSpacing.base * 6,
        AppSpacing.base * 6,
      ),
      child: const Column(
        children: [
          Icon(Icons.backpack_outlined, size: 64, color: AppColors.primary),
          SizedBox(height: AppSpacing.base * 5),
          Text(
            '背包里还没有精灵',
            style: TextStyle(fontSize: AppFontSizes.body, color: AppColors.text),
          ),
          SizedBox(height: AppSpacing.base * 2),
          Text(
            '回到地图，完成第一次相遇。',
            style: TextStyle(
              fontSize: AppFontSizes.body,
              color: AppColors.secondaryText,
            ),
          ),
        ],
      ),
    );
  }
}
