import '../../core/constants/api_constants.dart';
import '../../core/network/api_client.dart';
import '../models/api_response.dart';
import '../models/mentor_model.dart';
import '../models/page_response.dart';
import '../models/user_model.dart';

class MentorService {
  MentorService._();

  static final MentorService instance = MentorService._();

  final ApiClient _apiClient = ApiClient.instance;

  /// Fetch paginated list of mentors with optional filtering
  Future<PageResponse<MentorModel>> getMentors({
    int page = 0,
    int limit = 10,
    String? keyword,
    dynamic departmentId,
    bool? isActive,
    String? startDate,
    String? endDate,
  }) async {
    try {
      final queryParams = <String, dynamic>{
        'page': page,
        'limit': limit,
      };

      if (keyword != null && keyword.trim().isNotEmpty) {
        queryParams['keyword'] = keyword.trim();
      }
      if (departmentId != null) {
        queryParams['department_id'] = departmentId;
      }
      if (isActive != null) {
        queryParams['is_active'] = isActive;
      }
      if (startDate != null && startDate.isNotEmpty) {
        queryParams['start_date'] = startDate;
      }
      if (endDate != null && endDate.isNotEmpty) {
        queryParams['end_date'] = endDate;
      }

      final response = await _apiClient.dio.get(
        ApiConstants.mentors,
        queryParameters: queryParams,
      );

      final apiResponse = ApiResponse<PageResponse<MentorModel>>.fromJson(
        response.data as Map<String, dynamic>,
        (json) => PageResponse.fromJson(
          json as Map<String, dynamic>,
          (item) => MentorModel.fromJson(item as Map<String, dynamic>),
        ),
      );

      if (apiResponse.success && apiResponse.data != null) {
        return apiResponse.data!;
      }

      return PageResponse.empty();
    } catch (e) {
      throw Exception(ApiClient.getErrorMessage(e));
    }
  }

  /// Create a new mentor account
  Future<MentorModel> createMentor(CreateMentorRequest request) async {
    try {
      final response = await _apiClient.dio.post(
        ApiConstants.users,
        data: request.toJson(),
      );

      final apiResponse = ApiResponse<MentorModel>.fromJson(
        response.data as Map<String, dynamic>,
        (json) => MentorModel.fromJson(json as Map<String, dynamic>),
      );

      if (apiResponse.success && apiResponse.data != null) {
        return apiResponse.data!;
      }

      throw Exception(apiResponse.message ?? 'Failed to create mentor.');
    } catch (e) {
      throw Exception(ApiClient.getErrorMessage(e));
    }
  }

  /// Update an existing mentor account
  Future<MentorModel> updateMentor(
    String id,
    UpdateMentorRequest request,
  ) async {
    try {
      final response = await _apiClient.dio.put(
        '${ApiConstants.users}/$id',
        data: request.toJson(),
      );

      final apiResponse = ApiResponse<MentorModel>.fromJson(
        response.data as Map<String, dynamic>,
        (json) => MentorModel.fromJson(json as Map<String, dynamic>),
      );

      if (apiResponse.success && apiResponse.data != null) {
        return apiResponse.data!;
      }

      throw Exception(apiResponse.message ?? 'Failed to update mentor.');
    } catch (e) {
      throw Exception(ApiClient.getErrorMessage(e));
    }
  }

  /// Toggle active / inactive status of a mentor
  Future<MentorModel> toggleStatus(String id) async {
    try {
      final response = await _apiClient.dio.patch(
        '${ApiConstants.userStatus}/$id',
      );

      final apiResponse = ApiResponse<MentorModel>.fromJson(
        response.data as Map<String, dynamic>,
        (json) => MentorModel.fromJson(json as Map<String, dynamic>),
      );

      if (apiResponse.success && apiResponse.data != null) {
        return apiResponse.data!;
      }

      throw Exception(
        apiResponse.message ?? 'Failed to update mentor status.',
      );
    } catch (e) {
      throw Exception(ApiClient.getErrorMessage(e));
    }
  }

  /// Fetch all departments for selection dropdowns
  Future<List<DepartmentModel>> getDepartments() async {
    try {
      final response = await _apiClient.dio.get(
        ApiConstants.departments,
        queryParameters: {'limit': 100, 'page': 0},
      );

      final data = response.data;
      if (data is Map<String, dynamic> && data['data'] != null) {
        final innerData = data['data'];
        List<dynamic> itemsList = [];
        if (innerData is Map<String, dynamic> && innerData['items'] is List) {
          itemsList = innerData['items'] as List<dynamic>;
        } else if (innerData is List) {
          itemsList = innerData;
        }

        return itemsList
            .map((item) => DepartmentModel.fromJson(item as Map<String, dynamic>))
            .toList();
      }

      return [];
    } catch (e) {
      throw Exception(ApiClient.getErrorMessage(e));
    }
  }

  /// Fetch list of interns assigned to a mentor
  Future<List<MentorInternItemModel>> getInternsByMentor(
    String mentorId, {
    int page = 0,
    int limit = 50,
  }) async {
    try {
      final response = await _apiClient.dio.get(
        ApiConstants.interns,
        queryParameters: {
          'mentor_id': mentorId,
          'page': page,
          'limit': limit,
        },
      );

      final data = response.data;
      if (data is Map<String, dynamic> && data['data'] != null) {
        final innerData = data['data'];
        List<dynamic> itemsList = [];
        if (innerData is Map<String, dynamic> && innerData['items'] is List) {
          itemsList = innerData['items'] as List<dynamic>;
        } else if (innerData is List) {
          itemsList = innerData;
        }

        return itemsList
            .map((item) =>
                MentorInternItemModel.fromJson(item as Map<String, dynamic>))
            .toList();
      }

      return [];
    } catch (e) {
      throw Exception(ApiClient.getErrorMessage(e));
    }
  }
}

