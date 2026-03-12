import 'package:flutter/foundation.dart';
import 'package:stomp_dart_client/stomp_dart_client.dart';

class WsClient {
  StompClient? _client;
  bool _isConnected = false;

  bool get isConnected => _isConnected;

  void connect({
    required String url,
    required void Function(StompFrame frame) onConnect,
  }) {
    if (_client != null && _isConnected) return;

    debugPrint('WS connecting to: $url');

    _client = StompClient(
      config: StompConfig(
        url: url,
        reconnectDelay: const Duration(seconds: 5),
        heartbeatOutgoing: const Duration(seconds: 10),
        heartbeatIncoming: const Duration(seconds: 10),
        onConnect: (frame) {
          _isConnected = true;
          debugPrint('WS connected');
          onConnect(frame);
        },
        onDisconnect: (frame) {
          _isConnected = false;
          debugPrint('WS disconnected');
        },
        onWebSocketError: (dynamic error) {
          _isConnected = false;
          debugPrint('WebSocket error: $error');
        },
        onStompError: (frame) {
          debugPrint('STOMP error: ${frame.body}');
        },
      ),
    );

    _client!.activate();
  }

  void subscribe({
    required String destination,
    required void Function(StompFrame? frame) callback,
  }) {
    debugPrint('WS subscribe: $destination');
    _client?.subscribe(
      destination: destination,
      callback: callback,
    );
  }

  void disconnect() {
    _isConnected = false;
    debugPrint('WS disconnect');
    _client?.deactivate();
    _client = null;
  }
}