import 'package:flutter/material.dart';

class AppFormField extends StatelessWidget {
  final String label;
  final String? hintText;
  final TextEditingController? controller;
  final String? initialValue;
  final bool obscureText;
  final TextInputType keyboardType;
  final TextInputAction textInputAction;
  final FormFieldValidator<String>? validator;
  final Widget? prefixIcon;
  final Widget? suffixIcon;
  final bool readOnly;
  final VoidCallback? onTap;
  final int maxLines;
  final bool isRequired;
  final String? helperText;

  const AppFormField({
    super.key,
    required this.label,
    this.hintText,
    this.controller,
    this.initialValue,
    this.obscureText = false,
    this.keyboardType = TextInputType.text,
    this.textInputAction = TextInputAction.next,
    this.validator,
    this.prefixIcon,
    this.suffixIcon,
    this.readOnly = false,
    this.onTap,
    this.maxLines = 1,
    this.isRequired = false,
    this.helperText,
  });

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          children: [
            Text(
              label,
              style: const TextStyle(
                fontSize: 13,
                fontWeight: FontWeight.w600,
                color: Color(0xFF334155),
              ),
            ),
            if (isRequired)
              const Text(
                ' *',
                style: TextStyle(
                  color: Color(0xFFDC2626),
                  fontWeight: FontWeight.bold,
                  fontSize: 14,
                ),
              ),
          ],
        ),
        const SizedBox(height: 6),
        TextFormField(
          controller: controller,
          initialValue: initialValue,
          obscureText: obscureText,
          keyboardType: keyboardType,
          textInputAction: textInputAction,
          validator: validator,
          readOnly: readOnly,
          onTap: onTap,
          maxLines: maxLines,
          style: const TextStyle(fontSize: 14, color: Color(0xFF0F172A)),
          decoration: InputDecoration(
            hintText: hintText,
            hintStyle: const TextStyle(fontSize: 14, color: Color(0xFF94A3B8)),
            prefixIcon: prefixIcon,
            suffixIcon: suffixIcon,
            filled: true,
            fillColor: readOnly ? const Color(0xFFF8FAFC) : Colors.white,
            contentPadding: const EdgeInsets.symmetric(
              horizontal: 16,
              vertical: 14,
            ),
            border: OutlineInputBorder(
              borderRadius: BorderRadius.circular(14),
              borderSide: const BorderSide(color: Color(0xFFCBD5E1)),
            ),
            enabledBorder: OutlineInputBorder(
              borderRadius: BorderRadius.circular(14),
              borderSide: const BorderSide(color: Color(0xFFE2E8F0)),
            ),
            focusedBorder: OutlineInputBorder(
              borderRadius: BorderRadius.circular(14),
              borderSide: const BorderSide(
                color: Color(0xFF1E40AF),
                width: 1.6,
              ),
            ),
            errorBorder: OutlineInputBorder(
              borderRadius: BorderRadius.circular(14),
              borderSide: const BorderSide(color: Color(0xFFEF4444)),
            ),
            focusedErrorBorder: OutlineInputBorder(
              borderRadius: BorderRadius.circular(14),
              borderSide: const BorderSide(
                color: Color(0xFFEF4444),
                width: 1.6,
              ),
            ),
          ),
        ),
        if (helperText != null) ...[
          const SizedBox(height: 4),
          Text(
            helperText!,
            style: const TextStyle(
              fontSize: 11,
              color: Color(0xFF64748B),
            ),
          ),
        ],
      ],
    );
  }
}

