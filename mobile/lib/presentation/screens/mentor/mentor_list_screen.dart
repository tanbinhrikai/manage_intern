import 'package:flutter/material.dart';

import '../../../data/models/mentor_model.dart';
import '../../../data/models/user_model.dart';
import '../../../data/services/mentor_service.dart';
import '../../widgets/common/empty_state_view.dart';
import 'mentor_form_screen.dart';
import 'widgets/mentor_card.dart';
import 'widgets/mentor_detail_dialog.dart';
import 'widgets/mentor_filter_bar.dart';

class MentorListScreen extends StatefulWidget {
  const MentorListScreen({super.key});

  @override
  State<MentorListScreen> createState() => _MentorListScreenState();
}

class _MentorListScreenState extends State<MentorListScreen> {
  final ScrollController _scrollController = ScrollController();

  List<MentorModel> _mentors = [];
  List<DepartmentModel> _departments = [];

  bool _isLoadingInitial = true;
  bool _isLoadingMore = false;
  bool _hasMore = false;
  int _currentPage = 0;
  final int _pageSize = 10;

  String _searchKeyword = '';
  bool? _filterIsActive;
  dynamic _filterDepartmentId;

  final Set<String> _togglingMentorIds = {};

  @override
  void initState() {
    super.initState();
    _scrollController.addListener(_onScroll);
    _loadInitialData();
  }

  Future<void> _loadInitialData() async {
    setState(() => _isLoadingInitial = true);
    await Future.wait([
      _loadDepartments(),
      _fetchMentors(page: 0, reset: true),
    ]);
    if (mounted) {
      setState(() => _isLoadingInitial = false);
    }
  }

  Future<void> _loadDepartments() async {
    try {
      final list = await MentorService.instance.getDepartments();
      if (mounted) {
        setState(() => _departments = list);
      }
    } catch (_) {}
  }

  Future<void> _fetchMentors({required int page, bool reset = false}) async {
    try {
      final response = await MentorService.instance.getMentors(
        page: page,
        limit: _pageSize,
        keyword: _searchKeyword,
        departmentId: _filterDepartmentId,
        isActive: _filterIsActive,
      );

      if (!mounted) return;

      setState(() {
        if (reset) {
          _mentors = response.items;
        } else {
          _mentors.addAll(response.items);
        }
        _currentPage = response.currentPage;
        _hasMore = response.hasNext;
      });
    } catch (e) {
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(e.toString().replaceAll('Exception: ', '')),
          backgroundColor: const Color(0xFFDC2626),
        ),
      );
    }
  }

  void _onScroll() {
    if (_scrollController.position.pixels >=
            _scrollController.position.maxScrollExtent - 200 &&
        !_isLoadingMore &&
        _hasMore) {
      _loadMore();
    }
  }

  Future<void> _loadMore() async {
    if (_isLoadingMore || !_hasMore) return;
    setState(() => _isLoadingMore = true);
    await _fetchMentors(page: _currentPage + 1);
    if (mounted) {
      setState(() => _isLoadingMore = false);
    }
  }

  Future<void> _handleRefresh() async {
    await _fetchMentors(page: 0, reset: true);
  }

  void _onSearchChanged(String val) {
    setState(() => _searchKeyword = val);
    _fetchMentors(page: 0, reset: true);
  }

  void _onStatusFilterSelected(bool? active) {
    if (_filterIsActive == active) return;
    setState(() => _filterIsActive = active);
    _fetchMentors(page: 0, reset: true);
  }

  void _onDepartmentFilterSelected(dynamic deptId) {
    if (_filterDepartmentId == deptId) return;
    setState(() => _filterDepartmentId = deptId);
    _fetchMentors(page: 0, reset: true);
  }

  Future<void> _toggleMentorStatus(MentorModel mentor) async {
    final mentorId = mentor.id;
    setState(() => _togglingMentorIds.add(mentorId));

    try {
      final updated = await MentorService.instance.toggleStatus(mentorId);
      if (!mounted) return;

      setState(() {
        final index = _mentors.indexWhere((m) => m.id == mentorId);
        if (index != -1) {
          _mentors[index] = updated;
        }
        _togglingMentorIds.remove(mentorId);
      });

      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(
            '${mentor.fullName} is now ${updated.active ? "Active" : "Inactive"}',
          ),
          backgroundColor: updated.active
              ? const Color(0xFF16A34A)
              : const Color(0xFF64748B),
          duration: const Duration(seconds: 2),
        ),
      );
    } catch (e) {
      if (!mounted) return;
      setState(() => _togglingMentorIds.remove(mentorId));
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(e.toString().replaceAll('Exception: ', '')),
          backgroundColor: const Color(0xFFDC2626),
        ),
      );
    }
  }

  Future<void> _navigateToAddMentor() async {
    final created = await Navigator.of(context).push<bool>(
      MaterialPageRoute(builder: (context) => const MentorFormScreen()),
    );
    if (created == true) {
      _fetchMentors(page: 0, reset: true);
    }
  }

  Future<void> _navigateToEditMentor(MentorModel mentor) async {
    final updated = await Navigator.of(context).push<bool>(
      MaterialPageRoute(builder: (context) => MentorFormScreen(mentor: mentor)),
    );
    if (updated == true) {
      _fetchMentors(page: 0, reset: true);
    }
  }

  void _showMentorDetailDialog(MentorModel mentor) {
    showDialog(
      context: context,
      builder: (context) => MentorDetailDialog(
        mentor: mentor,
        onEdit: () => _navigateToEditMentor(mentor),
      ),
    );
  }

  @override
  void dispose() {
    _scrollController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    const primaryColor = Color(0xFF1E40AF);

    return Scaffold(
      backgroundColor: const Color(0xFFF8FAFC),
      appBar: AppBar(
        title: const Text(
          'Mentor Management',
          style: TextStyle(
            fontSize: 18,
            fontWeight: FontWeight.bold,
            color: Color(0xFF0F172A),
          ),
        ),
        backgroundColor: Colors.white,
        elevation: 0.5,
        leading: IconButton(
          icon: const Icon(Icons.arrow_back_rounded, color: Color(0xFF0F172A)),
          onPressed: () => Navigator.of(context).pop(),
        ),
      ),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: _navigateToAddMentor,
        backgroundColor: primaryColor,
        foregroundColor: Colors.white,
        icon: const Icon(Icons.person_add_rounded, size: 20),
        label: const Text(
          'Add Mentor',
          style: TextStyle(fontWeight: FontWeight.bold),
        ),
      ),
      body: SafeArea(
        child: Column(
          children: [
            // Search & Filter Header
            MentorFilterBar(
              departments: _departments,
              selectedStatus: _filterIsActive,
              selectedDepartmentId: _filterDepartmentId,
              onSearchChanged: _onSearchChanged,
              onStatusChanged: _onStatusFilterSelected,
              onDepartmentChanged: _onDepartmentFilterSelected,
            ),

            // Mentors List
            Expanded(
              child: _isLoadingInitial
                  ? const Center(
                      child: CircularProgressIndicator(strokeWidth: 2.4),
                    )
                  : _mentors.isEmpty
                      ? EmptyStateView(
                          icon: Icons.people_outline_rounded,
                          title: 'No Mentors Found',
                          message: _searchKeyword.isNotEmpty ||
                                  _filterIsActive != null ||
                                  _filterDepartmentId != null
                              ? 'No mentors match your active filters. Try adjusting your search query.'
                              : 'No mentors have been registered yet.',
                          buttonText: _searchKeyword.isNotEmpty ||
                                  _filterIsActive != null ||
                                  _filterDepartmentId != null
                              ? 'Clear Filters'
                              : 'Refresh',
                          onButtonPressed: () {
                            if (_searchKeyword.isNotEmpty ||
                                _filterIsActive != null ||
                                _filterDepartmentId != null) {
                              setState(() {
                                _searchKeyword = '';
                                _filterIsActive = null;
                                _filterDepartmentId = null;
                              });
                            }
                            _fetchMentors(page: 0, reset: true);
                          },
                        )
                      : RefreshIndicator(
                          onRefresh: _handleRefresh,
                          child: ListView.separated(
                            controller: _scrollController,
                            physics: const AlwaysScrollableScrollPhysics(),
                            padding: const EdgeInsets.fromLTRB(16, 16, 16, 80),
                            itemCount:
                                _mentors.length + (_isLoadingMore ? 1 : 0),
                            separatorBuilder: (_, _) =>
                                const SizedBox(height: 12),
                            itemBuilder: (context, index) {
                              if (index == _mentors.length) {
                                return const Padding(
                                  padding: EdgeInsets.symmetric(vertical: 16),
                                  child: Center(
                                    child: CircularProgressIndicator(
                                      strokeWidth: 2,
                                    ),
                                  ),
                                );
                              }

                              final mentor = _mentors[index];
                              final isToggling = _togglingMentorIds.contains(
                                mentor.id,
                              );

                              return MentorCard(
                                mentor: mentor,
                                isToggling: isToggling,
                                onTap: () => _showMentorDetailDialog(mentor),
                                onToggleStatus: () =>
                                    _toggleMentorStatus(mentor),
                                onEdit: () => _navigateToEditMentor(mentor),
                              );
                            },
                          ),
                        ),
            ),
          ],
        ),
      ),
    );
  }
}
