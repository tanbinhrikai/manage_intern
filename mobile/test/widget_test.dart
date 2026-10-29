import 'package:flutter_test/flutter_test.dart';
import 'package:mobile/main.dart';

void main() {
  testWidgets('App renders welcome message smoke test', (
    WidgetTester tester,
  ) async {
    await tester.pumpWidget(const RikaiInternApp());

    expect(find.text('Hệ Thống Quản Lý Thực Tập Sinh'), findsOneWidget);
  });
}
