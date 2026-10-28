class ApiResponse<T> {
  final bool success;
  final String? code;
  final String? message;
  final T? data;
  final String? timestamp;
  final int statusCode;

  const ApiResponse({
    required this.success,
    this.code,
    this.message,
    this.data,
    this.timestamp,
    required this.statusCode,
  });

  factory ApiResponse.fromJson(
    Map<String, dynamic> json, [
    T Function(dynamic json)? fromJsonT,
  ]) {
    return ApiResponse<T>(
      success: json['success'] as bool? ?? false,
      code: json['code'] as String?,
      message: json['message'] as String?,
      data: json['data'] != null && fromJsonT != null
          ? fromJsonT(json['data'])
          : json['data'] as T?,
      timestamp: json['timestamp'] as String?,
      statusCode: (json['statusCode'] as num?)?.toInt() ?? 200,
    );
  }

  Map<String, dynamic> toJson([Object? Function(T value)? toJsonT]) {
    return {
      'success': success,
      if (code != null) 'code': code,
      if (message != null) 'message': message,
      if (data != null) 'data': toJsonT != null ? toJsonT(data as T) : data,
      if (timestamp != null) 'timestamp': timestamp,
      'statusCode': statusCode,
    };
  }
}
