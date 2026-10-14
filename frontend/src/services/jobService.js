import api from './api';

export const jobService = {
  getAllJobs: async (filters = {}) => {
    const res = await api.get('/jobs', { params: filters });
    return res.data;
  },

  getJobById: async (id) => {
    const res = await api.get(`/jobs/${id}`);
    return res.data;
  },

  saveJob: async (jobId) => {
    const res = await api.post(`/jobs/${jobId}/save`);
    return res.data;
  },

  unsaveJob: async (jobId) => {
    const res = await api.delete(`/jobs/${jobId}/save`);
    return res.data;
  },

  getSavedJobs: async () => {
    const res = await api.get('/jobs/saved');
    return res.data;
  },

  applyForJob: async (jobId, resumeId, coverLetter = '') => {
    const res = await api.post(`/jobs/${jobId}/apply`, { resumeId, coverLetter });
    return res.data;
  },
};
