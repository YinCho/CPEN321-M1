import express, { type Express } from 'express';

export function createApp(): Express {
    const app = express();

    app.get('/health', (_req, res) => {
    res.json({ status: 'ok' });
    });

    app.get('/name', (_req, res) => {
      res.json({
          first: 'Jerry',
          last: 'You'
          });
      });

  app.get('/server-time', (_req, res) => {
        const now = new Date();

        const hours = String(now.getHours()).padStart(2,'0');
        const minutes = String(now.getMinutes()).padStart(2,'0');
        const seconds = String(now.getSeconds()).padStart(2, '0');

        const offsetMinutes = now.getTimezoneOffset();
        const sign = offsetMinutes > 0 ? '-' : '+';

        const offsetHours = String(Math.floor(Math.abs(offsetMinutes) / 60)).padStart(2, '0');
        const offsetMins = String(Math.abs(offsetMinutes) % 60).padStart(2, '0');

        const formattedTime = `${hours}:${minutes}:${seconds} GMT${sign}${offsetHours}:${offsetMins}`;

        res.json({
            time: formattedTime
        });
  });

  app.get('/server-ip', (_req, res) => {
        res.json({
            ip: process.env.SERVER_PUBLIC_IP
        });
    });


  app.use((_req, res) => {
    res.status(404).json({ error: 'Not Found' });
  });


  return app;
}
