import 'package:flutter/material.dart';

import '../../../../data/models/mentor_model.dart';
import '../../../widgets/common/status_badge.dart';

class MentorCard extends StatelessWidget {
  final MentorModel mentor;
  final bool isToggling;
  final VoidCallback onTap;
  final VoidCallback onToggleStatus;
  final VoidCallback onEdit;

  const MentorCard({
    super.key,
    required this.mentor,
    required this.isToggling,
    required this.onTap,
    required this.onToggleStatus,
    required this.onEdit,
  });

  @override
  Widget build(BuildContext context) {
    const primaryColor = Color(0xFF1E40AF);

    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(16),
      child: Container(
        padding: const EdgeInsets.all(16),
        decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.circular(16),
          border: Border.all(color: const Color(0xFFE2E8F0)),
          boxShadow: [
            BoxShadow(
              color: Colors.black.withValues(alpha: 0.02),
              blurRadius: 8,
              offset: const Offset(0, 2),
            ),
          ],
        ),
        child: Column(
          children: [
            Row(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                // Avatar Initials
                CircleAvatar(
                  radius: 24,
                  backgroundColor: const Color(0xFFEFF6FF),
                  child: Text(
                    mentor.initials,
                    style: const TextStyle(
                      fontSize: 16,
                      fontWeight: FontWeight.bold,
                      color: primaryColor,
                    ),
                  ),
                ),
                const SizedBox(width: 12),

                // Name & Email
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        mentor.fullName,
                        style: const TextStyle(
                          fontSize: 15,
                          fontWeight: FontWeight.bold,
                          color: Color(0xFF0F172A),
                        ),
                      ),
                      const SizedBox(height: 2),
                      Text(
                        mentor.email,
                        style: const TextStyle(
                          fontSize: 13,
                          color: Color(0xFF64748B),
                        ),
                      ),
                    ],
                  ),
                ),

                // Edit Action
                IconButton(
                  icon: const Icon(
                    Icons.edit_outlined,
                    color: Color(0xFF64748B),
                    size: 20,
                  ),
                  tooltip: 'Edit Mentor',
                  onPressed: onEdit,
                ),
              ],
            ),
            const SizedBox(height: 12),
            const Divider(height: 1, color: Color(0xFFF1F5F9)),
            const SizedBox(height: 12),

            // Badges & Toggle Row
            Row(
              children: [
                // Department Badge
                Flexible(
                  child: Container(
                    padding: const EdgeInsets.symmetric(
                      horizontal: 8,
                      vertical: 4,
                    ),
                    decoration: BoxDecoration(
                      color: const Color(0xFFF1F5F9),
                      borderRadius: BorderRadius.circular(8),
                    ),
                    child: Text(
                      mentor.department?.title ?? 'No Department',
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                      style: const TextStyle(
                        fontSize: 11,
                        fontWeight: FontWeight.w600,
                        color: Color(0xFF334155),
                      ),
                    ),
                  ),
                ),
                const SizedBox(width: 8),

                // Interns Count Badge
                Container(
                  padding: const EdgeInsets.symmetric(
                    horizontal: 8,
                    vertical: 4,
                  ),
                  decoration: BoxDecoration(
                    color: const Color(0xFFEFF6FF),
                    borderRadius: BorderRadius.circular(8),
                  ),
                  child: Row(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      const Icon(
                        Icons.people_outline_rounded,
                        size: 13,
                        color: primaryColor,
                      ),
                      const SizedBox(width: 4),
                      Text(
                        '${mentor.internCount} Interns',
                        style: const TextStyle(
                          fontSize: 11,
                          fontWeight: FontWeight.bold,
                          color: primaryColor,
                        ),
                      ),
                    ],
                  ),
                ),
                const Spacer(),

                // Status Badge & Switch
                StatusBadge(isActive: mentor.active),
                const SizedBox(width: 6),
                isToggling
                    ? const SizedBox(
                        width: 24,
                        height: 24,
                        child: CircularProgressIndicator(strokeWidth: 2),
                      )
                    : Transform.scale(
                        scale: 0.8,
                        child: Switch(
                          value: mentor.active,
                          activeThumbColor: const Color(0xFF16A34A),
                          activeTrackColor: const Color(0xFFDCFCE7),
                          onChanged: (_) => onToggleStatus(),
                        ),
                      ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}
