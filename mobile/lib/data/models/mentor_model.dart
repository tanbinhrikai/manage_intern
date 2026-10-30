import 'user_model.dart';

class MentorModel {
  final String id;
  final String email;
  final String fullName;
  final String? dateOfBirth;
  final bool active;
  final DepartmentModel? department;
  final int internCount;
  final String? createdAt;

  const MentorModel({
    required this.id,
    required this.email,
    required this.fullName,
    this.dateOfBirth,
    this.active = true,
    this.department,
    this.internCount = 0,
    this.createdAt,
  });
  // get first charactor to use for display avatar
  String get initials {
    final parts = fullName.trim().split(RegExp(r'\s+'));
    if (parts.isEmpty || parts[0].isEmpty) return 'M';
    if (parts.length == 1) return parts[0][0].toUpperCase();
    return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
  }

  factory MentorModel.fromJson(Map<String, dynamic> json) {
    return MentorModel(
      id: json['id'] as String? ?? '',
      email: json['email'] as String? ?? '',
      fullName: json['fullName'] as String? ?? '',
      dateOfBirth: json['dateOfBirth'] as String?,
      active: json['active'] as bool? ?? true,
      department:
          json['department'] != null &&
              json['department'] is Map<String, dynamic>
          ? DepartmentModel.fromJson(json['department'] as Map<String, dynamic>)
          : null,
      internCount: (json['internCount'] as num?)?.toInt() ?? 0,
      createdAt: json['createdAt'] as String?,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'email': email,
      'fullName': fullName,
      if (dateOfBirth != null) 'dateOfBirth': dateOfBirth,
      'active': active,
      if (department != null) 'department': department!.toJson(),
      'internCount': internCount,
      if (createdAt != null) 'createdAt': createdAt,
    };
  }

  MentorModel copyWith({
    String? id,
    String? email,
    String? fullName,
    String? dateOfBirth,
    bool? active,
    DepartmentModel? department,
    int? internCount,
    String? createdAt,
  }) {
    return MentorModel(
      id: id ?? this.id,
      email: email ?? this.email,
      fullName: fullName ?? this.fullName,
      dateOfBirth: dateOfBirth ?? this.dateOfBirth,
      active: active ?? this.active,
      department: department ?? this.department,
      internCount: internCount ?? this.internCount,
      createdAt: createdAt ?? this.createdAt,
    );
  }
}

class CreateMentorRequest {
  final String fullName;
  final String? email;
  final String password;
  final String roleName;
  final dynamic departmentId;
  final String? dateOfBirth;

  const CreateMentorRequest({
    required this.fullName,
    this.email,
    required this.password,
    this.roleName = 'MENTOR',
    required this.departmentId,
    this.dateOfBirth,
  });

  Map<String, dynamic> toJson() {
    return {
      'fullName': fullName,
      if (email != null && email!.trim().isNotEmpty) 'email': email!.trim(),
      'password': password,
      'roleName': roleName,
      'departmentId': departmentId,
      if (dateOfBirth != null && dateOfBirth!.trim().isNotEmpty)
        'dateOfBirth': dateOfBirth,
    };
  }
}

class UpdateMentorRequest {
  final String fullName;
  final String? email;
  final String? password;
  final dynamic departmentId;
  final String? dateOfBirth;
  final bool active;

  const UpdateMentorRequest({
    required this.fullName,
    this.email,
    this.password,
    required this.departmentId,
    this.dateOfBirth,
    required this.active,
  });

  Map<String, dynamic> toJson() {
    return {
      'fullName': fullName,
      if (email != null && email!.trim().isNotEmpty) 'email': email!.trim(),
      if (password != null && password!.trim().isNotEmpty)
        'password': password!.trim(),
      'departmentId': departmentId,
      if (dateOfBirth != null && dateOfBirth!.trim().isNotEmpty)
        'dateOfBirth': dateOfBirth,
      'active': active,
    };
  }
}

class MentorInternItemModel {
  final dynamic id;
  final String fullName;
  final String email;
  final String? phone;
  final String? positionTitle;
  final String? batchName;
  final String internStatus;
  final String? startDate;
  final String? endDate;

  const MentorInternItemModel({
    required this.id,
    required this.fullName,
    required this.email,
    this.phone,
    this.positionTitle,
    this.batchName,
    required this.internStatus,
    this.startDate,
    this.endDate,
  });

  factory MentorInternItemModel.fromJson(Map<String, dynamic> json) {
    String? pos;
    if (json['position'] is Map<String, dynamic>) {
      pos = json['position']['title'] as String?;
    }

    String? batch;
    if (json['internshipBatch'] is Map<String, dynamic>) {
      batch = json['internshipBatch']['name'] as String?;
    }

    return MentorInternItemModel(
      id: json['id'],
      fullName: json['fullName'] as String? ?? 'N/A',
      email: json['email'] as String? ?? '',
      phone: json['phone'] as String?,
      positionTitle: pos,
      batchName: batch,
      internStatus: json['internStatus'] as String? ?? 'ACTIVE',
      startDate: json['startDate'] as String?,
      endDate: json['endDate'] as String?,
    );
  }
}
