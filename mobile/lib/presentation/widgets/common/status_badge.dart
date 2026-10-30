import 'package:flutter/material.dart';

class StatusBadge extends StatelessWidget {
  final String? status;
  final bool? isActive;
  final double fontSize;
  final EdgeInsetsGeometry padding;

  const StatusBadge({
    super.key,
    this.status,
    this.isActive,
    this.fontSize = 12,
    this.padding = const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
  });

  const StatusBadge.active(
    bool active, {
    super.key,
    this.fontSize = 12,
    this.padding = const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
  })  : isActive = active,
        status = active ? 'Active' : 'Inactive';

  factory StatusBadge.fromActive(bool active) {
    return StatusBadge(
      isActive: active,
      status: active ? 'Active' : 'Inactive',
    );
  }

  @override
  Widget build(BuildContext context) {
    final (label, textColor, bgColor, dotColor) = _resolveColorsAndLabel();

    return Container(
      padding: padding,
      decoration: BoxDecoration(
        color: bgColor,
        borderRadius: BorderRadius.circular(20),
        border: Border.all(color: dotColor.withValues(alpha: 0.25)),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Container(
            width: 6,
            height: 6,
            decoration: BoxDecoration(
              color: dotColor,
              shape: BoxShape.circle,
            ),
          ),
          const SizedBox(width: 6),
          Text(
            label,
            style: TextStyle(
              color: textColor,
              fontSize: fontSize,
              fontWeight: FontWeight.w600,
              letterSpacing: 0.3,
            ),
          ),
        ],
      ),
    );
  }

  (String, Color, Color, Color) _resolveColorsAndLabel() {
    if (isActive != null) {
      if (isActive == true) {
        return (
          'Active',
          const Color(0xFF15803D),
          const Color(0xFFDCFCE7),
          const Color(0xFF22C55E),
        );
      } else {
        return (
          'Inactive',
          const Color(0xFF475569),
          const Color(0xFFF1F5F9),
          const Color(0xFF94A3B8),
        );
      }
    }

    final raw = (status ?? 'ACTIVE').toUpperCase().trim();
    switch (raw) {
      case 'ACTIVE':
        return (
          'Active',
          const Color(0xFF15803D),
          const Color(0xFFDCFCE7),
          const Color(0xFF22C55E),
        );
      case 'INACTIVE':
        return (
          'Inactive',
          const Color(0xFF475569),
          const Color(0xFFF1F5F9),
          const Color(0xFF94A3B8),
        );
      case 'WARNING':
        return (
          'Warning',
          const Color(0xFFB45309),
          const Color(0xFFFEF3C7),
          const Color(0xFFF59E0B),
        );
      case 'COMPLETED':
        return (
          'Completed',
          const Color(0xFF1D4ED8),
          const Color(0xFFDBEAFE),
          const Color(0xFF3B82F6),
        );
      case 'DROPPED':
        return (
          'Dropped',
          const Color(0xFFB91C1C),
          const Color(0xFFFEE2E2),
          const Color(0xFFEF4444),
        );
      default:
        return (
          raw,
          const Color(0xFF475569),
          const Color(0xFFF1F5F9),
          const Color(0xFF94A3B8),
        );
    }
  }
}
