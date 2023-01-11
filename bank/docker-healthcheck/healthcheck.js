// healthcheck.js

const http = require('node:http');

const options = { hostname: 'localhost', port: 9090, path: '/health', method: 'GET' };

http
  .request(options, (res) => {
    let body = '';

    res.on('data', (chunk) => {
      body += chunk;
    });

    res.on('end', () => {
      try {
        const response = JSON.parse(body);
        if (response.status === 'ok') {
          process.exit(0);
        }

        console.log('Unhealthy response received: ', body);
        process.exit(1);
      } catch (err) {
        console.log('Error parsing JSON response body: ', err);
        process.exit(1);
      }
    });
  })
  .on('error', (err) => {
    console.log('Error: ', err);
    process.exit(1);
  })
  .end();