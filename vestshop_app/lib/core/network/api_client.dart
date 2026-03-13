import 'dart:convert';
import 'package:flutter/foundation.dart';
import 'package:http/http.dart' as http;

class ApiClient {
  final String baseUrl;
  String? token;

  ApiClient({required this.baseUrl});

  Map<String, String> get _headers => {
        'Accept': 'application/json',
        'Content-Type': 'application/json',
        if (token != null) 'Authorization': 'Bearer $token',
      };

  Future<dynamic> get(String path) async {
    final url = Uri.parse('$baseUrl$path');
    debugPrint('GET URL: $url');

    final response = await http.get(url, headers: _headers);
    return _handleResponse(response, url.toString());
  }

  Future<dynamic> post(String path, Map<String, dynamic> body) async {
    final url = Uri.parse('$baseUrl$path');
    debugPrint('POST URL: $url');
    debugPrint('POST BODY: ${jsonEncode(body)}');

    final response = await http.post(
      url,
      headers: _headers,
      body: jsonEncode(body),
    );
    return _handleResponse(response, url.toString());
  }

  dynamic _handleResponse(http.Response response, String url) {
    final rawBody = response.body;
    final contentType = response.headers['content-type'] ?? '';

    debugPrint('STATUS: ${response.statusCode}');
    debugPrint('CONTENT-TYPE: $contentType');
    debugPrint('RAW BODY: $rawBody');

    if (response.statusCode < 200 || response.statusCode >= 300) {
      if (contentType.contains('application/json') && rawBody.isNotEmpty) {
        try {
          final data = jsonDecode(rawBody);
          throw Exception(data['message'] ?? 'API error: ${response.statusCode}');
        } catch (_) {
          throw Exception('API error: ${response.statusCode}');
        }
      }

      throw Exception(
        'API error: ${response.statusCode}. URL=$url. Body=$rawBody',
      );
    }

    if (rawBody.isEmpty) return null;

    if (!contentType.contains('application/json')) {
      throw Exception(
        'Response không phải JSON. URL=$url, status=${response.statusCode}, contentType=$contentType, body=$rawBody',
      );
    }

    try {
      return jsonDecode(rawBody);
    } catch (e) {
      throw Exception('JSON parse failed. URL=$url, error=$e, body=$rawBody');
    }
  }
}