class PageResponse<T> {
  final List<T> items;
  final int currentPage;
  final int totalPages;
  final int totalItems;
  final int pageSize;

  const PageResponse({
    required this.items,
    required this.currentPage,
    required this.totalPages,
    required this.totalItems,
    required this.pageSize,
  });

  bool get hasNext => currentPage < totalPages - 1;

  factory PageResponse.fromJson(
    Map<String, dynamic> json,
    T Function(dynamic json) fromJsonT,
  ) {
    final rawList = json['items'] as List<dynamic>? ?? [];
    final items = rawList.map((item) => fromJsonT(item)).toList();

    return PageResponse<T>(
      items: items,
      currentPage: (json['currentPage'] as num?)?.toInt() ?? 0,
      totalPages: (json['totalPages'] as num?)?.toInt() ?? 0,
      totalItems: (json['totalItems'] as num?)?.toInt() ?? 0,
      pageSize: (json['pageSize'] as num?)?.toInt() ?? 10,
    );
  }

  factory PageResponse.empty() {
    return PageResponse<T>(
      items: const [],
      currentPage: 0,
      totalPages: 0,
      totalItems: 0,
      pageSize: 10,
    );
  }
}

