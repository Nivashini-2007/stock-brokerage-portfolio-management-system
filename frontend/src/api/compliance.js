import { client } from './client';

export const complianceApi = {
  dailyActivityReport: (date) =>
    client
      .get('/compliance/daily-activity-report', { params: { date }, responseType: 'blob' })
      .then((r) => r.data),
  uccFile: () =>
    client.get('/compliance/ucc-file', { responseType: 'blob' }).then((r) => r.data),
};
