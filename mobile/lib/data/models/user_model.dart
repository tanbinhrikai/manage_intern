class RoleModel {
  final String roleName;
  final String? description;

  const RoleModel({required this.roleName, this.description});

  factory RoleModel.fromJson(Map<String, dynamic> json) {
    return RoleModel(
      roleName: json['roleName'] as String? ?? 'USER',
      description: json['description'] as String?,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'roleName': roleName,
      if (description != null) 'description': description,
    };
  }
}

class DepartmentModel {
  final dynamic id;
  final String title;

  const DepartmentModel({this.id, required this.title});

  factory DepartmentModel.fromJson(Map<String, dynamic> json) {
    return DepartmentModel(
      id: json['id'],
      title: json['title'] as String? ?? '',
    );
  }

  Map<String, dynamic> toJson() {
    return {if (id != null) 'id': id, 'title': title};
  }
}

class UserModel {
  final String id;
  final String email;
  final String fullName;
  final String? dateOfBirth;
  final bool isActive;
  final bool mustChangePassword;
  final RoleModel? role;
  final DepartmentModel? department;
  final int? internCount;

  const UserModel({
    required this.id,
    required this.email,
    required this.fullName,
    this.dateOfBirth,
    this.isActive = true,
    this.mustChangePassword = false,
    this.role,
    this.department,
    this.internCount,
  });

  String get roleDisplay => role?.roleName.toUpperCase() ?? 'MEMBER';
  String get departmentDisplay => department?.title ?? 'Not assigned';

  factory UserModel.fromJson(Map<String, dynamic> json) {
    return UserModel(
      id: json['id'] as String? ?? '',
      email: json['email'] as String? ?? '',
      fullName: json['fullName'] as String? ?? '',
      dateOfBirth: json['dateOfBirth'] as String?,
      isActive: json['isActive'] as bool? ?? true,
      mustChangePassword: json['mustChangePassword'] as bool? ?? false,
      role: json['role'] != null && json['role'] is Map<String, dynamic>
          ? RoleModel.fromJson(json['role'] as Map<String, dynamic>)
          : null,
      department:
          json['department'] != null &&
              json['department'] is Map<String, dynamic>
          ? DepartmentModel.fromJson(json['department'] as Map<String, dynamic>)
          : null,
      internCount: (json['internCount'] as num?)?.toInt(),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'email': email,
      'fullName': fullName,
      if (dateOfBirth != null) 'dateOfBirth': dateOfBirth,
      'isActive': isActive,
      'mustChangePassword': mustChangePassword,
      if (role != null) 'role': role!.toJson(),
      if (department != null) 'department': department!.toJson(),
      if (internCount != null) 'internCount': internCount,
    };
  }
}
