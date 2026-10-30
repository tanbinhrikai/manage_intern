import 'package:flutter/material.dart';

import '../../../core/utils/app_validators.dart';
import '../../../data/models/mentor_model.dart';
import '../../../data/models/user_model.dart';
import '../../../data/services/mentor_service.dart';
import '../../widgets/common/app_form_field.dart';

class MentorFormScreen extends StatefulWidget {
  final MentorModel? mentor;

  const MentorFormScreen({super.key, this.mentor});

  bool get isEdit => mentor != null;

  @override
  State<MentorFormScreen> createState() => _MentorFormScreenState();
}

class _MentorFormScreenState extends State<MentorFormScreen> {
  final _formKey = GlobalKey<FormState>();

  late final TextEditingController _fullNameController;
  late final TextEditingController _emailController;
  late final TextEditingController _passwordController;
  late final TextEditingController _dobController;

  bool _obscurePassword = true;
  bool active = true;
  dynamic _selectedDepartmentId;
  DateTime? _selectedDob;

  List<DepartmentModel> _departments = [];
  bool _isLoadingDepartments = true;
  bool _isSubmitting = false;

  @override
  void initState() {
    super.initState();
    final mentor = widget.mentor;
    _fullNameController = TextEditingController(text: mentor?.fullName ?? '');
    _emailController = TextEditingController(text: mentor?.email ?? '');
    _passwordController = TextEditingController();
    _dobController = TextEditingController(text: mentor?.dateOfBirth ?? '');

    active = mentor?.active ?? true;
    _selectedDepartmentId = mentor?.department?.id;

    if (mentor?.dateOfBirth != null && mentor!.dateOfBirth!.isNotEmpty) {
      try {
        _selectedDob = DateTime.parse(mentor.dateOfBirth!);
      } catch (_) {}
    }

    _loadDepartments();
  }

  Future<void> _loadDepartments() async {
    try {
      final list = await MentorService.instance.getDepartments();
      if (!mounted) return;
      setState(() {
        _departments = list;
        _isLoadingDepartments = false;

        // Auto-select first department if creating and list is not empty
        if (_selectedDepartmentId == null && list.isNotEmpty) {
          _selectedDepartmentId = list.first.id;
        } else if (_selectedDepartmentId != null) {
          final matched = list.any(
            (d) => d.id.toString() == _selectedDepartmentId.toString(),
          );
          if (!matched && list.isNotEmpty) {
            _selectedDepartmentId = list.first.id;
          }
        }
      });
    } catch (_) {
      if (!mounted) return;
      setState(() => _isLoadingDepartments = false);
    }
  }

  Future<void> _pickDateOfBirth() async {
    final now = DateTime.now();
    final initialDate = _selectedDob ?? DateTime(now.year - 25, 1, 1);
    final picked = await showDatePicker(
      context: context,
      initialDate: initialDate,
      firstDate: DateTime(1950),
      lastDate: DateTime(now.year - 16, 12, 31),
      helpText: 'Select Date of Birth',
    );

    if (picked != null) {
      setState(() {
        _selectedDob = picked;
        _dobController.text =
            '${picked.year}-${picked.month.toString().padLeft(2, '0')}-${picked.day.toString().padLeft(2, '0')}';
      });
    }
  }

  Future<void> _handleSubmit() async {
    if (!_formKey.currentState!.validate()) return;
    if (_selectedDepartmentId == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Please select a department'),
          backgroundColor: Color(0xFFDC2626),
        ),
      );
      return;
    }

    setState(() => _isSubmitting = true);

    try {
      if (widget.isEdit) {
        final request = UpdateMentorRequest(
          fullName: _fullNameController.text.trim(),
          email: _emailController.text.trim().isEmpty
              ? null
              : _emailController.text.trim(),
          password: _passwordController.text.trim().isEmpty
              ? null
              : _passwordController.text.trim(),
          departmentId: _selectedDepartmentId,
          dateOfBirth: _dobController.text.trim().isEmpty
              ? null
              : _dobController.text.trim(),
          active: active,
        );

        await MentorService.instance.updateMentor(widget.mentor!.id, request);
        if (!mounted) return;

        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('Mentor profile updated successfully'),
            backgroundColor: Color(0xFF16A34A),
          ),
        );
      } else {
        final request = CreateMentorRequest(
          fullName: _fullNameController.text.trim(),
          email: _emailController.text.trim().isEmpty
              ? null
              : _emailController.text.trim(),
          password: _passwordController.text.trim(),
          departmentId: _selectedDepartmentId,
          dateOfBirth: _dobController.text.trim().isEmpty
              ? null
              : _dobController.text.trim(),
        );

        await MentorService.instance.createMentor(request);
        if (!mounted) return;

        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('New mentor created successfully'),
            backgroundColor: Color(0xFF16A34A),
          ),
        );
      }

      Navigator.of(context).pop(true);
    } catch (e) {
      if (!mounted) return;
      setState(() => _isSubmitting = false);
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(e.toString().replaceAll('Exception: ', '')),
          backgroundColor: const Color(0xFFDC2626),
        ),
      );
    }
  }

  @override
  void dispose() {
    _fullNameController.dispose();
    _emailController.dispose();
    _passwordController.dispose();
    _dobController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final title = widget.isEdit ? 'Edit Mentor Profile' : 'Add New Mentor';

    return Scaffold(
      backgroundColor: const Color(0xFFF8FAFC),
      appBar: AppBar(
        title: Text(
          title,
          style: const TextStyle(
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
      body: SafeArea(
        child: SingleChildScrollView(
          padding: const EdgeInsets.all(20),
          child: Form(
            key: _formKey,
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                // Full Name Field
                AppFormField(
                  controller: _fullNameController,
                  label: 'Full Name',
                  hintText: 'e.g. John Doe',
                  isRequired: true,
                  prefixIcon: const Icon(
                    Icons.person_outline_rounded,
                    color: Color(0xFF64748B),
                    size: 20,
                  ),
                  validator: (val) => AppValidators.validateRequired(
                    val,
                    fieldName: 'Full name',
                  ),
                ),
                const SizedBox(height: 18),

                // Email Field
                AppFormField(
                  controller: _emailController,
                  label: 'Email Address',
                  hintText: 'e.g. john.doe@rikai.technology',
                  isRequired: true,
                  keyboardType: TextInputType.emailAddress,
                  prefixIcon: const Icon(
                    Icons.mail_outline_rounded,
                    color: Color(0xFF64748B),
                    size: 20,
                  ),
                  validator: AppValidators.validateEmail,
                ),
                const SizedBox(height: 18),

                // Password Field
                AppFormField(
                  controller: _passwordController,
                  label: widget.isEdit ? 'Reset Password' : 'Password',
                  hintText: widget.isEdit
                      ? 'Leave blank to keep current password'
                      : 'Enter strong password',
                  isRequired: !widget.isEdit,
                  obscureText: _obscurePassword,
                  helperText: '6-72 characters with uppercase, lowercase, and a number.',
                  prefixIcon: const Icon(
                    Icons.lock_outline_rounded,
                    color: Color(0xFF64748B),
                    size: 20,
                  ),
                  suffixIcon: IconButton(
                    icon: Icon(
                      _obscurePassword
                          ? Icons.visibility_outlined
                          : Icons.visibility_off_outlined,
                      color: const Color(0xFF64748B),
                      size: 20,
                    ),
                    onPressed: () {
                      setState(() => _obscurePassword = !_obscurePassword);
                    },
                  ),
                  validator: (val) => AppValidators.validatePassword(
                    val,
                    isRequired: !widget.isEdit,
                  ),
                ),
                const SizedBox(height: 18),

                // Department Dropdown
                Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Row(
                      children: [
                        Text(
                          'Department',
                          style: TextStyle(
                            fontSize: 13,
                            fontWeight: FontWeight.w600,
                            color: Color(0xFF334155),
                          ),
                        ),
                        Text(
                          ' *',
                          style: TextStyle(
                            color: Color(0xFFDC2626),
                            fontWeight: FontWeight.bold,
                            fontSize: 14,
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 6),
                    Container(
                      padding: const EdgeInsets.symmetric(horizontal: 16),
                      decoration: BoxDecoration(
                        color: Colors.white,
                        borderRadius: BorderRadius.circular(14),
                        border: Border.all(color: const Color(0xFFE2E8F0)),
                      ),
                      child: _isLoadingDepartments
                          ? const Padding(
                              padding: EdgeInsets.symmetric(vertical: 14),
                              child: Row(
                                children: [
                                  SizedBox(
                                    width: 16,
                                    height: 16,
                                    child: CircularProgressIndicator(
                                      strokeWidth: 2,
                                    ),
                                  ),
                                  SizedBox(width: 12),
                                  Text(
                                    'Loading departments...',
                                    style: TextStyle(
                                      fontSize: 14,
                                      color: Color(0xFF64748B),
                                    ),
                                  ),
                                ],
                              ),
                            )
                          : DropdownButtonHideUnderline(
                              child: DropdownButton<dynamic>(
                                isExpanded: true,
                                value: _selectedDepartmentId,
                                hint: const Text(
                                  'Select Department',
                                  style: TextStyle(
                                    fontSize: 14,
                                    color: Color(0xFF94A3B8),
                                  ),
                                ),
                                icon: const Icon(
                                  Icons.keyboard_arrow_down_rounded,
                                  color: Color(0xFF64748B),
                                ),
                                items: _departments.map((dept) {
                                  return DropdownMenuItem<dynamic>(
                                    value: dept.id,
                                    child: Text(
                                      dept.title,
                                      style: const TextStyle(
                                        fontSize: 14,
                                        color: Color(0xFF0F172A),
                                      ),
                                    ),
                                  );
                                }).toList(),
                                onChanged: (newVal) {
                                  setState(
                                    () => _selectedDepartmentId = newVal,
                                  );
                                },
                              ),
                            ),
                    ),
                  ],
                ),
                const SizedBox(height: 18),

                // Date of Birth Field
                AppFormField(
                  controller: _dobController,
                  label: 'Date of Birth',
                  hintText: 'YYYY-MM-DD',
                  readOnly: true,
                  onTap: _pickDateOfBirth,
                  prefixIcon: const Icon(
                    Icons.calendar_today_outlined,
                    color: Color(0xFF64748B),
                    size: 20,
                  ),
                  suffixIcon: IconButton(
                    icon: const Icon(
                      Icons.date_range_rounded,
                      color: Color(0xFF1E40AF),
                      size: 20,
                    ),
                    onPressed: _pickDateOfBirth,
                  ),
                ),
                const SizedBox(height: 18),

                // Active Switch (Edit mode only)
                if (widget.isEdit) ...[
                  Container(
                    padding: const EdgeInsets.symmetric(
                      horizontal: 16,
                      vertical: 10,
                    ),
                    decoration: BoxDecoration(
                      color: Colors.white,
                      borderRadius: BorderRadius.circular(14),
                      border: Border.all(color: const Color(0xFFE2E8F0)),
                    ),
                    child: Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        const Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(
                              'Account Active',
                              style: TextStyle(
                                fontSize: 14,
                                fontWeight: FontWeight.w600,
                                color: Color(0xFF0F172A),
                              ),
                            ),
                            SizedBox(height: 2),
                            Text(
                              'Allow mentor to access the system',
                              style: TextStyle(
                                fontSize: 12,
                                color: Color(0xFF64748B),
                              ),
                            ),
                          ],
                        ),
                        Switch(
                          value: active,
                          activeThumbColor: const Color(0xFF16A34A),
                          activeTrackColor: const Color(0xFFDCFCE7),
                          onChanged: (val) {
                            setState(() => active = val);
                          },
                        ),
                      ],
                    ),
                  ),
                  const SizedBox(height: 24),
                ] else
                  const SizedBox(height: 12),

                // Submit Button
                ElevatedButton(
                  onPressed: _isSubmitting ? null : _handleSubmit,
                  style: ElevatedButton.styleFrom(
                    backgroundColor: const Color(0xFF1E40AF),
                    foregroundColor: Colors.white,
                    padding: const EdgeInsets.symmetric(vertical: 16),
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(14),
                    ),
                    elevation: 1,
                  ),
                  child: _isSubmitting
                      ? const SizedBox(
                          width: 22,
                          height: 22,
                          child: CircularProgressIndicator(
                            strokeWidth: 2.2,
                            valueColor: AlwaysStoppedAnimation<Color>(
                              Colors.white,
                            ),
                          ),
                        )
                      : Text(
                          widget.isEdit ? 'Save Changes' : 'Create Mentor',
                          style: const TextStyle(
                            fontSize: 15,
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
