import 'package:flutter/material.dart';

import '../core/tokens.dart';

/// 全局错误横幅。
///
/// 放在四个页面的**上方**（不是 SnackBar），所以切到哪个 tab 都能看到。
/// 出现时机：任何走 `action()` 的操作抛异常。
/// 消失时机：下一次 `action()` 开始时自动清空。
///
/// 没有关闭按钮——这是刻意的，避免用户把错误叉掉后忘了问题还在。
class ErrorBanner extends StatelessWidget {
  const ErrorBanner({
    super.key,
    required this.message,
    required this.onRetry,
    this.retryEnabled = true,
  });

  /// 后端返回的错误原文
  final String message;

  final VoidCallback onRetry;

  /// 加载中时禁用重试，避免重复请求
  final bool retryEnabled;

  @override
  Widget build(BuildContext context) {
    return Material(
      color: AppColors.errorSurface,
      child: Padding(
        padding: const EdgeInsets.all(12),
        child: Row(
          children: [
            Expanded(
              child: Text(
                '连接失败或操作未完成：$message',
                style: const TextStyle(
                  fontSize: AppFontSizes.secondary,
                  color: AppColors.text,
                ),
              ),
            ),
            TextButton(
              onPressed: retryEnabled ? onRetry : null,
              child: const Text('重试'),
            ),
          ],
        ),
      ),
    );
  }
}
