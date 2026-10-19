import 'package:flutter/material.dart';

import '../core/tokens.dart';
import '../models/sprite.dart';

/// 背包里的一行：左边立绘、中间信息、右边持有数量。
///
/// 设计稿 `06-背包.png` 用的是**列表**而不是网格，
/// 注意不要跟图鉴的 [SpriteCard] 混用。
///
/// 这一版只有攻城狮一种，所以内容写死；将来有更多精灵时改成接收数据。
class BagRow extends StatelessWidget {
  const BagRow({super.key, required this.onTap, this.count = 1});

  final VoidCallback onTap;

  /// 持有数量
  final int count;

  @override
  Widget build(BuildContext context) {
    return Material(
      color: AppColors.surface,
      borderRadius: BorderRadius.circular(AppRadius.listCard),
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(AppRadius.listCard),
        child: Padding(
          padding: const EdgeInsets.all(AppSpacing.cardPadding),
          child: Row(
            children: [
              SizedBox(
                width: 64,
                height: 72,
                child: Image.asset(
                  AppAssets.siegeLion,
                  fit: BoxFit.contain,
                  errorBuilder: (context, error, stackTrace) => const Icon(
                    Icons.image_not_supported_outlined,
                    size: 32,
                    color: AppColors.divider,
                  ),
                ),
              ),
              const SizedBox(width: AppSpacing.base * 4),

              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Text(
                      '攻城狮',
                      style: TextStyle(
                        fontSize: 20,
                        fontWeight: FontWeight.bold,
                        color: AppColors.text,
                      ),
                    ),
                    const SizedBox(height: AppSpacing.base),

                    // 编号，用主色
                    Text(
                      'No.001',
                      style: const TextStyle(
                        fontSize: AppFontSizes.body,
                        color: AppColors.primary,
                        fontWeight: FontWeight.w500,
                      ),
                    ),
                    const SizedBox(height: AppSpacing.base),

                    const Text(
                      '$siegeLionLocationDetail\n本次会话捕获',
                      style: TextStyle(
                        fontSize: 11,
                        color: AppColors.secondaryText,
                        height: 1.5,
                      ),
                    ),
                  ],
                ),
              ),

              const SizedBox(width: AppSpacing.base * 2),

              Text(
                '× $count',
                style: const TextStyle(
                  fontSize: 22,
                  color: AppColors.primary,
                  fontWeight: FontWeight.w500,
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
