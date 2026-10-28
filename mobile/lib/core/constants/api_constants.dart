import 'dart:io';

import 'package:flutter/foundation.dart';

class ApiConstants {
  ApiConstants._();

  // Automatically resolve host for Android Emulator, iOS Simulator, Web and Desktop
  static String get baseUrl {
    if (kIsWeb) {
      return 'http://localhost:8080';
    }
    if (!kIsWeb && Platform.isAndroid) {
      // 10.0.2.2 is host localhost for Android Emulator
      return 'http://10.0.2.2:8080';
    }
    // iOS simulator or desktop
    return 'http://localhost:8080';
  }

  // Endpoints
  static const String authLogin = '/auth/login';
  static const String authLogout = '/auth/logout';
  static const String refreshToken = '/auth/refresh';
  static const String getMyInfo = '/users/get-my-info';
}
