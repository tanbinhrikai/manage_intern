import 'package:flutter/material.dart';

import 'data/models/user_model.dart';
import 'data/services/auth_service.dart';
import 'presentation/screens/auth/login_screen.dart';
import 'presentation/screens/dashboard/welcome_dashboard_screen.dart';
import 'storage/user_storage.dart';

void main() {
  WidgetsFlutterBinding.ensureInitialized();
  runApp(const RikaiInternApp());
}

class RikaiInternApp extends StatelessWidget {
  const RikaiInternApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Rikai Intern Management',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        useMaterial3: true,
        colorScheme: ColorScheme.fromSeed(
          seedColor: const Color(0xFF1E40AF),
          primary: const Color(0xFF1E40AF),
          brightness: Brightness.light,
        ),
        fontFamily: null,
      ),
      home: const AuthGate(),
    );
  }
}

class AuthGate extends StatefulWidget {
  const AuthGate({super.key});

  @override
  State<AuthGate> createState() => _AuthGateState();
}

class _AuthGateState extends State<AuthGate> {
  bool _isChecking = true;
  bool _isAuthenticated = false;
  UserModel? _cachedUser;

  @override
  void initState() {
    super.initState();
    _checkAuthStatus();
  }

  Future<void> _checkAuthStatus() async {
    try {
      final hasToken = await AuthService.instance.isLoggedIn();
      if (hasToken) {
        final user = await UserStorage.instance.getUser();
        if (mounted) {
          setState(() {
            _isAuthenticated = true;
            _cachedUser = user;
            _isChecking = false;
          });
        }
        return;
      }
    } catch (_) {
      // In case of storage read issue, default to login
    }

    if (mounted) {
      setState(() {
        _isAuthenticated = false;
        _isChecking = false;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    if (_isChecking) {
      return Scaffold(
        backgroundColor: Colors.white,
        body: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Container(
                width: 72,
                height: 72,
                decoration: const BoxDecoration(
                  color: Color(0xFFEFF6FF),
                  shape: BoxShape.circle,
                ),
                child: const Icon(
                  Icons.school_rounded,
                  size: 38,
                  color: Color(0xFF1E40AF),
                ),
              ),
              const SizedBox(height: 24),
              const SizedBox(
                width: 24,
                height: 24,
                child: CircularProgressIndicator(
                  strokeWidth: 2.4,
                  valueColor: AlwaysStoppedAnimation<Color>(Color(0xFF1E40AF)),
                ),
              ),
            ],
          ),
        ),
      );
    }

    if (_isAuthenticated) {
      return WelcomeDashboardScreen(user: _cachedUser);
    }

    return const LoginScreen();
  }
}
