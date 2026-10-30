import 'package:flutter_test/flutter_test.dart';
import 'package:mobile/core/utils/app_validators.dart';

void main() {
  group('AppValidators Email Tests', () {
    test('Empty or null email always returns error', () {
      expect(AppValidators.validateEmail(''), 'Email is required');
      expect(AppValidators.validateEmail(null), 'Email is required');
      expect(AppValidators.validateEmail('   '), 'Email is required');
    });

    test('Invalid email formats return error', () {
      expect(
        AppValidators.validateEmail('invalid'),
        'Please enter a valid email address',
      );
      expect(
        AppValidators.validateEmail('invalid@'),
        'Please enter a valid email address',
      );
      expect(
        AppValidators.validateEmail('invalid@domain'),
        'Please enter a valid email address',
      );
      expect(
        AppValidators.validateEmail('@domain.com'),
        'Please enter a valid email address',
      );
    });

    test('Valid email formats pass', () {
      expect(AppValidators.validateEmail('mentor@rikai.technology'), isNull);
      expect(AppValidators.validateEmail('john.doe123@example.com'), isNull);
    });
  });

  group(
    'AppValidators Password Tests (6 <= length <= 72, upper, lower, number)',
    () {
      test('Empty password when required returns error', () {
        expect(AppValidators.validatePassword(''), 'Password is required');
        expect(AppValidators.validatePassword(null), 'Password is required');
      });

      test('Empty password when not required (e.g. edit profile) passes', () {
        expect(AppValidators.validatePassword('', isRequired: false), isNull);
        expect(AppValidators.validatePassword(null, isRequired: false), isNull);
      });

      test('Password shorter than 6 characters fails', () {
        expect(
          AppValidators.validatePassword('Ab123'),
          'Password must be at least 6 characters',
        );
      });

      test('Password longer than 72 characters fails', () {
        final longPassword = 'A1a' * 30; // 90 chars
        expect(
          AppValidators.validatePassword(longPassword),
          'Password cannot exceed 72 characters',
        );
      });

      test('Password missing uppercase fails', () {
        expect(
          AppValidators.validatePassword('password123'),
          'Password must contain at least one uppercase letter',
        );
      });

      test('Password missing lowercase fails', () {
        expect(
          AppValidators.validatePassword('PASSWORD123'),
          'Password must contain at least one lowercase letter',
        );
      });

      test('Password missing number fails', () {
        expect(
          AppValidators.validatePassword('PasswordABC'),
          'Password must contain at least one number',
        );
      });

      test('Valid 6-72 char passwords with mixed cases and number pass', () {
        expect(
          AppValidators.validatePassword('Abc123'),
          isNull,
        ); // exactly 6 chars
        expect(AppValidators.validatePassword('StrongPass123'), isNull);
      });
    },
  );

  group('AppValidators Required Tests', () {
    test('Validate required non-empty string', () {
      expect(
        AppValidators.validateRequired('John Doe', fieldName: 'Full name'),
        isNull,
      );
      expect(
        AppValidators.validateRequired('', fieldName: 'Full name'),
        'Full name is required',
      );
      expect(
        AppValidators.validateRequired('  ', fieldName: 'Full name'),
        'Full name is required',
      );
    });
  });
}
