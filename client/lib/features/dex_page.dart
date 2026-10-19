import 'package:flutter/material.dart';

import '../core/tokens.dart';
import '../models/sprite.dart';
import '../widgets/section_header.dart';
import '../widgets/sprite_card.dart';

/// 图鉴页。对应设计稿 `05-图鉴.png`。
///
/// 三个格子中只有第 1 格会亮——后端目前只返回一个全局的「是否已捕获攻城狮」，
/// 没返回精灵列表，所以格子本身写死在 [dexSlots] 里。
class DexPage extends StatelessWidget {
  const DexPage({
    super.key,
    required this.captured,
    required this.collected,
    required this.totalSpecies,
    required this.onOpenDetail,
  });

  final bool captured;

  /// 已捕获种类数（来自后端）
  final int collected;

  /// 总种类数（来自后端）
  final int totalSpecies;

  /// 点第 1 格时打开精灵详情
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
            eyebrow: 'CAMPUS FIELD NOTES',
            title: '校园图鉴',
            subtitle: '每一次探索，都留下一页新的相遇。',
          ),

          const SizedBox(height: AppSpacing.base * 8),
          _buildProgress(),
          const SizedBox(height: AppSpacing.base * 6),
          _buildGrid(context),

          const SizedBox(height: AppSpacing.base * 8),
          const Text(
            '下一次相遇',
            style: TextStyle(
              fontSize: 18,
              fontWeight: FontWeight.bold,
              color: AppColors.text,
            ),
          ),
          const SizedBox(height: AppSpacing.base * 3),
          const Text(
            '未来可以通过自习驻留与校园漫步，解锁不同的校园精灵。\n'
            '正式内容计划扩充至 8–12 种。',
            style: TextStyle(
              fontSize: AppFontSizes.body,
              color: AppColors.secondaryText,
              height: 1.7,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildProgress() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          '$collected / $totalSpecies',
          style: const TextStyle(
            fontSize: 38,
            fontWeight: FontWeight.bold,
            color: AppColors.primary,
            height: 1.1,
          ),
        ),
        const SizedBox(height: AppSpacing.base * 3),

        // 进度条：外层是浅色轨道，内层按比例填充主色
        Container(
          height: 6,
          decoration: BoxDecoration(
            color: AppColors.primarySoft,
            borderRadius: BorderRadius.circular(5),
          ),
          child: FractionallySizedBox(
            alignment: Alignment.centerLeft,
            widthFactor: totalSpecies == 0 ? 0 : collected / totalSpecies,
            child: Container(
              decoration: BoxDecoration(
                color: AppColors.primary,
                borderRadius: BorderRadius.circular(5),
              ),
            ),
          ),
        ),
        const SizedBox(height: AppSpacing.base * 3),

        Text(
          '本次初稿展示 $totalSpecies 个图鉴格，已实现 $collected 种精灵。',
          style: const TextStyle(
            fontSize: AppFontSizes.secondary,
            color: AppColors.secondaryText,
          ),
        ),
      ],
    );
  }

  Widget _buildGrid(BuildContext context) {
    return GridView.count(
      crossAxisCount: 3,
      shrinkWrap: true,
      physics: const NeverScrollableScrollPhysics(),
      mainAxisSpacing: AppSpacing.base * 3,
      crossAxisSpacing: AppSpacing.base * 3,
      childAspectRatio: 0.70,
      children: [
        for (var i = 0; i < dexSlots.length; i++)
          GestureDetector(
            onTap: () => _handleTap(context, i),
            child: SpriteCard(
              slot: dexSlots[i],
              // 只有第 1 格会被捕获状态点亮
              unlocked: i == siegeLionSlotIndex && captured,
            ),
          ),
      ],
    );
  }

  void _handleTap(BuildContext context, int index) {
    if (index == siegeLionSlotIndex) {
      // 未捕获时也能打开，只是正文会显示「还未收集」
      onOpenDetail();
      return;
    }
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(content: Text('这是后续精灵的设计占位')),
    );
  }
}
