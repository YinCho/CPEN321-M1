import { createApp } from './app';
import { env } from './config/env';
import { WebSocket, WebSocketServer } from 'ws';

const app = createApp();

const server = app.listen(env.port, () => {
  console.log(`Server listening on port ${env.port}`);
});

const wss = new WebSocketServer({
  server,
  path: '/ws'
});

let courseSocket: WebSocket | null = null;

function connectToCourseServer() {
  console.log('Connecting to course WebSocket...');

  courseSocket = new WebSocket('wss://8.229.22.124');

  courseSocket.on('open', () => {
    console.log('Connected to course WebSocket');
  });

  courseSocket.on('message', (data, isBinary) => {
    // Send the exact update to every connected Android client
    for (const client of wss.clients) {
      if (client.readyState === WebSocket.OPEN) {
        client.send(data, {
          binary: isBinary
        });
      }
    }
  });

  courseSocket.on('close', () => {
    console.log('Course WebSocket disconnected');

    setTimeout(connectToCourseServer, 2000);
  });

  courseSocket.on('error', (error) => {
    console.error('Course WebSocket error:', error.message);
  });
}

connectToCourseServer();

wss.on('connection', (socket) => {
  console.log('Android WebSocket client connected');

  socket.on('close', () => {
    console.log('Android WebSocket client disconnected');
  });
});

for (const signal of ['SIGINT', 'SIGTERM'] as const) {
  process.on(signal, () => {
    courseSocket?.close();

    server.close(() => {
      process.exit(0);
    });
  });
}