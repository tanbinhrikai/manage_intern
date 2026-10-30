class AppValidators {
  AppValidators._();

  static final RegExp _emailRegex = RegExp(
    r'^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$',
  );

  static final RegExp _hasUppercase = RegExp(r'[A-Z]');
  static final RegExp _hasLowercase = RegExp(r'[a-z]');
  static final RegExp _hasDigit = RegExp(r'\d');

  static String? validateEmail(String? value) {
    final email = value?.trim() ?? '';
    if (email.isEmpty) {
      return 'Email is required';
    }
    if (!_emailRegex.hasMatch(email)) {
      return 'Please enter a valid email address';
    }
    return null;
  }

  /// Validates password with the following rules:
  /// - Length: between 6 and 72 characters inclusive (6 <= length <= 72)
  /// - Must include at least 1 uppercase letter
  /// - Must include at least 1 lowercase letter
  /// - Must include at least 1 number
  /// If [isRequired] is false and value is empty, validation passes.
  static String? validatePassword(String? value, {bool isRequired = true}) {
    final pass = value?.trim() ?? '';
    if (pass.isEmpty) {
      return isRequired ? 'Password is required' : null;
    }
    if (pass.length < 6) {
      return 'Password must be at least 6 characters';
    }
    if (pass.length > 72) {
      return 'Password cannot exceed 72 characters';
    }
    if (!_hasUppercase.hasMatch(pass)) {
      return 'Password must contain at least one uppercase letter';
    }
    if (!_hasLowercase.hasMatch(pass)) {
      return 'Password must contain at least one lowercase letter';
    }
    if (!_hasDigit.hasMatch(pass)) {
      return 'Password must contain at least one number';
    }
    return null;
  }

  /// Validates required text field.
  static String? validateRequired(
    String? value, {
    String fieldName = 'This field',
  }) {
    if (value == null || value.trim().isEmpty) {
      return '$fieldName is required';
    }
    return null;
  }
}
