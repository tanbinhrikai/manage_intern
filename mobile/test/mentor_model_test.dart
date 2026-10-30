import 'package:flutter_test/flutter_test.dart';
import 'package:mobile/data/models/mentor_model.dart';
import 'package:mobile/data/models/page_response.dart';
import 'package:mobile/data/models/user_model.dart';

void main() {
  group('MentorModel tests', () {
    test('MentorModel initializes and generates initials correctly', () {
      final mentor = MentorModel(
        id: '123e4567-e89b-12d3-a456-426614174000',
        email: 'john.doe@rikai.technology',
        fullName: 'John Doe',
        department: const DepartmentModel(id: 1, title: 'Engineering'),
        internCount: 5,
        active: true,
      );

      expect(mentor.initials, 'JD');
      expect(mentor.internCount, 5);
      expect(mentor.active, isTrue);
      expect(mentor.department?.title, 'Engineering');
    });

    test('MentorModel.fromJson and toJson roundtrip', () {
      final json = {
        'id': 'abc-123',
        'email': 'jane.smith@rikai.technology',
        'fullName': 'Jane Smith',
        'dateOfBirth': '1995-05-12',
        'isActive': true,
        'department': {'id': 2, 'title': 'Design'},
        'internCount': 3,
        'createdAt': '2026-01-01T00:00:00Z',
      };

      final mentor = MentorModel.fromJson(json);
      expect(mentor.id, 'abc-123');
      expect(mentor.fullName, 'Jane Smith');
      expect(mentor.email, 'jane.smith@rikai.technology');
      expect(mentor.department?.title, 'Design');
      expect(mentor.internCount, 3);
      expect(mentor.initials, 'JS');

      final serialized = mentor.toJson();
      expect(serialized['id'], 'abc-123');
      expect(serialized['internCount'], 3);
    });

    test('CreateMentorRequest builds payload properly', () {
      const request = CreateMentorRequest(
        fullName: 'Nguyen Van A',
        email: 'nguyen.vana@rikai.technology',
        password: 'Password123',
        departmentId: 1,
        dateOfBirth: '1992-04-15',
      );

      final json = request.toJson();
      expect(json['fullName'], 'Nguyen Van A');
      expect(json['email'], 'nguyen.vana@rikai.technology');
      expect(json['password'], 'Password123');
      expect(json['roleName'], 'MENTOR');
      expect(json['departmentId'], 1);
      expect(json['dateOfBirth'], '1992-04-15');
    });

    test('PageResponse parses items correctly', () {
      final json = {
        'items': [
          {
            'id': 'm1',
            'email': 'm1@example.com',
            'fullName': 'Mentor One',
            'internCount': 2,
          },
          {
            'id': 'm2',
            'email': 'm2@example.com',
            'fullName': 'Mentor Two',
            'internCount': 0,
          },
        ],
        'currentPage': 0,
        'totalPages': 2,
        'totalItems': 15,
        'pageSize': 10,
      };

      final page = PageResponse<MentorModel>.fromJson(
        json,
        (item) => MentorModel.fromJson(item as Map<String, dynamic>),
      );

      expect(page.items.length, 2);
      expect(page.items[0].fullName, 'Mentor One');
      expect(page.items[1].fullName, 'Mentor Two');
      expect(page.currentPage, 0);
      expect(page.totalPages, 2);
      expect(page.totalItems, 15);
      expect(page.hasNext, isTrue);
    });
  });
}
