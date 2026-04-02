const request = require('supertest');
const app = require('../src/index.js');

describe('Student System API', () => {
  test('GET / should return success message', async () => {
    const response = await request(app).get('/');
    expect(response.statusCode).toBe(200);
    expect(response.body).toEqual({ message: 'Student System API' });
  });
});
