import api from './api';

export const adminService = {
  getDashboardAnalytics: async () => {
    const res = await api.get('/admin/analytics');
    return res.data;
  },

  getAllUsers: async (page = 0, size = 10, search = '') => {
    const res = await api.get('/admin/users', { params: { page, size, search } });
    return res.data;
  },

  updateUserStatus: async (userId, active) => {
    const res = await api.put(`/admin/users/${userId}/status`, { active });
    return res.data;
  },

  deleteUser: async (userId) => {
    const res = await api.delete(`/admin/users/${userId}`);
    return res.data;
  },

  getAllJobs: async () => {
    const res = await api.get('/admin/jobs');
    return res.data;
  },

  deleteJob: async (jobId) => {
    const res = await api.delete(`/admin/jobs/${jobId}`);
    return res.data;
  },

  getSkills: async () => {
    const res = await api.get('/admin/skills');
    return res.data;
  },

  addSkill: async (skillData) => {
    const res = await api.post('/admin/skills', skillData);
    return res.data;
  },

  deleteSkill: async (skillId) => {
    const res = await api.delete(`/admin/skills/${skillId}`);
    return res.data;
  },
};
