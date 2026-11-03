import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mobile/presentation/widgets/common/app_search_bar.dart';
import 'package:mobile/presentation/widgets/common/status_badge.dart';

void main() {
  group('Common Widgets Tests', () {
    testWidgets('StatusBadge displays Active status correctly', (
      WidgetTester tester,
    ) async {
      await tester.pumpWidget(
        const MaterialApp(
          home: Scaffold(
            body: StatusBadge(isActive: true),
          ),
        ),
      );

      expect(find.text('Active'), findsOneWidget);
    });

    testWidgets('StatusBadge displays Inactive status correctly', (
      WidgetTester tester,
    ) async {
      await tester.pumpWidget(
        const MaterialApp(
          home: Scaffold(
            body: StatusBadge(isActive: false),
          ),
        ),
      );

      expect(find.text('Inactive'), findsOneWidget);
    });

    testWidgets('StatusBadge displays custom status correctly', (
      WidgetTester tester,
    ) async {
      await tester.pumpWidget(
        const MaterialApp(
          home: Scaffold(
            body: StatusBadge(status: 'WARNING'),
          ),
        ),
      );

      expect(find.text('Warning'), findsOneWidget);
    });

    testWidgets('AppSearchBar debounces search callback', (
      WidgetTester tester,
    ) async {
      String searched = '';

      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: AppSearchBar(
              hintText: 'Search mentor...',
              debounceDuration: const Duration(milliseconds: 200),
              onChanged: (val) => searched = val,
            ),
          ),
        ),
      );

      expect(find.text('Search mentor...'), findsOneWidget);

      await tester.enterText(find.byType(TextField), 'John');
      expect(searched, ''); // Not yet called before debounce

      await tester.pump(const Duration(milliseconds: 250));
      expect(searched, 'John'); // Called after debounce
    });
  });
}
