import 'package:flutter/material.dart';

import '../../../../data/models/user_model.dart';
import '../../../widgets/common/app_search_bar.dart';

class MentorFilterBar extends StatelessWidget {
  final List<DepartmentModel> departments;
  final bool? selectedStatus;
  final dynamic selectedDepartmentId;
  final TextEditingController? searchController;
  final ValueChanged<String> onSearchChanged;
  final ValueChanged<bool?> onStatusChanged;
  final ValueChanged<dynamic> onDepartmentChanged;

  const MentorFilterBar({
    super.key,
    required this.departments,
    required this.selectedStatus,
    required this.selectedDepartmentId,
    this.searchController,
    required this.onSearchChanged,
    required this.onStatusChanged,
    required this.onDepartmentChanged,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.fromLTRB(16, 12, 16, 8),
      color: Colors.white,
      child: Column(
        children: [
          AppSearchBar(
            controller: searchController,
            hintText: 'Search mentors by name or email...',
            onChanged: onSearchChanged,
          ),
          const SizedBox(height: 10),

          // Filter Row
          SingleChildScrollView(
            scrollDirection: Axis.horizontal,
            child: Row(
              children: [
                // Status Filter Chips
                _buildFilterChip(
                  label: 'All Status',
                  isSelected: selectedStatus == null,
                  onTap: () => onStatusChanged(null),
                ),
                const SizedBox(width: 8),
                _buildFilterChip(
                  label: 'Active',
                  isSelected: selectedStatus == true,
                  onTap: () => onStatusChanged(true),
                ),
                const SizedBox(width: 8),
                _buildFilterChip(
                  label: 'Inactive',
                  isSelected: selectedStatus == false,
                  onTap: () => onStatusChanged(false),
                ),
                const SizedBox(width: 12),
                Container(
                  height: 20,
                  width: 1,
                  color: const Color(0xFFE2E8F0),
                ),
                const SizedBox(width: 12),

                // Department Dropdown Chip
                Container(
                  padding: const EdgeInsets.symmetric(horizontal: 10),
                  decoration: BoxDecoration(
                    color: selectedDepartmentId != null
                        ? const Color(0xFFEFF6FF)
                        : const Color(0xFFF1F5F9),
                    borderRadius: BorderRadius.circular(20),
                    border: Border.all(
                      color: selectedDepartmentId != null
                          ? const Color(0xFFBFDBFE)
                          : const Color(0xFFE2E8F0),
                    ),
                  ),
                  child: DropdownButtonHideUnderline(
                    child: DropdownButton<dynamic>(
                      value: selectedDepartmentId,
                      hint: const Text(
                        'All Departments',
                        style: TextStyle(
                          fontSize: 12,
                          fontWeight: FontWeight.w600,
                          color: Color(0xFF475569),
                        ),
                      ),
                      icon: const Icon(
                        Icons.arrow_drop_down_rounded,
                        size: 20,
                        color: Color(0xFF475569),
                      ),
                      style: const TextStyle(
                        fontSize: 12,
                        fontWeight: FontWeight.w600,
                        color: Color(0xFF1E40AF),
                      ),
                      items: [
                        const DropdownMenuItem<dynamic>(
                          value: null,
                          child: Text('All Departments'),
                        ),
                        ...departments.map(
                          (dept) => DropdownMenuItem<dynamic>(
                            value: dept.id,
                            child: Text(dept.title),
                          ),
                        ),
                      ],
                      onChanged: onDepartmentChanged,
                    ),
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildFilterChip({
    required String label,
    required bool isSelected,
    required VoidCallback onTap,
  }) {
    return GestureDetector(
      onTap: onTap,
      child: AnimatedContainer(
        duration: const Duration(milliseconds: 200),
        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
        decoration: BoxDecoration(
          color: isSelected ? const Color(0xFF1E40AF) : const Color(0xFFF1F5F9),
          borderRadius: BorderRadius.circular(20),
          border: Border.all(
            color: isSelected ? const Color(0xFF1E40AF) : const Color(0xFFE2E8F0),
          ),
        ),
        child: Text(
          label,
          style: TextStyle(
            fontSize: 12,
            fontWeight: FontWeight.w600,
            color: isSelected ? Colors.white : const Color(0xFF475569),
          ),
        ),
      ),
    );
  }
}
