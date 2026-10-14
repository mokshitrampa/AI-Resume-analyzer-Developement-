import api from './api';

export const studentService = {
  getDashboardData: async () => {
    const res = await api.get('/student/dashboard');
    return res.data;
  },

  getProfile: async () => {
    const res = await api.get('/student/profile');
    return res.data;
  },

  updateProfile: async (profileData) => {
    const res = await api.put('/student/profile', profileData);
    return res.data;
  },
};
