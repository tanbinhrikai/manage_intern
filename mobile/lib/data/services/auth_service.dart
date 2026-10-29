import 'package:dio/dio.dart';

import '../../core/constants/api_constants.dart';
import '../../core/network/api_client.dart';
import '../../storage/token_storage.dart';
import '../../storage/user_storage.dart';
import '../models/api_response.dart';
import '../models/auth_response_model.dart';
import '../models/user_model.dart';

class AuthService {
  AuthService._();

  static final AuthService instance = AuthService._();

  final ApiClient _apiClient = ApiClient.instance;

  /// Attempt to log in with [email] and [password]
  Future<UserModel?> login({
    required String email,
    required String password,
  }) async {
    try {
      final response = await _apiClient.dio.post(
        ApiConstants.authLogin,
        data: {'email': email.trim(), 'password': password},
      );

      final apiResponse = ApiResponse<AuthResponseModel>.fromJson(
        response.data as Map<String, dynamic>,
        (json) => AuthResponseModel.fromJson(json as Map<String, dynamic>),
      );

      if (apiResponse.success && apiResponse.data != null) {
        final authData = apiResponse.data!;

        // Save tokens
        await TokenStorage.instance.saveTokens(
          accessToken: authData.accessToken,
          refreshToken: authData.refreshToken,
        );

        // Save user if present
        if (authData.user != null) {
          await UserStorage.instance.saveUser(authData.user!);
        }

        return authData.user;
      } else {
        throw Exception(
          apiResponse.message ?? 'Login failed. Please try again.',
        );
      }
    } catch (e) {
      throw Exception(ApiClient.getErrorMessage(e));
    }
  }

  /// Logout current session and clear stored tokens & profile
  Future<void> logout() async {
    try {
      final refreshToken = await TokenStorage.instance.getRefreshToken();

      await _apiClient.dio.post(
        ApiConstants.authLogout,
        options: Options(
          extra: {'withCredentials': true},
          headers: {
            if (refreshToken != null) 'Cookie': 'REFRESH_TOKEN=$refreshToken',
          },
        ),
      );
    } catch (_) {
      // Ignore network failures on logout so local cleanup always completes
    } finally {
      await TokenStorage.instance.clearAll();
      await UserStorage.instance.clearUser();
    }
  }

  /// Fetch currently authenticated user profile from backend
  Future<UserModel?> getMyInfo() async {
    try {
      final response = await _apiClient.dio.get(ApiConstants.getMyInfo);
      final apiResponse = ApiResponse<UserModel>.fromJson(
        response.data as Map<String, dynamic>,
        (json) => UserModel.fromJson(json as Map<String, dynamic>),
      );

      if (apiResponse.success && apiResponse.data != null) {
        await UserStorage.instance.saveUser(apiResponse.data!);
        return apiResponse.data;
      }
      return null;
    } on DioException catch (e) {
      if (e.response?.statusCode == 401) {
        // Token has expired and could not be refreshed
        await TokenStorage.instance.clearAll();
        await UserStorage.instance.clearUser();
        return null;
      }
      // Fallback to cached profile if network or other transient error occurs
      return await UserStorage.instance.getUser();
    } catch (_) {
      // Fallback to cached profile if available
      return await UserStorage.instance.getUser();
    }
  }

  /// Check whether an access token exists locally
  Future<bool> isLoggedIn() async {
    return await TokenStorage.instance.hasAccessToken();
  }
}
