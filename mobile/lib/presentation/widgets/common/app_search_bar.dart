import 'dart:async';
import 'package:flutter/material.dart';

class AppSearchBar extends StatefulWidget {
  final String hintText;
  final ValueChanged<String> onChanged;
  final VoidCallback? onClear;
  final Duration debounceDuration;
  final TextEditingController? controller;

  const AppSearchBar({
    super.key,
    this.hintText = 'Search...',
    required this.onChanged,
    this.onClear,
    this.debounceDuration = const Duration(milliseconds: 400),
    this.controller,
  });

  @override
  State<AppSearchBar> createState() => _AppSearchBarState();
}

class _AppSearchBarState extends State<AppSearchBar> {
  late final TextEditingController _controller;
  Timer? _debounceTimer;
  bool _hasText = false;

  @override
  void initState() {
    super.initState();
    _controller = widget.controller ?? TextEditingController();
    _hasText = _controller.text.isNotEmpty;
    _controller.addListener(_onTextChanged);
  }

  void _onTextChanged() {
    final hasText = _controller.text.isNotEmpty;
    if (hasText != _hasText) {
      setState(() => _hasText = hasText);
    }
  }

  void _handleChanged(String value) {
    _debounceTimer?.cancel();
    _debounceTimer = Timer(widget.debounceDuration, () {
      widget.onChanged(value);
    });
  }

  void _handleClear() {
    _controller.clear();
    _debounceTimer?.cancel();
    widget.onChanged('');
    widget.onClear?.call();
  }

  @override
  void dispose() {
    _debounceTimer?.cancel();
    if (widget.controller == null) {
      _controller.dispose();
    } else {
      _controller.removeListener(_onTextChanged);
    }
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Container(
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(14),
        border: Border.all(color: const Color(0xFFE2E8F0)),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withValues(alpha: 0.02),
            blurRadius: 8,
            offset: const Offset(0, 2),
          ),
        ],
      ),
      child: TextField(
        controller: _controller,
        onChanged: _handleChanged,
        style: const TextStyle(fontSize: 14, color: Color(0xFF0F172A)),
        decoration: InputDecoration(
          hintText: widget.hintText,
          hintStyle: const TextStyle(fontSize: 14, color: Color(0xFF94A3B8)),
          prefixIcon: const Icon(
            Icons.search_rounded,
            color: Color(0xFF64748B),
            size: 20,
          ),
          suffixIcon: _hasText
              ? IconButton(
                  icon: const Icon(
                    Icons.close_rounded,
                    color: Color(0xFF94A3B8),
                    size: 18,
                  ),
                  onPressed: _handleClear,
                )
              : null,
          border: InputBorder.none,
          contentPadding: const EdgeInsets.symmetric(
            horizontal: 16,
            vertical: 14,
          ),
        ),
      ),
    );
  }
}

