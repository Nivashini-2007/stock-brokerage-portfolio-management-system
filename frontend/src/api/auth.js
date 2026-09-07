import { client } from './client';

export const authApi = {
  register: (payload) => client.post('/auth/register', payload).then((r) => r.data),
  registerStaff: (payload) => client.post('/auth/register-staff', payload).then((r) => r.data),
  login: (payload) => client.post('/auth/login', payload).then((r) => r.data),
  logout: (token) =>
    client.post('/auth/logout', null, {
      headers: token ? { Authorization: `Bearer ${token}` } : {},
    }).then((r) => r.data),
  profile: () => client.get('/auth/profile').then((r) => r.data),
  roles: () => client.get('/auth/roles').then((r) => r.data),
};
